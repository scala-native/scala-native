// Due to enums source-compatibility reasons `ProcessBuilder` was split into two
// seperate files. `ProcessBuilder` contains constructors and Scala version specific
// definition of enums. `ProcessBuilderImpl` defines actual logic of ProcessBuilder
// that should be shared between both implementations

package java.lang.process

import java.util.List
import java.util.Map
import java.io.File
import java.util
import java.util.Arrays
import scala.scalanative.meta.LinktimeInfo.isWindows
import ProcessBuilder.Redirect

private[lang] class ProcessBuilderImpl(private var _command: List[String]) {
  self: java.lang.ProcessBuilder =>

  def command(): List[String] = _command

  def command(command: Array[String]): ProcessBuilder =
    set { _command = Arrays.asList(command) }

  def command(command: List[String]): ProcessBuilder = set {
    _command = command
  }

  def environment(): Map[String, String] = _environment

  def directory(): File = _directory

  def directory(dir: File): ProcessBuilder =
    set {
      _directory = dir match {
        case null => defaultDirectory
        case _    => dir
      }
    }

  def inheritIO(): ProcessBuilder = {
    redirectInput(Redirect.INHERIT)
    redirectOutput(Redirect.INHERIT)
    redirectError(Redirect.INHERIT)
  }

  def redirectError(destination: Redirect): ProcessBuilder = destination match {
    case null => set { _redirectOutput = Redirect.PIPE }
    case d =>
      d.`type`() match {
        case Redirect.Type.READ =>
          throw new IllegalArgumentException(
            s"Redirect.READ cannot be used for error."
          )
        case _ =>
          set { _redirectError = destination }
      }
  }

  def redirectInput(source: Redirect): ProcessBuilder = source match {
    case null => set { _redirectInput = Redirect.PIPE }
    case s =>
      s.`type`() match {
        case Redirect.Type.WRITE | Redirect.Type.APPEND =>
          throw new IllegalArgumentException(s"$s cannot be used for input.")
        case _ =>
          set { _redirectInput = source }
      }
  }

  def redirectOutput(destination: Redirect): ProcessBuilder =
    destination match {
      case null => set { _redirectOutput = Redirect.PIPE }
      case s =>
        s.`type`() match {
          case Redirect.Type.READ =>
            throw new IllegalArgumentException(
              s"Redirect.READ cannot be used for output."
            )
          case _ =>
            set { _redirectOutput = destination }
        }
    }

  def redirectInput(file: File): ProcessBuilder = {
    redirectInput(Redirect.from(file))
  }

  def redirectOutput(file: File): ProcessBuilder = {
    redirectOutput(Redirect.to(file))
  }

  def redirectError(file: File): ProcessBuilder = {
    redirectError(Redirect.to(file))
  }

  def redirectInput(): Redirect = _redirectInput

  def redirectOutput(): Redirect = _redirectOutput

  def redirectError(): Redirect = _redirectError

  def redirectErrorStream(): scala.Boolean = _redirectErrorStream

  def redirectErrorStream(redirectErrorStream: scala.Boolean): ProcessBuilder =
    set { _redirectErrorStream = redirectErrorStream }

  def start(): Process = {
    if (_command.isEmpty()) throw new IndexOutOfBoundsException()
    if (_command.contains(null)) throw new NullPointerException()
    if (isWindows) process.WindowsProcess(this)
    else process.UnixProcess(this)
  }

  @inline private[this] def set(f: => Unit): ProcessBuilder = {
    f
    this
  }
  private def defaultDirectory = System.getenv("user.dir") match {
    case null => new File(".")
    case f    => new File(f)
  }
  private var _directory = defaultDirectory
  private val _environment = {
    val env = System.getenv()
    new java.util.HashMap[String, String](env)
  }
  private var _redirectInput = Redirect.PIPE
  private var _redirectOutput = Redirect.PIPE
  private var _redirectError = Redirect.PIPE
  private var _redirectErrorStream = false
}
