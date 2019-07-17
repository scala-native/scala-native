import java.io.File.pathSeparator
import scala.util.Try
import scalanative.sbtplugin.ScalaNativePluginInternal._
import scalanative.io.packageNameFromPath

val sbt13Version          = "0.13.18"
val sbt13ScalaVersion     = "2.10.7"
val sbt10Version          = "1.2.8"
val sbt10ScalaVersion     = "2.12.8"
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

// Metals settings (next 3 items)
// Avoid 2.10 for sbt generated root project
scalaVersion := libScalaVersion

lazy val startupTransition: State => State = { s: State =>
  Option(System.getenv("METALS_ENABLED")) match {
    case Some(sb) => if (sb == "true") s"^^$sbt10Version" :: s else s
    case None     => s
  }
}

onLoad in Global := {
  val sbtCrossVersion = (sbtVersion in pluginCrossBuild).value
  val old             = (onLoad in Global).value
  if (sbtCrossVersion != sbt10Version) {
    startupTransition compose old
  } else {
    old
  }
}

// Provide consistent project name pattern.
lazy val nameSettings = Seq(
  normalizedName := projectName(thisProject.value), // Maven <artifactId>
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
  version := nativeVersion // Maven <version>
)

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
  val nscpluginjar = (Keys.`package` in nscplugin in Compile).value
  val nativelibjar = (Keys.`package` in nativelib in Compile).value
  val auxlibjar    = (Keys.`package` in auxlib in Compile).value
  val clibjar      = (Keys.`package` in clib in Compile).value
  val posixlibjar  = (Keys.`package` in posixlib in Compile).value
  val scalalibjar  = (Keys.`package` in scalalib in Compile).value
  val javalibjar   = (Keys.`package` in javalib in Compile).value
  val testingcompilercp =
    (fullClasspath in testingCompiler in Compile).value.files
  val testingcompilerjar = (Keys.`package` in testingCompiler in Compile).value

  sys.props("scalanative.nscplugin.jar") = nscpluginjar.getAbsolutePath
  sys.props("scalanative.testingcompiler.cp") =
    (testingcompilercp :+ testingcompilerjar) map (_.getAbsolutePath) mkString pathSeparator
  sys.props("scalanative.nativeruntime.cp") =
    Seq(nativelibjar, auxlibjar, clibjar, posixlibjar, scalalibjar, javalibjar) mkString pathSeparator
  sys.props("scalanative.nativelib.dir") =
    ((crossTarget in Compile).value / "nativelib").getAbsolutePath
}

// to publish plugin (we only need to do this once, it's already done!)
// follow: http://www.scala-sbt.org/0.13/docs/Bintray-For-Plugins.html
// then add a new package
// name: sbt-scala-native, license: BSD-like, version control: git@github.com:scala-native/scala-native.git
// to be available without a resolver
// follow: http://www.scala-sbt.org/0.13/docs/Bintray-For-Plugins.html#Linking+your+package+to+the+sbt+organization
lazy val bintrayPublishSettings = Seq(
  bintrayRepository := "sbt-plugins",
  bintrayOrganization := Some("scala-native")
) ++ publishSettings

lazy val mavenPublishSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { x =>
    false
  },
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
        Def.task()
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
  publishArtifact in Compile := true,
  publishArtifact in Test := false,
  publishArtifact in (Compile, packageDoc) :=
    !version.value.contains("SNAPSHOT"),
  publishArtifact in (Compile, packageSrc) :=
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
  skip in publish := true
) ++ nameSettings

lazy val toolSettings =
  baseSettings ++
    Seq(
      crossSbtVersions := List(sbt13Version, sbt10Version),
      scalaVersion := {
        (sbtBinaryVersion in pluginCrossBuild).value match {
          case "0.13" => sbt13ScalaVersion
          case _      => sbt10ScalaVersion
        }
      },
      scalacOptions ++= Seq(
        "-deprecation",
        "-unchecked",
        "-feature",
        "-encoding",
        "utf8"
      ),
      javacOptions ++= Seq("-encoding", "utf8")
    )

lazy val libSettings =
  (baseSettings ++ ScalaNativePlugin.projectSettings.tail) ++ Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil,
    scalacOptions ++= Seq("-encoding", "utf8")
  )

lazy val projectSettings =
  ScalaNativePlugin.projectSettings ++ Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil,
    scalacOptions ++= Seq("-target:jvm-1.8"),
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
      fullClasspath in Test := ((fullClasspath in Test) dependsOn setUpTestingCompiler).value,
      publishLocal := publishLocal
        .dependsOn(publishLocal in nir)
        .dependsOn(publishLocal in util)
        .value,
      // Running tests in parallel results in `FileSystemAlreadyExistsException`
      parallelExecution in Test := false,
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
      unmanagedSourceDirectories in Compile ++= Seq(
        (scalaSource in (nir, Compile)).value,
        (scalaSource in (util, Compile)).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      )
    )

