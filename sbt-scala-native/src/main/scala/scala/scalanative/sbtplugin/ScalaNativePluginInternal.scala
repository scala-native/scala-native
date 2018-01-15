package scala.scalanative
package sbtplugin

import java.lang.System.{lineSeparator => nl}
import java.io.ByteArrayInputStream
import java.nio.file.Files

import scala.util.Try

import sbt._
import sbt.Keys._
import sbt.complete.DefaultParsers._
import sbt.testing.Framework
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

import scalanative.nir
import scalanative.tools
import scalanative.io.VirtualDirectory
import scalanative.util.{Scope => ResourceScope}
import scalanative.sbtplugin.Utilities._
import scalanative.sbtplugin.TestUtilities._
import scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import scalanative.sbtplugin.SBTCompat.{Process, _}
import scalanative.sbtplugin.testinterface.ScalaNativeFramework

object ScalaNativePluginInternal {

  val nativeWarnOldJVM =
    taskKey[Unit]("Warn if JVM 7 or older is used.")

  val nativeTarget =
    taskKey[String]("Target triple.")

  val nativeLinkerReporter =
    settingKey[tools.LinkerReporter](
      "A reporter that gets notified whenever a linking event happens.")

  val nativeOptimizerReporter =
    settingKey[tools.OptimizerReporter](
      "A reporter that gets notified whenever an optimizer event happens.")

  val nativeOptimizerDriver =
    taskKey[tools.OptimizerDriver]("Pass manager for the optimizer.")

  val nativeWorkdir =
    taskKey[File]("Working directory for intermediate build files.")

  val nativeConfig =
    taskKey[tools.Config]("Aggregate config object that's used for tools.")

  val nativeLinkNIR =
    taskKey[tools.LinkerResult]("Link NIR using Scala Native linker.")

  val nativeOptimizeNIR =
    taskKey[Seq[nir.Defn]]("Optimize NIR produced after linking.")

  val nativeGenerateLL =
    taskKey[Seq[File]]("Generate LLVM IR based on the optimized NIR.")

  val nativeCompileLL =
    taskKey[Seq[File]]("Compile LLVM IR to native object files.")

  val nativeUnpackLib =
    taskKey[File]("Unpack native lib.")

  val nativeCompileLib =
    taskKey[File]("Precompile C/C++ code in native lib.")

  val nativeLinkLL =
    taskKey[File]("Link native object files into the final binary")

