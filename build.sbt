import scala.util.Try

val toolScalaVersion = "2.10.6"

val libScalaVersion  = "2.11.8"

lazy val baseSettings = Seq(
  organization := "org.scala-native",
  version      := nativeVersion,

  sources in doc in Compile := Nil,  // doc generation currently broken

  scalafmtConfig := Some(file(".scalafmt"))
)

lazy val publishSnapshot = taskKey[Unit](
  "Publish snapshot to sonatype on every commit to master.")

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
        println("not going to publish a snapshot due to: " +
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

  pomIncludeRepository := { x => false },
  pomExtra := (
    <url>https://github.com/scala-native/scala-native</url>
    <inceptionYear>2015</inceptionYear>
    <licenses>
      <license>
        <name>BSD-like</name>
        <url>http://www.scala-lang.org/downloads/license.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:scala-native/scala-native.git</url>
      <connection>scm:git@github.com:scala-native/scala-native.git</connection>
    </scm>
    <issueManagement>
      <system>GitHub Issues</system>
      <url>https://github.com/scala-native/scala-native/issues</url>
    </issueManagement>
    <developers>
      <developer>
        <id>densh</id>
        <name>Denys Shabalin</name>
        <url>http://den.sh</url>
      </developer>
    </developers>
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
        "-encoding", "utf8"
      ),
      javacOptions ++= Seq("-encoding", "utf8")
    )

lazy val libSettings =
  (baseSettings ++ ScalaNativePlugin.projectSettings.tail) :+
  (scalaVersion := libScalaVersion)

lazy val projectSettings =
  ScalaNativePlugin.projectSettings ++ Seq(
    scalaVersion := libScalaVersion,
    nativeVerbose := true,
    nativeClangOptions ++= Seq("-O0")
  )

lazy val util =
  project.in(file("util")).
    settings(toolSettings).
    settings(publishSettings)

lazy val nir =
  project.in(file("nir")).
    settings(toolSettings).
    settings(publishSettings).
    dependsOn(util)

lazy val tools =
  project.in(file("tools")).
    settings(toolSettings).
    settings(publishSettings).
    dependsOn(nir, util)

lazy val nscplugin =
  project.in(file("nscplugin")).
    settings(toolSettings).
    settings(publishSettings).
    settings(
      scalaVersion := "2.11.8",
      crossVersion := CrossVersion.full,
      unmanagedSourceDirectories in Compile ++= Seq(
        (scalaSource in (nir, Compile)).value,
        (scalaSource in (util, Compile)).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      ),
      publishArtifact in (Compile, packageDoc) := false
    )

lazy val sbtplugin =
  project.in(file("sbtplugin")).
    settings(toolSettings).
    settings(publishSettings).
    settings(
      sbtPlugin := true,
      // Scalafmt fails to format source of sbt plugins.
      scalafmtTest := {},
      // Support for scripted tests
      ScriptedPlugin.scriptedSettings,
      scriptedLaunchOpts := { scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
      },
      sbtTestDirectory := (baseDirectory in ThisBuild).value / "tests",
      // publish the other projects before running scripted tests.
      scripted <<= scripted dependsOn (publishLocal in util,
                                       publishLocal in nir,
                                       publishLocal in tools,
                                       publishLocal in nscplugin,
                                       publishLocal in nativelib,
                                       publishLocal in javalib,
                                       publishLocal in scalalib)
    ).
    dependsOn(tools)

lazy val nativelib =
  project.in(file("nativelib")).
    settings(libSettings).
    settings(publishSettings)

lazy val javalib =
  project.in(file("javalib")).
    settings(libSettings).
    settings(publishSettings).
    dependsOn(nativelib)

lazy val assembleScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala and then applies overrides.")

lazy val scalalib =
  project.in(file("scalalib")).
    settings(libSettings).
    settings(publishSettings).
    settings(
      assembleScalaLibrary := {
        import org.eclipse.jgit.api._

        val s = streams.value
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
        IO.copyDirectory(
          trgDir / "src" / "library" / "scala",
          file("scalalib/src/main/scala/scala"))

        val epoch :: major :: _ = scalaVersion.value.split("\\.").toList
        IO.copyDirectory(
          file(s"scalalib/overrides-$epoch.$major/scala"),
          file("scalalib/src/main/scala/scala"), overwrite = true)
      },

      scalafmtTest := {},

      compile in Compile <<= (compile in Compile) dependsOn assembleScalaLibrary,

      publishLocal <<= publishLocal dependsOn assembleScalaLibrary
    ).
    dependsOn(nativelib, javalib)

lazy val demoJVM =
  project.in(file("demo/jvm")).
    settings(noPublishSettings).
    settings(
      fork in run := true,
      javaOptions in run ++= Seq("-Xms64m", "-Xmx64m")
    )

lazy val demoNative =
  project.in(file("demo/native")).
    settings(projectSettings).
    settings(noPublishSettings)

lazy val sandbox =
  project.in(file("sandbox")).
    settings(projectSettings).
    settings(noPublishSettings)
