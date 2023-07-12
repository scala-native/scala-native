package scala.scalanative

import java.io._
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

/** Helper class to compile snippets of code.
 */
class NIRCompilerImpl(outputDir: Path) extends api.NIRCompiler {

  def this() = this(Files.createTempDirectory("scala-native-target"))

  override def compile(code: String): Array[Path] = ???

  override def compile(base: Path): Array[Path] = ???

  private def compile(sources: Seq[Path]): Seq[Path] = {
    val outPath = outputDir.toAbsolutePath
    val jarPath = sys.props("scalanative.nscplugin.jar")
    val classpath = List(sys.props("scalanative.nativeruntime.cp"))
      .filterNot(_.isEmpty)
      .mkString("-cp ", File.pathSeparator, "")

    val arguments = Seq(
      "-d",
      outPath.toAbsolutePath().toString(),
      s"-Xplugin:$jarPath",
      classpath
    )

    val args = arguments ++ sources.map(_.toAbsolutePath().toString())
    // TOOD: How to actually run scalac with correct classpath and retrive error messages?
    val res = ???
    val allErrors: Seq[String] = ???
    allErrors.headOption.foreach { error =>
      throw new api.CompilationFailedException(error)
    }

    // Remove .tasty files from list of output files - our tests expects only
    // .nir and .class fiels
    getFiles(outputDir.toFile, !_.getName.endsWith(".tasty")).map(_.toPath)
  }

  /** List of the files contained in `base` that sastisfy `filter`
   */
  private def getFiles(base: File, filter: File => Boolean): Seq[File] = {
    Seq(base).filter(filter) ++ Option(base.listFiles)
      .map(_.toList)
      .getOrElse(Nil)
      .flatMap(getFiles(_, filter))
  }

  private def reportError(error: String) =
    throw new api.CompilationFailedException(error)
}
