import java.io.File.pathSeparator
import scala.util.Try
import scalanative.sbtplugin.ScalaNativePluginInternal._
import scalanative.io.packageNameFromPath

val sbt13Version          = "0.13.16"
val sbt13ScalaVersion     = "2.10.7"
val sbt10Version          = "1.0.4"
val sbt10ScalaVersion     = "2.12.4"
val libScalaVersion       = "2.11.12"
val libCrossScalaVersions = Seq("2.11.8", "2.11.11", libScalaVersion)

// Convert "SomeName" to "some-name".
def convertCamelKebab(name: String): String = {
  name.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase
}

// Generate project name from project id.
def projectName(project: sbt.ResolvedProject): String = {
  convertCamelKebab(project.id).split("_arch-").head
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
  "compiler-test",
  Seq(
    "tools/test",
    "nirparser/test",
    "tools/mimaReportBinaryIssues"
  ).mkString(";", ";", "")
)

Seq("x86_64", "i386", "ARM").flatMap { platform =>
  Seq(
    addCommandAlias(
      s"rebuild_arch-$platform",
      Seq(
        "clean",
        "cleanCache",
        "cleanLocal",
        s"dirty-rebuild_arch-$platform"
      ).mkString(";", ";", "")
    ),
    addCommandAlias(
      s"dirty-rebuild_arch-$platform",
      Seq(
        s"scalalib_arch-$platform/publishLocal",
        "sbtScalaNative/publishLocal",
        s"testInterface_arch-$platform/publishLocal"
      ).mkString(";", ";", "")
    ),
    addCommandAlias(
      s"test-all_arch-$platform",
      Seq(
        s"sandbox_arch-$platform/run",
        "tests_arch-$platform/test",
        "tools/test",
        "nirparser/test",
        s"benchmarks_arch-$platform/run --test",
        "sbtScalaNative/scripted",
        "tools/mimaReportBinaryIssues"
      ).mkString(";", ";", "")
    ),
    addCommandAlias(
      s"test-runtime_arch-$platform",
      Seq(
        s"sandbox_arch-$platform/run",
        s"tests_arch-$platform/test",
        s"benchmarks_arch-$platform/run --test",
        "sbtScalaNative/scripted"
      ).mkString(";", ";", "")
    )
  )
}.flatten

lazy val publishSnapshot =
  taskKey[Unit]("Publish snapshot to sonatype on every commit to master.")

lazy val setUpTestingCompiler = Def.task {
  val nscpluginjar = (Keys.`package` in nscplugin in Compile).value
  val nativelibjar = (Keys.`package` in nativelibx86_64 in Compile).value
  val auxlibjar    = (Keys.`package` in auxlibx86_64 in Compile).value
  val scalalibjar  = (Keys.`package` in scalalibx86_64 in Compile).value
  val javalibjar   = (Keys.`package` in javalibx86_64 in Compile).value
  val testingcompilercp =
    (fullClasspath in testingCompiler in Compile).value.files
  val testingcompilerjar = (Keys.`package` in testingCompiler in Compile).value

  sys.props("scalanative.nscplugin.jar") = nscpluginjar.getAbsolutePath
  sys.props("scalanative.testingcompiler.cp") =
    (testingcompilercp :+ testingcompilerjar) map (_.getAbsolutePath) mkString pathSeparator
  sys.props("scalanative.nativeruntime.cp") =
    Seq(nativelibjar, auxlibjar, scalalibjar, javalibjar) mkString pathSeparator
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
  publishSnapshot := {
    println("no publish")
  }
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
  baseSettings ++ Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil,
    scalacOptions ++= Seq("-encoding", "utf8")
  )

lazy val projectSettings =
  Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil,
    scalacOptions ++= Seq("-target:jvm-1.8")
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
            "-XX:MaxPermSize=256M",
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
      addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0-M2"),
      sbtTestDirectory := (baseDirectory in ThisBuild).value / "scripted-tests",
      // `testInterfaceSerialization` needs to be available from the sbt plugin,
      // but it's a Scala Native project (and thus 2.11), and the plugin is 2.10 or 2.12.
      // We simply add the sources to mimic cross-compilation.
      sources in Compile ++= (sources in Compile in testInterfaceSerialization).value,
      publishLocal := publishLocal.dependsOn(publishLocal in tools).value
    )
    .dependsOn(tools)

import CrossArchitecturePlatform._
import scala.scalanative.build.{x86_64, i386, ARM}