  lazy val scalaNativeDependencySettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "nativelib"      % nativeVersion,
      "org.scala-native" %%% "javalib"        % nativeVersion,
      "org.scala-native" %%% "auxlib"         % nativeVersion,
      "org.scala-native" %%% "scalalib"       % nativeVersion,
      "org.scala-native" %%% "test-interface" % nativeVersion % Test
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full)
  )

  lazy val scalaNativeBaseSettings: Seq[Setting[_]] = Seq(
    crossVersion := ScalaNativeCrossVersion.binary,
    platformDepsCrossVersion := ScalaNativeCrossVersion.binary,
    nativeClang := {
      val clang = llvm.discover("clang", llvm.clangVersions)
      llvm.checkThatClangIsRecentEnough(clang)
      clang.toFile
    },
    nativeClang in NativeTest := (nativeClang in Test).value,
    nativeClangPP := {
      val clang = llvm.discover("clang++", llvm.clangVersions)
      llvm.checkThatClangIsRecentEnough(clang)
      clang.toFile
    },
    nativeClangPP in NativeTest := (nativeClangPP in Test).value,
    nativeCompileOptions := llvm.defaultCompileOptions,
    nativeCompileOptions in NativeTest := (nativeCompileOptions in Test).value,
    nativeLinkingOptions := llvm.defaultLinkingOptions,
    nativeLinkingOptions in NativeTest := (nativeLinkingOptions in Test).value,
    nativeMode := Option(System.getenv.get("SCALANATIVE_MODE"))
      .getOrElse(tools.Mode.default.name),
    nativeMode in NativeTest := (nativeMode in Test).value,
    nativeLinkStubs := false,
    nativeLinkStubs in NativeTest := (nativeLinkStubs in Test).value,
    nativeLinkerReporter := tools.LinkerReporter.empty,
    nativeLinkerReporter in NativeTest := (nativeLinkerReporter in Test).value,
    nativeOptimizerReporter := tools.OptimizerReporter.empty,
    nativeOptimizerReporter in NativeTest := (nativeOptimizerReporter in Test).value,
    nativeGC := Option(System.getenv.get("SCALANATIVE_GC"))
      .getOrElse(tools.GarbageCollector.default.name),
    nativeGC in NativeTest := (nativeGC in Test).value
  )

  lazy val scalaNativeGlobalSettings: Seq[Setting[_]] = Seq(
    nativeWarnOldJVM := {
      val logger = streams.value.log
      Try(Class.forName("java.util.function.Function")).toOption match {
        case None =>
          logger.warn("Scala Native is only supported on Java 8 or newer.")
        case Some(_) =>
          ()
      }
    }
  )

  lazy val scalaNativeConfigSettings: Seq[Setting[_]] = Seq(
    nativeTarget := {
      val logger = streams.value.log
      val cwd    = nativeWorkdir.value.toPath
      val clang  = nativeClang.value.toPath
      llvm.detectTarget(clang, cwd, logger.toLogger)
    },
    artifactPath in nativeLink := {
      crossTarget.value / (moduleName.value + "-out")
    },
    nativeOptimizerDriver := tools.OptimizerDriver(
      tools.Mode(nativeMode.value)),
    nativeWorkdir := {
      val workdir = crossTarget.value / "native"
      IO.delete(workdir)
      IO.createDirectory(workdir)
      workdir
    },
    nativeConfig := {
      val mainClass = selectMainClass.value.getOrElse(
        throw new MessageOnlyException("No main class detected.")
      )
      val classpath =
        fullClasspath.value.map(_.data.toPath).filter(f => Files.exists(f))

      val nativeLibJar =
        classpath.find { p =>
          val path = p.toAbsolutePath.toString
          path.contains("scala-native") && path.contains("nativelib")
        }.get
      val entry   = mainClass.toString + "$"
      val cwd     = nativeWorkdir.value.toPath
      val clang   = nativeClang.value.toPath
      val clangpp = nativeClangPP.value.toPath
      val gc      = tools.GarbageCollector(nativeGC.value)

      tools.Config.empty
        .withNativeLib(nativeLibJar)
        .withDriver(nativeOptimizerDriver.value)
        .withLinkerReporter(nativeLinkerReporter.value)
        .withOptimizerReporter(nativeOptimizerReporter.value)
        .withEntry(entry)
        .withPaths(classpath)
        .withWorkdir(cwd)
        .withClang(clang)
        .withClangPP(clangpp)
        .withTarget(nativeTarget.value)
        .withLinkingOptions(nativeLinkingOptions.value)
        .withGC(gc)
        .withLinkStubs(nativeLinkStubs.value)
    },
    nativeUnpackLib := {
      val cwd       = nativeWorkdir.value
      val classpath = (fullClasspath in Compile).value

      val jar =
        classpath
          .map(entry => entry.data.abs)
          .collectFirst {
            case p if p.contains("scala-native") && p.contains("nativelib") =>
              file(p)
          }
          .get

      build.unpackNativeLibrary(jar.toPath, cwd.toPath).toFile
    },
    nativeCompileLib := {
      val config = {
        val config0 = nativeConfig.value
        config0.withCompileOptions("-O2" +: config0.compileOptions)
      }

      val linked  = nativeLinkNIR.value
      val logger  = streams.value.log
      val libPath = nativeUnpackLib.value.toPath

      val outPath =
        build.compileNativeLib(config, linked, libPath, logger.toLogger)
      outPath.toFile
    },
    nativeLinkNIR := {
      val logger = streams.value.log
      val driver = nativeOptimizerDriver.value
      val config = nativeConfig.value

      val result = logger.time("Linking") {
        tools.link(config)
      }
      if (result.unresolved.nonEmpty) {
        result.unresolved.map(_.show).sorted.foreach { signature =>
          logger.error(s"cannot link: $signature")
        }
        throw new Exception("unable to link")
      }
      val classCount = result.defns.count {
        case _: nir.Defn.Class | _: nir.Defn.Module | _: nir.Defn.Trait => true
        case _                                                          => false
      }
      val methodCount = result.defns.count(_.isInstanceOf[nir.Defn.Define])
      logger.info(
        s"Discovered ${classCount} classes and ${methodCount} methods")
      result
    },
    nativeOptimizeNIR := {
      val logger = streams.value.log
      val result = nativeLinkNIR.value
      val config = nativeConfig.value
      val mode   = nativeMode.value
      logger.time(s"Optimizing ($mode mode)") {
        tools.optimize(config, result.defns, result.dyns)
      }
    },
    nativeGenerateLL := {
      val logger    = streams.value.log
      val config    = nativeConfig.value
      val optimized = nativeOptimizeNIR.value
      val cwd       = nativeWorkdir.value
      logger.time("Generating intermediate code") {
        tools.codegen(config, optimized)
      }
      logger.info(s"Produced ${(cwd ** "*.ll").get.length} files")
      (cwd ** "*.ll").get.toSeq
    },
    nativeCompileLL := {
      val logger    = streams.value.log
      val config    = nativeConfig.value
      val generated = nativeGenerateLL.value.map(_.toPath)

      val outPaths =
        llvm.compileLL(config, generated, logger.toLogger)
      outPaths.map(_.toFile)
    },
    nativeLinkLL := {
      val linked    = nativeLinkNIR.value
      val logger    = streams.value.log.toLogger
      val apppaths  = nativeCompileLL.value.map(_.toPath)
      val nativelib = nativeCompileLib.value.toPath
      val outpath   = (artifactPath in nativeLink).value.toPath
      val config    = nativeConfig.value

      val outPath =
        llvm.linkLL(config, linked, apppaths, nativelib, outpath, logger)
      outPath.toFile
    },
    nativeLink := {
      nativeWarnOldJVM.value
      // We explicitly mention all of the steps in the pipeline
      // although only the last one is strictly necessary.
      compile.value
      nativeLinkNIR.value
      nativeOptimizeNIR.value
      nativeGenerateLL.value
      nativeCompileLL.value
      nativeCompileLib.value
      nativeLinkLL.value
    },
    run := {
      val env    = (envVars in run).value.toSeq
      val logger = streams.value.log
      val binary = nativeLink.value.abs
      val args   = spaceDelimited("<arg>").parsed

      logger.running(binary +: args)
      val exitCode = Process(binary +: args, None, env: _*)
        .run(connectInput = true)
        .exitValue

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      message.foreach(sys.error)
    },
    nativeMissingDependencies := {
      (nativeExternalDependencies.value.toSet --
        nativeAvailableDependencies.value.toSet).toList.sorted
    },
    nativeAvailableDependencies := {
      val fcp = fullClasspath.value
      ResourceScope { implicit scope =>
        val globals = fcp
          .collect { case p if p.data.exists => p.data.toPath }
          .flatMap(p =>
            tools.LinkerPath(VirtualDirectory.real(p)).globals.toSeq)

        globals.map(_.show).sorted
      }
    },
    nativeExternalDependencies := {
      val forceCompile = compile.value
      val classDir     = classDirectory.value.toPath

      ResourceScope { implicit scope =>
        val globals = linker.ClassPath(VirtualDirectory.real(classDir)).globals
        val config  = tools.Config.empty.withPaths(Seq(classDir))
        val result  = (linker.Linker(config)).link(globals.toSeq)

        result.unresolved.map(_.show).sorted
      }
    }
  )

  lazy val scalaNativeCompileSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings

  lazy val scalaNativeTestSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings ++ Seq(
      test := (test in NativeTest).value,
      testOnly := (testOnly in NativeTest).evaluated,
      testQuick := (testQuick in NativeTest).evaluated
    )

  lazy val NativeTest = config("nativetest").extend(Test).hide

  lazy val scalaNativeNativeTestSettings: Seq[Setting[_]] =
    Defaults.testSettings ++
      scalaNativeConfigSettings ++ Seq(
      classDirectory := (classDirectory in Test).value,
      dependencyClasspath := (dependencyClasspath in Test).value,
      parallelExecution in test := false,
      sourceGenerators += Def.task {
        val frameworks = (loadedTestFrameworks in Test).value.map(_._2).toSeq
        val tests      = (definedTests in Test).value
        val output     = sourceManaged.value / "FrameworksMap.scala"
        IO.write(output, makeTestMain(frameworks, tests))
        Seq(output)
      }.taskValue,
      loadedTestFrameworks := {
        val frameworks = (loadedTestFrameworks in Test).value
        val logger     = streams.value.log
        val testBinary = nativeLink.value
        val envVars    = (Keys.envVars in (Test, test)).value
        (frameworks.zipWithIndex).map {
          case ((tf, f), id) =>
            (tf, new ScalaNativeFramework(f, id, logger, testBinary, envVars))
        }
      },
      definedTests := (definedTests in Test).value
    )

  lazy val scalaNativeProjectSettings: Seq[Setting[_]] =
    scalaNativeDependencySettings ++
      scalaNativeBaseSettings ++
      inConfig(Compile)(scalaNativeCompileSettings) ++
      inConfig(Test)(scalaNativeTestSettings) ++
      inConfig(NativeTest)(scalaNativeNativeTestSettings)
}
