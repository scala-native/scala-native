package java.lang

import scalanative.unsigned._
import scalanative.annotation.stub
import scalanative.libc.errno

class Thread private (runnable: Runnable) extends Runnable {
  if (runnable ne Thread.MainRunnable) ???

  private var interruptedState   = false
  private[this] var name: String = "main" // default name of the main thread

  def run(): Unit = ()

  def interrupt(): Unit =
    interruptedState = true

  def isInterrupted(): scala.Boolean =
    interruptedState

  final def setName(name: String): Unit =
    this.name = name

  final def getName(): String =
    this.name

  @stub
  def getStackTrace(): Array[StackTraceElement] = ???

  def getId(): scala.Long = 1

  @stub
  def getUncaughtExceptionHandler(): UncaughtExceptionHandler = ???

  @stub
  def setUncaughtExceptionHandler(handler: UncaughtExceptionHandler): Unit =
    ???

  @stub
  def setDaemon(on: scala.Boolean): Unit = ???

  @stub
  def this(name: String) = this(??? : Runnable)

  @stub
  def this() = this(??? : Runnable)

  @stub
  def join(): Unit = ???

  @stub
  def start(): Unit = ???

  @stub
  def getContextClassLoader(): java.lang.ClassLoader = ???

  trait UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, e: Throwable): Unit
  }
}

object Thread {
  private val MainRunnable = new Runnable { def run(): Unit = () }
  private val MainThread   = new Thread(MainRunnable)

  def currentThread(): Thread = MainThread

  def interrupted(): scala.Boolean = {
    val ret = currentThread.isInterrupted
    currentThread.interruptedState = false
    ret
  }

  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    import scala.scalanative.runtime.Platform

    if (millis < 0) {
      throw new IllegalArgumentException("millis must be >= 0")
    }
    if (nanos < 0 || nanos > 999999) {
      throw new IllegalArgumentException("nanos value out of range")
    }

    if (!Platform.thread_sleep(millis, nanos)) {
      throw new InterruptedException("Sleep was interrupted")
    }
  }

  def sleep(millis: scala.Long): Unit = sleep(millis, 0)

  @stub
  def dumpStack(): Unit = ???
}
