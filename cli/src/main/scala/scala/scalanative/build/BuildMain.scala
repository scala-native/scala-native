package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}

import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import scala.scalanative.util.Scope

/** A standalone CLI entry point for the lower part of the Scala Native
 *  compilation pipeline.
 *
 *  Takes NIR bytecode (produced by the nscplugin) on the classpath and runs
 *  linking, optimization, LLVM IR code-generation, native compilation, and
 *  system linking to produce a native binary.
 *
 *  By default the classpath includes the Scala Native runtime JARs (scalalib,
 *  javalib, etc.) baked in at build time via BuildInfo, plus the base-dir for
 *  user-compiled NIR files. Use --classpath for full control.
 *
 *  {{{
 *  Usage: scala-native-build [options]
 *
 *  Required:
 *    --main-class <class>            Fully qualified main class name
 *    --base-dir <dir>                Base output directory (crossTarget)
 *
 *  Optional (classpath):
 *    --classpath <path1:path2:...>   Override the entire NIR class path
 *
 *  Optional (general):
 *    --module-name <name>            Module name (default: "out")
 *    --test-config                   Enable test configuration
 *    --no-optimize                   Disable optimizations
 *    --base-name <name>              Base name for the output artifact
 *
 *  Optional (native config):
 *    --clang <path>                  Path to clang (auto-discovered)
 *    --clangpp <path>                Path to clang++ (auto-discovered)
 *    --gc <name>                     GC: none|boehm|immix|commix|experimental
 *    --mode <name>                   Mode: debug|release-fast|release-size|release-full
 *    --lto <name>                    LTO: none|thin|full
 *    --build-target <name>           Target: application|library-dynamic|library-static
 *    --target-triple <triple>        LLVM target triple
 *    --link-stubs                    Enable linking of stubs
 *    --multithreading                Enable multithreading support
 *    --no-multithreading             Disable multithreading support
 *    --embed-resources               Embed resources into the binary
 *    --incremental                   Enable incremental compilation (default)
 *    --no-incremental                Disable incremental compilation
 *    --check                         Enable NIR check
 *    --check-fatal-warnings          Treat check warnings as errors
 *    --dump                          Dump NIR
 *    --sanitizer <name>              Sanitizer: address|thread|undefined
 *    --compile-options <opts>        Additional compile options (comma-separated)
 *    --linking-options <opts>        Additional linking options (comma-separated)
 *    --sources-classpath <paths>     Source jars class path (colon-separated)
 *    --verbose                       Enable verbose (debug) logging
 *    --silent                        Suppress all logging
 *    --cached                        Use cached build (skip if unchanged)
 *  }}}
 */
object BuildMain {

