package scala.scalanative
package sbtplugin

import sbtcrossproject.CrossPlugin.autoImport._
import ScalaNativePlugin.autoImport._

import scalanative.nir
import scalanative.tools
import scalanative.io.VirtualDirectory
import scalanative.util.{Scope => ResourceScope}
import scalanative.sbtplugin.Utilities._
import scalanative.sbtplugin.TestUtilities._

import sbt.testing.Framework
import testinterface.ScalaNativeFramework

import sbt._, Keys._, complete.DefaultParsers._

import scala.util.Try

import System.{lineSeparator => nl}
import java.io.ByteArrayInputStream

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
      "org.scala-native" %%% "scalalib"       % nativeVersion,
      "org.scala-native" %%% "test-interface" % nativeVersion % Test
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full)
  )

  lazy val scalaNativeBaseSettings: Seq[Setting[_]] = Seq(
    crossVersion := ScalaNativeCrossVersion.binary,
    crossPlatform := NativePlatform,
    nativeClang := {
      val clang = discover("clang", clangVersions)
      checkThatClangIsRecentEnough(clang)
      clang
    },
    nativeClang in NativeTest := (nativeClang in Test).value,
    nativeClangPP := {
      val clang = discover("clang++", clangVersions)
      checkThatClangIsRecentEnough(clang)
      clang
    },
    nativeClangPP in NativeTest := (nativeClangPP in Test).value,
    nativeCompileOptions := {
      val includes = {
        val includedir =
          Try(Process("llvm-config --includedir").lines_!.toSeq)
            .getOrElse(Seq.empty)
        ("/usr/local/include" +: includedir).map(s => s"-I$s")
      }
      includes :+ "-Qunused-arguments"
    },
    nativeCompileOptions in NativeTest := (nativeCompileOptions in Test).value,
    nativeLinkingOptions := {
      val libs = {
        val libdir =
          Try(Process("llvm-config --libdir").lines_!.toSeq)
            .getOrElse(Seq.empty)
        ("/usr/local/lib" +: libdir).map(s => s"-L$s")
      }
      libs
    },
    nativeLinkingOptions in NativeTest := (nativeLinkingOptions in Test).value,
    nativeMode := "debug",
    nativeMode in NativeTest := (nativeMode in Test).value,
    nativeLinkerReporter := tools.LinkerReporter.empty,
    nativeLinkerReporter in NativeTest := (nativeLinkerReporter in Test).value,
    nativeOptimizerReporter := tools.OptimizerReporter.empty,
    nativeOptimizerReporter in NativeTest := (nativeOptimizerReporter in Test).value,
    nativeGC := "boehm",
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
      val cwd    = nativeWorkdir.value
      val clang  = nativeClang.value
      // Use non-standard extension to not include the ll file when linking (#639)
      val targetc  = cwd / "target" / "c.probe"
      val targetll = cwd / "target" / "ll.probe"
      val compilec =
        Seq(clang.abs,
            "-S",
            "-xc",
            "-emit-llvm",
            "-o",
            targetll.abs,
            targetc.abs)
      def fail =
        throw new MessageOnlyException("Failed to detect native target.")

      IO.write(targetc, "int probe;")
      logger.running(compilec)
      val exit = Process(compilec, cwd) ! logger
      if (exit != 0) fail
      IO.readLines(targetll)
        .collectFirst {
          case line if line.startsWith("target triple") =>
            line.split("\"").apply(1)
        }
        .getOrElse(fail)
    },
    artifactPath in nativeLink := {
      crossTarget.value / (moduleName.value + "-out")
    },
    nativeOptimizerDriver := tools.OptimizerDriver(nativeConfig.value),
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
      val classpath = fullClasspath.value.map(_.data)
      val entry     = nir.Global.Top(mainClass.toString + "$")
      val cwd       = nativeWorkdir.value

      tools.Config.empty
        .withEntry(entry)
        .withPaths(classpath)
        .withWorkdir(cwd)
        .withTarget(nativeTarget.value)
        .withMode(mode(nativeMode.value))
    },
    nativeUnpackLib := {
      val cwd       = nativeWorkdir.value
      val logger    = streams.value.log
      val classpath = (fullClasspath in Compile).value

      val lib = cwd / "lib"
      val jar =
        classpath
          .map(entry => entry.data.abs)
          .collectFirst {
            case p if p.contains("scala-native") && p.contains("nativelib") =>
              file(p)
          }
          .get
      val jarhash     = Hash(jar).toSeq
      val jarhashfile = lib / "jarhash"
      def unpacked =
        lib.exists &&
          jarhashfile.exists &&
          jarhash == IO.readBytes(jarhashfile).toSeq

      if (!unpacked) {
        IO.delete(lib)
        IO.unzip(jar, lib)
        IO.write(jarhashfile, Hash(jar))
      }

      lib
    },
    nativeCompileLib := {
      val linked    = nativeLinkNIR.value
      val cwd       = nativeWorkdir.value
      val clang     = nativeClang.value
      val clangpp   = nativeClangPP.value
      val gc        = nativeGC.value
      val opts      = "-O2" +: nativeCompileOptions.value
      val logger    = streams.value.log
      val nativelib = nativeUnpackLib.value
      val cpaths    = (cwd ** "*.c").get.map(_.abs)
      val cpppaths  = (cwd ** "*.cpp").get.map(_.abs)
      val paths     = cpaths ++ cpppaths

      // predicate to check if given file path shall be compiled
      // we only include sources of the current gc and exclude
      // all optional dependencies if they are not necessary
      def include(path: String) = {
        val sep = java.io.File.separator

        if (path.contains(sep + "optional" + sep)) {
          val name = file(path).getName.split("\\.").head
          linked.links.map(_.name).contains(name)
        } else if (path.contains(sep + "gc" + sep)) {
          path.contains("gc" + sep + gc)
        } else {
          true
        }
      }

      // delete .o files for all excluded source files
      paths.foreach { path =>
        if (!include(path)) {
          val ofile = file(path + ".o")
          if (ofile.exists) {
            IO.delete(ofile)
          }
        }
      }

      // generate .o files for all included source files in parallel
      paths.par.foreach {
        path =>
          val opath = path + ".o"
          if (include(path) && !file(opath).exists) {
            val isCpp    = path.endsWith(".cpp")
            val compiler = if (isCpp) clangpp.abs else clang.abs
            val flags    = (if (isCpp) Seq("-std=c++11") else Seq()) ++ opts
            val compilec = Seq(compiler) ++ flags ++ Seq("-c",
                                                         path,
                                                         "-o",
                                                         opath)

            logger.running(compilec)
            val result = Process(compilec, cwd) ! logger
            if (result != 0) {
              sys.error("Failed to compile native library runtime code.")
            }
          }
      }

      nativelib
    },
    nativeLinkNIR := {
      val logger   = streams.value.log
      val driver   = nativeOptimizerDriver.value
      val config   = nativeConfig.value
      val reporter = nativeLinkerReporter.value
      val result = logger.time("Linking") {
        tools.link(config, driver, reporter)
      }
      if (result.unresolved.nonEmpty) {
        result.unresolved.map(_.show).sorted.foreach { signature =>
          logger.error(s"cannot link: $signature")
        }
        throw new MessageOnlyException("unable to link")
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
      val logger   = streams.value.log
      val result   = nativeLinkNIR.value
      val config   = nativeConfig.value
      val reporter = nativeOptimizerReporter.value
      val driver   = nativeOptimizerDriver.value
      logger.time("Optimizing") {
        tools.optimize(config, driver, result.defns, result.dyns, reporter)
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
      val logger      = streams.value.log
      val generated   = nativeGenerateLL.value
      val clangpp     = nativeClangPP.value
      val cwd         = nativeWorkdir.value
      val compileOpts = nativeCompileOptions.value
      val optimizationOpt =
        mode(nativeMode.value) match {
          case tools.Mode.Debug   => "-O0"
          case tools.Mode.Release => "-O2"
        }
      val opts = optimizationOpt +: compileOpts

      logger.time("Compiling to native code") {
        generated.par
          .map { ll =>
            val apppath = ll.abs
            val outpath = apppath + ".o"
            val compile = Seq(clangpp.abs, "-c", apppath, "-o", outpath) ++ opts
            logger.running(compile)
            Process(compile, cwd) ! logger
            new File(outpath)
          }
          .seq
          .toSeq
      }
    },
    nativeLinkLL := {
      val linked      = nativeLinkNIR.value
      val logger      = streams.value.log
      val apppaths    = nativeCompileLL.value
      val nativelib   = nativeCompileLib.value
      val cwd         = nativeWorkdir.value
      val target      = nativeTarget.value
      val gc          = nativeGC.value
      val linkingOpts = nativeLinkingOptions.value
      val clangpp     = nativeClangPP.value
      val outpath     = (artifactPath in nativeLink).value

      val links = {
        val os   = Option(sys props "os.name").getOrElse("")
        val arch = target.split("-").head
        // we need re2 to link the re2 c wrapper (cre2.h)
        val librt = os match {
          case "Linux" => Seq("rt")
          case _       => Seq.empty
        }
        val libunwind = os match {
          case "Mac OS X" => Seq.empty
          case _          => Seq("unwind", "unwind-" + arch)
        }
        librt ++ libunwind ++ linked.links
          .map(_.name) ++ garbageCollector(gc).links
      }
      val linkopts  = links.map("-l" + _) ++ linkingOpts
      val targetopt = Seq("-target", target)
      val flags     = Seq("-o", outpath.abs) ++ linkopts ++ targetopt
      val opaths    = (nativelib ** "*.o").get.map(_.abs)
      val paths     = apppaths.map(_.abs) ++ opaths
      val compile   = clangpp.abs +: (flags ++ paths)

      logger.time("Linking native code") {
        logger.running(compile)
        Process(compile, cwd) ! logger
      }

      outpath
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

      Defaults.toError(message)
    },
    nativeMissingDependencies := {
      (nativeExternalDependencies.value.toSet --
        nativeAvailableDependencies.value.toSet).toList.sorted
    },
    nativeAvailableDependencies := ResourceScope { implicit scope =>
      val forceCompile = compile.value

      val globals = fullClasspath.value
        .collect { case p if p.data.exists => p.data }
        .flatMap(p => tools.LinkerPath(VirtualDirectory.real(p)).globals.toSeq)

      globals.map(_.show).sorted
    },
    nativeExternalDependencies := ResourceScope { implicit scope =>
      val forceCompile = compile.value
      val classDir     = classDirectory.value
      val globals      = linker.ClassPath(VirtualDirectory.real(classDir)).globals

      val config = tools.Config.empty.withPaths(Seq(classDir))
      val result = (linker.Linker(config)).link(globals.toSeq)

      result.unresolved.map(_.show).sorted
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
