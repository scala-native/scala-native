import java.io.File.pathSeparator
import scala.util.Try
import scalanative.sbtplugin.ScalaNativePluginInternal._
import scalanative.io.packageNameFromPath

Global / onChangedBuildSource := ReloadOnSourceChanges

val sbt10Version          = "1.1.6"
val sbt10ScalaVersion     = "2.12.10"
val libScalaVersion       = "2.11.12"
val libCrossScalaVersions = Seq("2.11.8", "2.11.11", libScalaVersion)

// Convert "SomeName" to "some-name".
def convertCamelKebab(name: String): String = {
  name.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase
}

// Generate project name from project id.
def projectName(project: sbt.ResolvedProject): String = {
  convertCamelKebab(project.id)
}

// Provide consistent project name pattern.
lazy val nameSettings = Seq(
  normalizedName := projectName(thisProject.value),         // Maven <artifactId>
  name := s"Scala Native ${projectName(thisProject.value)}" // Maven <name>
)

// The previous releases of Scala Native with which this version is binary compatible.
val binCompatVersions = Set()

lazy val mimaSettings: Seq[Setting[_]] = Seq(
  mimaPreviousArtifacts := binCompatVersions.map { version =>
    organization.value %% moduleName.value % version
  }
)

lazy val baseSettings = Seq(
  organization := "org.scala-native", // Maven <groupId>
  version := nativeVersion            // Maven <version>
)

// Common start but individual sub-projects may add or remove scalacOptions.
// project/build.sbt uses a less stringent set to bootstrap.
inThisBuild(
  Def.settings(
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "utf8",
      "-feature",
      "-target:jvm-1.8",
      "-unchecked",
      "-Xfatal-warnings"
    )
  ))

addCommandAlias(
  "rebuild",
  Seq(
    "clean",
    "cleanCache",
    "cleanLocal",
    "dirty-rebuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "dirty-rebuild",
  Seq(
    "scalalib/publishLocal",
    "testRunner/publishLocal",
    "sbtScalaNative/publishLocal",
    "testInterface/publishLocal"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "test-all",
  Seq(
    "sandbox/run",
    "tests/test",
    "tools/test",
    "nirparser/test",
    "sbtScalaNative/scripted",
    "tools/mimaReportBinaryIssues"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "test-tools",
  Seq(
    "tools/test",
    "nirparser/test",
    "tools/mimaReportBinaryIssues"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "test-runtime",
  Seq(
    "sandbox/run",
    "tests/test",
    "sbtScalaNative/scripted"
  ).mkString(";", ";", "")
)

lazy val publishSnapshot =
  taskKey[Unit]("Publish snapshot to sonatype on every commit to master.")

lazy val setUpTestingCompiler = Def.task {
  val nscpluginjar = (nscplugin / Compile / Keys.`package`).value
  val nativelibjar = (nativelib / Compile / Keys.`package`).value
  val auxlibjar    = (auxlib / Compile / Keys.`package`).value
  val clibjar      = (clib / Compile / Keys.`package`).value
  val posixlibjar  = (posixlib / Compile / Keys.`package`).value
  val scalalibjar  = (scalalib / Compile / Keys.`package`).value
  val javalibjar   = (javalib / Compile / Keys.`package`).value
  val testingcompilercp =
    (testingCompiler / Compile / fullClasspath).value.files
  val testingcompilerjar = (testingCompiler / Compile / Keys.`package`).value

  sys.props("scalanative.nscplugin.jar") = nscpluginjar.getAbsolutePath
  sys.props("scalanative.testingcompiler.cp") =
    (testingcompilercp :+ testingcompilerjar) map (_.getAbsolutePath) mkString pathSeparator
  sys.props("scalanative.nativeruntime.cp") =
    Seq(nativelibjar, auxlibjar, clibjar, posixlibjar, scalalibjar, javalibjar) mkString pathSeparator
  sys.props("scalanative.nativelib.dir") =
    ((Compile / crossTarget).value / "nativelib").getAbsolutePath
}

// to publish plugin (we only need to do this once, it's already done!)
// follow: https://www.scala-sbt.org/1.x/docs/Bintray-For-Plugins.html
// then add a new package
// name: sbt-scala-native, license: BSD-like, version control: git@github.com:scala-native/scala-native.git
// to be available without a resolver
// follow: https://www.scala-sbt.org/1.x/docs/Bintray-For-Plugins.html#Linking+your+package+to+the+sbt+organization
lazy val bintrayPublishSettings = Seq(
  bintrayRepository := "sbt-plugins",
  bintrayOrganization := Some("scala-native")
) ++ publishSettings

lazy val mavenPublishSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { x => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishSnapshot := Def.taskDyn {
    val travis = Try(sys.env("TRAVIS")).getOrElse("false") == "true"
    val pr = Try(sys.env("TRAVIS_PULL_REQUEST"))
      .getOrElse("false") != "false"
    val branch   = Try(sys.env("TRAVIS_BRANCH")).getOrElse("")
    val snapshot = version.value.trim.endsWith("SNAPSHOT")

    (travis, pr, branch, snapshot) match {
      case (true, false, "master", true) =>
        println("on master, going to publish a snapshot")
        publish

      case _ =>
        println(
          "not going to publish a snapshot due to: " +
            s"travis = $travis, pr = $pr, " +
            s"branch = $branch, snapshot = $snapshot")
        Def.task((): Unit)
    }
  }.value,
  credentials ++= {
    for {
      realm    <- sys.env.get("MAVEN_REALM")
      domain   <- sys.env.get("MAVEN_DOMAIN")
      user     <- sys.env.get("MAVEN_USER")
      password <- sys.env.get("MAVEN_PASSWORD")
    } yield {
      Credentials(realm, domain, user, password)
    }
  }.toSeq
) ++ publishSettings

lazy val publishSettings = Seq(
  Compile / publishArtifact := true,
  Test / publishArtifact := false,
  Compile / packageDoc / publishArtifact :=
    !version.value.contains("SNAPSHOT"),
  Compile / packageSrc / publishArtifact :=
    !version.value.contains("SNAPSHOT"),
  homepage := Some(url("http://www.scala-native.org")),
  startYear := Some(2015),
  licenses := Seq(
    "BSD-like" -> url("http://www.scala-lang.org/downloads/license.html")),
  developers += Developer(
    email = "denys.shabalin@epfl.ch",
    id = "densh",
    name = "Denys Shabalin",
    url = url("http://den.sh")
  ),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/scala-native/scala-native"),
      connection = "scm:git:git@github.com:scala-native/scala-native.git"
    )),
  pomExtra := (
    <issueManagement>
      <system>GitHub Issues</system>
      <url>https://github.com/scala-native/scala-native/issues</url>
    </issueManagement>
  )
) ++ nameSettings

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  packagedArtifacts := Map.empty,
  publish := {},
  publishLocal := {},
  publishSnapshot := { println("no publish") },
  publish / skip := true
) ++ nameSettings

lazy val toolSettings =
  baseSettings ++
    Seq(
      sbtVersion := sbt10Version,
      crossSbtVersions := List(sbt10Version),
      scalaVersion := sbt10ScalaVersion,
      javacOptions ++= Seq("-encoding", "utf8")
    )

lazy val libSettings =
  (baseSettings ++ ScalaNativePlugin.projectSettings.tail) ++ Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil
  )

