package scala.scalanative.build

object Validator {
  def validate(config: Config): Config = {
    println("In validate")
    validateMainClass(config) // can throw
    validateClasspath(config)
  }

  private def validateMainClass(config: Config): Unit = {
    val nativeConfig = config.compilerConfig
    nativeConfig.buildTarget match {
      case BuildTarget.Application =>
        if (config.mainClass.isEmpty) {
          throw new BuildException("No main class detected.")
        }
      case _: BuildTarget.Library => ()
    }
  }

  private def validateClasspath(config: Config): Config = {
    val fclasspath = NativeLib.filterClasspath(config.classPath)
    config.withClassPath(fclasspath)
  }
}
