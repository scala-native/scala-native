import java.io.File.pathSeparator
import scala.collection.mutable
import scala.util.Try

val sbt10Version          = "1.1.6" // minimum version
val sbt10ScalaVersion     = "2.12.11"
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
lazy val nameSettings: Seq[Setting[_]] = Seq(
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

// Common start but individual sub-projects may add or remove scalacOptions.
// project/build.sbt uses a less stringent set to bootstrap.
inThisBuild(
  Def.settings(
    organization := "org.scala-native", // Maven <groupId>
    version := nativeVersion,           // Maven <version>
    scalaVersion := libScalaVersion,
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
  "test-all",
  Seq(
    "sandbox/run",
    "testRunner/test",
    "testInterface/test",
    "tools/test",
    "tests/test",
    "nirparser/test",
    "sbtScalaNative/scripted",
    "tools/mimaReportBinaryIssues",
    "junitTestOutputsJVM/test",
    "junitTestOutputsNative/test"
  ).mkString(";")
)

addCommandAlias(
  "test-tools",
  Seq(
    "testRunner/test",
    "testInterface/test",
    "tools/test",
    "nirparser/test",
    "tools/mimaReportBinaryIssues"
  ).mkString(";")
)

addCommandAlias(
  "test-runtime",
  Seq(
    "sandbox/run",
    "tests/test",
    "sbtScalaNative/scripted",
    "junitTestOutputsJVM/test",
    "junitTestOutputsNative/test"
  ).mkString(";")
)

lazy val publishSnapshot =
  taskKey[Unit]("Publish snapshot to sonatype on every commit to master.")

// to publish plugin (we only need to do this once, it's already done!)
// follow: https://www.scala-sbt.org/1.x/docs/Bintray-For-Plugins.html
// then add a new package
// name: sbt-scala-native, license: BSD-like, version control: git@github.com:scala-native/scala-native.git
// to be available without a resolver
// follow: https://www.scala-sbt.org/1.x/docs/Bintray-For-Plugins.html#Linking+your+package+to+the+sbt+organization
lazy val bintrayPublishSettings: Seq[Setting[_]] = Seq(
  bintrayRepository := "sbt-plugins",
  bintrayOrganization := Some("scala-native")
) ++ publishSettings

lazy val mavenPublishSettings: Seq[Setting[_]] = Seq(
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

lazy val publishSettings: Seq[Setting[_]] = Seq(
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

lazy val noPublishSettings: Seq[Setting[_]] = Seq(
  publishArtifact := false,
  packagedArtifacts := Map.empty,
  publish := {},
  publishLocal := {},
  publishSnapshot := { println("no publish") },
  publish / skip := true
) ++ nameSettings

lazy val toolSettings: Seq[Setting[_]] =
  Def.settings(
    sbtVersion := sbt10Version,
    crossSbtVersions := List(sbt10Version),
    scalaVersion := sbt10ScalaVersion,
    javacOptions ++= Seq("-encoding", "utf8")
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

lazy val scalacheckDep = "org.scalacheck" %% "scalacheck" % "1.14.3" % "test"
lazy val scalatestDep  = "org.scalatest"  %% "scalatest"  % "3.1.1"  % "test"

lazy val nirparser =
  project
    .in(file("nirparser"))
    .settings(toolSettings)
    .settings(noPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "fastparse"  % "1.0.0",
        "com.lihaoyi" %% "scalaparse" % "1.0.0",
        scalacheckDep,
        scalatestDep
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
        scalacheckDep,
        scalatestDep
      ),
      Test / fork := true,
      Test / javaOptions ++= {
        val nscpluginjar = (nscplugin / Compile / Keys.`package`).value
        val testingcompilercp =
          (testingCompiler / Compile / fullClasspath).value.files
        val allCoreLibsCp =
          (allCoreLibs / Compile / fullClasspath).value.files
        Seq(
          "-Dscalanative.nscplugin.jar=" + nscpluginjar.getAbsolutePath,
          "-Dscalanative.testingcompiler.cp=" +
            testingcompilercp.map(_.getAbsolutePath).mkString(pathSeparator),
          "-Dscalanative.nativeruntime.cp=" +
            allCoreLibsCp.map(_.getAbsolutePath).mkString(pathSeparator)
        )
      },
      // Running tests in parallel results in `FileSystemAlreadyExistsException`
      Test / parallelExecution := false,
      mimaSettings
    )
    .dependsOn(nir, util, testingCompilerInterface % Test)

lazy val nscplugin =
  project
    .in(file("nscplugin"))
    .settings(mavenPublishSettings)
    .settings(
      crossScalaVersions := libCrossScalaVersions,
      crossVersion := CrossVersion.full,
      Compile / unmanagedSourceDirectories ++= Seq(
        (nir / Compile / scalaSource).value,
        (util / Compile / scalaSource).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      ),
      exportJars := true
    )
    .settings(scalacOptions += "-Xno-patmat-analysis")

lazy val sbtPluginSettings: Seq[Setting[_]] =
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
      // publish the other projects before running scripted tests.
      scriptedDependencies := {
        scriptedDependencies
          .dependsOn(
            // Compiler plugins
            nscplugin / publishLocal,
            junitPlugin / publishLocal,
            // Scala Native libraries
            nativelib / publishLocal,
            clib / publishLocal,
            posixlib / publishLocal,
            javalib / publishLocal,
            auxlib / publishLocal,
            scalalib / publishLocal,
            testInterfaceSbtDefs / publishLocal,
            testInterface / publishLocal,
            junitRuntime / publishLocal,
            // JVM libraries
            util / publishLocal,
            nir / publishLocal,
            tools / publishLocal,
            testRunner / publishLocal
          )
          .value
      }
    )
    .dependsOn(tools, testRunner)

lazy val nativelib =
  project
    .in(file("nativelib"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      exportJars := true
    )
    .dependsOn(nscplugin % "plugin")

lazy val clib =
  project
    .in(file("clib"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .dependsOn(nscplugin % "plugin", nativelib)

lazy val posixlib =
  project
    .in(file("posixlib"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .dependsOn(nscplugin % "plugin", clib)

lazy val javalib =
  project
    .in(file("javalib"))
    .enablePlugins(MyScalaNativePlugin)
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
        val classDir  = (Compile / classDirectory).value.getAbsolutePath
        val separator = sys.props("path.separator")
        "-javabootclasspath" +: s"$classDir$separator$javaBootClasspath" +: previous
      },
      // Don't include classfiles for javalib in the packaged jar.
      Compile / packageBin / mappings := {
        val previous = (Compile / packageBin / mappings).value
        previous.filter {
          case (_, path) =>
            !path.endsWith(".class")
        }
      },
      exportJars := true
    )
    .dependsOn(nscplugin % "plugin", nativelib, posixlib)

val fetchScalaSource =
  taskKey[File]("Fetches the scala source for the current scala version")

lazy val auxlib =
  project
    .in(file("auxlib"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .settings(
      exportJars := true
    )
    .dependsOn(nscplugin % "plugin", nativelib)

lazy val scalalib =
  project
    .in(file("scalalib"))
    .enablePlugins(MyScalaNativePlugin)
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
      // Code to fetch scala sources adapted, with gratitude, from
      // Scala.js Build.scala at the suggestion of @sjrd.
      // https://github.com/scala-js/scala-js/blob/\
      //    1761f94ee31902b61c579d5cb121117c9dc08295/\
      //    project/Build.scala#L1125-L1233
      //
      // By intent, the Scala Native code below is as identical as feasible.
      // Scala Native build.sbt uses a slightly different baseDirectory
      // than Scala.js. See commented starting with "SN Port:" below.
      libraryDependencies +=
        "org.scala-lang" % "scala-library" % scalaVersion.value classifier "sources",
      fetchScalaSource / artifactPath :=
        target.value / "scalaSources" / scalaVersion.value,
      // Scala.js original comment modified to clarify issue is Scala.js.
      /* Work around for https://github.com/scala-js/scala-js/issues/2649
       * We would like to always use `update`, but
       * that fails if the scalaVersion we're looking for happens to be the
       * version of Scala used by sbt itself. This is clearly a bug in sbt,
       * which we work around here by using `updateClassifiers` instead in
       * that case.
       */
      fetchScalaSource / update := Def.taskDyn {
        if (scalaVersion.value == scala.util.Properties.versionNumberString)
          updateClassifiers
        else
          update
      }.value,
      fetchScalaSource := {
        val s        = streams.value
        val cacheDir = s.cacheDirectory
        val ver      = scalaVersion.value
        val trgDir   = (fetchScalaSource / artifactPath).value

        val report = (fetchScalaSource / update).value
        val scalaLibSourcesJar = report
          .select(configuration = configurationFilter("compile"),
                  module = moduleFilter(name = "scala-library"),
                  artifact = artifactFilter(classifier = "sources"))
          .headOption
          .getOrElse {
            throw new Exception(
              s"Could not fetch scala-library sources for version $ver")
          }

        FileFunction.cached(cacheDir / s"fetchScalaSource-$ver",
                            FilesInfo.lastModified,
                            FilesInfo.exists) { dependencies =>
          s.log.info(s"Unpacking Scala library sources to $trgDir...")

          if (trgDir.exists)
            IO.delete(trgDir)
          IO.createDirectory(trgDir)
          IO.unzip(scalaLibSourcesJar, trgDir)
        }(Set(scalaLibSourcesJar))

        trgDir
      },
      Compile / unmanagedSourceDirectories := {
        // Calculates all prefixes of the current Scala version
        // (including the empty prefix) to construct override
        // directories like the following:
        // - override-2.13.0-RC1
        // - override-2.13.0
        // - override-2.13
        // - override-2
        // - override

        val ver = scalaVersion.value

        // SN Port: sjs uses baseDirectory.value.getParentFile here.
        val base  = baseDirectory.value
        val parts = ver.split(Array('.', '-'))
        val verList = parts.inits.map { ps =>
          val len = ps.mkString(".").length
          // re-read version, since we lost '.' and '-'
          ver.substring(0, len)
        }
        def dirStr(v: String) =
          if (v.isEmpty) "overrides" else s"overrides-$v"
        val dirs = verList.map(base / dirStr(_)).filter(_.exists)
        dirs.toSeq // most specific shadow less specific
      },
      // Compute sources
      // Files in earlier src dirs shadow files in later dirs
      Compile / sources := {
        // Sources coming from the sources of Scala
        val scalaSrcDir = fetchScalaSource.value

        // All source directories (overrides shadow scalaSrcDir)
        val sourceDirectories =
          (Compile / unmanagedSourceDirectories).value :+ scalaSrcDir

        // Filter sources with overrides
        def normPath(f: File): String =
          f.getPath.replace(java.io.File.separator, "/")

        val sources = mutable.ListBuffer.empty[File]
        val paths   = mutable.Set.empty[String]

        val s = streams.value

        for {
          srcDir <- sourceDirectories
          normSrcDir = normPath(srcDir)
          src <- (srcDir ** "*.scala").get
        } {
          val normSrc = normPath(src)
          val path    = normSrc.substring(normSrcDir.length)
          val useless =
            path.contains("/scala/collection/parallel/") ||
              path.contains("/scala/util/parsing/")
          if (!useless) {
            if (paths.add(path))
              sources += src
            else
              s.log.debug(s"not including $src")
          }
        }

        sources.result()
      },
      // Don't include classfiles for scalalib in the packaged jar.
      Compile / packageBin / mappings := {
        val previous = (Compile / packageBin / mappings).value
        previous.filter {
          case (file, path) =>
            !path.endsWith(".class")
        }
      },
      exportJars := true
    )
    .dependsOn(nscplugin % "plugin", auxlib, nativelib, javalib)

// Shortcut for further Native projects to depend on all core libraries
lazy val allCoreLibs: Project =
  scalalib // scalalib transitively depends on all the other core libraries

lazy val tests =
  project
    .in(file("unit-tests"))
    .enablePlugins(MyScalaNativePlugin)
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
      testFrameworks ++= Seq(
        new TestFramework("tests.NativeFramework"),
        new TestFramework("com.novocode.junit.JUnitFramework")
      ),
      Test / testOptions ++= Seq(
        Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
      ),
      Test / test / envVars ++= Map(
        "USER"                           -> "scala-native",
        "HOME"                           -> System.getProperty("user.home"),
        "SCALA_NATIVE_ENV_WITH_EQUALS"   -> "1+1=2",
        "SCALA_NATIVE_ENV_WITHOUT_VALUE" -> "",
        "SCALA_NATIVE_ENV_WITH_UNICODE"  -> 0x2192.toChar.toString,
        "SCALA_NATIVE_USER_DIR"          -> System.getProperty("user.dir")
      ),
      nativeLinkStubs := true
    )
    .dependsOn(nscplugin   % "plugin",
               junitPlugin % "plugin",
               allCoreLibs,
               testInterface,
               junitRuntime)

lazy val sandbox =
  project
    .in(file("sandbox"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(scalacOptions -= "-Xfatal-warnings")
    .settings(noPublishSettings)
    .settings(
      // nativeOptimizerReporter := OptimizerReporter.toDirectory(
      //   crossTarget.value),
    )
    .dependsOn(nscplugin % "plugin", allCoreLibs, testInterface % Test)

lazy val testingCompilerInterface =
  project
    .in(file("testing-compiler-interface"))
    .settings(noPublishSettings)
    .settings(
      crossPaths := false,
      crossVersion := CrossVersion.disabled,
      autoScalaLibrary := false
    )

lazy val testingCompiler =
  project
    .in(file("testing-compiler"))
    .settings(noPublishSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value
      ),
      exportJars := true
    )
    .dependsOn(testingCompilerInterface)

lazy val testInterfaceCommonSourcesSettings = Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "test-interface-common/src/main/scala",
  unmanagedSourceDirectories in Test += baseDirectory.value.getParentFile / "test-interface-common/src/test/scala"
)

lazy val testInterface =
  project
    .in(file("test-interface"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .settings(testInterfaceCommonSourcesSettings)
    .dependsOn(nscplugin   % "plugin",
               junitPlugin % "plugin",
               allCoreLibs,
               testInterfaceSbtDefs,
               junitRuntime,
               junitAsyncNative % "test")

lazy val testInterfaceSbtDefs =
  project
    .in(file("test-interface-sbt-defs"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .dependsOn(nscplugin % "plugin", allCoreLibs)

lazy val testRunner =
  project
    .in(file("test-runner"))
    .settings(toolSettings)
    .settings(mavenPublishSettings)
    .settings(testInterfaceCommonSourcesSettings)
    .settings(
      crossScalaVersions := Seq(sbt10ScalaVersion),
      libraryDependencies ++= Seq(
        "org.scala-sbt" % "test-interface"  % "1.0",
        "com.novocode"  % "junit-interface" % "0.11" % "test"
      )
    )
    .dependsOn(tools, junitAsyncJVM % "test")

// JUnit modules and settings ------------------------------------------------

lazy val junitRuntime =
  project
    .in(file("junit-runtime"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .settings(nameSettings)
    .dependsOn(
      nscplugin % "plugin",
      testInterfaceSbtDefs
    )

lazy val junitPlugin =
  project
    .in(file("junit-plugin"))
    .settings(mavenPublishSettings)
    .settings(
      crossScalaVersions := libCrossScalaVersions,
      crossVersion := CrossVersion.full,
      libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      exportJars := true
    )

val commonJUnitTestOutputsSettings = Def.settings(
  nameSettings,
  Compile / publishArtifact := false,
  Test / parallelExecution := false,
  Test / unmanagedSourceDirectories +=
    baseDirectory.value.getParentFile / "shared/src/test/scala",
  Test / testOptions ++= Seq(
    Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v"),
    Tests.Filter(_.endsWith("Assertions"))
  ),
  Test / scalacOptions --= Seq("-deprecation", "-Xfatal-warnings"),
  Test / scalacOptions += "-deprecation:false"
)

lazy val junitTestOutputsNative =
  project
    .in(file("junit-test/output-native"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(
      commonJUnitTestOutputsSettings,
      Test / scalacOptions ++= {
        val jar = (junitPlugin / Compile / packageBin).value
        Seq(s"-Xplugin:$jar")
      }
    )
    .dependsOn(
      nscplugin        % "plugin",
      junitRuntime     % "test",
      junitAsyncNative % "test",
      testInterface    % "test"
    )

lazy val junitTestOutputsJVM =
  project
    .in(file("junit-test/output-jvm"))
    .settings(
      commonJUnitTestOutputsSettings,
      scalaVersion := sbt10ScalaVersion,
      libraryDependencies ++= Seq(
        "com.novocode" % "junit-interface" % "0.11" % "test"
      )
    )
    .dependsOn(junitAsyncJVM % "test")

lazy val junitAsyncNative =
  project
    .in(file("junit-async/native"))
    .enablePlugins(MyScalaNativePlugin)
    .settings(
      nameSettings,
      Compile / publishArtifact := false
    )
    .dependsOn(nscplugin % "plugin", allCoreLibs)

lazy val junitAsyncJVM =
  project
    .in(file("junit-async/jvm"))
    .settings(
      scalaVersion := sbt10ScalaVersion,
      nameSettings,
      publishArtifact := false
    )