lazy val projectSettings =
  ScalaNativePlugin.projectSettings ++ Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil,
    nativeCheck := true,
    nativeDump := true
  )

lazy val util =
  project
    .in(file("util"))
    .settings(toolSettings)
    .settings(mavenPublishSettings)

lazy val nir =
  project
    .in(file("nir"))
    .settings(toolSettings)
    .settings(mavenPublishSettings)
    .dependsOn(util)

lazy val nirparser =
  project
    .in(file("nirparser"))
    .settings(toolSettings)
    .settings(noPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "fastparse"  % "1.0.0",
        "com.lihaoyi" %% "scalaparse" % "1.0.0",
        compilerPlugin(
          "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
        "org.scalatest"  %% "scalatest"  % "3.0.0"  % "test"
      )
    )
    .dependsOn(nir)

lazy val tools =
  project
    .in(file("tools"))
    .settings(toolSettings)
    .settings(mavenPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
        "org.scalatest"  %% "scalatest"  % "3.0.0"  % "test"
      ),
      Test / fullClasspath := ((Test / fullClasspath) dependsOn setUpTestingCompiler).value,
      publishLocal := publishLocal
        .dependsOn(nir / publishLocal)
        .dependsOn(util / publishLocal)
        .value,
      // Running tests in parallel results in `FileSystemAlreadyExistsException`
      Test / parallelExecution := false,
      mimaSettings
    )
    .dependsOn(nir, util, testingCompilerInterface % Test)

lazy val nscplugin =
  project
    .in(file("nscplugin"))
    .settings(baseSettings)
    .settings(mavenPublishSettings)
    .settings(
      scalaVersion := libScalaVersion,
      crossScalaVersions := libCrossScalaVersions,
      crossVersion := CrossVersion.full,
      Compile / unmanagedSourceDirectories ++= Seq(
        (nir / Compile / scalaSource).value,
        (util / Compile / scalaSource).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      )
    )
    .settings(scalacOptions += "-Xno-patmat-analysis")

lazy val sbtPluginSettings =
  toolSettings ++
    bintrayPublishSettings ++
    Seq(
      scriptedLaunchOpts := {
        scriptedLaunchOpts.value ++
          Seq("-Xmx1024M",
              "-XX:MaxMetaspaceSize=256M",
              "-Dplugin.version=" + version.value) ++
          ivyPaths.value.ivyHome.map(home => s"-Dsbt.ivy.home=${home}").toSeq
      }
    )

