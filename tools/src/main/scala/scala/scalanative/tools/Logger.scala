package scala.scalanative.tools

import java.lang.System.{err, out, lineSeparator => nl}

import scala.sys.process.ProcessLogger

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

  /** Executes `f` and logs at the info level how much long it took. */
  def time[T](msg: String)(f: => T): T
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
    override def time[T](msg: String)(f: => T): T = {
      import java.lang.System.nanoTime
      val start = nanoTime()
      val res   = f
      val end   = nanoTime()
      info(s"$msg (${(end - start) / 1000000} ms)")
      res
    }
  }

  def toProcessLogger(logger: Logger): ProcessLogger =
    ProcessLogger(logger.info, logger.error)
}
