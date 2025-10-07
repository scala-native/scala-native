// scalafmt: { maxColumn = 120}
package build

import sbt._
import Keys._

import scala.language.implicitConversions

import java.io.File.pathSeparator
import sbtbuildinfo.BuildInfoPlugin

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import pl.project13.scala.sbt.JmhPlugin
import JmhPlugin.JmhKeys._
import sbtbuildinfo._
import sbtbuildinfo.BuildInfoKeys._
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import scala.scalanative.build._
import ScriptedPlugin.autoImport._

object Build {
  import ScalaVersions._
  import Settings._
  import Deps._
  import NoIDEExport.noIDEExportSettings
  import MyScalaNativePlugin.{isGeneratingForIDE, ideScalaVersion}

// format: off
  lazy val compilerPlugins: List[MultiScalaProject] =  List(nscPlugin, junitPlugin)
  lazy val noCrossProjects: List[Project] = List(sbtScalaNative, javalibintf)
  lazy val publishedMultiScalaProjects = compilerPlugins ++ List(
    nir, util, tools,
    nirJVM, utilJVM, toolsJVM,
    nativelib, clib, posixlib, windowslib,
    auxlib, javalib, scalalib, scala3lib,
    testInterface, testInterfaceSbtDefs, testRunner,
    junitRuntime
  )
  lazy val testMultiScalaProjects = List(
      javalibExtDummies,
      testingCompiler,
      junitAsyncNative, junitAsyncJVM,
      junitTestOutputsJVM, junitTestOutputsNative,
      tests, testsJVM, testsExt, testsExtJVM, sandbox,
      scalaPartest, scalaPartestRuntime,
      scalaPartestTests, scalaPartestJunitTests,
      toolsBenchmarks
    )
  lazy val testNoCrossProject = List(testingCompilerInterface)
// format: on
  lazy val allMultiScalaProjects =
    publishedMultiScalaProjects ::: testMultiScalaProjects
  lazy val crossPublishedMultiScalaProjects =
    scalalib :: scala3lib :: compilerPlugins
  lazy val publishedProjects =
    noCrossProjects ::: publishedMultiScalaProjects.flatMap(_.componentProjects)
  lazy val testProjects =
    testMultiScalaProjects.flatMap(_.componentProjects) ::: testNoCrossProject
  lazy val allProjects = publishedProjects ::: testProjects

  private def setDependency[T](key: TaskKey[T], projects: Seq[Project]) = {
    key := key.dependsOn(projects.map(_ / key): _*).value
  }

  private def setDependencyForCurrentBinVersion[T](
      key: TaskKey[T],
      projects: Seq[MultiScalaProject],
      includeNoCrossProjects: Boolean = true
  ) = {
    key := Def.taskDyn {
      val binVersion = scalaBinaryVersion.value
      // There are 2 not cross build projects:
      // sbt-plugin which needs to build with 2.12
      // javalib-intf which contains only Java code and can be compiled with any version
      val optNoCrossProjects = noCrossProjects.filter(_ => includeNoCrossProjects && binVersion == "2.12")
      val dependencies =
        optNoCrossProjects ++ projects.map(_.forBinaryVersion(binVersion))
      val prev = key.value
      Def
        .task { prev }
        .dependsOn(dependencies.map(_ / key): _*)
    }.value
  }

  lazy val root: Project =
    Project(id = "scala-native", base = file("."))
      .settings(
        name := "Scala Native",
        scalaVersion := ScalaVersions.scala212,
        crossScalaVersions := ScalaVersions.libCrossScalaVersions,
        noIDEExportSettings,
        commonSettings,
        noPublishSettings,
        disabledTestsSettings,
        setDependency(clean, allProjects),
        mavenPublishSettings,
        Seq(Compile / compile, Test / compile).map(
          setDependencyForCurrentBinVersion(_, allMultiScalaProjects)
        ),
        Seq(publish, publishSigned, publishLocal, Compile / doc).map(
          setDependencyForCurrentBinVersion(_, publishedMultiScalaProjects)
        )
      )

  // Compiler plugins
  lazy val nscPlugin: MultiScalaProject = MultiScalaProject(
    "nscplugin",
    file("nscplugin"),
    additionalIDEScalaVersions = List("2.13")
  ).withBuildInfo
    .settings(
      compilerPluginSettings,
      scalacOptions ++= scalaVersionsDependendent(scalaVersion.value)(
        Seq.empty[String]
      ) {
        case (2, _) => Seq("-Xno-patmat-analysis")
      },
      scalacOptions --= ignoredScalaDeprecations(scalaVersion.value),
      libraryDependencies ++= Deps.JUnitJvm,
      Test / fork := true
    )
    .mapBinaryVersions {
      // Scaladoc for Scala 2.12 does not handle literal constants correctly
      // It does not allow integer constant < 255 to be passed as arugment of function taking byte
      case "2.12" => _.settings(disabledDocsSettings)
      case _      => identity
    }
    .settings(
      Test / unmanagedSourceDirectories ++= (testingCompilerInterface / Compile / unmanagedSourceDirectories).value
    )
    .dependsOnSource(nirJVM)
    .dependsOnSource(utilJVM)
    .zippedSettings(Seq("testingCompiler", "nativelib")) {
      case Seq(testingCompiler, nativelib) =>
        Test / javaOptions ++= {
          val nscCompilerJar =
            (Compile / Keys.`package`).value.getAbsolutePath()
          val testingCompilerCp =
            (testingCompiler / Compile / fullClasspath).value.files
              .map(_.getAbsolutePath)
              .mkString(pathSeparator)
          val nativelibCp = (nativelib / Compile / fullClasspath).value.files
            .map(_.getAbsolutePath)
            .mkString(pathSeparator)
          Seq(
            "-Dscalanative.nscplugin.jar=" + nscCompilerJar,
            "-Dscalanative.testingcompiler.cp=" + testingCompilerCp,
            "-Dscalanative.nativeruntime.cp=" + nativelibCp
          )
        }
    }

