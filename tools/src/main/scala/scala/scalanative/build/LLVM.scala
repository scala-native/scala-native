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
    import NativeSourcesCompilerPlugin._

    val defaultPlugins: Map[String, NativeSourcesCompilerPlugin] =
      LlSourcesCompilerPlugin.extensions.map(_ -> LlSourcesCompilerPlugin).toMap

    val plugins = config.compilerConfig.nativeSourcesCompilerPlugins
      .flatMap(p => p.extensions.map(_ -> p))
      .foldLeft(defaultPlugins) {
        case (plugins, (ext, plugin)) =>
          if (plugins.contains(ext)) {
            val msg =
              s"Multiple compiler plugins for extension $ext: ${plugins(ext).name} and ${plugin.name}"
            throw new BuildException(msg)
          } else {
            plugins + (ext -> plugin)
          }
      }

    // generate .o files for all included source files in parallel
    paths.par.flatMap { path =>
      val inpath = path.toAbsolutePath()

      val ext = inpath.toString.split('.').lastOption
      val compiler = ext
        .flatMap(ext => plugins.get(s".$ext"))
        .getOrElse(
          throw new BuildException(s"Unable to find compiler for ${inpath}")
        )

      val compilationCtx = compiler.compile(config, inpath)

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
      val (objectFiles, libraries) =
        compilationOutput.partition {
          case _: ObjectFile => true
          case _: Library    => false
        }
      (objectFiles ++ libraries).map(_.path.abs)
    }

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

  private[build] def target(config: Config): Seq[String] =
    config.compilerConfig.targetTriple match {
      case Some(tt) => Seq("-target", tt)
      case None     => Seq("-Wno-override-module")
    }

}
