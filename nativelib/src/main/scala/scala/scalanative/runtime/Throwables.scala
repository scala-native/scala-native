// scalafmt: { maxColumn = 120}

package scala.scalanative
package runtime

import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/** An exception that is thrown whenever an undefined behavior happens in a checked mode.
 */
final class UndefinedBehaviorError(message: String) extends java.lang.Error(message) {
  def this() = this(null)
}

import scala.collection.mutable
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo

object StackTrace {
  @noinline def currentStackTrace(): scala.Array[StackTraceElement] = {
    // Used to prevent filling stacktraces inside `currentStackTrace` which might lead to infinite loop
    val thread = NativeThread.currentNativeThread
    if (thread.isFillingStackTrace) scala.Array.empty
    else if (LinktimeInfo.asanEnabled) scala.Array.empty
    else {
      implicit val tlContext: Context = ThreadLocalContext.get()
      val cursor = tlContext.unwindCursor
      val context = tlContext.unwindContext
      try {
        thread.isFillingStackTrace = true
        val buffer = scala.Array.newBuilder[StackTraceElement]
        val ip = Intrinsics.stackalloc[RawSize]()
        var foundCurrentStackTrace = false
        var afterFillInStackTrace = false
        unwind.get_context(context)
        unwind.init_local(cursor, context)
        while (unwind.step(cursor) > 0) {
          unwind.get_reg(cursor, unwind.UNW_REG_IP, ip)
          val addr = Intrinsics.castRawSizeToLongUnsigned(Intrinsics.loadRawSize(ip))
          /* Creates a stack trace element in given unwind context. Finding a
           *  name of the symbol for current function is expensive, so we cache
           *  stack trace elements based on current instruction pointer.
           */
          val elem = tlContext.cache.getOrElseUpdate(
            addr,
            makeStackTraceElement(cursor, addr)
          )
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
      }
    }
  }

  @resolvedAtLinktime
  private def hasDebugInfo: Boolean =
    (LinktimeInfo.isMac || LinktimeInfo.isLinux) &&
      !LinktimeInfo.is32BitPlatform &&
      LinktimeInfo.sourceLevelDebuging.generateFunctionSourcePositions

  private def makeStackTraceElement(
      cursor: CVoidPtr,
      ip: Long
  )(implicit tlContext: Context): StackTraceElement = {

    val position =
      if (hasDebugInfo) Backtrace.decodePosition(ip)
      else Backtrace.Position.empty

    def withNameFromDWARF() = {
      // linkageName has an extra "_" that we don't want in stack traces
      def isScalaNativeMangledName =
        ffi.strncmp(position.linkageName, c"__SM", Intrinsics.castIntToRawSize(4)) == 0
      val symbol =
        if (isScalaNativeMangledName)
          // skip first `_`
          position.linkageName + 1
        else position.linkageName

      parseStackTraceElement(symbol, position)
    }

    def withNameFromUnwind() = {
      import Context._
      val symbol = tlContext.freshSymbolBuffer
      val offset = Intrinsics.stackalloc[Long]()
      unwind.get_proc_name(
        cursor,
        symbol,
        Intrinsics.castIntToRawSize(SymbolMaxLength),
        offset
      )
      // Make sure the name is definitely 0-terminated.
      // Unmangler is going to use strlen on this name and it's
      // behavior is not defined for non-zero-terminated strings.
      symbol(SymbolMaxLength - 1) = 0.toByte

      parseStackTraceElement(symbol, position)
    }

    if (hasDebugInfo) {
      if (position.linkageName != null) withNameFromDWARF()
      else withNameFromUnwind()
    } else withNameFromUnwind()
  }

  private def parseStackTraceElement(
      sym: CString,
      position: Backtrace.Position
  )(implicit tlContext: Context): StackTraceElement = {
    val className: Ptr[CChar] = tlContext.freshClassNameBuffer
    val methodName: Ptr[CChar] = tlContext.freshMethodNameBuffer
    val fileName: Ptr[CChar] =
      if (LinktimeInfo.isWindows) tlContext.freshFileNameBuffer
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

  private object ThreadLocalContext extends InheritableThreadLocal[Context] {
    override protected def initialValue(): Context =
      new Context(mutable.LongMap.empty, ByteArray.alloc(Context.DataSize))

    override def childValue(fromParent: Context): Context = {
      val cache = mutable.LongMap.empty[StackTraceElement]
      cache ++= fromParent.cache
      new Context(cache, ByteArray.alloc(Context.DataSize))
    }
  }
  private object Context {
    final val SymbolMaxLength = 512
    final val ClassNameMaxLength = 256
    final val MethodNameMaxLength = 256
    final val FileNameMaxLength = 512

    final val SymbolBufferOffset = 0
    final val ClassNameBufferOffset = SymbolBufferOffset + SymbolMaxLength
    final val MethodNameBufferOffset = ClassNameBufferOffset + ClassNameMaxLength
    final val FileNameBufferOffset = MethodNameBufferOffset + MethodNameMaxLength
    final val UnwindCursorOffset = FileNameBufferOffset + FileNameMaxLength
    final val UnwindContextOffset = UnwindCursorOffset + unwind.sizeOfCursor
    final val DataSize = UnwindContextOffset + unwind.sizeOfContext
  }
  private class Context(
      val cache: mutable.LongMap[StackTraceElement],
      val data: ByteArray
  ) {
    def freshAt(offset: Int, size: Int): Ptr[Byte] = {
      ffi.memset(
        data.atRawUnsafe(offset),
        0,
        Intrinsics.castIntToRawSizeUnsigned(size)
      )
      data.atUnsafe(offset)
    }
    import Context._
    def freshSymbolBuffer = freshAt(SymbolBufferOffset, SymbolMaxLength)
    def freshClassNameBuffer = freshAt(ClassNameBufferOffset, ClassNameMaxLength)
    def freshMethodNameBuffer = freshAt(MethodNameBufferOffset, MethodNameMaxLength)
    def freshFileNameBuffer = freshAt(FileNameBufferOffset, FileNameMaxLength)
    def unwindCursor = data.atUnsafe(UnwindCursorOffset)
    def unwindContext = data.atUnsafe(UnwindContextOffset)
  }
}