  lazy val junitPlugin = MultiScalaProject("junitPlugin", file("junit-plugin"))
    .settings(
      compilerPluginSettings,
      scalacOptions --= ignoredScalaDeprecations(scalaVersion.value)
    )

  private val withSharedCrossPlatformSources = {
    def sharedSourceDirs(
        scalaVersion: String,
        baseDirectory: File,
        subDir: String
    ) = {
      // baseDirectory = project/jvm/.<scala-version>
      val base = baseDirectory.getParentFile().getParentFile() / "src" / subDir
      val common = base / "scala"
      CrossVersion.partialVersion(scalaVersion) match {
        case Some((2, 12)) =>
          Seq(base / "scala", base / "scala-2", base / "scala-2.12")
        case Some((2, 13)) =>
          Seq(
            base / "scala",
            base / "scala-2",
            base / "scala-2.13",
            base / "scala-2.13+"
          )
        case Some((3, _)) =>
          Seq(base / "scala", base / "scala-3", base / "scala-2.13+")
        case _ => sys.error(s"Unsupported Scala version: ${scalaVersion}")
      }
    }
    Def.settings(
      Compile / unmanagedSourceDirectories ++= sharedSourceDirs(
        scalaVersion.value,
        baseDirectory.value,
        "main"
      ),
      Test / unmanagedSourceDirectories ++= sharedSourceDirs(
        scalaVersion.value,
        baseDirectory.value,
        "test"
      )
    )
  }

  // NIR compiler
  lazy val util = MultiScalaProject("util", file("util/native"))
    .settings(
      toolSettings,
      withSharedCrossPlatformSources
    )
    .withNativeCompilerPlugin
    .withScalaStandardLibrary

  lazy val utilJVM =
    MultiScalaProject(id = "utilJVM", name = "util", file("util/jvm"))
      .settings(
        toolSettings,
        withSharedCrossPlatformSources
      )

  lazy val nir = MultiScalaProject("nir", file("nir/native"))
    .mapBinaryVersions {
      // Scaladoc for Scala 2.12 is not compliant with normal compiler (see nscPlugin)
      case "2.12" => _.settings(disabledDocsSettings)
      case _      => identity
    }
    .withNativeCompilerPlugin
    .withCommonTools
    .withJUnitPlugin
    .dependsOn(util)
    .dependsOn(testInterface % "test", junitRuntime % "test")

  lazy val nirJVM = MultiScalaProject("nirJVM", "nir", file("nir/jvm"))
    .settings(
      libraryDependencies ++= Deps.JUnitJvm
    )
    .withCommonTools
    .mapBinaryVersions {
      // Scaladoc for Scala 2.12 is not compliant with normal compiler (see nscPlugin)
      case "2.12" => _.settings(disabledDocsSettings)
      case _      => identity
    }
    .dependsOn(utilJVM)

  private val scalalibProjectSelect: Map[String, Map[String, String]] = Map(
    "3" -> Map("scalalib" -> "scala3lib"),
    "3-next" -> Map("scalalib" -> "scala3lib")
  )

  lazy val tools = MultiScalaProject("tools", file("tools/native"))
    .settings(
      // Multiple check warnings due to usage of self-types
      nativeConfig ~= { _.withCheckFatalWarnings(false) },
      // One of the biggest blockers is lack of ZipFileSystemProvider required to operate on JARs
      Test / test := {
        val log = streams.value.log
        log.warn(
          "Unable to test tools using Scala Native yet - missing javalib dependencies / compiler integration"
        )
      }
    )
    .withJUnitPlugin
    .withNativeCompilerPlugin
    .withCommonTools
    .dependsOn(nir, util)
    .dependsOn(testInterface % "test", junitRuntime % "test")
    .zippedSettings(
      Seq("nscplugin", "javalib", "scalalib"),
      versionsProjectReplacement = scalalibProjectSelect
    ) {
      case Seq(nscPlugin, javalib, scalalib) =>
        toolsBuildInfoSettings(nscPlugin, javalib, scalalib)
    }

  lazy val toolsJVM =
    MultiScalaProject(id = "toolsJVM", name = "tools", file("tools/jvm"))
      .settings(
        libraryDependencies ++= Deps.JUnitJvm,
        Test / fork := true
      )
      .withCommonTools
      .zippedSettings(Seq("nscplugin", "javalib", "scalalib"), versionsProjectReplacement = scalalibProjectSelect) {
        case Seq(nscPlugin, javalib, scalalib) =>
          toolsBuildInfoSettings(nscPlugin, javalib, scalalib)
      }
      .dependsOn(nirJVM, utilJVM)

