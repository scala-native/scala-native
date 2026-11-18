package scala.scalanative.build

import java.nio.file.{Files, Path, Paths}

import scala.util.Try

/** Used to validate config objects */
private[build] object Validator {

  /** Runs all the individual private validators
   *
   *  @param config
   *    the pre-validation original [[Config]]
   *  @return
   *    potentially a modified [[Config]] that is valid
   */
  def validate(config: Config): Config =
    (validateMainClass(_))
      .andThen(validateClasspath)
      .andThen(validateCompileConfig)
      .apply(config)

  // throws if Application with no mainClass
  private def validateMainClass(config: Config): Config = {
    val nativeConfig = config.compilerConfig
    nativeConfig.buildTarget match {
      case BuildTarget.Application =>
        if (config.mainClass.isEmpty) {
          throw new BuildException(
            "No main class detected with Application selected."
          )
        }
      case _: BuildTarget.Library => ()
    }
    config
  }

  // filter so classpath only has jars or directories
  private def validateClasspath(config: Config): Config = {
    val fclasspath = NativeLib.filterClasspath(config.classPath)
    config.withClassPath(fclasspath)
  }

  private def validateCompileConfig(config: Config): Config = {
    val c = config.compilerConfig
    val issues = List.newBuilder[String]
    def warn(msg: String) = config.logger.warn(msg)

    if (!Files.exists(c.clang))
      issues += s"Provided clang path '${c.clang.toAbsolutePath()}' does not exist, specify a valid path to LLVM Toolchain distribution using config or LLVM_BIN environment variable"
    else {
      Discover.checkClangVersion(c.clang, config.logger)
      mismatchedLlvmDirs(c.clang, c.compileOptions ++ c.linkingOptions)
        .foreach {
          case (option, llvmPrefix) =>
            warn(
              s"Option '$option' points to the LLVM installation at '$llvmPrefix', which does not provide the configured clang '${c.clang}'. Its headers or libraries may not match the compiler."
            )
        }
    }
    if (!Files.exists(c.clangPP))
      issues += s"Provided clang++ path '${c.clangPP.toAbsolutePath()}' does not exist, specify a valid path to LLVM Toolchain distribution using config or LLVM_BIN environment variable"
    // config.baseName provides default value when config.compileConfig.baseName is empty
    if (config.baseName.trim().isEmpty())
      issues += s"Provided baseName is blank, provide a name of target artifact without extensions to allow for determinstic builds"

    if (config.targetsApple && c.lto == LTO.thin)
      warn(
        "LTO.thin is unstable on MacOS, it can lead to compilation errors. Consider using LTO.full (legacy, slower) or LTO.none (disabled)"
      )

    val gcConfig = GarbageCollectorConfig.Settings.resolve(c)
    gcConfig.warnings.foreach(warn)
    issues ++= gcConfig.issues.map("Garbage collector setting " + _)

    for {
      sdk <- config.appleSdkName
      if !sys.env.get("SDKROOT").exists(_.nonEmpty)
      if !c.compileOptions.exists(_.contains("-isysroot"))
    } warn(
      s"Target ${c.targetTriple.mkString} requires the $sdk SDK, but no sysroot is set. " +
        "Add `-isysroot <path>` to compile and linking options or set SDKROOT environment variable, " +
        s"the path can be found using `xcrun --sdk $sdk --show-sdk-path`"
    )

    issues.result() match {
      case Nil    => config
      case issues =>
        throw new BuildException(
          (s"Found ${issues.size} issue within provided confguration: " :: issues)
            .mkString("\n  - ")
        )
    }
  }

  /** Finds `-I`/`-L` options pointing into an LLVM installation other than the
   *  one providing `clang`. Returns pairs of option and LLVM prefix.
   */
  private[build] def mismatchedLlvmDirs(
      clang: Path,
      options: Seq[String]
  ): Seq[(String, Path)] = {
    def realPath(p: Path) = Try(p.toRealPath()).toOption
    val configuredClang = realPath(clang)
    def isLlvmPrefix(prefix: Path) =
      Seq("llvm-config", "llvm-config.exe")
        .exists(n => Files.isRegularFile(prefix.resolve("bin").resolve(n)))
    def providesClang(prefix: Path) =
      Seq("clang", "clang.exe").exists { n =>
        val candidate = realPath(prefix.resolve("bin").resolve(n))
        candidate.isDefined && candidate == configuredClang
      }
    for {
      option <- options
      if option.startsWith("-I") || option.startsWith("-L")
      dir <- Try(Paths.get(option.drop(2))).toOption
      prefix <- Option(dir.toAbsolutePath().getParent())
      if isLlvmPrefix(prefix) && !providesClang(prefix)
    } yield (option, prefix)
  }
}
