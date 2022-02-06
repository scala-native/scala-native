package build

import sbt._
import Keys._

import scala.language.implicitConversions

import java.io.File.pathSeparator
import sbtbuildinfo.BuildInfoPlugin

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import ScriptedPlugin.autoImport._

object Build {
  import ScalaVersions._
  import Settings._
  import Deps._

  lazy val root: Project =
    Project(id = "scala-native", base = file("."))
      .settings(
        name := "Scala Native",
        scalaVersion := ScalaVersions.scala212,
        crossScalaVersions := ScalaVersions.libCrossScalaVersions,
        commonSettings,
        noPublishSettings,
        disabledTestsSettings, {
// format: off
          val allProjects: Seq[Project] = Seq(
              sbtScalaNative
            ) ++ Seq(
                nscPlugin, junitPlugin,
                nativelib, clib, posixlib, windowslib,
                auxlib, javalib, javalibExtDummies, scalalib,
                testInterface, testInterfaceSbtDefs,
                testingCompiler, testingCompilerInterface,
                junitRuntime, junitAsyncNative, junitAsyncJVM,
                junitTestOutputsJVM, junitTestOutputsNative,
                tests, testsJVM, testsExt, testsExtJVM, sandbox,
                scalaPartest, scalaPartestRuntime,
                scalaPartestTests, scalaPartestJunitTests
            ).flatMap(_.componentProjects)
// format: on
          val keys = Seq[TaskKey[_]](clean)
          for (key <- keys) yield {
            /* The match is only used to capture the type parameter `a` of
             * each individual TaskKey.
             */
            key match {
              case key: TaskKey[a] =>
                key := key.dependsOn(allProjects.map(_ / key): _*).value
            }
          }
        }
      )

  // Compiler plugins
  lazy val nscPlugin = MultiScalaProject("nscplugin", file("nscplugin"))
    .settings(
      compilerPluginSettings,
      scalacOptions ++= scalaVersionsDependendent(scalaVersion.value)(
        Seq.empty[String]
      ) {
        case (2, _) => Seq("-Xno-patmat-analysis")
      }
    )
    .dependsOnSource(nir)
    .dependsOnSource(util)

  lazy val junitPlugin = MultiScalaProject("junitPlugin", file("junit-plugin"))
    .settings(compilerPluginSettings)

  // NIR compiler
  lazy val util = MultiScalaProject("util")
    .settings(toolSettings, mavenPublishSettings)

  lazy val nir = MultiScalaProject("nir")
    .settings(toolSettings, mavenPublishSettings)
    .dependsOn(util)

  lazy val tools = MultiScalaProject("tools")
    .enablePlugins(BuildInfoPlugin)
    .settings(toolSettings, mavenPublishSettings, buildInfoSettings)
    .settings(
      libraryDependencies ++= Deps.Tools(scalaVersion.value),
      Test / fork := true,
      scalacOptions := {
        val prev = scalacOptions.value
        CrossVersion
          .partialVersion(scalaVersion.value)
          .fold(prev) {
            case (2, 11 | 12) => prev
            case (2, 13)      =>
              // 2.13 and 2.11 tools are only used in partest.
              // It looks like it's impossible to provide alternative sources - it fails to compile plugin sources,
              // before attaching them to other build projects. We disable unsolvable fatal-warnings with filters below
              prev ++ Seq(
                // In 2.13 lineStream_! was replaced with lazyList_!.
                "-Wconf:cat=deprecation&msg=lineStream_!:s",
                // OpenHashMap is used with value class parameter type, we cannot replace it with AnyRefMap or LongMap
                // Should not be replaced with HashMap due to performance reasons.
                "-Wconf:cat=deprecation&msg=OpenHashMap:s"
              )
            case _ =>
              prev.diff(Seq("-Xfatal-warnings"))
          }
      },
      // Running tests in parallel results in `FileSystemAlreadyExistsException`
      Test / parallelExecution := false
    )
    .zippedSettings(Seq("nscplugin", "testingCompiler", "scalalib")) {
      case Seq(nscPlugin, testingCompiler, scalalib) =>
        Test / javaOptions ++= {
          val nscCompilerJar =
            (nscPlugin / Compile / Keys.`package`).value.getAbsolutePath()
          val testingCompilerCp =
            (testingCompiler / Compile / fullClasspath).value.files
              .map(_.getAbsolutePath)
              .mkString(pathSeparator)
          val scalalibCp = (scalalib / Compile / fullClasspath).value.files
            .map(_.getAbsolutePath)
            .mkString(pathSeparator)
          Seq(
            "-Dscalanative.nscplugin.jar=" + nscCompilerJar,
            "-Dscalanative.testingcompiler.cp=" + testingCompilerCp,
            "-Dscalanative.nativeruntime.cp=" + scalalibCp
          )
        },
    }
    .dependsOn(nir, util, testingCompilerInterface % "test")

