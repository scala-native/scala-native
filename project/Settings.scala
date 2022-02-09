package build

import sbt._
import sbt.Keys._
import sbt.nio.Keys.fileTreeView
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import ScriptedPlugin.autoImport._

import scala.collection.mutable
import scala.scalanative.build.Platform

object Settings {
  lazy val fetchScalaSource = taskKey[File](
    "Fetches the scala source for the current scala version"
  )

  lazy val shouldPartest = settingKey[Boolean](
    "Whether we should partest the current scala version (or skip if we can't)"
  )

  lazy val javaVersion = settingKey[Int](
    "The major Java SDK version that should be assumed for compatibility. " +
      "Defaults to what sbt is running with."
  )

  // JDK version we are running with
  lazy val thisBuildSettings = Def.settings(
    Global / javaVersion := {
      val fullVersion = System.getProperty("java.version")
      val v = fullVersion.stripPrefix("1.").takeWhile(_.isDigit).toInt
      sLog.value.info(s"Detected JDK version $v")
      if (v < 8)
        throw new MessageOnlyException(
          "This build requires JDK 8 or later. Aborting."
        )
      v
    }
  )

// Generate project name from project id.
  private[build] def projectName(id: String): String = {
    // Convert "SomeName" to "some-name".
    id.replaceAll(
      "([a-z])([A-Z]+)",
      "$1-$2"
    ).toLowerCase
  }