lazy val sbtPluginSettings =
  toolSettings ++
    bintrayPublishSettings ++
    ScriptedPlugin.scriptedSettings ++
    Seq(
      sbtPlugin := true,
      scriptedLaunchOpts ++=
        Seq("-Xmx1024M",
            "-XX:MaxMetaspaceSize=256M",
            "-Dplugin.version=" + version.value) ++
          ivyPaths.value.ivyHome.map(home => s"-Dsbt.ivy.home=${home}").toSeq
    )

lazy val sbtScalaNative =
  project
    .in(file("sbt-scala-native"))
    .settings(sbtPluginSettings)
    .settings(
      crossScalaVersions := libCrossScalaVersions,
      // fixed in https://github.com/sbt/sbt/pull/3397 (for sbt 0.13.17)
      sbtBinaryVersion in update := (sbtBinaryVersion in pluginCrossBuild).value,
      addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0"),
      sbtTestDirectory := (baseDirectory in ThisBuild).value / "scripted-tests",
      // `testInterfaceSerialization` needs to be available from the sbt plugin,
      // but it's a Scala Native project (and thus 2.11), and the plugin is 2.10 or 2.12.
      // We simply add the sources to mimic cross-compilation.
      sources in Compile ++= (sources in Compile in testInterfaceSerialization).value,
      // publish the other projects before running scripted tests.
      scripted := scripted
        .dependsOn(publishLocal in testInterface)
        .dependsOn(publishLocal in ThisProject)
        .dependsOn(publishLocal in scalalib)
        .evaluated,
      publishLocal := publishLocal
        .dependsOn(publishLocal in tools, publishLocal in testRunner)
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
        .dependsOn(publishLocal in nscplugin)
        .value
    )

lazy val clib =
  project
    .in(file("clib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      publishLocal := publishLocal
        .dependsOn(publishLocal in nativelib)
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
        .dependsOn(publishLocal in clib)
        .value
    )
    .dependsOn(clib)

lazy val javalib =
  project
    .in(file("javalib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      sources in doc in Compile := Nil, // doc generation currently broken
      // This is required to have incremental compilation to work in javalib.
      // We put our classes on scalac's `javabootclasspath` so that it uses them
      // when compiling rather than the definitions from the JDK.
      scalacOptions in Compile := {
        val previous = (scalacOptions in Compile).value
        val javaBootClasspath =
          scala.tools.util.PathResolver.Environment.javaBootClassPath
        val classDir  = (classDirectory in Compile).value.getAbsolutePath()
        val separator = sys.props("path.separator")
        "-javabootclasspath" +: s"$classDir$separator$javaBootClasspath" +: previous
      },
      // Don't include classfiles for javalib in the packaged jar.
      mappings in packageBin in Compile := {
        val previous = (mappings in packageBin in Compile).value
        previous.filter {
          case (file, path) =>
            !path.endsWith(".class")
        }
      },
      publishLocal := publishLocal
        .dependsOn(publishLocal in nativelib, publishLocal in posixlib)
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
        .dependsOn(publishLocal in javalib)
        .value
    )
    .dependsOn(nativelib)

lazy val scalalib =
  project
    .in(file("scalalib"))
    .settings(libSettings)
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
      compile in Compile := (compile in Compile)
        .dependsOn(assembleScalaLibrary)
        .value,
      // Don't include classfiles for scalalib in the packaged jar.
      mappings in packageBin in Compile := {
        val previous = (mappings in packageBin in Compile).value
        previous.filter {
          case (file, path) =>
            !path.endsWith(".class")
        }
      },
      publishLocal := publishLocal
        .dependsOn(assembleScalaLibrary, publishLocal in auxlib)
        .value
    )
    .dependsOn(auxlib, nativelib, javalib)

lazy val tests =
  project
    .in(file("unit-tests"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value),
      // nativeLinkerReporter := LinkerReporter.toFile(
      //   target.value / "out.dot"),
      libraryDependencies += "org.scala-native" %%% "test-interface" % nativeVersion,
      testFrameworks += new TestFramework("tests.NativeFramework"),
      envVars in (Test, test) ++= Map(
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
      crossVersion := CrossVersion.Disabled,
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
        .dependsOn(publishLocal in testInterfaceSerialization)
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
        .dependsOn(publishLocal in testInterfaceSbtDefs)
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
      crossScalaVersions := Seq(sbt13ScalaVersion, sbt10ScalaVersion),
      libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
      sources in Compile ++= (sources in testInterfaceSerialization in Compile).value
    )
    .dependsOn(tools)
