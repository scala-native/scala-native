package scala.scalanative.runtime

import scala.scalanative.unsafe._

  self: java.lang.Throwable =>

  protected var stackTrace: scala.Array[StackTraceElement] = _

  if (writableStackTrace)
    fillInStackTrace()

  def fillInStackTrace(): Throwable = {
    // currentStackTrace should be handling exclusion in its own
    // critical section, but does not. So do
    if (writableStackTrace) this.synchronized {
      this.stackTrace = StackTrace.currentStackTrace()
    }
    this
  }

  def setStackTrace(stackTrace: scala.Array[StackTraceElement]): Unit = {
    if (writableStackTrace) this.synchronized {
      var i = 0
      while (i < stackTrace.length) {
        if (stackTrace(i) eq null)
          throw new NullPointerException()
        i += 1
      }
      this.stackTrace = stackTrace.clone()
    }
  }
}