  private def toolsBuildInfoSettings(
      nscPlugin: LocalProject,
      javalib: LocalProject,
      scalalib: LocalProject
  ) = {
    buildInfoKeys ++= Seq[BuildInfoKey](
      BuildInfoKey.map(scalaInstance) {
        case (_, v) =>
          "scalacJars" -> v.allJars
            .map(_.getAbsolutePath())
            .mkString(pathSeparator)
      },
      BuildInfoKey.map(Compile / managedClasspath) {
        case (_, v) =>
          "compileClasspath" -> v.files
            .map(_.getAbsolutePath())
            .mkString(pathSeparator)
      },
      BuildInfoKey.map(nscPlugin / Compile / Keys.`package`) {
        case (_, v) =>
          "pluginJar" -> v.getAbsolutePath()
      },
      BuildInfoKey.map(
        for {
          scalalibCp <- (scalalib / Compile / fullClasspath).taskValue
          javalibCp <- (javalib / Compile / fullClasspath).taskValue
        } yield scalalibCp ++ javalibCp
      ) {
        case (_, v) =>
          "nativeRuntimeClasspath" ->
            v.files
              .map(_.getAbsolutePath)
              .distinct
              .mkString(pathSeparator)
      }
    )
  }

  lazy val toolsBenchmarks =
    MultiScalaProject("toolsBenchmarks", file("tools-benchmarks"))
      .enablePlugins(JmhPlugin, BuildInfoPlugin)
      .dependsOn(toolsJVM % "compile->test")
      .settings(
        toolSettings,
        noPublishSettings,
        inConfig(Jmh)(
          Def.settings(
            sourceDirectory := (Compile / sourceDirectory).value,
            classDirectory := (Compile / classDirectory).value,
            dependencyClasspath := (Compile / dependencyClasspath).value,
            compile := (Jmh / compile).dependsOn(Compile / compile).value,
            run := (Jmh / run).dependsOn(Jmh / compile).evaluated
          )
        )
      )
      .zippedSettings(Seq("testInterface")) {
        case Seq(testInterface) =>
          Def.settings(
            // Only generate build info for test configuration
            // Compile / buildInfoObject := "TestSuiteBuildInfo",
            Compile / buildInfoPackage := "scala.scalanative.benchmarks",
            Compile / buildInfoKeys := List(
              BuildInfoKey.map(testInterface / Test / fullClasspath) {
                case (key, value) =>
                  ("fullTestSuiteClasspath", value.toList.map(_.data))
              }
            )
          )
      }

  lazy val sbtScalaNative: Project =
    project
      .in(file("sbt-scala-native"))
      .enablePlugins(ScriptedPlugin)
      .settings(
        {
          if (ideScalaVersion == "2.12") Nil
          else noIDEExportSettings
        },
        sbtPluginSettings,
        disabledDocsSettings,
        addSbtPlugin(Deps.SbtPlatformDeps),
        sbtTestDirectory := (ThisBuild / baseDirectory).value / "scripted-tests",
        // publish the other projects before running scripted tests.
        scriptedDependencies := {
          import java.nio.file.{Files, StandardCopyOption}
          // Synchronize SocketHelpers used in java-net-socket test
          // Each scripted test creates its own environment in tmp directory
          // which does not allow us to define external sources in script build
          Files.copy(
            ((javalib.v2_12 / Compile / scalaSource).value / "java/net/SocketHelpers.scala").toPath,
            (sbtTestDirectory.value / "run/java-net-socket/SocketHelpers.scala").toPath,
            StandardCopyOption.REPLACE_EXISTING
          )
          scriptedDependencies
            .dependsOn(Def.taskDyn {
              // Read scriptedLaunchOpts to get rid of cyclic dependency with root project
              val ver = {
                val versionProp = "-Dscala.version="
                val scalaVersion = scriptedLaunchOpts.value
                  .find(_.startsWith(versionProp))
                  .map(_.stripPrefix(versionProp))
                  .getOrElse(
                    throw new RuntimeException(
                      "scala.version not set in scripted launch opts"
                    )
                  )
                MultiScalaProject.scalaCrossVersions
                  .collectFirst {
                    case (binV, crossV) if crossV.contains(scalaVersion) => binV
                  }
                  .getOrElse(CrossVersion.binaryScalaVersion(scalaVersion))
              }

              def publishLocalVersion(ver: String) = {
                Def
                  .task(())
                  .dependsOn(
                    // Compiler plugins
                    nscPlugin.forBinaryVersion(ver) / publishLocal,
                    junitPlugin.forBinaryVersion(ver) / publishLocal,
                    // Native libraries
                    nativelib.forBinaryVersion(ver) / publishLocal,
                    clib.forBinaryVersion(ver) / publishLocal,
                    posixlib.forBinaryVersion(ver) / publishLocal,
                    windowslib.forBinaryVersion(ver) / publishLocal,
                    // Standard language libraries
                    javalib.forBinaryVersion(ver) / publishLocal,
                    auxlib.forBinaryVersion(ver) / publishLocal,
                    scalalib.forBinaryVersion(ver) / publishLocal,
                    scala3lib.forBinaryVersion(ver) / publishLocal,
                    // Testing infrastructure
                    testInterfaceSbtDefs.forBinaryVersion(ver) / publishLocal,
                    testInterface.forBinaryVersion(ver) / publishLocal,
                    junitRuntime.forBinaryVersion(ver) / publishLocal,
                    // JVM libraries
                    utilJVM.forBinaryVersion(ver) / publishLocal,
                    nirJVM.forBinaryVersion(ver) / publishLocal,
                    toolsJVM.forBinaryVersion(ver) / publishLocal,
                    testRunner.forBinaryVersion(ver) / publishLocal
                  )
              }

              publishLocalVersion(ver)
                .dependsOn(
                  // Scala 3 needs 2.13 deps for its cross version compat tests
                  if (ver.startsWith("3")) publishLocalVersion("2.13")
                  else Def.task(())
                )
            })
            .value
        }
      )
      .dependsOn(toolsJVM.v2_12, testRunner.v2_12)

// Native modules ------------------------------------------------
  lazy val nativelib =
    MultiScalaProject("nativelib")
      .settings(
        publishSettings(Some(VersionScheme.BreakOnMajor)),
        docsSettings,
        libraryDependencies ++= Deps.NativeLib(scalaVersion.value)
      )
      .withNativeCompilerPlugin
      .mapBinaryVersions(_ => _.dependsOn(javalibintf % Provided))
      .mapBinaryVersions {
        // issue with Zinc does not detect changes
        case "2.13" => _.settings(recompileAllOrNothingSettings)
        case _      => identity
      }

