package scala.scalanative.build

import java.lang.System.{err, lineSeparator => nl, out}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.sys.process.ProcessLogger

/** Interface to report and/or collect messages given by the toolchain. */
trait Logger {

  /** Logs `msg` at the trace level. */
  def trace(msg: Throwable): Unit

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
    val msg = "Running" + nl + cmd.mkString(nl + "\t")
    debug(msg)
  }

  /** Executes `f` and logs at the info level how much long it took. */
  def time[T](msg: String)(f: => T): T = {
    import java.lang.System.nanoTime
    val start = nanoTime()
    val res = f
    val end = nanoTime()
    info(s"$msg (${(end - start) / 1000000} ms)")
    res
  }

  def timeAsync[T](
      msg: String
  )(f: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    import java.lang.System.nanoTime
    val start = nanoTime()
    val res = f
    res.onComplete { _ =>
      val end = nanoTime()
      info(s"$msg (${(end - start) / 1000000} ms)")
    }
    res
  }
}

import java.lang.System.nanoTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

import Timer.*
class Timer private (log: Node => Unit, tree: Node) {
  def apply[A](label: String)(f: Timer => A): A = {
    val childNode = Node(label, 0, ArrayBuffer.empty)
    val child = new Timer(_ => (), childNode)
    val start = nanoTime()
    val res = f(child)
    val duration = nanoTime() - start

    val updated = childNode.copy(wallClock = duration)

    tree.synchronized {
      tree.children.addOne(updated)
    }

    log(updated)

    res
  }

  def measure[A](label: String)(f: => A): A =
    apply(label)(_ => f)

  def measureAsync[A](label: String)(f: => Future[A])(implicit
      ec: ExecutionContext
  ): Future[A] =
    async(label)(_ => f)

  def async[A](
      label: String
  )(f: Timer => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val childNode = Node(label, 0, ArrayBuffer.empty)
    val child = new Timer(_ => (), childNode)

    val start = nanoTime()
    f(child).map { result =>
      val duration = nanoTime() - start
      val updated = childNode.copy(wallClock = duration)

      tree.synchronized {
        tree.children.addOne(updated)
      }

      log(updated)
      result
    }

  }
}

object Timer {
  case class Node(label: String, wallClock: Long, children: ArrayBuffer[Node])

  def apply(label: String, log: Node => Unit): Timer =
    new Timer(log, Node(label, 0, ArrayBuffer.empty))

}

object Logger {

  /** A `Logger` that writes `info` and `warn` messages to `stdout`, and
   *  `error`, `debug` and `trace` messages to `stderr`.
   */
  def default: Logger = new Logger {

    /** Logs `msg` at the trace level. */
    def trace(msg: Throwable): Unit = err.println(s"[trace] $msg")
    def debug(msg: String): Unit = err.println(s"[debug] $msg")
    def info(msg: String): Unit = out.println(s"[info] $msg")
    def warn(msg: String): Unit = out.println(s"[warn] $msg")
    def error(msg: String): Unit = err.println(s"[error] $msg")

  }

  /** A 'Logger' that discards all messagess
   */
  def nullLogger: Logger = new Logger {
    override def trace(msg: Throwable): Unit = ()
    override def debug(msg: String): Unit = ()
    override def info(msg: String): Unit = ()
    override def warn(msg: String): Unit = ()
    override def error(msg: String): Unit = ()
  }

  /** A logger that uses the supplied functions as implementations for `debug`,
   *  `info`, `warn` and `error`.
   *
   *  @param debugFn
   *    The function to call when `debug` is called.
   *  @param infoFn
   *    The function to call when `info` is called.
   *  @param warnFn
   *    The function to call when `warn` is called.
   *  @param errorFn
   *    The function to call when `error` is called.
   *  @return
   *    A logger that uses the supplied functions as implementations for
   *    `debug`, `info`, `warn` and `error`.
   */
  def apply(
      traceFn: Throwable => Unit,
      debugFn: String => Unit,
      infoFn: String => Unit,
      warnFn: String => Unit,
      errorFn: String => Unit
  ): Logger = new Logger {

    override def trace(msg: Throwable): Unit = traceFn(msg)
    override def debug(msg: String): Unit = debugFn(msg)
    override def info(msg: String): Unit = infoFn(msg)
    override def warn(msg: String): Unit = warnFn(msg)
    override def error(msg: String): Unit = errorFn(msg)
  }

  /** Turns the given logger into a `ProcessLogger`. */
  def toProcessLogger(logger: Logger): ProcessLogger =
    ProcessLogger(logger.info, logger.error)
}
