package scala.scalanative
package runtime

import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/** An exception that is thrown whenever an undefined behavior happens in a
 *  checked mode.
 */
final class UndefinedBehaviorError(message: String)
    extends java.lang.Error(message) {
  def this() = this(null)
}

import scala.collection.mutable
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.ffi.{malloc, calloc, free}

import java.util.concurrent.ConcurrentHashMap
import java.{util => ju}

object StackTrace {
  private val cache: ju.AbstractMap[CUnsignedLong, StackTraceElement] =
    if (isMultithreadingEnabled) new ConcurrentHashMap
    else new ju.HashMap

  @noinline def currentStackTrace(): scala.Array[StackTraceElement] = {
    // Used to prevent filling stacktraces inside `currentStackTrace` which might lead to infinite loop
    val thread = NativeThread.currentNativeThread
    if (thread.isFillingStackTrace) scala.Array.empty
    else if (LinktimeInfo.asanEnabled) scala.Array.empty
    else {
      val cursor = fromRawPtr(malloc(unwind.sizeOfCursor))
      val context = fromRawPtr(malloc(unwind.sizeOfContext))
      try {
        thread.isFillingStackTrace = true
        val buffer = scala.Array.newBuilder[StackTraceElement]
        val ip = fromRawPtr[CSize](Intrinsics.stackalloc[CSize]())
        var foundCurrentStackTrace = false
        var afterFillInStackTrace = false
        unwind.get_context(context)
        unwind.init_local(cursor, context)
        while (unwind.step(cursor) > 0) {
          unwind.get_reg(cursor, unwind.UNW_REG_IP, ip)
          val elem = cachedStackTraceElement(cursor, !ip)
          buffer += elem

          // Look for intrinsic stack frames and remove them to not polute stack traces
          if (!afterFillInStackTrace) {
            if (!foundCurrentStackTrace) {
              if (elem.getClassName == "scala.scalanative.runtime.StackTrace$" &&
                  elem.getMethodName == "currentStackTrace") {
                foundCurrentStackTrace = true
                buffer.clear()
              }
            } else {
              // Not guaranteed to be found, may be inlined.
              // This branch would be visited exactly 1 time
              if (elem.getClassName == "java.lang.Throwable" &&
                  elem.getMethodName == "fillInStackTrace") {
                buffer.clear()
              }
              afterFillInStackTrace = true
            }
          }
        }

        buffer.result()
      } finally {
        thread.isFillingStackTrace = false
        free(cursor)
        free(context)
      }
    }
  }

  @resolvedAtLinktime
  private def hasDebugInfo: Boolean =
    (LinktimeInfo.isMac || LinktimeInfo.isLinux) && LinktimeInfo.sourceLevelDebuging.generateFunctionSourcePositions

  private def makeStackTraceElement(
      cursor: CVoidPtr,
      ip: CUnsignedLong
  ): StackTraceElement = {

    val position =
      if (hasDebugInfo) Backtrace.decodePosition(ip.toLong)
      else Backtrace.Position.empty

    def withNameFromDWARF() = {
      // linkageName has an extra "_" that we don't want in stack traces
      def isScalaNativeMangledName =
        position.linkageName(0) == '_'.toByte &&
          position.linkageName(1) == '_'.toByte &&
          position.linkageName(2) == 'S'.toByte
      val name =
        if (isScalaNativeMangledName)
          // skip first `_`
          position.linkageName + 1
        else position.linkageName

      StackTraceElement(name, position)
    }

    def withNameFromUnwind() = {
      val nameMax = 1024
      val name = fromRawPtr[CChar](
        calloc(
          Intrinsics.castIntToRawSizeUnsigned(nameMax),
          Intrinsics.sizeOf[CChar]
        )
      )
      try {
        val offset = fromRawPtr[Long](Intrinsics.stackalloc[Long]())

        unwind.get_proc_name(cursor, name, nameMax.toUSize, offset)

        // Make sure the name is definitely 0-terminated.
        // Unmangler is going to use strlen on this name and it's
        // behavior is not defined for non-zero-terminated strings.
        name(nameMax - 1) = 0.toByte

        StackTraceElement(name, position)
      } finally free(name)
    }

    if (hasDebugInfo) {
      if (position.linkageName != null) withNameFromDWARF()
      else withNameFromUnwind()
    } else withNameFromUnwind()
  }

  /** Creates a stack trace element in given unwind context. Finding a name of
   *  the symbol for current function is expensive, so we cache stack trace
   *  elements based on current instruction pointer.
   */
  private def cachedStackTraceElement(
      cursor: CVoidPtr,
      ip: CUnsignedLong
  ): StackTraceElement =
    cache.computeIfAbsent(ip, makeStackTraceElement(cursor, _))

}

private object StackTraceElement {
  // ScalaNative specific
  def apply(
      sym: CString,
      position: Backtrace.Position
  ): StackTraceElement = {
    val className: Ptr[CChar] = fromRawPtr(
      Intrinsics.stackalloc[CChar](Intrinsics.castIntToRawSizeUnsigned(512))
    )
    val methodName: Ptr[CChar] = fromRawPtr(
      Intrinsics.stackalloc[CChar](Intrinsics.castIntToRawSizeUnsigned(256))
    )
    val fileName: Ptr[CChar] =
      if (LinktimeInfo.isWindows)
        fromRawPtr(
          Intrinsics.stackalloc[CChar](Intrinsics.castIntToRawSizeUnsigned(512))
        )
      else null
    val lineOut: Ptr[Int] = fromRawPtr(Intrinsics.stackalloc[Int]())
    SymbolFormatter.asyncSafeFromSymbol(
      sym = sym,
      classNameOut = className,
      methodNameOut = methodName,
      fileNameOut = fileName,
      lineOut = lineOut
    )
    val filename =
      if (position.filename != null || fileName == null) position.filename
      else fromCString(fileName).trim()
    val line =
      if (position.line > 0 || filename == null) position.line
      else !lineOut

    new StackTraceElement(
      fromCString(className),
      fromCString(methodName),
      filename,
      line
    )
  }
}