  lazy val clib = MultiScalaProject("clib")
    .settings(publishSettings(Some(VersionScheme.BreakOnMajor)))
    .dependsOn(nativelib)
    .withNativeCompilerPlugin

  lazy val posixlib = MultiScalaProject("posixlib")
    .settings(publishSettings(Some(VersionScheme.BreakOnMajor)))
    .dependsOn(nativelib, clib)
    .withNativeCompilerPlugin

  lazy val windowslib =
    MultiScalaProject("windowslib")
      .settings(publishSettings(Some(VersionScheme.BreakOnMajor)))
      .dependsOn(nativelib, clib)
      .withNativeCompilerPlugin

// Language standard libraries ------------------------------------------------
  lazy val javalib = MultiScalaProject("javalib")
    .settings(
      publishSettings(Some(VersionScheme.BreakOnMajor)),
      commonJavalibSettings
    )
    .mapBinaryVersions {
      // Scaladoc in Scala 3 fails to generate documentation in javalib
      // https://github.com/lampepfl/dotty/issues/16709
      case "3" => _.settings(disabledDocsSettings)
      case _   => _.settings(docsSettings)
    }
    .dependsOn(posixlib, windowslib, clib)
    .withNativeCompilerPlugin

  lazy val javalibintf: Project = Project(
    id = "javalibintf",
    base = file("javalib-intf")
  ).settings(
    commonSettings,
    publishSettings(Some(VersionScheme.BreakOnMajor)),
    name := "javalib-intf",
    crossPaths := false,
    autoScalaLibrary := false,
    scalaVersion := scala3
  )

  lazy val javalibExtDummies =
    MultiScalaProject("javalibExtDummies", file("javalib-ext-dummies"))
      .settings(noPublishSettings, commonJavalibSettings, disabledDocsSettings)
      .dependsOn(nativelib)
      .withNativeCompilerPlugin

  lazy val auxlib = MultiScalaProject("auxlib")
    .settings(
      publishSettings(Some(VersionScheme.BreakOnMajor)),
      NIROnlySettings,
      recompileAllOrNothingSettings,
      disabledDocsSettings
    )
    .dependsOn(nativelib, clib)
    .withNativeCompilerPlugin

