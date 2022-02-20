package scala.scalanative

import java.io._
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets
import dotty.tools.io.AbstractFile
import dotty.tools.dotc._
import dotty.tools.dotc.util._
import dotty.tools.dotc.reporting._
import dotty.tools.dotc.config._
import dotty.tools.dotc.core.Contexts._

/** Helper class to compile snippets of code.
 */
class NIRCompiler(outputDir: Path) extends api.NIRCompiler {

  def this() = this(Files.createTempDirectory("scala-native-target"))

  override def compile(code: String): Array[Path] = {
    val file = AbstractFile.getFile(
      File.createTempFile("scala-native-input", ".scala").toPath
    )
    val output = file.bufferedOutput
    output.write(code.getBytes(StandardCharsets.UTF_8))
    output.close()
    val source = SourceFile(file, io.Codec.UTF8)
    compile(Seq(source)).toArray
  }

  override def compile(base: Path): Array[Path] = {
    val sources = getFiles(base.toFile, _.getName.endsWith(".scala"))
    val sourceFiles = sources.map { s =>
      val abstractFile = AbstractFile.getFile(s.toPath)
      SourceFile(abstractFile, io.Codec.default)
    }
    compile(sourceFiles).toArray
  }

  private def compile(sources: Seq[SourceFile]): Seq[Path] = {
    val outPath = outputDir.toAbsolutePath
    val jarPath = sys.props("scalanative.nscplugin.jar")
    val classpath = List(sys.props("scalanative.nativeruntime.cp"))
      .filterNot(_.isEmpty)
      .mkString("-cp ", File.pathSeparator, "")

    val arguments = CommandLineParser
      .tokenize(
        s"-d $outPath -Xplugin:$jarPath $classpath"
      )

    val args = arguments ++ sources.map(_.file.absolutePath)
    val res = Driver().process(args.toArray, TestReporter(), null)
    res.allErrors.headOption.foreach { error =>
      throw api.CompilationFailedException(error.message)
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

  /** Reporter that ignores INFOs and WARNINGs, but directly aborts the
   *  compilation on ERRORs.
   */
  private class TestReporter() extends AbstractReporter {
    override def doReport(diagnostics: Diagnostic)(using Context): Unit = {
      import dotty.tools.dotc.interfaces.Diagnostic.ERROR
      diagnostics.level match {
        case ERROR => report(diagnostics)
        case _     => ()
      }
    }
  }
}
