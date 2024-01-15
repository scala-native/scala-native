package scala.scalanative
package build

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import scala.sys.process._
import scalanative.build.core.IO.RichPath
import scalanative.compat.CompatParColls.Converters._
import scalanative.nir.Attr.Link
import scala.scalanative.build.BuildTarget._

/** Internal utilities to interact with LLVM command-line tools. */
private[scalanative] object LLVM {

  /** Object file extension: ".o" */
  val oExt = ".o"

  /** C++ file extension: ".cpp" */
  val cppExt = ".cpp"

  /** LLVM intermediate file extension: ".ll" */
  val llExt = ".ll"

  /** List of source patterns used: ".c, .cpp, .S" */
  val srcExtensions = Seq(".c", cppExt, ".S")

  /** Compile the given files to object files
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param paths
   *    The directory paths containing native files to compile.
   *  @return
   *    The paths of the `.o` files.
   */
  def compile(config: Config, paths: Seq[Path]): Seq[Path] = {
    implicit val _config: Config = config
    // generate .o files for all included source files in parallel
    paths.par.map { srcPath =>
      val inpath = srcPath.abs
      val outpath = inpath + oExt
      val objPath = Paths.get(outpath)
      // compile if out of date or no object file
      if (needsCompiling(srcPath, objPath)) {
        compileFile(srcPath, objPath)
      } else objPath
    }.seq
  }

  private def compileFile(srcPath: Path, objPath: Path)(implicit
      config: Config
  ): Path = {
    val inpath = srcPath.abs
    val outpath = objPath.abs
    val isCpp = inpath.endsWith(cppExt)
    val isLl = inpath.endsWith(llExt)
    val workdir = config.workdir

    val compiler = if (isCpp) config.clangPP.abs else config.clang.abs
    val stdflag = {
      if (isLl) llvmIrFeatures
      else if (isCpp) {
        // C++14 or newer standard is needed to compile code using Windows API
        // shipped with Windows 10 / Server 2016+ (we do not plan supporting older versions)
        if (config.targetsWindows) Seq("-std=c++14")
        else Seq("-std=c++11")
      } else Seq("-std=gnu11")
    }
    val platformFlags = {
      if (config.targetsWindows) {
        val common = Seq("-g") // needed for debug symbols in stack traces
        val optional = if (config.targetsMsys) msysExtras else Nil
        common ++ optional
      } else Nil
    }
    val exceptionsHandling = {
      val opt = if (isCpp) List("-fcxx-exceptions") else Nil
      List("-fexceptions", "-funwind-tables") ::: opt
    }
    val flags: Seq[String] =
      buildTargetCompileOpts ++ flto ++ target ++
        stdflag ++ platformFlags ++ exceptionsHandling ++
        Seq("-fvisibility=hidden", opt) ++
        config.compileOptions
    val compilec: Seq[String] =
      Seq(compiler, "-c", inpath, "-o", outpath) ++ flags

    // compile
    config.logger.running(compilec)
    val result = Process(compilec, config.workdir.toFile) !
      Logger.toProcessLogger(config.logger)
    if (result != 0) {
      throw new BuildException(s"Failed to compile ${inpath}")
    }

    objPath
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
      objectsPaths: Seq[Path],
      outpath: Path
  ): Path = {
    implicit val _config: Config = config

    val command = config.compilerConfig.buildTarget match {
      case BuildTarget.Application | BuildTarget.LibraryDynamic =>
        prepareLinkCommand(objectsPaths, linkerResult, outpath)
      case BuildTarget.LibraryStatic =>
        prepareArchiveCommand(objectsPaths, outpath)
    }
    // link
    val result = command ! Logger.toProcessLogger(config.logger)
    if (result != 0) {
      throw new BuildException(s"Failed to link ${outpath}")
    }

    outpath
  }

  private def prepareLinkCommand(
      objectsPaths: Seq[Path],
      linkerResult: linker.Result,
      outpath: Path
  )(implicit config: Config) = {
    val workdir = config.workdir
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
    config.logger.info(s"Linking with [${links.mkString(", ")}]")
    val linkopts = config.linkingOptions ++ links.map("-l" + _)

    val flags = {
      val platformFlags =
        if (!config.targetsWindows) Nil
        else {
          // https://github.com/scala-native/scala-native/issues/2372
          // When using LTO make sure to use lld linker instead of default one
          // LLD might find some duplicated symbols defined in both C and C++,
          // runtime libraries (libUCRT, libCPMT), we ignore this warnings.
          val ltoSupport = config.compilerConfig.lto match {
            case LTO.None => Nil
            case _        => Seq("-fuse-ld=lld", "-Wl,/force:multiple")
          }
          Seq("-g") ++ ltoSupport
        }

      // This is to ensure that the load path of the resulting dynamic library
      // only contains the library filename, instead of the full path
      // (i.e. in the target folder of SBT build) - this would make the library
      // non-portable
      val linkNameFlags = {
        val artifactName = outpath.getFileName().toString
        if (config.compilerConfig.buildTarget == BuildTarget.LibraryDynamic)
          if (config.targetsLinux)
            List(s"-Wl,-soname,$artifactName")
          else if (config.targetsMac)
            List(s"-Wl,-install_name,$artifactName")
          else Nil
        else Nil
      }

      val output = Seq("-o", outpath.abs)

      buildTargetLinkOpts ++ flto ++ platformFlags ++ linkNameFlags ++ output ++ target
    }
    val paths = objectsPaths.map(_.abs)
    // it's a fix for passing too many file paths to the clang compiler,
    // If too many packages are compiled and the platform is windows, windows
    // terminal doesn't support too many characters, which will cause an error.
    val llvmLinkInfo = flags ++ paths ++ linkopts
    val configFile = workdir.resolve("llvmLinkInfo").toFile
    locally {
      val pw = new PrintWriter(configFile)
      try
        llvmLinkInfo.foreach {
          // Paths containg whitespaces needs to be escaped, otherwise
          // config file might be not interpretted correctly by the LLVM
          // in windows system, the file separator doesn't work very well, so we
          // replace it to linux file separator
          str => pw.println(escapeWhitespaces(str.replace("\\", "/")))
        }
      finally pw.close()
    }

    val command = Seq(config.clangPP.abs, s"@${configFile.getAbsolutePath()}")
    config.logger.running(command)
    Process(command, config.workdir.toFile())
  }

  private def prepareArchiveCommand(
      objectPaths: Seq[Path],
      outpath: Path
  )(implicit config: Config) = {
    val workdir = config.workdir

    val MRICompatibleAR =
      Discover.tryDiscover("llvm-ar", "LLVM_BIN").toOption orElse
        // MacOS ar command does not support -M flag...
        Discover.tryDiscover("ar").toOption.filter(_ => config.targetsLinux)

    def stageFiles(): Seq[String] = {
      objectPaths.map { path =>
        val uniqueName =
          workdir
            .relativize(path)
            .toString()
            .replace(File.separator, "_")
        val newPath = workdir.resolve(uniqueName)
        Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING)
        newPath.abs
      }
    }

    def useMRIScript(ar: Path) = {
      val MIRScriptFile = workdir.resolve("MIRScript").toFile
      val pw = new PrintWriter(MIRScriptFile)
      try {
        pw.println(s"CREATE ${escapeWhitespaces(outpath.abs)}")
        stageFiles().foreach { path =>
          pw.println(s"ADDMOD ${escapeWhitespaces(path)}")
        }
        pw.println("SAVE")
        pw.println("END")
      } finally pw.close()

      val command = Seq(ar.abs, "-M")
      config.logger.running(command)

      Process(command, config.workdir.toFile()) #< MIRScriptFile
    }

    MRICompatibleAR match {
      case None =>
        val ar = Discover.discover("ar")
        val command = Seq(ar.abs, "rc", outpath.abs) ++ stageFiles()
        config.logger.running(command)
        Process(command, config.workdir.toFile())
      case Some(path) => useMRIScript(path)
    }
  }

  /** Checks the input timestamp to see if the file needs compiling. The call to
   *  lastModified will return 0 for a non existent output file but that makes
   *  the timestamp always less forcing a recompile.
   *
   *  @param in
   *    the source file
   *  @param out
   *    the object file
   *  @return
   *    true if it needs compiling false otherwise.
   */
  @inline private def needsCompiling(in: Path, out: Path): Boolean = {
    in.toFile().lastModified() > out.toFile().lastModified()
  }

  /** Looks at all the object files to see if one is newer than the output
   *  (executable). All object files will be compiled at this time so
   *  lastModified will always be a real time stamp. The output executable
   *  lastModified can be 0 but that forces the link to occur.
   *
   *  @param in
   *    the list of object file to link
   *  @param out
   *    the executable
   *  @return
   *    true if it need linking
   */
  @inline private def needsLinking(in: Seq[Path], out: Path): Boolean = {
    val inmax = in.map(_.toFile().lastModified()).max
    val outmax = out.toFile().lastModified()
    inmax > outmax
  }

  private def flto(implicit config: Config): Seq[String] =
    config.compilerConfig.lto match {
      case LTO.None => Seq.empty
      case lto      => Seq(s"-flto=${lto.name}")
    }

  private def target(implicit config: Config): Seq[String] =
    config.compilerConfig.targetTriple match {
      case Some(tt) => Seq("-target", tt)
      case None     => Seq("-Wno-override-module")
    }

  private def opt(implicit config: Config): String =
    config.mode match {
      case Mode.Debug       => "-O0"
      case Mode.ReleaseFast => "-O2"
      case Mode.ReleaseSize => "-Oz"
      case Mode.ReleaseFull => "-O3"
    }

  private def llvmIrFeatures(implicit config: Config): Seq[String] = {
    implicit def nativeConfig: NativeConfig = config.compilerConfig
    val opaquePointers = Discover.features.opaquePointers.requiredFlag.toList
      .flatMap(Seq("-mllvm", _))

    opaquePointers
  }

  private def buildTargetCompileOpts(implicit config: Config): Seq[String] =
    config.compilerConfig.buildTarget match {
      case BuildTarget.Application =>
        Nil
      case BuildTarget.LibraryStatic =>
        optionalPICflag ++ Seq("--emit-static-lib")
      case BuildTarget.LibraryDynamic =>
        optionalPICflag :+
          "-DSCALANATIVE_DYLIB" // allow to compile dynamic library constructor in dylib_init.c
    }

  private def buildTargetLinkOpts(implicit config: Config): Seq[String] = {
    val optRdynamic = if (config.targetsWindows) Nil else Seq("-rdynamic")
    config.compilerConfig.buildTarget match {
      case BuildTarget.Application =>
        optRdynamic
      case BuildTarget.LibraryStatic =>
        optionalPICflag ++ Seq("--emit-static-lib")
      case BuildTarget.LibraryDynamic =>
        val libFlag = if (config.targetsMac) "-dynamiclib" else "-shared"
        Seq(libFlag) ++ optionalPICflag ++ optRdynamic
    }
  }

  private def optionalPICflag(implicit config: Config): Seq[String] =
    if (config.targetsWindows) Nil
    else Seq("-fPIC")

  private def escapeWhitespaces(str: String): String = {
    if (str.exists(_.isWhitespace)) s""""$str""""
    else str
  }

  lazy val msysExtras = Seq(
    "-D_WIN64",
    "-D__MINGW64__",
    "-D_X86_64_ -D__X86_64__ -D__x86_64",
    "-D__USING_SJLJ_EXCEPTIONS__",
    "-DNO_OLDNAMES",
    "-D_LIBUNWIND_BUILD_ZERO_COST_APIS"
  )

  private[scalanative] def generateLLVMIdent(config: Config): Seq[Path] = {
    def constructIdent: String = {
      val snVersion = scala.scalanative.nir.Versions.current

      val ident1 = s"Scala Native ${snVersion}"
      val ident2 = s"Mode: ${config.mode}, LTO: ${config.LTO}, GC: ${config.gc}"

      s"${ident1} (${ident2})"
    }

    /* Enable feature only where known to work. Add to list as experience grows
     * FreeBSD uses elf format so it _should_ work, but it has not been
     * exercised.
     */
    if (!config.targetsLinux) Seq.empty[Path]
    else {
      // From lld.llvm.org doc: readelf --string-dump .comment <output-file>
      val workDir = config.workdir
      val identPath = workDir.resolve("ScalaNativeIdent.ll")
      val ident = constructIdent

      val pw = new java.io.PrintWriter(identPath.toFile) // truncate if exists

      try {
        pw.println("!llvm.ident = !{!0}")
        pw.println(s"""!0 = !{!"${ident}"}""")
      } finally pw.close()

      Seq(identPath)
    }
  }

}