  lazy val scalalib: MultiScalaProject =
    MultiScalaProject("scalalib")
      .settings(
        publishSettings(Some(VersionScheme.BreakOnMajor)),
        disabledDocsSettings,
        scalacOptions --= ignoredScalaDeprecations(scalaVersion.value),
        NIROnlySettings,
        commonScalalibSettings(
          "scala-library",
          shouldAddDependencyForVersion = usesSelfContainedStdlib(_)
        )
      )
      .withNativeCompilerPlugin
      .mapBinaryVersions {
        case "2.12" | "2.13" =>
          _.settings(
            scalacOptions ++= Seq(
              "-deprecation:false",
              "-language:postfixOps",
              "-language:implicitConversions",
              "-language:existentials",
              "-language:higherKinds"
            ),
            /* Used to disable fatal warnings due to problems with compilation of `@nowarn` annotation */
            scalacOptions --= {
              scalaVersionsDependendent(scalaVersion.value)(
                List.empty[String]
              ) {
                case (2, 12)
                    if scalaVersion.value
                      .stripPrefix("2.12.")
                      .takeWhile(_.isDigit)
                      .toInt >= 13 =>
                  List("-Xfatal-warnings")
              }
            }
          )
        case "3" | "3-next" =>
          _.settings(
            Compile / sources := {
              if (usesSelfContainedStdlib(scalaVersion.value)) (Compile / sources).value
              else Seq.empty[File]
            },
            scalacOptions ++= Seq(
              "-language:implicitConversions",
              "-Wconf:any:silent"
            ),
            scalacOptions ++= {
              if (!usesSelfContainedStdlib(scalaVersion.value)) Nil
              else
                Seq(
                  "-Yno-stdlib-patches"
                )
            },
            Compile / packageBin / mappings := Def.taskDyn {
              val currentMappings = (Compile / packageBin / mappings).value
              Def.task {
                if (!usesSelfContainedStdlib(scalaVersion.value)) currentMappings
                else {
                  // Scala 3 does not emit specialized classes, it's solved by copying them from Scala 2.13 jar
                  // We need to do the same to ensure binary compatibility of Scala Native scalalib
                  val newMappings = (scalalib.v2_13 / Compile / packageBin / mappings).value

                  // Keep in sync with Scala 3 compiler logic
                  // https://github.com/scala/scala3/blob/eb1bb7350a99208d9ced9863a996850316d583f7/project/ScalaLibraryPlugin.scala#L116
                  val overridenFiles = Set(
                    "scala/Tuple1.nir",
                    "scala/Tuple2.nir",
                    "scala/collection/DoubleStepper.nir",
                    "scala/collection/IntStepper.nir",
                    "scala/collection/LongStepper.nir",
                    "scala/collection/immutable/DoubleVectorStepper.nir",
                    "scala/collection/immutable/IntVectorStepper.nir",
                    "scala/collection/immutable/LongVectorStepper.nir",
                    "scala/jdk/DoubleAccumulator.nir",
                    "scala/jdk/IntAccumulator.nir",
                    "scala/jdk/LongAccumulator.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoubleBinaryOperator.nir",
                    "scala/jdk/FunctionWrappers$FromJavaBooleanSupplier.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoubleConsumer.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoublePredicate.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoubleSupplier.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoubleToIntFunction.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoubleToLongFunction.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntBinaryOperator.nir",
                    "scala/jdk/FunctionWrappers$FromJavaDoubleUnaryOperator.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntPredicate.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntConsumer.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntSupplier.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntToDoubleFunction.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntToLongFunction.nir",
                    "scala/jdk/FunctionWrappers$FromJavaIntUnaryOperator.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongBinaryOperator.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongConsumer.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongPredicate.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongSupplier.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongToDoubleFunction.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongToIntFunction.nir",
                    "scala/jdk/FunctionWrappers$FromJavaLongUnaryOperator.nir",
                    "scala/collection/ArrayOps$ReverseIterator.nir",
                    "scala/runtime/NonLocalReturnControl.nir",
                    "scala/util/Sorting.nir",
                    "scala/util/Sorting$.nir" // Contains @specialized annotation
                  )

                  val mappingOverrides = newMappings.collect {
                    case mapping @ (_, path) if overridenFiles.contains(path) => path -> mapping
                  }.toMap
                  assert(
                    mappingOverrides.keySet == overridenFiles,
                    s"Some specialized files are missing: ${overridenFiles -- mappingOverrides.keySet}"
                  )
                  val currentPaths = currentMappings.map(_._2).toSet
                  val scala213ExtraFiles = newMappings.filter {
                    case (file, path) => !currentPaths.contains(path)
                  }
                  val maybeReplacedScala3Files = currentMappings.map {
                    case mapping @ (_, path) => mappingOverrides.getOrElse(path, mapping)
                  }
                  maybeReplacedScala3Files ++ scala213ExtraFiles
                }
              }
            }.value
          )
      }
      .dependsOn(auxlib)

  lazy val scala3lib: MultiScalaProject =
    MultiScalaProject("scala3lib")
      .enablePlugins(MyScalaNativePlugin)
      .settings(
        publishSettings(Some(VersionScheme.BreakOnMajor)),
        disabledDocsSettings,
        scalacOptions --= ignoredScalaDeprecations(scalaVersion.value),
        NIROnlySettings
      )
      .withNativeCompilerPlugin
      .mapBinaryVersions {
        case ("2.12" | "2.13") =>
          _.settings(
            noPublishSettings
          )

        case version @ ("3" | "3-next") =>
          _.settings(
            commonScalalibSettings("scala3-library_3"),
            scalacOptions ++= Seq(
              "-language:implicitConversions"
            ),
            Compile / sources := {
              if (usesSelfContainedStdlib(scalaVersion.value)) Seq.empty[File]
              else (Compile / sources).value
            },
            libraryDependencies += {
              val nativeVersion = (ThisBuild / Keys.version).value
              if (usesSelfContainedStdlib(scalaVersion.value)) {
                organization.value %%% "scalalib" % scalalibVersion(scalaVersion.value, nativeVersion)
              } else {
                (organization.value %%% "scalalib" % scalalibVersion(ScalaVersions.scala213, nativeVersion))
                  .excludeAll(ExclusionRule(organization.value))
                  .cross(CrossVersion.for3Use2_13)
              }
            },
            update := update.dependsOn {
              Def.taskDyn {
                if (usesSelfContainedStdlib(scalaVersion.value))
                  scalalib.forBinaryVersion(version) / Compile / publishLocal
                else
                  scalalib.v2_13 / Compile / publishLocal
              }
            }.value
          )
      }
      .dependsOn(auxlib)

  // Tests ------------------------------------------------
  lazy val tests = MultiScalaProject("tests", file("unit-tests") / "native")
    .settings(
      noPublishSettings,
      testsCommonSettings,
      sharedTestSource(withDenylist = false),
      javaVersionSharedTestSources,
      nativeConfig ~= { c =>
        c.withLinkStubs(true)
          .withEmbedResources(true)
          // Tests using threads are ignored in runtime, skip checks and allow to link
          .withCheckFeatures(false)
          .withServiceProviders(
            Map(
              "org.scalanative.testsuite.javalib.util.MyService" -> Seq(
                "org.scalanative.testsuite.javalib.util.MyServiceImpl1",
                "org.scalanative.testsuite.javalib.util.MyServiceImpl2"
              )
            )
          )
      },
      Test / unmanagedSourceDirectories ++= {
        val base = (Test / sourceDirectory).value
        scalaVersionsDependendent(scalaVersion.value)(Seq.empty[File]) {
          case (2, n) if n >= 12 =>
            Seq(
              base / "scala-2",
              base / "scala-2.12+"
            )
        }
      }
    )
    .withBuildInfo
    .withNativeCompilerPlugin
    .withJUnitPlugin
    .dependsOn(
      testInterface,
      junitRuntime
    )