lazy val nativelib =
  crossProject(CrossArchitectureLibPlatform(x86_64),
               CrossArchitectureLibPlatform(i386),
               CrossArchitectureLibPlatform(ARM))
    .crossType(CrossType.Full)
    .in(file("nativelib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      publishLocal := publishLocal
        .dependsOn(publishLocal in nscplugin)
        .value
    )

lazy val nativelibx86_64 = nativelib.crossArchitecture(x86_64)
lazy val nativelibi386   = nativelib.crossArchitecture(i386)
lazy val nativelibARM    = nativelib.crossArchitecture(ARM)

lazy val javalib =
  crossProject(CrossArchitectureLibPlatform(x86_64),
               CrossArchitectureLibPlatform(i386),
               CrossArchitectureLibPlatform(ARM))
    .crossType(CrossType.Pure)
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
      }
    )
    .architectureSettings(x86_64)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in nativelibx86_64)
        .value
    )
    .architectureSettings(i386)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in nativelibi386)
        .value
    )
    .architectureSettings(ARM)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in nativelibARM)
        .value
    )
    .dependsOn(nativelib)

val javalibx86_64 = javalib.crossArchitecture(x86_64)
val javalibi386   = javalib.crossArchitecture(i386)
val javalibARM    = javalib.crossArchitecture(ARM)

lazy val assembleScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala and then applies overrides.")

lazy val auxlib =
  crossProject(CrossArchitectureLibPlatform(x86_64),
               CrossArchitectureLibPlatform(i386),
               CrossArchitectureLibPlatform(ARM))
    .crossType(CrossType.Pure)
    .in(file("auxlib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)
    .architectureSettings(x86_64)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in javalibx86_64)
        .value
    )
    .architectureSettings(i386)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in javalibi386)
        .value
    )
    .architectureSettings(ARM)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in javalibARM)
        .value
    )
    .dependsOn(nativelib)

lazy val auxlibx86_64 = auxlib.crossArchitecture(x86_64)
lazy val auxlibi386   = auxlib.crossArchitecture(i386)
lazy val auxlibARM    = auxlib.crossArchitecture(ARM)

lazy val scalalib =
  crossProject(CrossArchitectureLibPlatform(x86_64),
               CrossArchitectureLibPlatform(i386),
               CrossArchitectureLibPlatform(ARM))
    .crossType(CrossType.Pure)
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
      }
    )
    .architectureSettings(x86_64)(
      publishLocal := publishLocal
        .dependsOn(assembleScalaLibrary, publishLocal in auxlibx86_64)
        .value
    )
    .architectureSettings(i386)(
      publishLocal := publishLocal
        .dependsOn(assembleScalaLibrary, publishLocal in auxlibi386)
        .value
    )
    .architectureSettings(ARM)(
      publishLocal := publishLocal
        .dependsOn(assembleScalaLibrary, publishLocal in auxlibARM)
        .value
    )
    .dependsOn(auxlib, nativelib, javalib)

lazy val scalalibx86_64 = scalalib.crossArchitecture(x86_64)
lazy val scalalibi386   = scalalib.crossArchitecture(i386)
lazy val scalalibARM    = scalalib.crossArchitecture(ARM)

lazy val tests =
  crossProject(CrossArchitecturePlatform(x86_64),
               CrossArchitecturePlatform(i386),
               CrossArchitecturePlatform(ARM))
    .crossType(CrossType.Pure)
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
      )
    )

lazy val testsx86_64 = tests.crossArchitecture(x86_64)
lazy val testsi386   = tests.crossArchitecture(i386)
lazy val testsARM    = tests.crossArchitecture(ARM)

lazy val sandbox =
  crossProject(CrossArchitecturePlatform(x86_64),
               CrossArchitecturePlatform(i386),
               CrossArchitecturePlatform(ARM))
    .crossType(CrossType.Pure)
    .in(file("sandbox"))
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value),
      scalaVersion := libScalaVersion
    )

lazy val sandboxx86_64 = sandbox.crossArchitecture(x86_64)
lazy val sandboxi386   = sandbox.crossArchitecture(i386)
lazy val sandboxARM    = sandbox.crossArchitecture(ARM)

