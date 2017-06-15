package scala.scalanative
package sbtplugin

import sbtcrossproject.CrossPlugin.autoImport._
import ScalaNativePlugin.autoImport._

import scalanative.nir
import scalanative.tools
import scalanative.io.VirtualDirectory
import scalanative.util.{Scope => ResourceScope}

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

  val nativeLogger =
    taskKey[Logger]("Logger, that's used by sbt-scala-native.")

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

  private def externalDependenciesTask[T](compileTask: TaskKey[T]) =
    nativeExternalDependencies := ResourceScope { implicit scope =>
      val forceCompile = compileTask.value
      val classDir     = classDirectory.value
      val globals      = linker.ClassPath(VirtualDirectory.real(classDir)).globals

      val config = tools.Config.empty.withPaths(Seq(classDir))
      val result = (linker.Linker(config)).link(globals.toSeq)

      result.unresolved.map(_.show).sorted
    }

  private def availableDependenciesTask[T](compileTask: TaskKey[T]) =
    nativeAvailableDependencies := ResourceScope { implicit scope =>
      val forceCompile = compileTask.value

      val globals = fullClasspath.value
        .collect { case p if p.data.exists => p.data }
        .flatMap(p => tools.LinkerPath(VirtualDirectory.real(p)).globals.toSeq)

      globals.map(_.show).sorted
    }

  lazy val NativeTest = config("nativetest").extend(Test).hide

  def nativeMissingDependenciesTask =
    nativeMissingDependencies := {
      (nativeExternalDependencies.value.toSet --
        nativeAvailableDependencies.value.toSet).toList.sorted
    }

  lazy val projectSettings =
    dependencies ++
      inScope(Global)(globalSettings) ++
      inConfig(Compile)(scalaNativeSettings) ++
      inConfig(Test)(scalaNativeSettings) ++
      inConfig(NativeTest)(scalaNativeSettings) ++
      inConfig(NativeTest)(nativeTestSettings) ++
      inConfig(Test)(testSettings)

  lazy val scalaNativeSettings =
    scopedSettings ++
      externalDependenciesTask(compile) ++
      availableDependenciesTask(compile) ++
      nativeMissingDependenciesTask

  lazy val dependencies = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "nativelib"      % nativeVersion,
      "org.scala-native" %%% "javalib"        % nativeVersion,
      "org.scala-native" %%% "scalalib"       % nativeVersion,
      "org.scala-native" %%% "test-interface" % nativeVersion % Test
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full)
  )

  lazy val globalSettings = Seq(
    nativeWarnOldJVM := {
      val logger = nativeLogger.value
      Try(Class.forName("java.util.function.Function")).toOption match {
        case None =>
          logger.warn("Scala Native is only supported on Java 8 or newer.")
        case Some(_) =>
          ()
      }
    },
    nativeClang := {
      val clang = discover("clang", clangVersions)
      checkThatClangIsRecentEnough(clang)
      clang
    },
    nativeClangPP := {
      val clang = discover("clang++", clangVersions)
      checkThatClangIsRecentEnough(clang)
      clang
    },
    nativeCompileOptions := {
      val includes = {
        val includedir =
          Try(Process("llvm-config --includedir").lines_!.toSeq)
            .getOrElse(Seq.empty)
        ("/usr/local/include" +: includedir).map(s => s"-I$s")
      }
      includes :+ "-Qunused-arguments" :+
        (mode(nativeMode.value) match {
          case tools.Mode.Debug   => "-O0"
          case tools.Mode.Release => "-O2"
        })
    },
    nativeLinkingOptions := {
      val libs = {
        val libdir =
          Try(Process("llvm-config --libdir").lines_!.toSeq)
            .getOrElse(Seq.empty)
        ("/usr/local/lib" +: libdir).map(s => s"-L$s")
      }
      libs
    },
    nativeMode := "debug",
    nativeLinkerReporter := tools.LinkerReporter.empty,
    nativeOptimizerReporter := tools.OptimizerReporter.empty,
    nativeLogger := streams.value.log,
    nativeGC := "boehm"
  )

  lazy val testSettings = Seq(
    test := (test in NativeTest).value,
    testOnly := (testOnly in NativeTest).evaluated,
    testQuick := (testQuick in NativeTest).evaluated
  )

  lazy val nativeTestSettings =
    Defaults.compileSettings ++
      Defaults.testSettings ++
      Seq(
        classDirectory := (classDirectory in Test).value,
        dependencyClasspath := (dependencyClasspath in Test).value,
        parallelExecution in test := false,
        sourceGenerators += Def.task {
          val frameworks = (loadedTestFrameworks in Test).value.map(_._2).toSeq
          val tests      = (definedTests in Test).value
          val output     = sourceManaged.value / "FrameworksMap.scala"
          IO.write(output, TestUtilities.makeTestMain(frameworks, tests))
          Seq(output)
        }.taskValue,
        loadedTestFrameworks := {
          val frameworks = (loadedTestFrameworks in Test).value
          val logger     = streams.value.log
          val testBinary = nativeLink.value
          val envVars    = (Keys.envVars in (Test, test)).value
          (frameworks.zipWithIndex).map {
            case ((tf, f), id) =>
              (tf,
               new ScalaNativeFramework(f, id, logger, testBinary, envVars))
          }
        },
        definedTests := (definedTests in Test).value
      )

  lazy val scopedSettings = Seq(
    nativeTarget := {
      val logger = nativeLogger.value
      val cwd    = nativeWorkdir.value
      val clang  = nativeClang.value
      // Use non-standard extension to not include the ll file when linking (#639)
      val targetc  = cwd / "target" / "c.probe"
      val targetll = cwd / "target" / "ll.probe"
      val compilec =
        Seq(abs(clang),
            "-S",
            "-xc",
            "-emit-llvm",
            "-o",
            abs(targetll),
            abs(targetc))
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
      val logger    = nativeLogger.value
      val classpath = (fullClasspath in Compile).value

      val lib = cwd / "lib"
      val jar =
        classpath
          .map(entry => abs(entry.data))
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
      val opts      = nativeCompileOptions.value ++ Seq("-O2")
      val logger    = nativeLogger.value
      val nativelib = nativeUnpackLib.value
      val cpaths    = (cwd ** "*.c").get.map(abs)
      val cpppaths  = (cwd ** "*.cpp").get.map(abs)
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
            val compiler = abs(if (isCpp) clangpp else clang)
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
      val logger   = nativeLogger.value
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
      val logger   = nativeLogger.value
      val result   = nativeLinkNIR.value
      val config   = nativeConfig.value
      val reporter = nativeOptimizerReporter.value
      val driver   = nativeOptimizerDriver.value
      logger.time("Optimizing") {
        tools.optimize(config, driver, result.defns, result.dyns, reporter)
      }
    },
    nativeGenerateLL := {
      val logger    = nativeLogger.value
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
      val logger      = nativeLogger.value
      val generated   = nativeGenerateLL.value
      val clangpp     = nativeClangPP.value
      val cwd         = nativeWorkdir.value
      val compileOpts = nativeCompileOptions.value
      logger.time("Compiling to native code") {
        generated.par
          .map { ll =>
            val apppath = abs(ll)
            val outpath = apppath + ".o"
            val compile = Seq(abs(clangpp), "-c", apppath, "-o", outpath) ++ compileOpts
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
      val logger      = nativeLogger.value
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
      val flags     = Seq("-o", abs(outpath)) ++ linkopts ++ targetopt
      val opaths    = (nativelib ** "*.o").get.map(abs)
      val paths     = apppaths.map(abs) ++ opaths
      val compile   = abs(clangpp) +: (flags ++ paths)

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
      val binary = abs(nativeLink.value)
      val args   = spaceDelimited("<arg>").parsed

      logger.running(binary +: args)
      val exitCode = Process(binary +: args, None, env: _*)
        .run(connectInput = true)
        .exitValue

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      Defaults.toError(message)
    }
  )

  val scalaNativeEcosystemSettings = Seq(
    crossVersion := ScalaNativeCrossVersion.binary,
    crossPlatform := NativePlatform
  )

  private def abs(file: File): String =
    file.getAbsolutePath

  private def discover(binaryName: String,
                       binaryVersions: Seq[(String, String)]): File = {
    val docSetup =
      "http://www.scala-native.org/en/latest/user/setup.html"

    val envName =
      if (binaryName == "clang") "CLANG"
      else if (binaryName == "clang++") "CLANGPP"
      else binaryName

    sys.env.get(s"${envName}_PATH") match {
      case Some(path) => file(path)
      case None => {
        val binaryNames = binaryVersions.flatMap {
          case (major, minor) =>
            Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
        } :+ binaryName

        Process("which" +: binaryNames).lines_!
          .map(file(_))
          .headOption
          .getOrElse {
            throw new MessageOnlyException(
              s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang ($docSetup)")
          }
      }
    }
  }

  private val clangVersions =
    Seq(("4", "0"), ("3", "9"), ("3", "8"), ("3", "7"))

  /**
   * Tests whether the clang compiler is recent enough.
   * <p/>
   * This is determined through looking up a built-in #define which is
   * more reliable than testing for a specific version.
   * <p/>
   * It might be better to use feature checking macros:
   * http://clang.llvm.org/docs/LanguageExtensions.html#feature-checking-macros
   */
  private def checkThatClangIsRecentEnough(pathToClangBinary: File): Unit = {
    def maybeFile(f: File) = f match {
      case file if file.exists => Some(abs(file))
      case none                => None
    }

    def definesBuiltIn(
        pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Seq("echo", "") #| Seq(clang, "-dM", "-E", "-")
      def splitIntoLines(s: String)      = s.split(f"%n")
      def removeLeadingDefine(s: String) = s.substring(s.indexOf(' ') + 1)

      for {
        clang <- pathToClangBinary
        output = commandLineToListBuiltInDefines(clang).!!
        lines  = splitIntoLines(output)
      } yield lines map removeLeadingDefine
    }

    val clang                = maybeFile(pathToClangBinary)
    val defines: Seq[String] = definesBuiltIn(clang).to[Seq].flatten
    val clangIsRecentEnough =
      defines.contains("__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__")

    if (!clangIsRecentEnough) {
      throw new MessageOnlyException(
        s"No recent installation of clang found " +
          s"at $pathToClangBinary.\nSee http://scala-native.readthedocs.io" +
          s"/en/latest/user/setup.html for details.")
    }
  }

  private def mode(mode: String) = mode match {
    case "debug"   => tools.Mode.Debug
    case "release" => tools.Mode.Release
    case value =>
      throw new MessageOnlyException(
        "nativeMode can be either \"debug\" or \"release\", not: " + value)
  }

  private def garbageCollector(gc: String) = gc match {
    case "none"  => GarbageCollector.None
    case "boehm" => GarbageCollector.Boehm
    case "immix" => GarbageCollector.Immix
    case value =>
      throw new MessageOnlyException(
        "nativeGC can be either \"none\", \"boehm\" or \"immix\", not: " + value)
  }

  private implicit class RichLogger(logger: Logger) {
    def time[T](msg: String)(f: => T): T = {
      import java.lang.System.nanoTime
      val start = nanoTime()
      val res   = f
      val end   = nanoTime()
      logger.info(s"$msg (${(end - start) / 1000000} ms)")
      res
    }

    def running(command: Seq[String]): Unit =
      logger.debug("running" + nl + command.mkString(nl + "\t"))
  }

}