  lazy val commonSettings = Def.settings(
    organization := "org.scala-native",
    name := projectName(thisProject.value.id),
    version := nativeVersion,
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xfatal-warnings",
      "-target:jvm-1.8",
      "-encoding",
      "utf8"
    ),
    publishSettings,
    mimaSettings,
    docsSettings
  )

  // Docs and API settings
  lazy val docsSettings: Seq[Setting[_]] = {
    val javaDocBaseURL: String = "https://docs.oracle.com/javase/8/docs/api/"
    // partially ported from Scala.js
    Def.settings(
      autoAPIMappings := true,
      exportJars := true, // required so ScalaDoc linking works
      Compile / doc / scalacOptions --= scalaVersionsDependendent(
        scalaVersion.value
      )(Seq.empty[String]) {
        case (2, 11) => Seq("-Xfatal-warnings")
        case (3, 0 | 1) =>
          val prev = (Compile / doc / scalacOptions).value
          val version = scalaVersion.value
          // Remove all plugins as they lead to throwing exceptions by the compiler
          // Bug was fixes in 3.1.1
          if (version.startsWith("3.1.") &&
              version.stripPrefix("3.1.").takeWhile(_.isDigit).toInt < 1)
            prev.filter(_.contains("-Xplugin"))
          else Seq.empty
        case (3, _) => Seq.empty
      },
      // Add Java Scaladoc mapping
      apiMappings ++= {
        val optRTJar = {
          val bootClasspath = System.getProperty("sun.boot.class.path")
          if (bootClasspath != null) {
            // JDK <= 8, there is an rt.jar (or classes.jar) on the boot classpath
            val jars = bootClasspath.split(java.io.File.pathSeparator)

            def matches(path: String, name: String): Boolean =
              path.endsWith(s"${java.io.File.separator}$name.jar")

            jars
              .find(matches(_, "rt")) // most JREs
              .map(file)
          } else {
            // JDK >= 9, maybe sbt gives us a fake rt.jar in `scala.ext.dirs`
            val scalaExtDirs = Option(System.getProperty("scala.ext.dirs"))
            scalaExtDirs.map(extDirs => file(extDirs) / "rt.jar")
          }
        }

        optRTJar.fold[Map[File, URL]] {
          Map.empty
        } { rtJar =>
          assert(rtJar.exists, s"$rtJar does not exist")
          Map(rtJar -> url(javaDocBaseURL))
        }
      },
      /* Add a second Java Scaladoc mapping for cases where Scala actually
       * understands the jrt:/ filesystem of Java 9.
       */
      apiMappings += file("/modules/java.base") -> url(javaDocBaseURL),
      Compile / doc / sources := {
        val prev = (Compile / doc / sources).value
        if (Platform.isWindows &&
            sys.env.contains("CI") && // Always present in GitHub Actions
            scalaVersion.value.startsWith("3.") // Bug in Scala 3 scaladoc
        ) Nil
        else prev
      }
    )
  }

  lazy val disabledDocsSettings = Def.settings(
    Compile / doc / sources := Nil
  )

  // MiMa
  lazy val mimaSettings = Seq(
    mimaFailOnNoPrevious := false,
    mimaBinaryIssueFilters ++= BinaryIncompatibilities.moduleFilters(
      name.value
    ),
    mimaPreviousArtifacts ++= {
      // The previous releases of Scala Native with which this version is binary compatible.
      val binCompatVersions = Set("0.4.0", "0.4.1", "0.4.2", "0.4.3")
      val toolsProjects = Set("util", "tools", "nir", "test-runner")
      lazy val neverPublishedProjects040 = Map(
        "2.11" -> (toolsProjects ++ Set("windowslib", "scala3lib")),
        "2.12" -> Set("windowslib", "scala3lib"),
        "2.13" -> (toolsProjects ++ Set("windowslib", "scala3lib"))
      )
      lazy val neverPublishedProjects041 = neverPublishedProjects040
        .mapValues(_.diff(Set("windowslib")))
      lazy val neverPublishedProjects042 = neverPublishedProjects041
        .mapValues(_.diff(toolsProjects))

      def wasPublishedInRelease(
          notPublishedProjectsInRelease: Map[String, Set[String]]
      ): Boolean = {
        notPublishedProjectsInRelease
          .get(scalaBinaryVersion.value)
          .exists(!_.contains((thisProject / name).value))
      }
      def wasPreviouslyPublished(version: String) = version match {
        case "0.4.0" => wasPublishedInRelease(neverPublishedProjects040)
        case "0.4.1" => wasPublishedInRelease(neverPublishedProjects041)
        case "0.4.2" => wasPublishedInRelease(neverPublishedProjects042)
        case _       => true // all projects were published
      }
      binCompatVersions
        .filter(wasPreviouslyPublished)
        .map { version =>
          ModuleID(organization.value, moduleName.value, version)
            .cross(crossVersion.value)
        }
    }
  )

  // Publishing
  lazy val publishSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url("http://www.scala-native.org")),
    startYear := Some(2015),
    licenses := Seq(
      "BSD-like" -> url("http://www.scala-lang.org/downloads/license.html")
    ),
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
      )
    ),
    pomExtra := (
      <issueManagement>
        <system>GitHub Issues</system>
        <url>https://github.com/scala-native/scala-native/issues</url>
      </issueManagement>
    ),
    Compile / publishArtifact := true,
    Test / publishArtifact := false
  ) ++ mimaSettings

  lazy val mavenPublishSettings = Def.settings(
    publishSettings,
    publishMavenStyle := true,
    pomIncludeRepository := (_ => false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    credentials ++= {
      for {
        realm <- sys.env.get("MAVEN_REALM")
        domain <- sys.env.get("MAVEN_DOMAIN")
        user <- sys.env.get("MAVEN_USER")
        password <- sys.env.get("MAVEN_PASSWORD")
      } yield Credentials(realm, domain, user, password)
    }.toSeq
  )

  lazy val noPublishSettings = Def.settings(
    disabledDocsSettings,
    publishArtifact := false,
    packagedArtifacts := Map.empty,
    publish := {},
    publishLocal := {},
    publish / skip := true
  )

  // Build Info
  lazy val buildInfoJVMSettings = Def.settings(
    buildInfoPackage := "scala.scalanative.buildinfo",
    buildInfoObject := "ScalaNativeBuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      sbtVersion,
      scalaVersion
    )
  )

  lazy val buildInfoSettings = Def.settings(
    buildInfoJVMSettings,
    buildInfoKeys +=
      "nativeScalaVersion" -> scalaVersion.value
  )

  // Tests
  lazy val testsCommonSettings = Def.settings(
    scalacOptions -= "-deprecation",
    scalacOptions ++= Seq("-deprecation:false"),
    scalacOptions -= "-Xfatal-warnings",
    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
    ),
    Test / envVars ++= Map(
      "USER" -> "scala-native",
      "HOME" -> System.getProperty("user.home"),
      "SCALA_NATIVE_ENV_WITH_EQUALS" -> "1+1=2",
      "SCALA_NATIVE_ENV_WITHOUT_VALUE" -> "",
      "SCALA_NATIVE_ENV_WITH_UNICODE" -> 0x2192.toChar.toString,
      "SCALA_NATIVE_USER_DIR" -> System.getProperty("user.dir")
    ),
    // Some of the tests are designed with an assumptions about default encoding
    // Make sure that tests run on JVM are using default defaults
    Test / javaOptions ++= Seq(
      "-Dfile.encoding=UTF-8" // Windows uses Cp1250 as default
    )
  )

  lazy val testsExtCommonSettings = Def.settings(
    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
    )
  )

  lazy val disabledTestsSettings = {
    def testsTaskUnsupported[T] = Def.task[T] {
      throw new MessageOnlyException(
        s"""Usage of this task in ${(thisProject / name).value} project is not supported in this build.
             |To run tests use explicit syntax containing name of project: <project_name>/<task>.
             |You can also use one of predefined aliases: test-all, test-tools, test-runtime, test-scripted.
             |""".stripMargin
      )
    }

    Def.settings(
      inConfig(Test) {
        Seq(
          test / aggregate := false,
          test := testsTaskUnsupported.value,
          testOnly / aggregate := false,
          testOnly := testsTaskUnsupported.value,
          testQuick / aggregate := false,
          testQuick := testsTaskUnsupported.value,
          executeTests / aggregate := false,
          executeTests := testsTaskUnsupported[Tests.Output].value
        )
      }
    )
  }

  // Get all blacklisted tests from a file
  def blacklistedFromFile(file: File) =
    IO.readLines(file)
      .filter(l => l.nonEmpty && !l.startsWith("#"))
      .toSet

  // Get all scala sources from a directory
  def allScalaFromDir(dir: File): Seq[(String, java.io.File)] =
    (dir ** "*.scala").get.flatMap { file =>
      file.relativeTo(dir) match {
        case Some(rel) => List((rel.toString.replace('\\', '/'), file))
        case None      => Nil
      }
    }

  // Check the coherence of the blacklist against the files found.
  def checkBlacklistCoherency(
      blacklist: Set[String],
      sources: Seq[(String, File)]
  ) = {
    val allClasses = sources.map(_._1).toSet
    val nonexistentBlacklisted = blacklist.diff(allClasses)
    if (nonexistentBlacklisted.nonEmpty) {
      throw new AssertionError(
        s"Sources not found for blacklisted tests:\n$nonexistentBlacklisted"
      )
    }
  }

  def sharedTestSource(withBlacklist: Boolean) = Def.settings(
    Test / unmanagedSources ++= {
      val blacklist: Set[String] =
        if (withBlacklist)
          blacklistedFromFile(
            (Test / resourceDirectory).value / "BlacklistedTests.txt"
          )
        else Set.empty

      // start from scala to avoid jdk specific tests
      // baseDirectory = project/{native,jvm}/.{binVersion}
      val testsRootDir = baseDirectory.value.getParentFile.getParentFile()
      val sharedTestsDir = testsRootDir / "shared/src/test"
      val extraSharedDirectories =
        scalaVersionsDependendent(scalaVersion.value)(List.empty[File]) {
          case (2, 13) => sharedTestsDir / "scala-2.13+" :: Nil
          case (3, _)  => sharedTestsDir / "scala-2.13+" :: Nil
        }
      val sharedScalaSources =
        scalaVersionDirectories(sharedTestsDir, "scala", scalaVersion.value)
          .++(extraSharedDirectories)
          .flatMap(allScalaFromDir(_))
      // Blacklist contains relative paths from inside of scala version directory (scala, scala-2, etc)
      // List content of all scala directories when checking blacklist coherency
      val allScalaSources = sharedTestsDir
        .listFiles()
        .toList
        .filter(_.getName().startsWith("scala"))
        .flatMap(allScalaFromDir(_))
      checkBlacklistCoherency(blacklist, allScalaSources)

      sharedScalaSources.collect {
        case (path, file) if !blacklist.contains(path) => file
      }
    },
    Test / unmanagedResourceDirectories += {
      val testsRootDir = baseDirectory.value.getParentFile.getParentFile()
      testsRootDir / "shared/src/test/resources"
    }
  )

  lazy val javaVersionBasedTestSources = Def.settings(
    Test / unmanagedSourceDirectories ++= {
      val testDir = (Test / baseDirectory).value
      val sharedTestDir = testDir.getParentFile / "shared/src/test"
      // Java 8 is reference so start at 9
      (9 to (Global / javaVersion).value).map { v =>
        sharedTestDir / s"require-jdk$v"
      }
    }
  )

  lazy val testInterfaceCommonSourcesSettings: Seq[Setting[_]] = Def.settings(
    Compile / unmanagedSourceDirectories +=
      baseDirectory.value
        .getParentFile()
        .getParentFile() / "test-interface-common/src/main/scala",
    Test / unmanagedSourceDirectories += baseDirectory.value
      .getParentFile()
      .getParentFile() / "test-interface-common/src/test/scala",
    scalacOptions --= scalaVersionsDependendent(scalaVersion.value)(
      Seq.empty[String]
    ) {
      // In Scala 2 enum `Status.value` is defined as `values()`, however in Scala 3 it's `values`
      case (2, 13) => Seq("-Xfatal-warnings")
    }
  )

  // Projects
  lazy val compilerPluginSettings = Def.settings(
    crossVersion := CrossVersion.full,
    libraryDependencies ++= Deps.compilerPluginDependencies(scalaVersion.value),
    mavenPublishSettings,
    exportJars := true
  )

  lazy val sbtPluginSettings = Def.settings(
    commonSettings,
    toolSettings,
    mavenPublishSettings,
    sbtPlugin := true,
    sbtVersion := ScalaVersions.sbt10Version,
    scalaVersion := ScalaVersions.sbt10ScalaVersion,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq(
          "-Xmx1024M",
          "-XX:MaxMetaspaceSize=256M",
          "-Dplugin.version=" + version.value,
          // Default scala.version, can be overriden in test-scrippted command
          "-Dscala.version=" + ScalaVersions.scala212,
          "-Dfile.encoding=UTF-8" // Windows uses Cp1250 as default
        ) ++
        ivyPaths.value.ivyHome.map(home => s"-Dsbt.ivy.home=$home").toSeq
    }
  )

  lazy val toolSettings: Seq[Setting[_]] =
    Def.settings(
      javacOptions ++= Seq("-encoding", "utf8")
    )

  lazy val commonJavalibSettings = Def.settings(
    disabledDocsSettings,
    // This is required to have incremental compilation to work in javalib.
    // We put our classes on scalac's `javabootclasspath` so that it uses them
    // when compiling rather than the definitions from the JDK.
    Compile / scalacOptions := {
      val previous = (Compile / scalacOptions).value
      val javaBootClasspath =
        scala.tools.util.PathResolver.Environment.javaBootClassPath
      val classDir = (Compile / classDirectory).value.getAbsolutePath
      val separator = sys.props("path.separator")
      "-javabootclasspath" +: s"$classDir$separator$javaBootClasspath" +: previous
    },
    Compile / scalacOptions ++= scalaNativeCompilerOptions(
      "genStaticForwardersForNonTopLevelObjects"
    ),
    // Don't include classfiles for javalib in the packaged jar.
    Compile / packageBin / mappings := {
      val previous = (Compile / packageBin / mappings).value
      val ignoredExtensions = Set(".class", ".tasty")
      previous.filter {
        case (_, path) => !ignoredExtensions.exists(path.endsWith(_))
      }
    },
    exportJars := true
  )

  // Calculates all prefixes of the current Scala version
  // (including the empty prefix) to construct Scala version depenent
  // directories like the following:
  // - override-2.13.0-RC1
  // - override-2.13.0
  // - override-2.13
  // - override-2
  // - override
  def scalaVersionDirectories(
      base: File,
      name: String,
      scalaVersion: String
  ): Seq[File] = {
    val parts = scalaVersion.split(Array('.', '-'))
    val verList = parts.inits.map { ps =>
      val len = ps.mkString(".").length
      // re-read version, since we lost '.' and '-'
      scalaVersion.substring(0, len)
    }
    def dirStr(v: String) =
      if (v.isEmpty) name else s"$name-$v"
    val dirs = verList.map(base / dirStr(_)).filter(_.exists)
    dirs.toSeq // most specific shadow less specific
  }

  def commonScalalibSettings(libraryName: String): Seq[Setting[_]] =
    Def.settings(
      mavenPublishSettings,
      disabledDocsSettings,
      // Code to fetch scala sources adapted, with gratitude, from
      // Scala.js Build.scala at the suggestion of @sjrd.
      // https://github.com/scala-js/scala-js/blob/\
      //    1761f94ee31902b61c579d5cb121117c9dc08295/\
      //    project/Build.scala#L1125-L1233
      //
      // By intent, the Scala Native code below is as identical as feasible.
      // Scala Native build.sbt uses a slightly different baseDirectory
      // than Scala.js. See commented starting with "SN Port:" below.
      libraryDependencies += "org.scala-lang" % libraryName % scalaVersion.value classifier "sources",
      fetchScalaSource / artifactPath :=
        baseDirectory.value.getParentFile / "target" / "scalaSources" / scalaVersion.value,
      // Scala.js original comment modified to clarify issue is Scala.js.
      /* Work around for https://github.com/scala-js/scala-js/issues/2649
       * We would like to always use `update`, but
       * that fails if the scalaVersion we're looking for happens to be the
       * version of Scala used by sbt itself. This is clearly a bug in sbt,
       * which we work around here by using `updateClassifiers` instead in
       * that case.
       */
      fetchScalaSource / update := Def.taskDyn {
        val version = scalaVersion.value
        val usedScalaVersion = scala.util.Properties.versionNumberString
        if (version == usedScalaVersion) updateClassifiers
        else update
      }.value,
      fetchScalaSource := {
        val version = scalaVersion.value
        val trgDir = (fetchScalaSource / artifactPath).value
        val s = streams.value
        val cacheDir = s.cacheDirectory
        val report = (fetchScalaSource / update).value
        val scalaLibSourcesJar = report
          .select(
            configuration = configurationFilter("compile"),
            module = moduleFilter(name = libraryName),
            artifact = artifactFilter(classifier = "sources")
          )
          .headOption
          .getOrElse {
            throw new Exception(
              s"Could not fetch $libraryName sources for version $version"
            )
          }

        FileFunction.cached(
          cacheDir / s"fetchScalaSource-$version",
          FilesInfo.lastModified,
          FilesInfo.exists
        ) { dependencies =>
          s.log.info(s"Unpacking Scala library sources to $trgDir...")

          if (trgDir.exists)
            IO.delete(trgDir)
          IO.createDirectory(trgDir)
          IO.unzip(scalaLibSourcesJar, trgDir)
        }(Set(scalaLibSourcesJar))
        trgDir
      },
      Compile / unmanagedSourceDirectories := scalaVersionDirectories(
        baseDirectory.value.getParentFile(),
        "overrides",
        scalaVersion.value
      ),
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
        val paths = mutable.Set.empty[String]

        val s = streams.value
        val fileTree = fileTreeView.value
        def listFilesInOrder(patterns: Glob*) =
          patterns.flatMap(fileTree.list(_))

        var failedToApplyPatches = false
        for {
          srcDir <- sourceDirectories
          normSrcDir = normPath(srcDir)
          scalaGlob = srcDir.toGlob / ** / "*.scala"
          patchGlob = srcDir.toGlob / ** / "*.scala.patch"
          (sourcePath, _) <- listFilesInOrder(scalaGlob, patchGlob)
          path = normPath(sourcePath.toFile).substring(normSrcDir.length)
        } {
          def addSource(path: String)(optSource: => Option[File]): Unit = {
            if (paths.contains(path)) s.log.debug(s"not including $path")
            else {
              optSource.foreach { source =>
                paths += path
                sources += source
              }
            }
          }

          def copy(source: File, destination: File) = {
            import java.nio.file.Files
            import java.nio.file.StandardCopyOption._
            Files.copy(
              source.toPath(),
              destination.toPath(),
              COPY_ATTRIBUTES,
              REPLACE_EXISTING
            )
          }

          def tryApplyPatch(sourceName: String): Option[File] = {
            val scalaSourcePath = scalaSrcDir / sourceName
            val scalaSourceCopyPath = scalaSrcDir / (sourceName + ".copy")
            val outputFile = crossTarget.value / "patched" / sourceName
            val outputDir = outputFile.getParentFile
            if (!outputDir.exists()) {
              IO.createDirectory(outputDir)
            }
            // There is not a single JVM library for diff that can apply
            // patches in a fuzzy way (using context lines). We also
            // canot use jgit to apply patches - it fails due to "invalid hunk headers".
            // Becouse of that we use git apply instead.
            // We need to create copy of original file and restore it after creating
            // patched file to allow for recompilation of sources (re-applying patches)
            // git apply command needs to be used from within fetchedScalaSource directory.
            try {
              import scala.sys.process._
              copy(scalaSourcePath, scalaSourceCopyPath)
              Process(
                command = Seq(
                  "git",
                  "apply",
                  "--whitespace=fix",
                  "--recount",
                  sourcePath.toAbsolutePath().toString()
                ),
                cwd = scalaSrcDir
              ) !! s.log

              copy(scalaSourcePath, outputFile)
              Some(outputFile)
            } catch {
              case _: Exception =>
                // Postpone failing to check which other patches do not apply
                failedToApplyPatches = true
                val path = sourcePath.toFile.relativeTo(srcDir.getParentFile)
                s.log.error(s"Cannot apply patch for $path")
                None
            } finally {
              if (scalaSourceCopyPath.exists()) {
                copy(scalaSourceCopyPath, scalaSourcePath)
                scalaSourceCopyPath.delete()
              }
            }
          }

          val useless =
            path.contains("/scala/collection/parallel/") ||
              path.contains("/scala/util/parsing/")
          if (!useless) {
            if (!patchGlob.matches(sourcePath))
              addSource(path)(Some(sourcePath.toFile))
            else {
              val sourceName = path.stripSuffix(".patch")
              addSource(sourceName)(
                tryApplyPatch(sourceName)
              )
            }
          }
        }

        if (failedToApplyPatches) {
          throw new Exception(
            "Failed to apply some of scalalib patches, check logs for more information"
          )
        }
        sources.result()
      },
      // Don't include classfiles/tasty for scalalib in the packaged jar.
      Compile / packageBin / mappings := {
        val previous = (Compile / packageBin / mappings).value
        val ignoredExtensions = Set(".class", ".tasty")
        previous.filter {
          case (file, path) => !ignoredExtensions.exists(path.endsWith)
        }
      },
      // Sources in scalalib are only internal overrides, we don't include them in the resulting sources jar
      Compile / packageSrc / mappings := Seq.empty,
      exportJars := true
    )

  lazy val commonJUnitTestOutputsSettings = Def.settings(
    noPublishSettings,
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

// Partests
  def shouldPartestSetting: Seq[Def.Setting[_]] = {
    Def.settings(
      shouldPartest := {
        baseDirectory.value.getParentFile.getParentFile / "scala-partest-tests" / "src" / "test" / "resources" /
          "scala" / "tools" / "partest" / "scalanative" / scalaVersion.value
      }.exists()
    )
  }

// Compat
  lazy val scala3CompatSettings = Def.settings(
    scalacOptions := {
      val prev = scalacOptions.value
      prev.map {
        case "-target:jvm-1.8" => "-Xtarget:8"
        case v                 => v
      }
    }
  )

  def scalaNativeCompilerOptions(options: String*): Seq[String] = {
    options.map(opt => s"-P:scalanative:$opt")
  }

  def scalaVersionsDependendent[T](scalaVersion: String)(default: T)(
      matching: PartialFunction[(Long, Long), T]
  ): T =
    CrossVersion
      .partialVersion(scalaVersion)
      .collect(matching)
      .getOrElse(default)
}