lazy val benchmarks =
  crossProject(CrossArchitecturePlatform(x86_64),
               CrossArchitecturePlatform(i386),
               CrossArchitecturePlatform(ARM))
    .crossType(CrossType.Pure)
    .in(file("benchmarks"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .settings(
      nativeMode := "release",
      sourceGenerators in Compile += Def.task {
        val dir = new File("benchmarks/src/main/scala")
        val benchmarks = (dir ** "*Benchmark.scala").get
          .flatMap(IO.relativizeFile(dir, _))
          .map(file => packageNameFromPath(file.toPath))
          .filter(_ != "benchmarks.Benchmark")
          .mkString("Seq(new ", ", new ", ")")
        val file = (sourceManaged in Compile).value / "benchmarks" / "Discover.scala"
        IO.write(
          file,
          s"""
          package benchmarks
          object Discover {
            val discovered: Seq[benchmarks.Benchmark[_]] = $benchmarks
          }
        """
        )
        Seq(file)
      }
    )

lazy val benchmarksx86_64 = benchmarks.crossArchitecture(x86_64)
lazy val benchmarksi386   = benchmarks.crossArchitecture(i386)
lazy val benchmarksARM    = benchmarks.crossArchitecture(ARM)

lazy val testingCompilerInterface =
  project
    .in(file("testing-compiler-interface"))
    .settings(libSettings)
    .settings(ScalaNativePlugin.projectSettings.tail)
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
    .settings(ScalaNativePlugin.projectSettings.tail)
    .settings(noPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      )
    )
    .dependsOn(testingCompilerInterface, nativelibx86_64)

lazy val testInterface =
  crossProject(CrossArchitecturePlatform(x86_64),
               CrossArchitecturePlatform(i386),
               CrossArchitecturePlatform(ARM))
    .crossType(CrossType.Pure)
    .settings(toolSettings)
    .settings(scalaVersion := libScalaVersion)
    .settings(mavenPublishSettings)
    .in(file("test-interface"))
    .settings(
      libraryDependencies += "org.scala-sbt"    % "test-interface"   % "1.0",
      libraryDependencies -= "org.scala-native" %%% "test-interface" % version.value % Test
    )
    .architectureSettings(x86_64)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in testInterfaceSerializationx86_64)
        .value
    )
    .architectureSettings(i386)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in testInterfaceSerializationi386)
        .value
    )
    .architectureSettings(ARM)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in testInterfaceSerializationARM)
        .value
    )
    .dependsOn(testInterfaceSerialization)

lazy val testInterfacex86_64 = testInterface.crossArchitecture(x86_64)
lazy val testInterfacei386   = testInterface.crossArchitecture(i386)
lazy val testInterfaceARM    = testInterface.crossArchitecture(ARM)

lazy val testInterfaceSerialization =
  crossProject(CrossArchitecturePlatform(x86_64),
               CrossArchitecturePlatform(i386),
               CrossArchitecturePlatform(ARM))
    .crossType(CrossType.Pure)
    .settings(toolSettings)
    .settings(scalaVersion := libScalaVersion)
    .settings(mavenPublishSettings)
    .in(file("test-interface-serialization"))
    .settings(
      libraryDependencies -= "org.scala-native" %%% "test-interface" % version.value % Test
    )
    .architectureSettings(x86_64)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in testInterfaceSbtDefsx86_64)
        .value
    )
    .architectureSettings(i386)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in testInterfaceSbtDefsi386)
        .value
    )
    .architectureSettings(ARM)(
      publishLocal := publishLocal
        .dependsOn(publishLocal in testInterfaceSbtDefsARM)
        .value
    )
    .dependsOn(testInterfaceSbtDefs)

lazy val testInterfaceSerializationx86_64 =
  testInterfaceSerialization.crossArchitecture(x86_64)
lazy val testInterfaceSerializationi386 =
  testInterfaceSerialization.crossArchitecture(i386)
lazy val testInterfaceSerializationARM =
  testInterfaceSerialization.crossArchitecture(ARM)

lazy val testInterfaceSbtDefs =
  crossProject(CrossArchitecturePlatform(x86_64),
               CrossArchitecturePlatform(i386),
               CrossArchitecturePlatform(ARM))
    .crossType(CrossType.Pure)
    .settings(toolSettings)
    .settings(scalaVersion := libScalaVersion)
    .settings(mavenPublishSettings)
    .in(file("test-interface-sbt-defs"))
    .settings(
      libraryDependencies -= "org.scala-native" %%% "test-interface" % version.value % Test
    )

lazy val testInterfaceSbtDefsx86_64 =
  testInterfaceSbtDefs.crossArchitecture(x86_64)
lazy val testInterfaceSbtDefsi386 = testInterfaceSbtDefs.crossArchitecture(i386)
lazy val testInterfaceSbtDefsARM  = testInterfaceSbtDefs.crossArchitecture(ARM)
