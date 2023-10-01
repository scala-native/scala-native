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

}