  lazy val testsJVM =
    MultiScalaProject("testsJVM", file("unit-tests/jvm"))
      .enablePlugins(BuildInfoPlugin)
      .settings(
        buildInfoJVMSettings,
        noPublishSettings,
        testsCommonSettings,
        sharedTestSource(withDenylist = true),
        javaVersionSharedTestSources,
        Test / fork := true,
        Test / parallelExecution := false,
        libraryDependencies ++= Deps.JUnitJvm
      )
      .dependsOn(junitAsyncJVM % "test")

  lazy val testsExt =
    MultiScalaProject("testsExt", file("unit-tests-ext/native"))
      .settings(noPublishSettings)
      .settings(
        // Setting only used to ensure that compiler does not crash when reporting deprecated options
        scalacOptions += "-P:scalanative:mapSourceURI:path->unused",
        scalacOptions -= "-Xfatal-warnings",
        nativeConfig ~= {
          _.withLinkStubs(true)
        },
        testsExtCommonSettings,
        sharedTestSource(withDenylist = false)
      )
      .withNativeCompilerPlugin
      .withJUnitPlugin
      .dependsOn(
        testInterface % "test",
        tests,
        junitRuntime,
        javalibExtDummies
      )

  lazy val testsExtJVM =
    MultiScalaProject("testsExtJVM", file("unit-tests-ext/jvm"))
      .settings(
        noPublishSettings,
        testsExtCommonSettings,
        sharedTestSource(withDenylist = true),
        libraryDependencies ++= Deps.JUnitJvm
      )
      .dependsOn(junitAsyncJVM % "test")

  lazy val sandbox =
    MultiScalaProject("sandbox", file("sandbox"))
      .settings(
        noJavaReleaseSettings(Compile),
        noJavaReleaseSettings(Test)
      )
      .withJUnitPlugin
      .withNativeCompilerPlugin
      .withScalaStandardLibrary
      .dependsOn(javalib, testInterface % "test", junitRuntime % "test")

// Testing infrastructure ------------------------------------------------
  lazy val testingCompilerInterface =
    project
      .in(file("testing-compiler-interface"))
      .settings(
        noPublishSettings,
        crossPaths := false,
        crossVersion := CrossVersion.disabled,
        autoScalaLibrary := false
      )

  lazy val testingCompiler =
    MultiScalaProject("testingCompiler", file("testing-compiler"))
      .settings(
        noPublishSettings,
        libraryDependencies ++= Deps.compilerPluginDependencies(
          scalaVersion.value
        ),
        Compile / unmanagedSourceDirectories ++= {
          val base = baseDirectory.value.getParentFile()
          val oldCompat: File = base / "src/main/compat-old"
          val newCompat: File = base / "src/main/compat-new"
          CrossVersion
            .partialVersion(scalaVersion.value)
            .collect {
              case (2, 12) =>
                val revision =
                  scalaVersion.value
                    .stripPrefix("2.12.")
                    .takeWhile(_.isDigit)
                    .toInt
                if (revision < 13) oldCompat
                else newCompat
              case (2, 13) => newCompat
            }
            .toSeq
        },
        exportJars := true
      )
      .mapBinaryVersions(_ => _.dependsOn(testingCompilerInterface))

  lazy val testInterface =
    MultiScalaProject("testInterface", file("test-interface"))
      .settings(
        publishSettings(Some(VersionScheme.BreakOnPatch)),
        testInterfaceCommonSourcesSettings
      )
      .withNativeCompilerPlugin
      .withJUnitPlugin
      .withScalaStandardLibrary
      .dependsOn(
        javalib,
        testInterfaceSbtDefs,
        junitRuntime % "test",
        junitAsyncNative % "test"
      )

  lazy val testInterfaceSbtDefs =
    MultiScalaProject("testInterfaceSbtDefs", file("test-interface-sbt-defs"))
      .settings(publishSettings(Some(VersionScheme.BreakOnMajor)))
      .settings(docsSettings)
      .withNativeCompilerPlugin
      .withScalaStandardLibrary

  lazy val testRunner =
    MultiScalaProject("testRunner", file("test-runner"))
      .settings(
        publishSettings(None),
        testInterfaceCommonSourcesSettings,
        libraryDependencies ++= Deps.TestRunner
      )
      .dependsOn(toolsJVM, junitAsyncJVM % "test")

// JUnit modules ------------------------------------------------
  lazy val junitRuntime =
    MultiScalaProject("junitRuntime", file("junit-runtime"))
      .settings(publishSettings(Some(VersionScheme.BreakOnMajor)))
      .withNativeCompilerPlugin
      .dependsOn(testInterfaceSbtDefs)

  lazy val junitTestOutputsNative =
    MultiScalaProject(
      "junitTestOutputsNative",
      file("junit-test/output-native")
    )
      .settings(commonJUnitTestOutputsSettings)
      .withNativeCompilerPlugin
      .withJUnitPlugin
      .dependsOn(
        junitRuntime % "test",
        junitAsyncNative % "test",
        testInterface % "test"
      )

