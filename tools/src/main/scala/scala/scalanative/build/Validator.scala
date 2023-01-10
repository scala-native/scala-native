package scala.scalanative.build

object Validator {
  def validate(config: Config): Config = {
    println("In validate")
    validateMain(config)
    config
  }

  private def validateMain(config: Config): Unit = {
    val nativeConfig = config.compilerConfig
    nativeConfig.buildTarget match {
      case BuildTarget.Application =>
        if (config.mainClass.isEmpty) {
          throw new BuildException("No main class detected.")
        }
      case _: BuildTarget.Library => ()
    }
  }
}
