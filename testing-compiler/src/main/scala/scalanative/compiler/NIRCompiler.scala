package scala.scalanative

import scala.reflect.internal.util.{BatchSourceFile, NoFile, SourceFile}
import scala.reflect.internal.util.Position
import scala.tools.cmd.CommandLineParser
import scala.tools.nsc.{CompilerCommand, Global, Settings}
import scala.tools.nsc.io.AbstractFile
import java.nio.file.{Files, Path}
import java.io.File
import scala.scalanative.compiler.CompatReporter

/**
 * Helper class to compile snippets of code.
 */
class NIRCompiler(outputDir: Path) extends api.NIRCompiler {

  def this() = this(Files.createTempDirectory("scala-native-target"))

  override def compile(code: String): Array[Path] = {
    val source = new BatchSourceFile(NoFile, code)
    compile(Seq(source)).toArray
  }

  override def compile(base: Path): Array[Path] = {
    val sources = getFiles(base.toFile, _.getName endsWith ".scala")
    val sourceFiles = sources map { s =>
      val abstractFile = AbstractFile.getFile(s)
      new BatchSourceFile(abstractFile)
    }
    compile(sourceFiles).toArray
  }

  override def compileAndReport(code: String): Array[api.CompilerError] = {

    val (global, reporter) =
      getCompilerWithReporter(ReportedErrors(_), options = ScalaNative)
    val source = new BatchSourceFile(NoFile, code)
    import global._
    val run = new Run
    run.compileSources(List(source))
    reporter.errors.toArray
  }

  private def compile(sources: Seq[SourceFile]): Seq[Path] = {
    val global = getCompiler(options = ScalaNative)
    import global._
    val run = new Run
    run.compileSources(sources.toList)
    getFiles(outputDir.toFile, _ => true).map(_.toPath)
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

  private abstract class BaseReporter(override val settings: Settings)
      extends CompatReporter {
    override def display(pos: Position, msg: String, severity: Severity): Unit =
      severity match {
        case INFO | WARNING => ()
        case ERROR          => handleError(pos, msg)
      }

    def handleError(pos: Position, msg: String): Unit

    override def displayPrompt(): Unit = ()
  }

  /**
   * Reporter that ignores INFOs and WARNINGs, but directly aborts the compilation
   * on ERRORs.
   */
  private class TestReporter(override val settings: Settings)
      extends BaseReporter(settings) {
    def handleError(pos: Position, msg: String): Unit = reportError(msg)
  }

  object ReportedErrors {
    def apply(settings: Settings): AbstractReporter with ReportedErrors =
      new StoreErrorsReporter(settings)

    private class StoreErrorsReporter(override val settings: Settings)
        extends BaseReporter(settings)
        with ReportedErrors {

      private var errors0: List[(Position, String)] = Nil
      def handleError(pos: Position, msg: String): Unit =
        errors0 = (pos, msg) :: errors0
      def errors: List[api.CompilerError] = errors0.map {
        case (p, msg) => CompilerError(p.startOrPoint, msg)
      }

    }

    private case class CompilerError(pos: Int, msg: String)
        extends api.CompilerError {
      override def getPosition: Integer = pos

      override def getErrorMsg: String = msg

      override def equals(obj: Any): Boolean = obj match {
        case err: api.CompilerError =>
          (err.getErrorMsg == getErrorMsg) &&
            (err.getPosition == getPosition)
        case _ =>
          false
      }
    }
  }

  /**
   * Interface for returning encountered errors
   */
  trait ReportedErrors {
    def errors: List[api.CompilerError]
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
  private class CompilerPlugin(val jarPath: String, val classpath: List[String])
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
                               List(sys props "scalanative.nativeruntime.cp"))

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
    val outPath = outputDir.toAbsolutePath
    val arguments =
      CommandLineParser.tokenize(s"-d $outPath " + (options mkString " "))
    val command  = new CompilerCommand(arguments.toList, reportError _)
    val reporter = new TestReporter(command.settings)

    new Global(command.settings, reporter)
  }

  /**
   * Returns an instance of `Global` configured according to the given options,
   * along with the reporter that will be used during the compilation.
   */
  private def getCompilerWithReporter(
      f: Settings => AbstractReporter with ReportedErrors,
      options: CompilerOption*): (Global, ReportedErrors) = {
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
    val reporter = f(command.settings)
    (new Global(command.settings, reporter), reporter)
  }

}