  lazy val junitTestOutputsJVM =
    MultiScalaProject("junitTestOutputsJVM", file("junit-test/output-jvm"))
      .settings(
        commonJUnitTestOutputsSettings,
        libraryDependencies ++= Deps.JUnitJvm
      )
      .dependsOn(junitAsyncJVM % "test")

  lazy val junitAsyncNative =
    MultiScalaProject("junitAsyncNative", file("junit-async/native"))
      .settings(
        Compile / publishArtifact := false
      )
      .withNativeCompilerPlugin
      .withScalaStandardLibrary
      .dependsOn(javalib)

  lazy val junitAsyncJVM =
    MultiScalaProject("junitAsyncJVM", file("junit-async/jvm"))
      .settings(
        publishArtifact := false
      )

  lazy val scalaPartest =
    MultiScalaProject("scalaPartest", file("scala-partest"))
      .settings(
        scalacOptions --= Seq(
          "-Xfatal-warnings"
        ), {
          if (ideScalaVersion.startsWith("2.")) Nil
          else noIDEExportSettings
        },
        noPublishSettings,
        shouldPartestSetting,
        resolvers += Resolver.typesafeIvyRepo("releases"),
        fetchScalaSource / artifactPath :=
          baseDirectory.value.getParentFile / "fetchedSources" / scalaVersion.value,
        fetchScalaSource := {
          import org.eclipse.jgit.api._

          val s = streams.value
          val ver = scalaVersion.value
          val trgDir = (fetchScalaSource / artifactPath).value

          val (repoURL, tag) = CrossVersion
            .partialVersion(ver)
            .collect {
              case (2, _) => "https://github.com/scala/scala.git" -> s"v$ver"
              case (3, _) => "https://github.com/lampepfl/dotty.git" -> ver
            }
            .getOrElse(throw new RuntimeException("Invalid Scala version"))

          if (!trgDir.exists) {
            s.log.info(s"Fetching Scala source version $ver")

            // Make parent dirs and stuff
            sbt.IO.createDirectory(trgDir)

            // Clone scala source code
            new CloneCommand()
              .setDirectory(trgDir)
              .setURI(repoURL)
              .call()
          }

          // Checkout proper ref. We do this anyway so we fail if
          // something is wrong
          val git = Git.open(trgDir)
          s.log.info(s"Checking out Scala source version $ver")
          git.checkout().setName(tag).call()

          trgDir
        },
        Compile / unmanagedSourceDirectories ++= {
          if (!shouldPartest.value) Nil
          else Seq(sourceDirectory.value / "main" / "new-partest")
        },
        libraryDependencies ++= {
          if (!shouldPartest.value) Nil
          else Deps.ScalaPartest(scalaVersion.value)
        },
        Compile / sources := {
          if (!shouldPartest.value) Nil
          else (Compile / sources).value
        }
      )
      .dependsOn(nscPlugin, toolsJVM)

  lazy val scalaPartestTests =
    MultiScalaProject("scalaPartestTests", file("scala-partest-tests"))
      .settings(
        noPublishSettings,
        shouldPartestSetting,
        noIDEExportSettings,
        Test / fork := true,
        Test / javaOptions += "-Xmx1G",
        // Override the dependency of partest - see Scala.js issue #1889
        dependencyOverrides += Deps.ScalaLibrary(scalaVersion.value) % "test",
        testFrameworks ++= {
          if (shouldPartest.value)
            Seq(new TestFramework("scala.tools.partest.scalanative.Framework"))
          else Seq.empty
        }
      )
      .zippedSettings(
        Seq("scalaPartest", "auxlib", "scalalib", "scalaPartestRuntime"),
        versionsProjectReplacement = scalalibProjectSelect
      ) {
        case Seq(scalaPartest, auxlib, scalalib, scalaPartestRuntime) =>
          Def.settings(
            Test / definedTests ++= Def
              .taskDyn[Seq[sbt.TestDefinition]] {
                if (!shouldPartest.value) Def.task(Seq.empty)
                else
                  Def.task {
                    val _ = (scalaPartest / fetchScalaSource).value
                    Seq(
                      new sbt.TestDefinition(
                        s"partest-${scalaVersion.value}",
                        // marker fingerprint since there are no test classes
                        // to be discovered by sbt:
                        new sbt.testing.AnnotatedFingerprint {
                          def isModule = true
                          def annotationName = "partest"
                        },
                        true,
                        Array()
                      )
                    )
                  }
              }
              .value,
            testOptions += {
              val nativeCp = Seq(
                (auxlib / Compile / packageBin).value,
                (scalalib / Compile / packageBin).value,
                (scalaPartestRuntime / Compile / packageBin).value
              ).map(_.absolutePath).mkString(pathSeparator)

              Tests.Argument(s"--nativeClasspath=$nativeCp")
            }
          )
      }
      .dependsOn(scalaPartest % "test", javalib)

