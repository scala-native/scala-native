package scala.scalanative.tools

import java.lang.System.{err, out, lineSeparator => nl}

/** A `Logger` is in charge of collecting log messages. */
trait Logger {

  /** Logs `msg` at the debug level. */
  def debug(msg: String): Unit

  /** Logs `msg` at the info level. */
  def info(msg: String): Unit

  /** Logs `msg` at the warn level. */
  def warn(msg: String): Unit

  /** Logs `msg` at the errro level. */
  def error(msg: String): Unit

  /** Logs at the debug level that the command `cmd` will start running. */
  def running(cmd: Seq[String]): Unit
}

object Logger {

  def default: Logger =
    Logger(msg => err.println(s"[debug] $msg"),
           msg => out.println(s"[info] $msg"),
           msg => out.println(s"[warn] $msg"),
           msg => err.println(s"[error] $msg"))

  def apply(debugFn: String => Unit,
            infoFn: String => Unit,
            warnFn: String => Unit,
            errorFn: String => Unit): Logger = new Logger {
    override def debug(msg: String): Unit = debugFn(msg)
    override def info(msg: String): Unit  = infoFn(msg)
    override def warn(msg: String): Unit  = warnFn(msg)
    override def error(msg: String): Unit = errorFn(msg)
    override def running(cmd: Seq[String]): Unit = {
      val msg = "running" + nl + cmd.mkString(nl + "\t")
      debugFn(msg)
    }
  }
}
