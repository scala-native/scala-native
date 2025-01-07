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

  // Variation of Throwable.printStackTrace that prints using printf (System.out requires synchronization)
  def showStackTrace(): Unit = {
    val stacktrace = this.stackTrace

    usingCString(this.toString()) {
      ffi.printf(c"%s\n", _)
    }
    if (stacktrace.isEmpty) ffi.printf(c"\n")
    else {
      var i = 0
      var duplicates = 0
      while (i < stacktrace.length) {
        val trace = stacktrace(i)
        if (i > 0 && (trace eq stacktrace(i - 1))) duplicates += 1
        else {
          if (duplicates > 0)
            usingCString(stacktrace(i - 1).toString()) { trace =>
              ffi.printf(
                c"\tat %s (called recursively %d times)\n",
                trace,
                duplicates
              )
            }
          duplicates = 0
          usingCString(trace.toString()) { trace =>
            ffi.printf(c"\tat %s\n", trace)
          }
        }
        i += 1
      }
    }
  }

  // Candidate for being included in unsafe package
  // Can be implemented more efficently using Scala 3 inlines
  private def usingCString[T](
      str: String,
      charset: Charset = Charset.defaultCharset()
  )(
      usage: CString => T
  ): T = usage {
    if (str == null) null
    else {
      val bytes = str.getBytes(charset)
      if (bytes.length == 0) c""
      else {
        val len = bytes.length
        val rawSize = castIntToRawSizeUnsigned(len + 1)
        val cstr: CString = fromRawPtr(Intrinsics.stackalloc[CChar](rawSize))
        ffi.memcpy(toRawPtr(cstr), toRawPtr(bytes.at(0)), rawSize)
        cstr(len) = 0.toByte
        cstr
      }
    }
  }
}

private object Throwable {
  @resolvedAtLinktime
  private def usingCxxExceptions: Boolean = LinktimeInfo.isWindows
  
  @extern private object ffi {
    @name("scalanative_Throwable_sizeOfExceptionWrapperr")
    def sizeOfExceptionWrapper: Int = extern
  }

  @exported("scalanative_Throwable_showStackTrace")
  def showStackTrace(self: Throwable): Unit = self.showStackTrace()

  @exported("scalanative_Throwable_exceptionWrapper")
  def exceptionWrapper(self: Throwable): RawPtr =
    self.exceptionWrapper.atRawUnsafe(0)
}
