package scala.scalanative.build

import java.nio.file.Files

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
    (validateMainClass _)
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
    if (!Files.exists(c.clangPP))
      issues += s"Provided clang++ path '${c.clangPP.toAbsolutePath()}' does not exist, specify a valid path to LLVM Toolchain distribution using config or LLVM_BIN environment variable"
    // config.baseName provides default value when config.compileConfig.baseName is empty
    if (config.baseName.trim().isEmpty())
      issues += s"Provided baseName is blank, provide a name of target artifact without extensions to allow for determinstic builds"

    if (config.targetsMac && c.lto == LTO.thin)
      warn(
        "LTO.thin is unstable on MacOS, it can lead to compilation errors. Consider using LTO.full (legacy, slower) or LTO.none (disabled)"
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

}