lazy val sbtScalaNative =
  project
    .in(file("sbt-scala-native"))
    .enablePlugins(SbtPlugin)
    .settings(sbtPluginSettings)
    .settings(
      crossScalaVersions := libCrossScalaVersions,
      addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0"),
      sbtTestDirectory := (ThisBuild / baseDirectory).value / "scripted-tests",
      // `testInterfaceSerialization` needs to be available from the sbt plugin,
      // but it's a Scala Native project (and thus 2.11), and the plugin is 2.12.
      // We simply add the sources to mimic cross-compilation.
      Compile / sources ++= (testInterfaceSerialization / Compile / sources).value,
      // publish the other projects before running scripted tests.
      scripted := scripted
        .dependsOn(testInterface / publishLocal)
        .dependsOn(ThisProject / publishLocal)
        .dependsOn(scalalib / publishLocal)
        .evaluated,
      publishLocal := publishLocal
        .dependsOn(tools / publishLocal, testRunner / publishLocal)
        .value
    )
    .dependsOn(tools, testRunner)

lazy val nativelib =
  project
    .in(file("nativelib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      publishLocal := publishLocal
        .dependsOn(nscplugin / publishLocal)
        .value
    )

lazy val clib =
  project
    .in(file("clib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      publishLocal := publishLocal
        .dependsOn(nativelib / publishLocal)
        .value
    )
    .dependsOn(nativelib)

lazy val posixlib =
  project
    .in(file("posixlib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      publishLocal := publishLocal
        .dependsOn(clib / publishLocal)
        .value
    )
    .dependsOn(clib)

lazy val javalib =
  project
    .in(file("javalib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      Compile / doc / sources := Nil, // doc generation currently broken
      // This is required to have incremental compilation to work in javalib.
      // We put our classes on scalac's `javabootclasspath` so that it uses them
      // when compiling rather than the definitions from the JDK.
      Compile / scalacOptions := {
        val previous = (Compile / scalacOptions).value
        val javaBootClasspath =
          scala.tools.util.PathResolver.Environment.javaBootClassPath
        val classDir  = (Compile / classDirectory).value.getAbsolutePath()
        val separator = sys.props("path.separator")
        "-javabootclasspath" +: s"$classDir$separator$javaBootClasspath" +: previous
      },
      // Don't include classfiles for javalib in the packaged jar.
      Compile / packageBin / mappings := {
        val previous = (Compile / packageBin / mappings).value
        previous.filter {
          case (file, path) =>
            !path.endsWith(".class")
        }
      },
      publishLocal := publishLocal
        .dependsOn(nativelib / publishLocal, posixlib / publishLocal)
        .value
    )
    .dependsOn(nativelib, posixlib)

lazy val assembleScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala and then applies overrides.")

lazy val auxlib =
  project
    .in(file("auxlib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      publishLocal := publishLocal
        .dependsOn(javalib / publishLocal)
        .value
    )
    .dependsOn(nativelib)

lazy val scalalib =
  project
    .in(file("scalalib"))
    .settings(libSettings)
    .settings(
      // This build uses libScalaVersion, which is currently 2.11.12
      // to compile what appears to be 2.11.0 sources. This yields 114
      // deprecations. Editing those sources is not an option (long story),
      // so do not spend compile time looking for the deprecations.
      // Keep the log file clean so that real issues stand out.
      // This futzing can probably removed for scala >= 2.12.
      scalacOptions -= "-deprecation",
      scalacOptions += "-deprecation:false"
    )
    .settings(mavenPublishSettings)
    .settings(
      assembleScalaLibrary := {
        import org.eclipse.jgit.api._

        val s      = streams.value
        val trgDir = target.value / "scalaSources" / scalaVersion.value
        val scalaRepo = sys.env
          .get("SCALANATIVE_SCALAREPO")
          .getOrElse("https://github.com/scala/scala.git")

        if (!trgDir.exists) {
          s.log.info(
            s"Fetching Scala source version ${scalaVersion.value} from $scalaRepo")

          // Make parent dirs and stuff
          IO.createDirectory(trgDir)

          // Clone scala source code
          new CloneCommand()
            .setDirectory(trgDir)
            .setURI(scalaRepo)
            .call()
        }

        // Checkout proper ref. We do this anyway so we fail if
        // something is wrong
        val git = Git.open(trgDir)
        s.log.info(s"Checking out Scala source version ${scalaVersion.value}")
        git.checkout().setName(s"v${scalaVersion.value}").call()

        IO.delete(file("scalalib/src/main/scala"))
        IO.copyDirectory(trgDir / "src" / "library" / "scala",
                         file("scalalib/src/main/scala/scala"))

        val epoch :: major :: _ = scalaVersion.value.split("\\.").toList
        IO.copyDirectory(file(s"scalalib/overrides-$epoch.$major/scala"),
                         file("scalalib/src/main/scala/scala"),
                         overwrite = true)

        // Remove all java code, as it's not going to be available
        // in the NIR anyway. This also resolves issues wrt overrides
        // of code that was previously in Java but is in Scala now.
        (file("scalalib/src/main/scala") ** "*.java").get.foreach(IO.delete)
      },
      Compile / compile := (Compile / compile)
        .dependsOn(assembleScalaLibrary)
        .value,
      // Don't include classfiles for scalalib in the packaged jar.
      Compile / packageBin / mappings := {
        val previous = (Compile / packageBin / mappings).value
        previous.filter {
          case (file, path) =>
            !path.endsWith(".class")
        }
      },
      publishLocal := publishLocal
        .dependsOn(assembleScalaLibrary, auxlib / publishLocal)
        .value
    )
    .dependsOn(auxlib, nativelib, javalib)

lazy val tests =
  project
    .in(file("unit-tests"))
    .settings(projectSettings)
    .settings(
      scalacOptions -= "-deprecation",
      scalacOptions += "-deprecation:false"
    )
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value),
      // nativeLinkerReporter := LinkerReporter.toFile(
      //   target.value / "out.dot"),
      libraryDependencies += "org.scala-native" %%% "test-interface" % nativeVersion,
      testFrameworks += new TestFramework("tests.NativeFramework"),
      Test / test / envVars ++= Map(
        "USER"                           -> "scala-native",
        "HOME"                           -> baseDirectory.value.getAbsolutePath,
        "SCALA_NATIVE_ENV_WITH_EQUALS"   -> "1+1=2",
        "SCALA_NATIVE_ENV_WITHOUT_VALUE" -> "",
        "SCALA_NATIVE_ENV_WITH_UNICODE"  -> 0x2192.toChar.toString,
        "SCALA_NATIVE_USER_DIR"          -> System.getProperty("user.dir")
      ),
      nativeLinkStubs := true
    )
    .enablePlugins(ScalaNativePlugin)

lazy val sandbox =
  project
    .in(file("sandbox"))
    .settings(projectSettings)
    .settings(scalacOptions -= "-Xfatal-warnings")
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value),
      scalaVersion := libScalaVersion
    )
    .enablePlugins(ScalaNativePlugin)

lazy val testingCompilerInterface =
  project
    .in(file("testing-compiler-interface"))
    .settings(libSettings)
    .settings(noPublishSettings)
    .settings(
      crossPaths := false,
      crossVersion := CrossVersion.disabled,
      autoScalaLibrary := false
    )

lazy val testingCompiler =
  project
    .in(file("testing-compiler"))
    .settings(libSettings)
    .settings(noPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      )
    )
    .dependsOn(testingCompilerInterface, nativelib)

