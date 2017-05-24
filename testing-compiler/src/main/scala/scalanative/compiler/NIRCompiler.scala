package scala.scalanative

import scala.reflect.internal.util.{BatchSourceFile, NoFile, SourceFile}
import scala.reflect.internal.util.Position

import scala.tools.cmd.CommandLineParser
import scala.tools.nsc.{CompilerCommand, Global, Settings}
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.reporters.AbstractReporter

import java.nio.file.Files
import java.io.File

/**
 * Helper class to compile snippets of code.
 */
class NIRCompiler(outputDir: File) extends api.NIRCompiler {

  def this() = this(Files.createTempDirectory("scala-native-target").toFile())

  override def compile(code: String): Array[File] = {
    val source = new BatchSourceFile(NoFile, code)
    compile(Seq(source)).toArray
  }

  override def compile(base: File): Array[File] = {
    val sources = getFiles(base, _.getName endsWith ".scala")
    val sourceFiles = sources map { s =>
      val abstractFile = AbstractFile.getFile(s)
      new BatchSourceFile(abstractFile)
    }
    compile(sourceFiles).toArray
  }

  private def compile(sources: Seq[SourceFile]): Seq[File] = {
    val global = getCompiler(options = ScalaNative)
    import global._
    val run = new Run
    run.compileSources(sources.toList)
    getFiles(outputDir, _ => true)
  }

  /**
   * List of the files contained in `base` that sastisfy `filter`
   */
  private def getFiles(base: File, filter: File => Boolean): Seq[File] =
    (if (filter(base)) Seq(base) else Seq()) ++
      (Option(base.listFiles()) getOrElse Array.empty flatMap (getFiles(
        _,
        filter)))

  private def reportError(error: String) =
    throw new api.CompilationFailedException(error)

  /**
   * Reporter that ignores INFOs and WARNINGs, but directly aborts the compilation
   * on ERRORs.
   */
  private class TestReporter(override val settings: Settings)
      extends AbstractReporter {
    override def display(pos: Position,
                         msg: String,
                         severity: Severity): Unit = severity match {
      case INFO | WARNING => ()
      case ERROR          => reportError(msg)
    }

    override def displayPrompt(): Unit = ()
  }

  /**
   * Represents a basic compiler option (the string given to the command line invocation
   * of scalac)
   */
  private implicit class CompilerOption(s: String) {
    override def toString: String = s
  }

  /**
   * An option to add a compiler plugin
   */
  private class CompilerPlugin(val jarPath: String,
                               val classpath: List[String])
      extends CompilerOption(
        s"-Xplugin:$jarPath" + (if (classpath.nonEmpty)
                                  classpath
                                    .mkString(" -cp ", File.pathSeparator, "")
                                else ""))

  /**
   * Option to add the scala-native compiler plugin
   */
  private case object ScalaNative
      extends CompilerPlugin(jarPath = sys props "scalanative.nscplugin.jar",
                             classpath =
                               List(sys props "scalanative.testingcompiler.cp",
                                    sys props "scalanative.nscplugin.jar"))

  /**
   * Returns an instance of `Global` configured according to the given options.
   */
  private def getCompiler(options: CompilerOption*): Global = {
    // I don't really know how I can reset the compiler after a run, nor what else
    // should also be reset, so for now this method creates new instances of everything,
    // which is not so cool.
    //
    // Also, using `command.settings.outputDirs.setSingleOutput` I get strange classpath problems.
    // What's even stranger, is that everything works fine using `-d`!
    val outPath = outputDir.getAbsolutePath
    val arguments =
      CommandLineParser.tokenize(s"-d $outPath " + (options mkString " "))
    val command  = new CompilerCommand(arguments.toList, reportError _)
    val reporter = new TestReporter(command.settings)

    new Global(command.settings, reporter)
  }

}
