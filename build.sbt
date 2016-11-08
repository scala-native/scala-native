import scala.util.Try

val toolScalaVersion = "2.10.6"

val libScalaVersion = "2.11.8"

lazy val baseSettings = Seq(
  organization := "org.scala-native",
  version := nativeVersion,
  sources in doc in Compile := Nil // doc generation currently broken
)

lazy val publishSnapshot =
  taskKey[Unit]("Publish snapshot to sonatype on every commit to master.")

lazy val publishSettings = Seq(
  publishArtifact in Compile := true,
  publishArtifact in Test := false,
  publishMavenStyle := true,
  publishTo <<= version { v: String =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishSnapshot := Def.taskDyn {
    val travis   = Try(sys.env("TRAVIS")).getOrElse("false") == "true"
    val pr       = Try(sys.env("TRAVIS_PULL_REQUEST")).getOrElse("false") != "false"
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
  }.toSeq,
  pomIncludeRepository := { x =>
    false
  },
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
    resolvers := Nil
  )

lazy val projectSettings =
  ScalaNativePlugin.projectSettings ++ Seq(
    scalaVersion := libScalaVersion,
    nativeVerbose := true,
    nativeClangOptions ++= Seq("-O0"),
    resolvers := Nil
  )

lazy val util =
  project.in(file("util")).settings(toolSettings).settings(publishSettings)

lazy val nir =
  project
    .in(file("nir"))
    .settings(toolSettings)
    .settings(publishSettings)
    .dependsOn(util)

lazy val tools =
  project
    .in(file("tools"))
    .settings(toolSettings)
    .settings(publishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "fastparse"  % "0.4.2",
        "com.lihaoyi" %% "scalaparse" % "0.4.2",
        compilerPlugin(
          "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
        "org.scalatest" %% "scalatest" % "3.0.0" % "test"
      ))
    .dependsOn(nir, util)

lazy val nscplugin =
  project
    .in(file("nscplugin"))
    .settings(toolSettings)
    .settings(publishSettings)
    .settings(
      scalaVersion := "2.11.8",
      crossVersion := CrossVersion.full,
      unmanagedSourceDirectories in Compile ++= Seq(
        (scalaSource in (nir, Compile)).value,
        (scalaSource in (util, Compile)).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      ),
      publishArtifact in (Compile, packageDoc) := false
    )

lazy val sbtplugin =
  project
    .in(file("sbtplugin"))
    .settings(toolSettings)
    .settings(publishSettings)
    .settings(
      sbtPlugin := true,
      // Support for scripted tests
      ScriptedPlugin.scriptedSettings,
      scriptedLaunchOpts := {
        scriptedLaunchOpts.value ++
          Seq("-Xmx1024M",
              "-XX:MaxPermSize=256M",
              "-Dplugin.version=" + version.value)
      },
      sbtTestDirectory := (baseDirectory in ThisBuild).value / "scripted-tests",
      // publish the other projects before running scripted tests.
      scripted <<= scripted.dependsOn(publishLocal in util,
                                      publishLocal in nir,
                                      publishLocal in tools,
                                      publishLocal in nscplugin,
                                      publishLocal in nativelib,
                                      publishLocal in javalib,
                                      publishLocal in scalalib)
    )
    .dependsOn(tools)

lazy val nativelib =
  project.in(file("nativelib")).settings(libSettings).settings(publishSettings)

lazy val javalib =
  project
    .in(file("javalib"))
    .settings(libSettings)
    .settings(publishSettings)
    .dependsOn(nativelib)

lazy val assembleScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala and then applies overrides.")

lazy val scalalib =
  project
    .in(file("scalalib"))
    .settings(libSettings)
    .settings(publishSettings)
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
      },
      compile in Compile <<= (compile in Compile) dependsOn assembleScalaLibrary,
      publishLocal <<= publishLocal dependsOn assembleScalaLibrary
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

lazy val tests =
  project
    .in(file("unit-tests"))
    .settings(projectSettings)
    .settings(noPublishSettings)
    .settings(
      sourceGenerators in Compile += Def.task {
        val dir    = sourceDirectory.value
        val prefix = dir.getAbsolutePath + "/main/scala/"
        val suites = (dir ** "*Suite.scala").get.map { f =>
          f.getAbsolutePath
            .replace(prefix, "")
            .replace(".scala", "")
            .split("/")
            .mkString(".")
        }.filter(_ != "tests.Suite").mkString("Seq(", ", ", ")")
        val file = (sourceManaged in Compile).value / "tests" / "Disover.scala"
        IO.write(file,
                 s"""
          package tests
          object Discover {
            val suites: Seq[tests.Suite] = $suites
          }
        """)
        Seq(file)
      }.taskValue
    )

lazy val sandbox =
  project
    .in(file("sandbox"))
    .settings(projectSettings)
    .settings(noPublishSettings)
