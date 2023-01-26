package scala.scalanative.build

import java.nio.file.Files

object Validator {
  def validate(config: Config): Config = {
    validateMainClass(config) // side effecting
    // returns Config
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

  // filter so classpath only has jars or directories
  private def validateClasspath(config: Config): Config = {
    val fclasspath = NativeLib.filterClasspath(config.classPath)
    config.withClassPath(fclasspath)
  }
}