  lazy val scalaPartestRuntime =
    MultiScalaProject("scalaPartestRuntime", file("scala-partest-runtime"))
      .settings(noPublishSettings)
      .zippedSettings(Seq("scalaPartest", "junitRuntime")) {
        case Seq(scalaPartest, junitRuntime) =>
          Def.settings(
            Compile / unmanagedSources ++= {
              if (!(scalaPartest / shouldPartest).value) Nil
              else {
                val upstreamDir = (scalaPartest / fetchScalaSource).value
                CrossVersion
                  .partialVersion(scalaVersion.value)
                  .collect {
                    case (2, 13) =>
                      val testkit =
                        upstreamDir / "src/testkit/scala/tools/testkit"
                      val partest =
                        upstreamDir / "src/partest/scala/tools/partest"
                      Seq(
                        testkit / "AssertUtil.scala",
                        partest / "Util.scala"
                      )
                  }
                  .getOrElse(Seq.empty[File])
              }
            },
            Compile / unmanagedSourceDirectories ++= {
              if (!(scalaPartest / shouldPartest).value) Nil
              else
                Seq(
                  (junitRuntime / Compile / scalaSource).value / "org"
                )
            }
          )
      }
      .withNativeCompilerPlugin
      .dependsOn(scalalib)

  lazy val scalaPartestJunitTests = MultiScalaProject(
    "scalaPartestJunitTests",
    file("scala-partest-junit-tests")
  )
    .settings(
      noPublishSettings,
      noIDEExportSettings,
      scalacOptions ++= Seq(
        "-language:higherKinds"
      ),
      scalacOptions ++= {
        // Suppress deprecation warnings for Scala partest sources
        Seq("-Wconf:cat=deprecation:s")
      },
      scalacOptions --= Seq(
        "-Xfatal-warnings"
      ),
      // No control over sources
      nativeConfig ~= { _.withCheckFeatures(false) },
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s"),
      shouldPartest := {
        (Test / resourceDirectory).value / scalaVersion.value
      }.exists()
    )
    .zippedSettings(scalaPartest) { scalaPartest =>
      Def.settings(
        Compile / unmanagedSources ++= {
          if (!shouldPartest.value) Nil
          else {
            val upstreamDir = (scalaPartest / fetchScalaSource).value
            CrossVersion.partialVersion(scalaVersion.value) match {
              case Some((2, 12)) => Seq.empty[File]
              case _             =>
                Seq(
                  upstreamDir / "src/testkit/scala/tools/testkit/AssertUtil.scala"
                )
            }
          }
        },
        Test / unmanagedSources ++= {
          if (!shouldPartest.value) Nil
          else {
            val denylist: Set[String] = {
              val versionTestsDir =
                (Test / resourceDirectory).value / scalaVersion.value
              val base =
                denylistedFromFile(versionTestsDir / "DenylistedTests.txt")
              val requiringMultithreading =
                if (nativeConfig.value.multithreading.getOrElse(true))
                  Set.empty[String]
                else
                  denylistedFromFile(
                    versionTestsDir / "DenylistedTests-require-threads.txt",
                    ignoreMissing = true
                  )
              base ++ requiringMultithreading
            }

            val jUnitTestsPath =
              (scalaPartest / fetchScalaSource).value / "test" / "junit"
            val scalaScalaJUnitSources = allScalaFromDir(jUnitTestsPath)
            checkDenylistCoherency(denylist, scalaScalaJUnitSources)
            scalaScalaJUnitSources.collect {
              case (rel, file) if !denylist.contains(rel) => file
            }
          }
        }
      )
    }
    .withNativeCompilerPlugin
    .withJUnitPlugin
    .dependsOn(
      junitRuntime,
      testInterface % "test"
    )

  implicit class MultiProjectOps(val project: MultiScalaProject) extends AnyVal {
    def withScalaStandardLibrary: MultiScalaProject = {
      project.mapBinaryVersions {
        case v @ ("2.12" | "2.13") => _.dependsOn(scalalib.forBinaryVersion(v))
        case v @ ("3" | "3-next")  => _.dependsOn(scala3lib.forBinaryVersion(v))
      }
    }

    /** Uses the Scala Native compiler plugin. */
    def withNativeCompilerPlugin: MultiScalaProject = {
      if (isGeneratingForIDE) project
      else project.dependsOn(nscPlugin % "plugin")
    }.enablePlugins(MyScalaNativePlugin)

    def withJUnitPlugin: MultiScalaProject = {
      if (isGeneratingForIDE) project
      else
        project.mapBinaryVersions { version =>
          _.settings(
            Test / scalacOptions += Def.taskDyn {
              val pluginProject = junitPlugin.forBinaryVersion(version)
              (pluginProject / Compile / packageBin).map { jar =>
                s"-Xplugin:$jar"
              }
            }.value
          )
        }
    }

    /** Depends on the sources of another project. */
    def dependsOnSource(dependency: MultiScalaProject): MultiScalaProject = {
      if (isGeneratingForIDE && !project.dependsOnSourceInIDE)
        project.dependsOn(dependency)
      else
        project.zippedSettings(dependency) { dependency =>
          Compile / unmanagedSourceDirectories ++=
            (dependency / Compile / unmanagedSourceDirectories).value
        }
    }

    def withBuildInfo: MultiScalaProject =
      project
        .settings(
          buildInfoJVMSettings,
          buildInfoKeys += "nativeScalaVersion" -> scalaVersion.value
        )
        .enablePlugins(BuildInfoPlugin)

    def withCommonTools: MultiScalaProject =
      project
        .settings(
          toolSettings,
          withSharedCrossPlatformSources,
          // Running tests in parallel results in `FileSystemAlreadyExistsException`
          Test / parallelExecution := false
        )
        .withBuildInfo

  }
}
