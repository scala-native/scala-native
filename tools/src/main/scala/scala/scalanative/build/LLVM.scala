package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.sys.process._
import scalanative.build.IO.RichPath
import scalanative.compat.CompatParColls.Converters._
import scala.scalanative.build.Mode._
import scalanative.nir.Attr.Link

/** Internal utilities to interact with LLVM command-line tools. */
private[scalanative] object LLVM {

  /** Object file extension: ".o" */
  val oExt = ".o"

  /** C++ file extension: ".cpp" */
  val cppExt = ".cpp"

  /** Rust source file extension */
  val rustExt = ".rs"

  /** LLVM intermediate file extension: ".ll" */
  val llExt = ".ll"

  /** List of source patterns used: ".c, .cpp, .S" */
  val srcExtensions = Seq(".c", cppExt, ".S", rustExt)

  private case class CompilationContext(
      command: Seq[String],
      result: CompilationResult
  )

  /** Compile the given files to object files
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param paths
   *    The directory paths containing native files to compile.
   *  @return
   *    The paths of the `.o` files.
   */
  def compile(
      config: Config,
      paths: Seq[Path]
  ): Seq[CompilationResult] = {
    // generate .o files for all included source files in parallel
    paths.par.flatMap { path =>
      val inpath = path.toAbsolutePath()
      val isRust = inpath.toString.endsWith(rustExt)

      val compilationCtx =
        if (isRust)
          compileWithRustc(config, inpath)
        else
          compileWithClang(config, inpath)

      for {
        CompilationContext(cmd, output) <- compilationCtx
      } yield {
        if (cmd.nonEmpty) {
          config.logger.running(cmd)
          val proc = Process(cmd, config.workdir.toFile)
          val result = proc ! Logger.toProcessLogger(config.logger)
          if (result != 0) {
            throw new BuildException(s"Failed to compile ${inpath}")
          }
        }
        output
      }
    }.seq
  }

  /** Links a collection of `.ll.o` files and the `.o` files from the
   *  `nativelib`, other libaries, and the application project into the native
   *  binary.
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param linkerResult
   *    The results from the linker.
   *  @param objectPaths
   *    The paths to all the `.o` files.
   *  @param outpath
   *    The path where to write the resulting binary.
   *  @return
   *    `outpath`
   */
  def link(
      config: Config,
      linkerResult: linker.Result,
      compilationOutput: Seq[CompilationResult],
      outpath: Path
  ): Path = {
    val links = {
      val srclinks = linkerResult.links.collect {
        case Link("z") if config.targetsWindows => "zlib"
        case Link(name)                         => name
      }
      val gclinks = config.gc.links
      // We need extra linking dependencies for:
      // * libdl for our vendored libunwind implementation.
      // * libpthread for process APIs and parallel garbage collection.
      // * Dbghelp for windows implementation of unwind libunwind API
      val platformsLinks =
        if (config.targetsWindows) Seq("Dbghelp")
        else Seq("pthread", "dl")
      platformsLinks ++ srclinks ++ gclinks
    }
    val linkopts = config.linkingOptions ++ links.map("-l" + _)
    val flags = {
      val platformFlags =
        if (config.targetsWindows) {
          // https://github.com/scala-native/scala-native/issues/2372
          // When using LTO make sure to use lld linker instead of default one
          // LLD might find some duplicated symbols defined in both C and C++,
          // runtime libraries (libUCRT, libCPMT), we ignore this warnings.
          val ltoSupport = config.compilerConfig.lto match {
            case LTO.None => Nil
            case _        => Seq("-fuse-ld=lld", "-Wl,/force:multiple")
          }
          Seq("-g") ++ ltoSupport
        } else Seq("-rdynamic")
      flto(config) ++ platformFlags ++ Seq("-o", outpath.abs) ++ target(config)
    }
    // Make sure that libraries are linked as the last ones
    val paths = {
      compilationOutput.filter(_.isInstanceOf[ObjectFile]) ++
        compilationOutput.filter(_.isInstanceOf[Library])
    }.map(_.path.abs)

    val compile = config.clangPP.abs +: (flags ++ paths ++ linkopts)

    config.logger.time(
      s"Linking native code (${config.gc.name} gc, ${config.LTO.name} lto)"
    ) {
      config.logger.running(compile)
      val result = Process(compile, config.workdir.toFile) !
        Logger.toProcessLogger(config.logger)
      if (result != 0) {
        throw new BuildException(s"Failed to link ${outpath}")
      }
    }
    outpath
  }

  private[build] def flto(config: Config): Seq[String] =
    config.compilerConfig.lto match {
      case LTO.None => Seq.empty
      case lto      => Seq(s"-flto=${lto.name}")
    }

  private def target(config: Config): Seq[String] =
    config.compilerConfig.targetTriple match {
      case Some(tt) => Seq("-target", tt)
      case None     => Seq("-Wno-override-module")
    }

  private def opt(config: Config): String =
    config.mode match {
      case Mode.Debug       => "-O0"
      case Mode.ReleaseFast => "-O2"
      case Mode.ReleaseFull => "-O3"
    }

  private def compileWithClang(
      config: Config,
      inpath: Path
  ): Option[CompilationContext] = {
    val isCpp = inpath.toString.endsWith(cppExt)
    val isLl = inpath.toString.endsWith(llExt)
    val outpath = Paths.get(inpath.abs + oExt)
    val alreadyExists = Files.exists(outpath)
    // LL is generated so always rebuild
    if (isLl || !alreadyExists) Some {
      val compiler = if (isCpp) config.clangPP.abs else config.clang.abs
      val stdflag = {
        if (isLl) Seq()
        else if (isCpp) {
          // C++14 or newer standard is needed to compile code using Windows API
          // shipped with Windows 10 / Server 2016+ (we do not plan supporting older versions)
          if (config.targetsWindows) Seq("-std=c++14")
          else Seq("-std=c++11")
        } else Seq("-std=gnu11")
      }
      val platformFlags = {
        if (config.targetsWindows) Seq("-g")
        else Nil
      }
      val flags = opt(config) +: "-fvisibility=hidden" +:
        stdflag ++: platformFlags ++: config.compileOptions

      CompilationContext(
        command = Seq(compiler) ++
          flto(config) ++
          flags ++
          target(config) ++
          Seq("-c", inpath.abs, "-o", outpath.abs),
        result = ObjectFile(outpath)
      )
    }
    else if (alreadyExists) Some(CompilationContext(Nil, ObjectFile(outpath)))
    else None
  }

  private val compiledCargoCrates = collection.mutable.Set.empty[File]
  private def compileWithRustc(
      config: Config,
      inPath: Path
  ): Option[CompilationContext] = {
    val optional = List(
      config.compilerConfig.targetTriple.map("--target=" + _)
    ).collect {
      case Some(config) => config
    }

    def genStaticLibraryName(name: String): String = {
      if (Platform.isWindows) s"$name.lib"
      else s"lib$name.a"
    }

    val cargoDefinition = {
      def loop(currentDir: Path): Option[File] = {
        if (currentDir.endsWith(NativeLib.nativeCodeDir)) None
        else {
          currentDir
            .toFile()
            .listFiles()
            .find(_.getName().toLowerCase == "cargo.toml")
            .orElse(loop(currentDir.getParent()))
        }
      }
      loop(inPath.getParent())
    }

    def compileUsingCargo(cargoFile: File) = {
      val shouldBuildCrate = compiledCargoCrates.synchronized {
        compiledCargoCrates.add(cargoFile)
      }
      if (!shouldBuildCrate) None
      else
        Some {
          val content = scala.io.Source.fromFile(cargoFile).getLines().toList
          def getScope(name: String) = {
            val header = s"[$name]"
            content
              .dropWhile(!_.trim().startsWith(header))
              .drop(1)
              .takeWhile(!_.trim().startsWith("["))
          }

          def findValue(
              prefix: String
          )(scopeContent: List[String]): Option[String] = {
            scopeContent
              .find(_.trim().startsWith(prefix))
              .map(_.split("="))
              .map {
                case Array(_, rhs) => rhs.trim()
                case arr           => arr.tail.mkString("=")
              }
          }

          def findInLibOrPackage(field: String) =
            findValue(field)(getScope("lib")) orElse
              findValue(field)(getScope("package"))

          val name = findInLibOrPackage("name").map(_.replace("\"", ""))
          val crateTypes = findInLibOrPackage("crate-type").toList.flatMap {
            _.replace("\"", "")
              .stripPrefix("[")
              .stripSuffix("]")
              .split(",")
              .map(_.trim())
          }.toSet

          if (!crateTypes.contains("staticlib")) {
            throw new BuildException(
              s"Rust crete needs to be published as staticlib, currently published as: ${crateTypes
                .mkString(", ")};"
            )
          }

          val (releaseDir, releaseOptions) = config.mode match {
            case Debug                     => "debug" -> None
            case ReleaseFast | ReleaseFull => "release" -> Some("--release")
          }

          val outPath = name
            .fold {
              throw new BuildException(
                "Failed to compute expected name of cargo artifact"
              )
            } { name =>
              Paths.get(
                cargoFile.getParent(),
                "target",
                releaseDir,
                genStaticLibraryName(name)
              )
            }

          CompilationContext(
            command = Discover.cargo.abs ::
              "build" ::
              "--lib" ::
              s"--manifest-path=${cargoFile.getAbsolutePath()}" ::
              "--quiet" ::
              releaseOptions.toList :::
              optional,
            result = Library(outPath)
          )
        }
    }

    def compileStandalone = Some {
      val outPath = inPath.resolveSibling(
        genStaticLibraryName(
          inPath.getFileName().toString.stripSuffix(rustExt)
        )
      )

      CompilationContext(
        command = Discover.rustc.abs ::
          inPath.abs ::
          "-o" + outPath.abs ::
          "--crate-type=staticlib" :: // Need to be compiled as staticlib to include rust stdlib
          optional,
        result = Library(outPath)
      )
    }

    cargoDefinition match {
      case None       => compileStandalone
      case Some(path) => compileUsingCargo(path)
    }
  }
}