lazy val testInterface =
  project
    .settings(toolSettings)
    .settings(scalaVersion := libScalaVersion)
    .settings(mavenPublishSettings)
    .in(file("test-interface"))
    .settings(
      libraryDependencies += "org.scala-sbt"    % "test-interface"   % "1.0",
      libraryDependencies -= "org.scala-native" %%% "test-interface" % version.value % Test,
      publishLocal := publishLocal
        .dependsOn(testInterfaceSerialization / publishLocal)
        .value
    )
    .enablePlugins(ScalaNativePlugin)
    .dependsOn(testInterfaceSerialization)

lazy val testInterfaceSerialization =
  project
    .settings(toolSettings)
    .settings(scalaVersion := libScalaVersion)
    .settings(mavenPublishSettings)
    .in(file("test-interface-serialization"))
    .settings(
      libraryDependencies -= "org.scala-native" %%% "test-interface" % version.value % Test,
      publishLocal := publishLocal
        .dependsOn(testInterfaceSbtDefs / publishLocal)
        .value
    )
    .dependsOn(testInterfaceSbtDefs)
    .enablePlugins(ScalaNativePlugin)

lazy val testInterfaceSbtDefs =
  project
    .settings(toolSettings)
    .settings(scalaVersion := libScalaVersion)
    .settings(mavenPublishSettings)
    .in(file("test-interface-sbt-defs"))
    .settings(
      libraryDependencies -= "org.scala-native" %%% "test-interface" % version.value % Test
    )
    .enablePlugins(ScalaNativePlugin)

lazy val testRunner =
  project
    .settings(toolSettings)
    .settings(mavenPublishSettings)
    .in(file("test-runner"))
    .settings(
      crossScalaVersions := Seq(sbt10ScalaVersion),
      libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
      Compile / sources ++= (testInterfaceSerialization / Compile / sources).value
    )
    .dependsOn(tools)
