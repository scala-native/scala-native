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

    if (c.multithreadingSupport) c.gc match {
      case GC.Commix =>
        issues += "CommixGC does not support multithreading yet, use other GC implementation (Immix, Boehm)"
      case _ => ()
    }
    if (!Files.exists(c.clang))
      issues += s"Provided clang path '${c.clang.toAbsolutePath()}' does not exist, specify a valid path to LLVM Toolchain distribution using config or LLVM_BIN environment variable"
    if (!Files.exists(c.clangPP))
      issues += s"Provided clang++ path '${c.clangPP.toAbsolutePath()}' does not exist, specify a valid path to LLVM Toolchain distribution using config or LLVM_BIN environment variable"
    if (c.baseName.trim().isEmpty())
      issues += s"Provided baseName is blank, provide a name of target artifact without extensions to allow for determinstic builds"

    issues.result() match {
      case Nil => config
      case issues =>
        throw new BuildException(
          (s"Found ${issues.size} issue within provided confguration: " :: issues)
            .mkString("\n  - ")
        )
    }
  }

}
