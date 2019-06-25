package scala.scalanative.build

import java.lang.System.{err, out, lineSeparator => nl}

import scala.sys.process.ProcessLogger

/** Interface to report and/or collect messages given by the toolchain. */
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
  def running(cmd: Seq[String]): Unit = {
    val msg = "running" + nl + cmd.mkString(nl + "\t")
    debug(msg)
  }

  /** Executes `f` and logs at the info level how much long it took. */
  def time[T](msg: String)(f: => T): T = {
    import java.lang.System.nanoTime
    val start = nanoTime()
    val res   = f
    val end   = nanoTime()
    info(s"$msg (${(end - start) / 1000000} ms)")
    res
  }
}

object Logger {

  /**
   * A `Logger` that writes `info` and `warn` messages to `stdout`,
   * and `error` and `debug` messages to `stderr`.
   */
  def default: Logger = new Logger {
    def debug(msg: String): Unit = err.println(s"[debug] $msg")
    def info(msg: String): Unit  = out.println(s"[info] $msg")
    def warn(msg: String): Unit  = out.println(s"[warn] $msg")
    def error(msg: String): Unit = err.println(s"[error] $msg")
  }

  /**
   * A logger that uses the supplied functions as implementations
   * for `debug`, `info`, `warn` and `error`.
   *
   * @param debugFn The function to call when `debug` is called.
   * @param infoFn  The function to call when `info` is called.
   * @param warnFn  The function to call when `warn` is called.
   * @param errorFn The function to call when `error` is called.
   * @return A logger that uses the supplied functions as implementations
   *         for `debug`, `info`, `warn` and `error`.
   */
  def apply(debugFn: String => Unit,
            infoFn: String => Unit,
            warnFn: String => Unit,
            errorFn: String => Unit): Logger = new Logger {
    override def debug(msg: String): Unit = debugFn(msg)
    override def info(msg: String): Unit  = infoFn(msg)
    override def warn(msg: String): Unit  = warnFn(msg)
    override def error(msg: String): Unit = errorFn(msg)
  }

  /** Turns the given logger into a `ProcessLogger`. */
  def toProcessLogger(logger: Logger): ProcessLogger =
    ProcessLogger(logger.info, logger.error)
}