  def main(args: Array[String]): Unit = {
    val parsed = parseArgs(args.toList)

    val mainClass = parsed.getOrError("main-class", "--main-class is required")
    val baseDirStr = parsed.getOrError("base-dir", "--base-dir is required")
    val baseDir = Paths.get(baseDirStr)

    val classpathPaths: Seq[Path] = parsed.get("classpath") match {
      case Some(cp) => cp.split(File.pathSeparator).map(Paths.get(_)).toSeq
      case None     =>
        // Automatically include the Scala Native runtime classpath
        // (scalalib, javalib, etc.) baked in at build time via BuildInfo,
        // plus the user's output directory and extracted dependencies.
        val runtimeCp =
          ScalaNativeBuildInfo.nativeRuntimeClasspath
            .split(File.pathSeparator)
            .map(Paths.get(_))
        runtimeCp.toSeq ++ discoverClasspath(
          baseDir,
          parsed.flag("test-config")
        )
    }

    val sourcesClassPath: Seq[Path] = parsed
      .get("sources-classpath")
      .map(_.split(File.pathSeparator).map(Paths.get(_)).toSeq)
      .getOrElse(Seq.empty)

    val logger =
      if (parsed.flag("silent")) Logger.nullLogger
      else if (parsed.flag("verbose")) Logger.default
      else infoLogger

    val cpSource =
      if (parsed.get("classpath").isDefined) "provided via --classpath"
      else "runtime (BuildInfo) + discovered base-dir"
    logger.info(s"Classpath ($cpSource, ${classpathPaths.size} entries):")
    classpathPaths.foreach(p => logger.info(s"  $p"))

    // Build NativeConfig
    var nativeConfig = NativeConfig.empty
      .withClang(
        parsed.get("clang").map(Paths.get(_)).getOrElse(Discover.clang())
      )
      .withClangPP(
        parsed.get("clangpp").map(Paths.get(_)).getOrElse(Discover.clangpp())
      )
      .withCompileOptions(
        parsed
          .get("compile-options")
          .map(_.split(",").toSeq)
          .getOrElse(Discover.compileOptions())
      )
      .withLinkingOptions(
        parsed
          .get("linking-options")
          .map(_.split(",").toSeq)
          .getOrElse(Discover.linkingOptions())
      )
      .withGC(parsed.get("gc").map(GC(_)).getOrElse(Discover.GC()))
      .withMode(parsed.get("mode").map(Mode(_)).getOrElse(Discover.mode()))
      .withLTO(parsed.get("lto").map(LTO(_)).getOrElse(Discover.LTO()))
      .withOptimize(
        if (parsed.flag("no-optimize")) false else Discover.optimize()
      )
      .withLinkStubs(parsed.flag("link-stubs"))
      .withCheck(parsed.flag("check"))
      .withCheckFatalWarnings(parsed.flag("check-fatal-warnings"))
      .withDump(parsed.flag("dump"))
      .withEmbedResources(parsed.flag("embed-resources"))
      .withIncrementalCompilation(!parsed.flag("no-incremental"))

    parsed.get("build-target").foreach { bt =>
      nativeConfig = nativeConfig.withBuildTarget(parseBuildTarget(bt))
    }

    parsed.get("target-triple").foreach { triple =>
      nativeConfig = nativeConfig.withTargetTriple(triple)
    }

    parsed.get("sanitizer").foreach { san =>
      nativeConfig = nativeConfig.withSanitizer(parseSanitizer(san))
    }

    if (parsed.flag("multithreading"))
      nativeConfig = nativeConfig.withMultithreading(true)
    else if (parsed.flag("no-multithreading"))
      nativeConfig = nativeConfig.withMultithreading(false)

    parsed.get("base-name").foreach { name =>
      nativeConfig = nativeConfig.withBaseName(name)
    }

    val moduleName = parsed.get("module-name").getOrElse("out")

    val config = Config.empty
      .withBaseDir(baseDir)
      .withModuleName(moduleName)
      .withClassPath(classpathPaths)
      .withSourcesClassPath(sourcesClassPath)
      .withMainClass(Some(mainClass))
      .withTestConfig(parsed.flag("test-config"))
      .withCompilerConfig(nativeConfig)
      .withLogger(logger)

    val useCached = parsed.flag("cached")

    Scope { implicit scope =>
      try {
        // buildCachedAwait handles its own thread pool and execution context
        val artifact =
          if (useCached) Build.buildCachedAwait(config)
          else Build.buildAwait(config)
        logger.info(s"Native binary: $artifact")
      } catch {
        case e: BuildException =>
          System.err.println(s"Build failed: ${e.getMessage}")
          sys.exit(1)
        case e: linker.LinkingException =>
          System.err.println(s"Linking failed: ${e.getMessage}")
          sys.exit(1)
        case e: Exception =>
          System.err.println(s"Unexpected error: ${e.getMessage}")
          e.printStackTrace(System.err)
          sys.exit(2)
      }
    }
  }

  /** Minimal logger that only prints info, warn and error messages (no debug
   *  output).
   */
  private val infoLogger: Logger = Logger(
    traceFn = _ => (),
    debugFn = _ => (),
    infoFn = msg => System.out.println(s"[info] $msg"),
    warnFn = msg => System.err.println(s"[warn] $msg"),
    errorFn = msg => System.err.println(s"[error] $msg")
  )

  /** Derive user classpath entries from the base directory.
   *
   *  Convention (matching sbt crossTarget layout):
   *  {{{
   *    baseDir/
   *      classes/           → user-compiled NIR files
   *      test-classes/      → test NIR files (optional, when --test-config)
   *  }}}
   *
   *  If baseDir itself contains `.nir` files (test-style layout where outDir ==
   *  baseDir), it is included directly instead of `classes/`.
   *
   *  Note: `native/dependencies/` is NOT included here. The build pipeline
   *  extracts dependency NIR from the JARs on the classpath automatically.
   */
  private def discoverClasspath(
      baseDir: Path,
      includeTest: Boolean
  ): Seq[Path] = {
    val classesDir = baseDir.resolve("classes")
    val testClassesDir = baseDir.resolve("test-classes")

    val primary =
      if (Files.isDirectory(classesDir)) classesDir
      else baseDir // outDir == baseDir (test-style layout)

    if (includeTest && Files.isDirectory(testClassesDir))
      Seq(primary, testClassesDir)
    else Seq(primary)
  }

  private def parseBuildTarget(value: String): BuildTarget = value match {
    case "application"     => BuildTarget.application
    case "library-dynamic" => BuildTarget.libraryDynamic
    case "library-static"  => BuildTarget.libraryStatic
    case _                 =>
      throw new IllegalArgumentException(
        s"Unknown build target: '$value'. Use: application, library-dynamic, library-static"
      )
  }

  private def parseSanitizer(value: String): Sanitizer = value match {
    case "address"   => Sanitizer.AddressSanitizer
    case "thread"    => Sanitizer.ThreadSanitizer
    case "undefined" => Sanitizer.UndefinedBehaviourSanitizer
    case _           =>
      throw new IllegalArgumentException(
        s"Unknown sanitizer: '$value'. Use: address, thread, undefined"
      )
  }

  // ── Argument parsing ─────────────────────────────────────────────────

