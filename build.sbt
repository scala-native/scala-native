import java.io.File.pathSeparator
import scala.util.Try
import scalanative.tools.OptimizerReporter
import scalanative.sbtplugin.ScalaNativePluginInternal._
import scalanative.io.packageNameFromPath

val toolScalaVersion      = "2.10.6"
val libScalaVersion       = "2.11.11"
val libCrossScalaVersions = Seq("2.11.8", "2.11.11")

lazy val baseSettings = Seq(
  organization := "org.scala-native",
  version := nativeVersion
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
    "nscplugin/publishLocal",
    "nativelib/publishLocal",
    "publishLocal"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "test-all",
  Seq(
    "sandbox/run",
    "demoNative/run",
    "tests/run",
    "tools/test",
    "benchmarks/run --test",
    "scripted"
  ).mkString(";", ";", "")
)

lazy val publishSnapshot =
  taskKey[Unit]("Publish snapshot to sonatype on every commit to master.")

lazy val setUpTestingCompiler = Def.task {
  val nscpluginjar = (Keys.`package` in nscplugin in Compile).value
  val nativelibjar = (Keys.`package` in nativelib in Compile).value
  val scalalibjar  = (Keys.`package` in scalalib in Compile).value
  val javalibjar   = (Keys.`package` in javalib in Compile).value
  val testingcompilercp =
    (fullClasspath in testingCompiler in Compile).value.files
  val testingcompilerjar = (Keys.`package` in testingCompiler in Compile).value

  sys.props("scalanative.nscplugin.jar") = nscpluginjar.getAbsolutePath
  sys.props("scalanative.testingcompiler.cp") =
    (testingcompilercp :+ testingcompilerjar) map (_.getAbsolutePath) mkString pathSeparator
  sys.props("scalanative.nativeruntime.cp") =
    Seq(nativelibjar, scalalibjar, javalibjar) mkString pathSeparator
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
)

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  packagedArtifacts := Map.empty,
  publish := {},
  publishLocal := {},
  publishSnapshot := {
    println("no publish")
  }
)

lazy val toolSettings =
  baseSettings ++
    Seq(
      scalaVersion := toolScalaVersion,
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
    scalacOptions ++= Seq(
      "-encoding",
      "utf8"
    )
  )

lazy val projectSettings =
  ScalaNativePlugin.projectSettings ++ Seq(
    scalaVersion := libScalaVersion,
    resolvers := Nil
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

lazy val tools =
  project
    .in(file("tools"))
    .settings(toolSettings)
    .settings(mavenPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.lihaoyi"    %% "fastparse"  % "0.4.2",
        "com.lihaoyi"    %% "scalaparse" % "0.4.2",
        "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
        compilerPlugin(
          "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
        "org.scalatest" %% "scalatest" % "3.0.0" % "test"
      ),
      fullClasspath in Test := ((fullClasspath in Test) dependsOn setUpTestingCompiler).value,
      publishLocal := publishLocal
        .dependsOn(publishLocal in nir)
        .dependsOn(publishLocal in util)
        .value,
      // Running tests in parallel results in `FileSystemAlreadyExistsException`
      parallelExecution in Test := false
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
            "-Dplugin.version=" + version.value)
    )

lazy val sbtScalaNative =
  project
    .in(file("sbt-scala-native"))
    .settings(sbtPluginSettings)
    .settings(
      addSbtPlugin("org.scala-native" % "sbt-crossproject" % "0.1.0"),
      moduleName := "sbt-scala-native",
      sbtTestDirectory := (baseDirectory in ThisBuild).value / "scripted-tests",
      // publish the other projects before running scripted tests.
      scripted := scripted
        .dependsOn(publishLocal in util,
                   publishLocal in nir,
                   publishLocal in tools,
                   publishLocal in nscplugin,
                   publishLocal in nativelib,
                   publishLocal in javalib,
                   publishLocal in scalalib)
        .evaluated,
      publishLocal := publishLocal.dependsOn(publishLocal in tools).value
    )
    .dependsOn(tools)

lazy val nativelib =
  project
    .in(file("nativelib"))
    .settings(libSettings)
    .settings(mavenPublishSettings)

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
      }
    )
    .dependsOn(nativelib)

lazy val assembleScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala and then applies overrides.")

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

        if (!trgDir.exists) {
          s.log.info(s"Fetching Scala source version ${scalaVersion.value}")

          // Make parent dirs and stuff
          IO.createDirectory(trgDir)

          // Clone scala source code
          new CloneCommand()
            .setDirectory(trgDir)
            .setURI("https://github.com/scala/scala.git")
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
      publishLocal := publishLocal.dependsOn(assembleScalaLibrary).value
    )
    .dependsOn(nativelib, javalib)

lazy val demoJVM =
  project
    .in(file("demo/jvm"))
    .settings(noPublishSettings)
    .settings(
      fork in run := true,
      javaOptions in run ++= Seq("-Xms64m", "-Xmx64m")
    )

lazy val demoNative =
  project
    .in(file("demo/native"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .enablePlugins(ScalaNativePlugin)

lazy val tests =
  project
    .in(file("unit-tests"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value),
      sourceGenerators in Compile += Def.task {
        val dir = (scalaSource in Compile).value
        val suites = (dir ** "*Suite.scala").get
          .flatMap(IO.relativizeFile(dir, _))
          .map(file => packageNameFromPath(file.toPath))
          .filter(_ != "tests.Suite")
          .mkString("Seq(", ", ", ")")
        val file = (sourceManaged in Compile).value / "tests" / "Discover.scala"
        IO.write(file,
                 s"""
          package tests
          object Discover {
            val suites: Seq[tests.Suite] = $suites
          }
        """)
        Seq(file)
      }.taskValue,
      envVars in run ++= Map(
        "USER"                           -> "scala-native",
        "HOME"                           -> baseDirectory.value.getAbsolutePath,
        "SCALA_NATIVE_ENV_WITH_EQUALS"   -> "1+1=2",
        "SCALA_NATIVE_ENV_WITHOUT_VALUE" -> ""
      )
    )
    .enablePlugins(ScalaNativePlugin)

lazy val sandbox =
  project
    .in(file("sandbox"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value)
    )
    .enablePlugins(ScalaNativePlugin)

lazy val benchmarks =
  project
    .in(file("benchmarks"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .settings(
      nativeMode := "release",
      sourceGenerators in Compile += Def.task {
        val dir = (scalaSource in Compile).value
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
      }.taskValue
    )
    .enablePlugins(ScalaNativePlugin)

lazy val testingCompilerInterface =
  project
    .in(file("testing-compiler-interface"))
    .settings(libSettings)
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