  lazy val sbtScalaNative: Project =
    project
      .in(file("sbt-scala-native"))
      .enablePlugins(ScriptedPlugin)
      .settings(
        sbtPluginSettings,
        disabledDocsSettings,
        addSbtPlugin(Deps.SbtPlatformDeps),
        sbtTestDirectory := (ThisBuild / baseDirectory).value / "scripted-tests",
        // publish the other projects before running scripted tests.
        scriptedDependencies := {
          import java.nio.file.{Files, StandardCopyOption}
          // Synchronize SocketHelpers used in java-net-socket test
          // Each scripted test creates it's own environment in tmp directory
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
                CrossVersion.binaryScalaVersion(scalaVersion)
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
                    // Testing infrastructure
                    testInterfaceSbtDefs.forBinaryVersion(ver) / publishLocal,
                    testInterface.forBinaryVersion(ver) / publishLocal,
                    junitRuntime.forBinaryVersion(ver) / publishLocal,
                    // JVM libraries
                    util.forBinaryVersion(ver) / publishLocal,
                    nir.forBinaryVersion(ver) / publishLocal,
                    tools.forBinaryVersion(ver) / publishLocal,
                    testRunner.forBinaryVersion(ver) / publishLocal
                  )
              }

              publishLocalVersion(ver)
                .dependsOn(
                  // Scala 3 needs 2.13 deps for it's cross version compat tests
                  if (ver == "3") publishLocalVersion("2.13")
                  else Def.task(())
                )
            })
            .value
        }
      )
      .dependsOn(tools.v2_12, testRunner.v2_12)

// Native moduels ------------------------------------------------
  lazy val nativelib =
    MultiScalaProject("nativelib")
      .enablePlugins(MyScalaNativePlugin)
      .settings(
        mavenPublishSettings,
        docsSettings,
        libraryDependencies ++= Deps.NativeLib(scalaVersion.value)
      )
      .withNativeCompilerPlugin

  lazy val clib = MultiScalaProject("clib")
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .dependsOn(nativelib)
    .withNativeCompilerPlugin

  lazy val posixlib = MultiScalaProject("posixlib")
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings)
    .dependsOn(nativelib)
    .withNativeCompilerPlugin

  lazy val windowslib =
    MultiScalaProject("windowslib")
      .enablePlugins(MyScalaNativePlugin)
      .settings(mavenPublishSettings)
      .dependsOn(nativelib, clib)
      .withNativeCompilerPlugin