  private case class ParsedArgs(
      map: Map[String, String],
      flags: Set[String]
  ) {
    def get(key: String): Option[String] = map.get(key)

    def getOrError(key: String, message: String): String =
      map.getOrElse(key, { printUsageAndExit(message); "" })

    def flag(key: String): Boolean = flags.contains(key)
  }

  private val knownFlags: Set[String] = Set(
    "test-config",
    "no-optimize",
    "link-stubs",
    "multithreading",
    "no-multithreading",
    "embed-resources",
    "incremental",
    "no-incremental",
    "check",
    "check-fatal-warnings",
    "dump",
    "verbose",
    "silent",
    "cached",
    "help"
  )

  private val knownOptions: Set[String] = Set(
    "classpath",
    "main-class",
    "base-dir",
    "module-name",
    "base-name",
    "clang",
    "clangpp",
    "gc",
    "mode",
    "lto",
    "build-target",
    "target-triple",
    "sanitizer",
    "compile-options",
    "linking-options",
    "sources-classpath"
  )

  private def parseArgs(args: List[String]): ParsedArgs = {
    var map = Map.empty[String, String]
    var flags = Set.empty[String]
    var remaining = args

    while (remaining.nonEmpty) {
      remaining match {
        case "--help" :: _ =>
          printUsageAndExit(null)

        case arg :: value :: tail
            if arg.startsWith("--") && !arg.contains("=") &&
              knownOptions.contains(arg.substring(2)) =>
          map = map + (arg.substring(2) -> value)
          remaining = tail

        case arg :: tail
            if arg.startsWith("--") && !arg.contains("=") &&
              knownFlags.contains(arg.substring(2)) =>
          flags = flags + arg.substring(2)
          remaining = tail

        // Support --key=value syntax
        case arg :: tail if arg.startsWith("--") && arg.contains("=") =>
          val eqIdx = arg.indexOf('=')
          val key = arg.substring(2, eqIdx)
          val value = arg.substring(eqIdx + 1)
          if (knownOptions.contains(key)) {
            map = map + (key -> value)
          } else {
            printUsageAndExit(s"Unknown option: --$key")
          }
          remaining = tail

        // Support -cp as alias for --classpath
        case arg :: value :: tail if arg == "-cp" || arg == "-classpath" =>
          map = map + ("classpath" -> value)
          remaining = tail

        case unknown :: _ =>
          printUsageAndExit(s"Unknown argument: $unknown")

        case Nil => // guarded by while condition, unreachable
      }
    }

    ParsedArgs(map, flags)
  }

  private def printUsageAndExit(errorMessage: String): Nothing = {
    if (errorMessage != null) {
      System.err.println(s"Error: $errorMessage")
      System.err.println()
    }
    val usage =
      """|Usage: scala-native-build [options]
         |
         |Runs the lower part of the Scala Native compilation pipeline:
         |NIR linking → optimization → LLVM IR codegen → native compilation → system linking
         |
         |By default the classpath includes the Scala Native runtime JARs
         |(scalalib, javalib, etc.) from BuildInfo, plus the base-dir.
         |
         |Required:
         |  --main-class <class>          Fully qualified main class name
         |  --base-dir <dir>              Base output directory (crossTarget)
         |
         |Classpath:
         |  --classpath, -cp <paths>      Override the entire NIR class path
         |
         |General:
         |  --module-name <name>          Module name (default: "out")
         |  --base-name <name>            Base name for the output artifact
         |  --test-config                 Enable test configuration
         |  --cached                      Skip build if inputs unchanged
         |  --help                        Show this help message
         |
         |Native configuration:
         |  --clang <path>                Path to clang
         |  --clangpp <path>              Path to clang++
         |  --gc <name>                   GC: none|boehm|immix|commix|experimental (default: immix)
         |  --mode <name>                 Mode: debug|release-fast|release-size|release-full (default: debug)
         |  --lto <name>                  LTO: none|thin|full (default: none)
         |  --build-target <name>         Target: application|library-dynamic|library-static
         |  --target-triple <triple>      LLVM target triple
         |  --sanitizer <name>            Sanitizer: address|thread|undefined
         |
         |Compilation flags:
         |  --no-optimize                 Disable optimizations
         |  --link-stubs                  Enable linking of stubs
         |  --multithreading              Enable multithreading support
         |  --no-multithreading           Disable multithreading support
         |  --embed-resources             Embed resources into the binary
         |  --no-incremental              Disable incremental compilation
         |  --check                       Enable NIR check
         |  --check-fatal-warnings        Treat check warnings as errors
         |  --dump                        Dump NIR
         |
         |Compiler options:
         |  --compile-options <opts>      Additional compile options (comma-separated)
         |  --linking-options <opts>      Additional linking options (comma-separated)
         |  --sources-classpath <paths>   Source jars class path (colon/semicolon-separated)
         |
         |Logging:
         |  --verbose                     Enable verbose (debug) logging
         |  --silent                      Suppress all logging
         |""".stripMargin
    System.err.println(usage)
    sys.exit(if (errorMessage != null) 1 else 0)
  }
}
