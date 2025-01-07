package scala.scalanative.runtime

import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo

abstract class Throwable protected (writableStackTrace: scala.Boolean) {
  self: java.lang.Throwable =>

  protected var stackTrace: scala.Array[StackTraceElement] = _
  private val exceptionWrapper: BlobArray =
    if (Throwable.usingCxxExceptions) null // unused
    else BlobArray.alloc(Throwable.ffi.sizeOfExceptionWrapper)

  if (Throwable.usingCxxExceptions) {
    // ExceptionWrapper { Throwable, _UnwindException }
    Intrinsics.storeObject(exceptionWrapper.atRawUnsafe(0), this)
  }

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
private object Throwable {
  @resolvedAtLinktime
  private def usingCxxExceptions: Boolean = LinktimeInfo.isWindows
  
  @extern private object ffi {
    @name("scalanative_Throwable_sizeOfExceptionWrapperr")
    def sizeOfExceptionWrapper: Int = extern
  }

  @exported("scalanative_Throwable_exceptionWrapper")
  def exceptionWrapper(self: Throwable): RawPtr =
    self.exceptionWrapper.atRawUnsafe(0)
}
