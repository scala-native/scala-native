package java.lang

import java.util.{ArrayList, List}
import java.util.Map
import java.io.{File, IOException}
import java.util
import java.util.Arrays
import scala.scalanative.unsafe._
import scala.scalanative.posix.unistd
import scala.scalanative.runtime.Platform
import ProcessBuilder.Redirect

final class ProcessBuilder(private var _command: List[String]) {
  def this(command: Array[String]) {
    this(Arrays.asList(command))
  }

  def command(): List[String] = _command

  def command(command: Array[String]): ProcessBuilder =
    set(_command = Arrays.asList(command))

  def command(command: List[String]): ProcessBuilder = set(_command = command)

  def environment(): Map[String, String] = _environment

  def directory(): File = _directory

  def directory(dir: File): ProcessBuilder =
    set(_directory = dir match {
      case null => defaultDirectory
      case _    => dir
    })

  def inheritIO(): ProcessBuilder = {
    redirectInput(Redirect.INHERIT)
    redirectOutput(Redirect.INHERIT)
    redirectError(Redirect.INHERIT)
  }

  def redirectError(destination: Redirect): ProcessBuilder = destination match {
    case null => set(_redirectOutput = Redirect.PIPE)
    case d =>
      d.`type` match {
        case Redirect.Type.READ =>
          throw new IllegalArgumentException(
            s"Redirect.READ cannot be used for error.")
        case _ =>
          set(_redirectError = destination)
      }
  }

  def redirectInput(source: Redirect): ProcessBuilder = source match {
    case null => set(_redirectInput = Redirect.PIPE)
    case s =>
      s.`type` match {
        case Redirect.Type.WRITE | Redirect.Type.APPEND =>
          throw new IllegalArgumentException(s"$s cannot be used for input.")
        case _ =>
          set(_redirectInput = source)
      }
  }

  def redirectOutput(destination: Redirect): ProcessBuilder =
    destination match {
      case null => set(_redirectOutput = Redirect.PIPE)
      case s =>
        s.`type` match {
          case Redirect.Type.READ =>
            throw new IllegalArgumentException(
              s"Redirect.READ cannot be used for output.")
          case _ =>
            set(_redirectOutput = destination)
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
    set(_redirectErrorStream = redirectErrorStream)

  def start(): Process = {
    if (_command.isEmpty()) throw new IndexOutOfBoundsException()
    if (_command.contains(null)) throw new NullPointerException()
    if (Platform.isWindows) {
      val msg = "No windows implementation of java.lang.Process"
      throw new UnsupportedOperationException(msg)
    } else {
      UnixProcess(this)
    }
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
    import scala.collection.JavaConverters._
    val env = System.getenv
    val res = new java.util.HashMap[String, String]
    env.asScala foreach { case (k, v) => res.put(k, v) }
    res
  }
  private var _redirectInput       = Redirect.PIPE
  private var _redirectOutput      = Redirect.PIPE
  private var _redirectError       = Redirect.PIPE
  private var _redirectErrorStream = false
}

object ProcessBuilder {
  abstract class Redirect {
    def file(): File = null

    def `type`(): Redirect.Type

    override def equals(other: Any): scala.Boolean = other match {
      case that: Redirect => file == that.file && `type` == that.`type`
      case _              => false
    }

    override def hashCode(): Int = {
      var hash = 1
      hash = hash * 31 + file.hashCode()
      hash = hash * 31 + `type`.hashCode()
      hash
    }
  }

  object Redirect {
    private class RedirectImpl(tpe: Redirect.Type, file: File)
        extends Redirect {
      override def `type`(): Type = tpe

      override def file(): File = file

      override def toString =
        s"Redirect.$tpe${if (file != null) s": ${file}" else ""}"
    }

    val INHERIT: Redirect = new RedirectImpl(Type.INHERIT, null)

    val PIPE: Redirect = new RedirectImpl(Type.PIPE, null)

    def appendTo(file: File): Redirect = {
      if (file == null) throw new NullPointerException()
      new RedirectImpl(Type.APPEND, file)
    }

    def from(file: File): Redirect = {
      if (file == null) throw new NullPointerException()
      new RedirectImpl(Type.READ, file)
    }

    def to(file: File): Redirect = {
      if (file == null) throw new NullPointerException()
      new RedirectImpl(Type.WRITE, file)
    }

    class Type private (name: String, ordinal: Int)
        extends Enum[Type](name, ordinal)

    object Type {
      final val PIPE    = new Type("PIPE", 0)
      final val INHERIT = new Type("INHERIT", 1)
      final val READ    = new Type("READ", 2)
      final val WRITE   = new Type("WRITE", 3)
      final val APPEND  = new Type("APPEND", 4)

      def valueOf(name: String): Type = {
        if (name == null) throw new NullPointerException()
        _values.toSeq.find(_.name == name) match {
          case Some(t) => t
          case None =>
            throw new IllegalArgumentException(
              s"$name is not a valid Type name")
        }
      }

      def values(): Array[Type] = _values

      private val _values = Array(PIPE, INHERIT, READ, WRITE, APPEND)
    }
  }
}
