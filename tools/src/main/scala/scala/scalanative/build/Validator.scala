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
  def validate(config: Config): Config = {
    // side effecting
    validateMainClass(config)
    validateBasename(config)
    // returns new Config
    validateClasspath(config)
  }

  // throws if Application with no mainClass
  private def validateMainClass(config: Config): Unit = {
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
  }

  // throws if moduleName or basename is not set
  private def validateBasename(config: Config): Unit =
    if (config.basename.trim.isEmpty) { // trim for non default error
      throw new BuildException(
        "Config defaultBasename or NativeConfig basename must be set."
      )
    }

  // filter so classpath only has jars or directories
  private def validateClasspath(config: Config): Config = {
    val fclasspath = NativeLib.filterClasspath(config.classPath)
    config.withClassPath(fclasspath)
  }
}
