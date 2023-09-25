package scala.scalanative.build

import java.nio.file.Files

/** Used to validate config objects */
object Validator {

  /** Runs all the individual private validators
   *
   *  @param config
   *    the pre-validation original [[Config]]
   *  @return
   *    potentially a modified [[Config]] that is valid
   */
  def validate(config: Config): Config =
    (validateMainClass _)
      .andThen(validateBasename)
      .andThen(validateClasspath)
      .andThen(checkCompilerConfig)
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

  // throws if moduleName or baseName is not set
  private def validateBasename(config: Config): Config =
    if (config.baseName.trim.isEmpty) { // trim for non default error
      throw new BuildException(
        "Config defaultBasename or NativeConfig baseName must be set."
      )
    } else config

  // filter so classpath only has jars or directories
  private def validateClasspath(config: Config): Config = {
    val fclasspath = NativeLib.filterClasspath(config.classPath)
    config.withClassPath(fclasspath)
  }

  private def checkCompilerConfig(config: Config): Config = {
    val log = config.logger
    (config.compilerConfig.mode, config.compilerConfig.lto) match {
      case (mode: Mode.Release, LTO.None) =>
        log.warn(
          s"Build mode ${mode.name} combined with disabled LTO produces single, slow to compile output file." +
            s" It's recommended to enable LTO.${LTO.thin.name} for best optimizations and faster builds."
        )
      case _ => ()
    }
    config
  }

}