// Language standard libraries ------------------------------------------------
  lazy val javalib = MultiScalaProject("javalib")
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings, commonJavalibSettings)
    .dependsOn(posixlib, windowslib, clib)
    .withNativeCompilerPlugin

  lazy val javalibExtDummies =
    MultiScalaProject("javalibExtDummies", file("javalib-ext-dummies"))
      .enablePlugins(MyScalaNativePlugin)
      .settings(noPublishSettings, commonJavalibSettings)
      .dependsOn(nativelib)
      .withNativeCompilerPlugin

  lazy val auxlib = MultiScalaProject("auxlib")
    .enablePlugins(MyScalaNativePlugin)
    .settings(mavenPublishSettings, commonJavalibSettings)
    .dependsOn(nativelib)
    .withNativeCompilerPlugin

  lazy val scalalib: MultiScalaProject =
    MultiScalaProject("scalalib")
      .enablePlugins(MyScalaNativePlugin)
      .settings(mavenPublishSettings, disabledDocsSettings)
      .withNativeCompilerPlugin
      .mapBinaryVersions {
        case "2.11" | "2.12" | "2.13" =>
          _.settings(
            commonScalalibSettings("scala-library"),
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
        case "3" =>
          _.settings(
            name := "scala3lib",
            commonScalalibSettings("scala3-library_3"),
            scalacOptions ++= Seq(
              "-language:implicitConversions"
            ),
            libraryDependencies += ("org.scala-native" %%% "scalalib" % nativeVersion)
              .excludeAll(ExclusionRule("org.scala-native"))
              .cross(CrossVersion.for3Use2_13),
            update := {
              update.dependsOn {
                Def.taskDyn(scalalib.v2_13 / Compile / publishLocal)
              }.value
            }
          )
      }
      .dependsOn(auxlib, javalib)

  // Tests ------------------------------------------------
  lazy val tests = MultiScalaProject("tests", file("unit-tests") / "native")
    .enablePlugins(MyScalaNativePlugin, BuildInfoPlugin)
    .settings(
      buildInfoSettings,
      noPublishSettings,
      testsCommonSettings,
      sharedTestSource(withBlacklist = false),
      javaVersionBasedTestSources,
      nativeConfig ~= {
        _.withLinkStubs(true)
          .withEmbedResources(true)
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
    .withNativeCompilerPlugin
    .withJUnitPlugin
    .dependsOn(
      scalalib,
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
        sharedTestSource(withBlacklist = true),
        javaVersionBasedTestSources,
        Test / fork := true,
        Test / parallelExecution := false,
        libraryDependencies ++= Deps.JUnitJvm
      )
      .dependsOn(junitAsyncJVM % "test")

  lazy val testsExt =
    MultiScalaProject("testsExt", file("unit-tests-ext/native"))
      .enablePlugins(MyScalaNativePlugin)
      .settings(noPublishSettings)
      .settings(
        nativeConfig ~= {
          _.withLinkStubs(true)
        },
        testsExtCommonSettings,
        sharedTestSource(withBlacklist = false)
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
        sharedTestSource(withBlacklist = true),
        libraryDependencies ++= Deps.JUnitJvm
      )
      .dependsOn(junitAsyncJVM % "test")

  lazy val sandbox =
    MultiScalaProject("sandbox", file("sandbox"))
      .enablePlugins(MyScalaNativePlugin)
      .withNativeCompilerPlugin
      .withJUnitPlugin
      .dependsOn(scalalib, testInterface % "test")

// Testing infrastructure ------------------------------------------------
  lazy val testingCompilerInterface =
    MultiScalaProject(
      "testingCompilerInterface",
      file("testing-compiler-interface")
    ).settings(
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
              case (2, 11) => oldCompat
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
      .dependsOn(testingCompilerInterface)

  lazy val testInterface =
    MultiScalaProject("testInterface", file("test-interface"))
      .enablePlugins(MyScalaNativePlugin)
      .settings(mavenPublishSettings, testInterfaceCommonSourcesSettings)
      .withNativeCompilerPlugin
      .withJUnitPlugin
      .dependsOn(
        scalalib,
        testInterfaceSbtDefs,
        junitRuntime,
        junitAsyncNative % "test"
      )

  lazy val testInterfaceSbtDefs =
    MultiScalaProject("testInterfaceSbtDefs", file("test-interface-sbt-defs"))
      .enablePlugins(MyScalaNativePlugin)
      .settings(mavenPublishSettings)
      .settings(docsSettings)
      .withNativeCompilerPlugin
      .dependsOn(scalalib)

  lazy val testRunner =
    MultiScalaProject("testRunner", file("test-runner"))
      .settings(
        mavenPublishSettings,
        testInterfaceCommonSourcesSettings,
        libraryDependencies ++= Deps.TestRunner
      )
      .dependsOn(tools, junitAsyncJVM % "test")

// JUnit modules ------------------------------------------------
  lazy val junitRuntime =
    MultiScalaProject("junitRuntime", file("junit-runtime"))
      .enablePlugins(MyScalaNativePlugin)
      .settings(mavenPublishSettings)
      .withNativeCompilerPlugin
      .dependsOn(testInterfaceSbtDefs)

  lazy val junitTestOutputsNative =
    MultiScalaProject(
      "junitTestOutputsNative",
      file("junit-test/output-native")
    )
      .enablePlugins(MyScalaNativePlugin)
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
      .enablePlugins(MyScalaNativePlugin)
      .settings(
        Compile / publishArtifact := false
      )
      .withNativeCompilerPlugin
      .dependsOn(scalalib)

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
        ),
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
            IO.createDirectory(trgDir)

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
          else {
            Seq(CrossVersion.partialVersion(scalaVersion.value) match {
              case Some((2, 11)) =>
                sourceDirectory.value / "main" / "legacy-partest"
              case _ => sourceDirectory.value / "main" / "new-partest"
            })
          }
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
      .dependsOn(nscPlugin, tools)

  lazy val scalaPartestTests =
    MultiScalaProject("scalaPartestTests", file("scala-partest-tests"))
      .settings(
        noPublishSettings,
        shouldPartestSetting,
        Test / fork := true,
        Test / javaOptions += "-Xmx1G",
        // Override the dependency of partest - see Scala.js issue #1889
        dependencyOverrides += Deps.ScalaLibrary(scalaVersion.value) % "test",
        testFrameworks ++= {
          if (shouldPartest.value)
            Seq(new TestFramework("scala.tools.partest.scalanative.Framework"))
          else Seq()
        }
      )
      .zippedSettings(
        Seq("scalaPartest", "auxlib", "scalalib", "scalaPartestRuntime")
      ) {
        case Seq(scalaPartest, auxlib, scalalib, scalaPartestRuntime) =>
          Def.settings(
            Test / definedTests ++= Def
              .taskDyn[Seq[sbt.TestDefinition]] {
                if (!shouldPartest.value) Def.task(Seq())
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
      .enablePlugins(MyScalaNativePlugin)
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
  ).enablePlugins(MyScalaNativePlugin)
    .settings(
      noPublishSettings,
      scalacOptions ++= Seq(
        "-language:higherKinds"
      ),
      scalacOptions ++= {
        // Suppress deprecation warnings for Scala partest sources
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 11)) => Nil
          case _ =>
            Seq("-Wconf:cat=deprecation:s")
        }
      },
      scalacOptions --= Seq(
        "-Xfatal-warnings"
      ),
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v"),
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
              case Some((2, 11 | 12)) => Seq.empty[File]
              case _ =>
                Seq(
                  upstreamDir / "src/testkit/scala/tools/testkit/AssertUtil.scala"
                )
            }
          }
        },
        Test / unmanagedSources ++= {
          if (!shouldPartest.value) Nil
          else {
            val blacklist: Set[String] =
              blacklistedFromFile(
                (Test / resourceDirectory).value / scalaVersion.value / "BlacklistedTests.txt"
              )

            val jUnitTestsPath =
              (scalaPartest / fetchScalaSource).value / "test" / "junit"
            val scalaScalaJUnitSources = allScalaFromDir(jUnitTestsPath)
            checkBlacklistCoherency(blacklist, scalaScalaJUnitSources)
            scalaScalaJUnitSources.collect {
              case (rel, file) if !blacklist.contains(rel) => file
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

  implicit class MultiProjectOps(val project: MultiScalaProject)
      extends AnyVal {

    /** Uses the Scala Native compiler plugin. */
    def withNativeCompilerPlugin: MultiScalaProject = {
      project.dependsOn(nscPlugin % "plugin")
    }

    def withJUnitPlugin: MultiScalaProject = {
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
      project.zippedSettings(dependency) { dependency =>
        Compile / unmanagedSourceDirectories ++=
          (dependency / Compile / unmanagedSourceDirectories).value
      }
    }
  }

}
