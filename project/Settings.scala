package build

import sbt._
import sbt.Keys._
import sbt.nio.Keys.fileTreeView
import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

// Hack warning: special object mimicking build-info plugin outputs, defined in project/ScalaNativeBuildInfo
import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import ScriptedPlugin.autoImport._
import com.jsuereth.sbtpgp.PgpKeys

import scala.collection.mutable
import MyScalaNativePlugin.isGeneratingForIDE

import java.io.File
import java.util.Locale

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
    organization := "org.scala-native",
    version := ScalaNativeBuildInfo.version,
    Global / javaVersion := {
      val fullVersion = System.getProperty("java.version")
      val v = fullVersion.stripPrefix("1.").takeWhile(_.isDigit).toInt
      sLog.value.info(s"Detected JDK version $v")
      if (v < 8)
        throw new MessageOnlyException(
          "This build requires JDK 8 or later. Aborting."
        )
      v
    },
    Global / onLoad ~= { prev =>
      if (!scala.util.Properties.isWin) {
        import java.nio.file._
        val prePush = Paths.get(".git", "hooks", "pre-push")
        Files.createDirectories(prePush.getParent)
        Files.write(
          prePush,
          """#!/bin/sh
          |set -eux
          |CHECK_MODIFIED_ONLY=1 ./scripts/check-lint.sh
          |""".stripMargin.getBytes()
        )
        prePush.toFile.setExecutable(true)
      }
      prev
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
    name := projectName(thisProject.value.id),
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xfatal-warnings",
      "-encoding",
      "utf8"
    ),
    javaReleaseSettings,
    mimaSettings,
    docsSettings,
    scalacOptions ++= ignoredScalaDeprecations(scalaVersion.value),
    resolvers += Resolver.scalaNightlyRepository
  )

  def targetJDKVersion(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((3, minor)) if minor >= 8 => 17
      case _                              => 8
    }
  // Target version as a string, for javac -target and -source flags - jdk 8 compatible
  def targetJDKVersionString(jdkVersion: Int) =
    jdkVersion match {
      case 8       => "1.8"
      case version => version.toString
    }

  def javaReleaseSettings = {
    def patchVersion(prefix: String, scalaVersion: String): Int =
      scalaVersion.stripPrefix(prefix).takeWhile(_.isDigit).toInt
    def canUseRelease(scalaVersion: String) = CrossVersion
      .partialVersion(scalaVersion)
      .fold(false) {
        case (2, 13) => patchVersion("2.13.", scalaVersion) > 8
        case (2, _)  => false
        case (3, 1)  => patchVersion("3.1.", scalaVersion) > 1
        case (3, _)  => true
      }

    Def.settings(
      Compile / scalacOptions += {
        val jdkVersion = targetJDKVersion(scalaVersion.value)
        if (canUseRelease(scalaVersion.value)) s"-release:$jdkVersion"
        else if (scalaVersion.value.startsWith("3.")) s"-Xtarget:$jdkVersion"
        else s"-target:jvm-${targetJDKVersionString(jdkVersion)}"
      },
      Compile / javacOptions ++= {
        val jdkVersion = targetJDKVersion(scalaVersion.value)
        if (canUseRelease(scalaVersion.value)) Nil
        else List(s"-source", targetJDKVersionString(jdkVersion))
      },
      // Remove -source flags from tests to allow for multi-jdk version compliance tests
      Test / scalacOptions ~= { _.filterNot(isScalacJDKTargetOption) },
      Test / javacOptions := {
        val prev = javacOptions.value
        val targetVersion =
          targetJDKVersionString(targetJDKVersion(scalaVersion.value))
        prev.filterNot { opt =>
          isJavacJDKTargetOption(opt) || opt == targetVersion
        }
      }
    )
  }

  def isScalacJDKTargetOption(scalacOption: String) = {
    Seq("-target:", "-Xtarget", "-release:").exists(scalacOption.startsWith)
  }
  def isJavacJDKTargetOption(javacOption: String) = {
    Seq("-source", "-target").exists(javacOption.startsWith)
  }

  def noJavaReleaseSettings = Def.settings(
    scalacOptions ~= { _.filterNot(isScalacJDKTargetOption) },
    javacOptions := {
      val prev = javacOptions.value
      val targetVersion =
        targetJDKVersionString(targetJDKVersion(scalaVersion.value))
      prev.filterNot { opt =>
        isJavacJDKTargetOption(opt) || opt == targetVersion
      }
    }
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
        val isWindows = System
          .getProperty("os.name", "unknown")
          .toLowerCase(Locale.ROOT)
          .startsWith("windows")
        if (isWindows &&
            sys.env.contains("CI") // Always present in GitHub Actions
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
    mimaBinaryIssueFilters ++= BinaryIncompatibilities.moduleFilters
      .getOrElse(name.value, Nil),
    mimaPreviousArtifacts ++= {
      // The previous releases of Scala Native with which this version is binary compatible.
      val binCompatVersions = Set("0.5.4")
      binCompatVersions
        .map { version =>
          ModuleID(organization.value, moduleName.value, version)
            .cross(crossVersion.value)
        }
    }
  )
  lazy val disableMimaSettings = Seq(
    mimaPreviousArtifacts := Set.empty
  )

  // Publishing
  lazy val basePublishSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url("http://www.scala-native.org")),
    startYear := Some(2015),
    licenses := Seq(
      "BSD-like" -> url("http://www.scala-lang.org/downloads/license.html")
    ),
    developers := List(
      Developer(
        email = "denys.shabalin@epfl.ch",
        id = "densh",
        name = "Denys Shabalin",
        url = url("http://den.sh")
      ),
      Developer(
        id = "wojciechmazur",
        name = "Wojciech Mazur",
        email = "wmazur@virtuslab.com",
        url = url("https://github.com/WojciechMazur")
      )
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

  def publishSettings(verScheme: Option[String]): Seq[Setting[_]] =
    Def.settings(
      basePublishSettings,
      mavenPublishSettings,
      versionScheme := verScheme
    )

  /** Based on Scala.js versioning policy Constants for the `verScheme`
   *  parameter of `publishSettings`.
   *
   *  sbt does not define constants in its API for `versionScheme`. It specifies
   *  some strings instead. We use the following version schemes, depending on
   *  the artifacts and the versioning policy in `VERSIONING.md`:
   *
   *    - `"strict"` for artifacts whose public API can break in patch releases
   *    - `"pvp"` for artifacts whose public API can break in minor releases
   *    - `"semver-spec"` for artifacts whose public API can only break in major
   *      releases (e.g., `nativelib`)
   *
   *  At the moment, we only set the version scheme for artifacts in the
   *  "library ecosystem", i.e., javalib nativelib etc. Artifacts of the "tools
   *  ecosystem" do not have a version scheme set.
   *
   *  See also https://www.scala-sbt.org/1.x/docs/Publishing.html#Version+scheme
   */
  object VersionScheme {
    final val BreakOnPatch = "strict"
    final val BreakOnMinor = "pvp"
    final val BreakOnMajor = "early-semver"
  }

  lazy val mavenPublishSettings = Def.settings(
    publishMavenStyle := true,
    pomIncludeRepository := (_ => false),
    publishTo := {
      val centralSnapshots =
        "https://central.sonatype.com/repository/maven-snapshots/"
      if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
      else localStaging.value
    },
    credentials ++= {
      for {
        user <- sys.env.get("SONATYPE_USER")
        password <- sys.env.get("SONATYPE_PASSWORD")
      } yield Credentials(
        realm = "Sonatype Nexus Repository Manager",
        host = "central.sonatype.com",
        userName = user,
        passwd = password
      )
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

  // Tests
  lazy val testsCommonSettings = Def.settings(
    scalacOptions -= "-deprecation",
    scalacOptions ++= Seq("-deprecation:false"),
    scalacOptions --= {
      if (
          // Disable fatal warnings when
          // Scala 3, becouse null.isInstanceOf[String] warning cannot be supressed
          scalaVersion.value.startsWith("3.") ||
          // Scala Native - due to specific warnings for unsafe ops in IssuesTest
          !moduleName.value.contains("jvm")) Seq("-Xfatal-warnings")
      else Nil
    },
    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.JUnit, "-a", "-s")
    ),
    Test / envVars ++= Map(
      "USER" -> System.getProperty("user.name"),
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
      Tests.Argument(TestFrameworks.JUnit, "-a", "-s")
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

  // Get all denylisted tests from a file
  def denylistedFromFile(
      file: File,
      ignoreMissing: Boolean = false
  ): Set[String] =
    if (file.exists())
      IO.readLines(file)
        .filter(l => l.nonEmpty && !l.startsWith("#"))
        .toSet
    else {
      if (ignoreMissing) System.err.println(s"Ignore not existing file $file")
      else throw new RuntimeException(s"Missing file: $file")
      Set.empty
    }

  // Get all scala sources from a directory
  def allScalaFromDir(dir: File): Seq[(String, java.io.File)] =
    (dir ** "*.scala").get.flatMap { file =>
      file.relativeTo(dir) match {
        case Some(rel) => List((rel.toString.replace('\\', '/'), file))
        case None      => Nil
      }
    }

  // Check the coherence of the denylist against the files found.
  def checkDenylistCoherency(
      denylist: Set[String],
      sources: Seq[(String, File)]
  ) = {
    val allClasses = sources.map(_._1).toSet
    val nonexistentDenylisted = denylist.diff(allClasses)
    if (nonexistentDenylisted.nonEmpty) {
      throw new AssertionError(
        s"Sources not found for denylisted tests:\n$nonexistentDenylisted"
      )
    }
  }

  def sharedTestSource(withDenylist: Boolean) = Def.settings(
    Test / unmanagedSources ++= {
      val denylist: Set[String] =
        if (withDenylist)
          denylistedFromFile(
            (Test / resourceDirectory).value / "DenylistedTests.txt"
          )
        else Set.empty

      // start from scala to avoid jdk specific tests
      // baseDirectory = project/{native,jvm}/.{binVersion}
      val testsRootDir = baseDirectory.value.getParentFile.getParentFile
      val sharedTestsDir = testsRootDir / "shared/src/test"
      val `sources 2.12+` = Seq(sharedTestsDir / "scala-2.12+")
      val `sources 2.13+` = `sources 2.12+` :+ sharedTestsDir / "scala-2.13+"
      val `sources 3.2+` = `sources 2.13+` :+ sharedTestsDir / "scala-3.2"
      val extraSharedDirectories =
        scalaVersionsDependendent(scalaVersion.value)(Seq.empty[File]) {
          case (2, 12) => `sources 2.12+`
          case (2, 13) => `sources 2.13+`
          case (3, 1)  => `sources 2.13+`
          case (3, _)  => `sources 3.2+`
        }
      val sharedScalaSources =
        scalaVersionDirectories(sharedTestsDir, "scala", scalaVersion.value)
          .++(extraSharedDirectories)
          .flatMap(allScalaFromDir(_))
      // Denylist contains relative paths from inside of scala version directory (scala, scala-2, etc.)
      // List content of all scala directories when checking denylist coherency
      val allScalaSources = sharedTestsDir
        .listFiles()
        .toList
        .filter(_.getName().startsWith("scala"))
        .flatMap(allScalaFromDir(_))
      checkDenylistCoherency(denylist, allScalaSources)

      sharedScalaSources.collect {
        case (path, file) if !denylist.contains(path) => file
      }
    },
    Test / unmanagedResourceDirectories += {
      val testsRootDir = baseDirectory.value.getParentFile.getParentFile
      testsRootDir / "shared/src/test/resources"
    }
  )

  lazy val javaVersionSharedTestSources = Def.settings(
    Test / unmanagedSourceDirectories ++= {
      val testsRootDir = baseDirectory.value.getParentFile.getParentFile
      val sharedTestDir = testsRootDir / "shared/src/test"
      val scalaVersions = CrossVersion
        .partialVersion(scalaVersion.value)
        .collect {
          case (3, minor) => "3" :: 0.to(minor.toInt).map("3." + _).toList
          case (2, minor) => List("2", s"2.$minor")
        }
        .getOrElse(sys.error("Unsupported Scala version"))
      // Java 8 is reference so start at 9
      for {
        jdkVersion <- 9 to (Global / javaVersion).value
        requireJDK = s"jdk${jdkVersion}"
        scalaVersion <- scalaVersions
        requireScala = s"scala${scalaVersion}"
        requireDir <- List(
          sharedTestDir / s"require-$requireScala",
          sharedTestDir / s"require-$requireJDK",
          sharedTestDir / s"require-$requireScala-$requireJDK"
        )
      } yield requireDir
    }.distinct,
    Test / sourceGenerators += Def.task {
      val nio = file(
        "shared/src/test/scala/org/scalanative/testsuite/javalib/nio"
      )
      // Templates for test file that are hard to decouple to jdk-specific versions
      val resolvableSources = Seq(
        nio / "BufferAdapter.scala.template",
        nio / "ByteBufferTest.scala.template"
      )

      resolvableSources.map { relativePath =>
        val baseDir =
          (Test / baseDirectory).value.getParentFile().getParentFile()
        val outFile =
          (Test / sourceManaged).value / "jdk-resolved" / relativePath
            .toString()
            .stripSuffix(".template")
        val jdkVersion = (Test / javaVersion).value
        println(
          s"Adapting ${relativePath} to JDK $jdkVersion"
        )
        IO.write(
          outFile,
          9.to(jdkVersion)
            .foldLeft(IO.read(baseDir / relativePath.toString())) {
              case (source, jdkVersion) =>
                source
                  .replaceAllLiterally(s"/* >>REQUIRE-JDK-$jdkVersion", "")
                  .replaceAllLiterally(s"<<REQUIRE-JDK-$jdkVersion */", "")
            }
        )
        outFile
      }
    }.taskValue
  )

  lazy val testInterfaceCommonSourcesSettings: Seq[Setting[_]] = {
    def unmanagedSources(baseDirectory: File, dir: String) = baseDirectory
      .getParentFile()
      .getParentFile() / s"test-interface-common/src/$dir/scala"
    def setSourceDirectory(scope: Configuration, dirName: String) =
      scope / unmanagedSourceDirectories += unmanagedSources(
        baseDirectory.value,
        dirName
      )

    Def.settings(
      setSourceDirectory(Compile, "main"),
      setSourceDirectory(Test, "test")
    )
  }

  lazy val experimentalScalaSources: Seq[Setting[_]] = {
    val baseDir = "scala-next"
    def setSourceDirectory(scope: Configuration) = Def.settings(
      // scope / unmanagedSourceDirectories += (scope / sourceDirectory).value / baseDir,
      scope / unmanagedSources := {
        val log = streams.value.log
        val previous = (scope / unmanagedSources).value
        val sourcesDir = (scope / sourceDirectory).value
        val experimentalSources = allScalaFromDir(sourcesDir / baseDir).toMap

        val updatedSources = previous.map { f =>
          val replacement = for {
            relPath <- f.relativeTo(sourcesDir)
            sourceDir = relPath.toPath().getName(0).toString()
            normalizedPath <- f.relativeTo(sourcesDir / sourceDir)
            experimentalSource <- experimentalSources.get(
              normalizedPath.toString().replace(File.separatorChar, '/')
            )
            _ = log.info(
              s"Replacing source $relPath with experimental $baseDir/$normalizedPath in module ${thisProject.value.id}"
            )
          } yield experimentalSource
          replacement.getOrElse(f)
        }
        val newSources = experimentalSources.values.toList.diff(updatedSources)
        updatedSources ++ newSources
      },
      // Adjustment for bloopInstall which tries to add whole source directory leading to double definitions
      scope / sourceDirectories --= {
        val sourcesDir = (scope / sourceDirectory).value
        lazy val experimentalSources = allScalaFromDir(sourcesDir / baseDir)
        if (isGeneratingForIDE && experimentalSources.nonEmpty)
          Seq((scope / scalaSource).value)
        else Nil
      }
    )

    Def.settings(
      setSourceDirectory(Compile),
      setSourceDirectory(Test)
    )
  }

  // Projects
  lazy val compilerPluginSettings = Def.settings(
    crossVersion := CrossVersion.full,
    libraryDependencies ++= Deps.compilerPluginDependencies(scalaVersion.value),
    publishSettings(None),
    mavenPublishSettings,
    exportJars := true,
    scalacOptions --= Seq("-deprecation", "-Xfatal-warnings"),
    scalacOptions ++= ignoredScalaDeprecations(scalaVersion.value),
    disableMimaSettings
  )

  lazy val sbtPluginSettings = Def.settings(
    commonSettings,
    toolSettings,
    publishSettings(None),
    sbtPlugin := true,
    sbtVersion := ScalaVersions.sbt10Version,
    scalaVersion := ScalaVersions.sbt10ScalaVersion,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq(
          "-Xmx1024M",
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
      publishSettings(None),
      javacOptions ++= Seq("-encoding", "utf8")
    )

  def ignoredScalaDeprecations(scalaVersion: String): Seq[String] = {
    def scala213StdLibDeprecations = Seq(
      // In 2.13 lineStream_! was replaced with lazyList_!.
      "method lineStream_!",
      // OpenHashMap is used with value class parameter type, we cannot replace it with AnyRefMap or LongMap
      // Should not be replaced with HashMap due to performance reasons.
      "class|object OpenHashMap",
      "class Stream",
      "method retain in trait SetOps",
      "object AnyRefMap.*Use `scala.collection.mutable.HashMap` "
    ).map(msg => s"-Wconf:cat=deprecation&msg=$msg:s")

    def scala3Deprecations = Seq(
      "`= _` has been deprecated",
      "`_` is deprecated for wildcard arguments of types",
      // -Wconf msg string cannot contain ':' character, it cannot be escaped
      /*The syntax `x: _* is */ "no longer supported for vararg splice",
      "The syntax `<function> _` is no longer supported",
      "with as a type operator has been deprecated",
      "Implicit parameters should be provided with a `using` clause"
    ).map(msg => s"-Wconf:msg=$msg:s")

    CrossVersion
      .partialVersion(scalaVersion)
      .fold(Seq.empty[String]) {
        case (2, 12) => Nil
        case (2, 13) => scala213StdLibDeprecations
        case (3, _)  => scala213StdLibDeprecations ++ scala3Deprecations
      }
  }

  lazy val recompileAllOrNothingSettings = Def.settings(
    /* Recompile all sources when at least 1/10,000 of the source files have
     * changed, i.e., as soon as at least one source file changed.
     */
    incOptions ~= { _.withRecompileAllFraction(0.0001) }
  )

  lazy val NIROnlySettings = Def.settings(
    // Don't include classfiles for javalib in the packaged jar.
    Compile / packageBin / mappings := {
      val previous = (Compile / packageBin / mappings).value
      val ignoredExtensions = Set(".class", ".tasty")
      previous.filter {
        case (_, path) => !ignoredExtensions.exists(path.endsWith(_))
      }
    },
    exportJars := true,
    mimaPreviousArtifacts := Set.empty // No bytecode, so no point to check MiMa
  )
  lazy val commonJavalibSettings = Def.settings(
    recompileAllOrNothingSettings,
    noJavaReleaseSettings, // we don't emit classfiles
    Compile / scalacOptions ++= scalaNativeCompilerOptions(
      "genStaticForwardersForNonTopLevelObjects"
    ),
    NIROnlySettings
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

  def usesSelfContainedStdlib(scalaVersion: String): Boolean =
    CrossVersion.partialVersion(scalaVersion) match {
      // Scala 3.8+ uses self-contained stdlib, previously it was using Scala 2.13 stdlib
      case Some((3, minor)) => minor >= 8
      case _                => true // all Scala 2 stdlibs are self-contained
    }

  def commonScalalibSettings(
      scalaStdLibraryName: String,
      shouldAddDependencyForVersion: String => Boolean = { _ => true }
  ): Seq[Setting[_]] = {
    Def.settings(
      version := scalalibVersion(
        scalaVersion.value,
        (ThisBuild / version).value
      ),
      mavenPublishSettings,
      disabledDocsSettings,
      recompileAllOrNothingSettings,
      // Code to fetch scala sources adapted, with gratitude, from
      // Scala.js Build.scala at the suggestion of @sjrd.
      // https://github.com/scala-js/scala-js/blob/\
      //    1761f94ee31902b61c579d5cb121117c9dc08295/\
      //    project/Build.scala#L1125-L1233
      //
      // By intent, the Scala Native code below is as identical as feasible.
      // Scala Native build.sbt uses a slightly different baseDirectory
      // than Scala.js. See commented starting with "SN Port:" below.
      libraryDependencies ++= {
        if (shouldAddDependencyForVersion(scalaVersion.value))
          Some("org.scala-lang" % scalaStdLibraryName % scalaVersion.value)
        else None
      },
      fetchScalaSource / artifactPath :=
        baseDirectory.value.getParentFile / "target" / "scalaSources" / scalaVersion.value,
      // Create nir.SourceFile relative to Scala sources dir instead of root dir
      // It should use -sourcepath for both, but it fails to compile under Scala 2
      scalacOptions ++=
        scalaNativeCompilerOptions(
          s"positionRelativizationPaths:${crossTarget.value / "patched"};${(fetchScalaSource / artifactPath).value}"
        ),
      scalacOptions --= Seq("-deprecation", "-Xfatal-warnings"),
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
      // Scala.js always uses the same version of sources as used in the runtime
      // In Scala Native to 0.4.x we don't make a full cross version of Scala standard library
      // This means we need to have only 1 version of scalalib to not break current build tools
      // We cannot publish artifacts with 3.2.x, becouse it would not be usable from 3.1.x projects
      // Becouse of that we compile Scala 3.2.x or newer sources with 3.1.3 compiler
      // In theory we can enforce usage of latest version of Scala for compiling only scalalib module,
      // as we don't store .tasty or .class files. This solution however might be more complicated and usnafe
      fetchScalaSource := {
        val version = scalaVersion.value
        val trgDir = (fetchScalaSource / artifactPath).value
        val s = streams.value
        val cacheDir = s.cacheDirectory
        val report = (fetchScalaSource / update).value
        lazy val lm = {
          import sbt.librarymanagement.ivy._
          val ivyConfig = InlineIvyConfiguration()
            .withLog(s.log)
            .withResolvers(
              resolvers.value.toVector ++ InlineIvyConfiguration().resolvers
            )
          IvyDependencyResolution(ivyConfig)
        }
        lazy val scalaLibSourcesJar = lm
          .retrieve(
            "org.scala-lang" % scalaStdLibraryName % version classifier "sources",
            scalaModuleInfo = None,
            retrieveDirectory = cacheDir,
            log = s.log
          )
          .map(
            _.find(
              _.name.endsWith(s"${scalaStdLibraryName}-$version-sources.jar")
            )
          )
          .toOption
          .flatten
          .getOrElse {
            throw new Exception(
              s"Could not fetch ${scalaStdLibraryName} sources for version $version"
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
        (ThisBuild / baseDirectory).value / "scalalib",
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

          /* Exclude files coming from Scala's `library-aux` directory, as they are not
           * meant to be compiled. They are part of the source jar since Scala 2.13.14.
           */
        val ignoredSourceFiles = Set(
          "Any.scala",
          "AnyRef.scala",
          "Singleton.scala",
          "Nothing.scala",
          "Null.scala",
          // Since 3.5.1
          "AnyKind.scala",
          "Matchable.scala"
        ).map(java.nio.file.Paths.get("scala", _))
        var failedToApplyPatches = false
        for {
          srcDir <- sourceDirectories
          normSrcDir = normPath(srcDir)
          scalaGlob = srcDir.toGlob / ** / "*.scala"
          patchGlob = srcDir.toGlob / ** / "*.scala.patch"
          (sourcePath, _) <- listFilesInOrder(scalaGlob, patchGlob)
          if !ignoredSourceFiles.exists(sourcePath.endsWith(_))
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
            if (!scalaSourcePath.exists()) {
              s.log.warn(
                s"Not found matching source file $sourceName for patch in Scala ${scalaVersion.value} sources, skipped"
              )
              return None
            }
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
              var hasErrors = false
              Process(
                command = Seq(
                  "git",
                  "apply",
                  "-C1",
                  "--reject",
                  "--whitespace=fix",
                  "--recount",
                  "--ignore-space-change",
                  sourcePath.toAbsolutePath().toString()
                ),
                cwd = scalaSrcDir
              ).!!(
                ProcessLogger(
                  stdout => (),
                  stderr => {
                    if (stderr.contains("error")) {
                      hasErrors = true
                    }
                    if (hasErrors) s.log.warn(stderr)
                    else s.log.debug(stderr)
                  }
                )
              )
              copy(scalaSourcePath, outputFile)
              Some(outputFile)
            } catch {
              case ex: Exception =>
                // Postpone failing to check which other patches do not apply
                failedToApplyPatches = true
                val path = sourcePath.toFile.relativeTo(srcDir.getParentFile)
                s.log.error(s"Cannot apply patch for $path - $ex")
                None
            } finally {
              if (scalaSourceCopyPath.exists()) {
                copy(scalaSourceCopyPath, scalaSourcePath)
                scalaSourceCopyPath.delete()
              }
            }
          }

          if (!patchGlob.matches(sourcePath))
            addSource(path)(Some(sourcePath.toFile))
          else {
            val sourceName = path.stripSuffix(".patch")
            addSource(sourceName)(
              tryApplyPatch(sourceName)
            )
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
  }

  lazy val commonJUnitTestOutputsSettings = Def.settings(
    noPublishSettings,
    Compile / publishArtifact := false,
    Test / parallelExecution := false,
    Test / unmanagedSourceDirectories +=
      baseDirectory.value
        .getParentFile()
        .getParentFile() / "shared/src/test/scala",
    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.JUnit, "-a", "-s"),
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

  def scalaNativeCompilerOptions(options: String*): Seq[String] = {
    if (isGeneratingForIDE) Nil
    else options.map(opt => s"-P:scalanative:$opt")
  }

  def scalaVersionsDependendent[T](scalaVersion: String)(default: T)(
      matching: PartialFunction[(Long, Long), T]
  ): T =
    CrossVersion
      .partialVersion(scalaVersion)
      .collect(matching)
      .getOrElse(default)
}
