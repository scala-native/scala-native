package scala.scalanative.build

import java.nio.file.Files

object Validator {
  def validate(config: Config): Config = {
    println("In validate")
    validateMainClass(config)
    validateWorkdirExists(config)
    validateClasspath(config)
  }

  // throws if Application with no mainClass
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

  // validate that workdir exists - can throw exception
  private def validateWorkdirExists(config: Config): Unit = {
    // create workdir if needed
    if (Files.notExists(config.workdir)) {
      Files.createDirectories(config.workdir)
    }
  }

  // filter so classpath only has jars or directories
  private def validateClasspath(config: Config): Config = {
    val fclasspath = NativeLib.filterClasspath(config.classPath)
    config.withClassPath(fclasspath)
  }
}
