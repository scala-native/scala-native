package scala.scalanative.runtime

import java.util.Arrays

import scala.collection.mutable

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[runtime] object StackTrace {
  @noinline def stackTraceIterator(): Iterator[StackTraceElement] = {
    new Iterator[StackTraceElement] {
      val tlContext = ThreadLocalContext.get()
      implicit val localContext: Context = tlContext
      val cursor = tlContext.unwindCursor
      val context = tlContext.unwindContext
      val ip = tlContext.ip
      unwind.get_context(context)
      unwind.init_local(cursor, context)

      override def hasNext: Boolean = unwind.step(cursor) > 0
      override def next(): StackTraceElement = {
        unwind.get_reg(cursor, unwind.UNW_REG_IP, ip)
        val addr =
          Intrinsics.castRawSizeToLongUnsigned(Intrinsics.loadRawSize(ip))
        tlContext.cache.getOrElseUpdate(
          addr,
          makeStackTraceElement(cursor, addr)
        )
      }
    }
      .dropWhile { elem =>
        elem.getClassName.startsWith("scala.scalanative.runtime.") ||
        elem.getClassName.contains("scala.collection.")
      }
  }

  private[runtime] type InstructionPointer = Long
  @noinline private[runtime] def currentRawStackTrace()
      : scala.Array[InstructionPointer] = {
    def emptyStackTrace = scala.Array.emptyLongArray

    val thread = NativeThread.currentNativeThread
    if (thread.isFillingStackTrace)
      return emptyStackTrace

    if (LinktimeInfo.asanEnabled)
      return emptyStackTrace

    implicit val tlContext: Context = ThreadLocalContext.get()
    val context = tlContext.unwindContext
    if (unwind.get_context(context) < 0)
      return emptyStackTrace

    val cursor = tlContext.unwindCursor
    if (unwind.init_local(cursor, context) < 0)
      return emptyStackTrace
    val ip = tlContext.ip
    try {
      thread.isFillingStackTrace = true

      val buffer = scala.Array.newBuilder[Long]
      buffer.sizeHint(32) // at least

      // JVM limit stack trace to 1024 entries
      var frames = 0
      while (unwind.step(cursor) > 0 && frames < 1024) {
        frames += 1
        if (unwind.get_reg(cursor, unwind.UNW_REG_IP, ip) == 0) {
          buffer += Intrinsics.castRawSizeToLongUnsigned(
            Intrinsics.loadRawSize(ip)
          )
        }
      }
      buffer.result()
    } finally {
      thread.isFillingStackTrace = false
    }

  }
  private[runtime] def materializeStackTrace(
      raw: scala.Array[Long]
  ): scala.Array[StackTraceElement] = {
    def emptyStackTrace = scala.Array.emptyObjectArray
      .asInstanceOf[scala.Array[StackTraceElement]]
    if (raw.isEmpty)
      return emptyStackTrace

    implicit val tlContext: Context = ThreadLocalContext.get()
    val context = tlContext.unwindContext
    if (unwind.get_context(context) < 0)
      return emptyStackTrace

    val cursor = tlContext.unwindCursor
    if (unwind.init_local(cursor, context) < 0)
      return emptyStackTrace

    val buffer = scala.Array.newBuilder[StackTraceElement]
    buffer.sizeHint(raw.length)

    var ipIdx = 0
    while (ipIdx < raw.length) {
      val addr = raw(ipIdx)

      /* Creates a stack trace element in given unwind context. Finding a
       *  name of the symbol for current function is expensive, so we cache
       *  stack trace elements based on current instruction pointer.
       */
      val elem = tlContext.cache.getOrElseUpdate(
        addr,
        makeStackTraceElement(cursor, addr)
      )
      buffer += elem

      // Stack trace cleanup
      if (ipIdx < 4) {
        if (elem.getClassName.startsWith("scala.scalanative.runtime.")) {
          val shouldClear =
            (elem.getClassName == "scala.scalanative.runtime.Throwable" && {
              elem.getMethodName == "fillInStackTrace" || elem.getMethodName == "<init>"
            })
          if (shouldClear) buffer.clear()
        }
      }
      ipIdx += 1
    }

    buffer.result()
  }

  // Used only on Windows where we are forced to use the exactly the same context/cursor
  @noinline def currentStackTrace(): scala.Array[StackTraceElement] = {
    def emptyStackTrace = scala.Array.emptyObjectArray
      .asInstanceOf[scala.Array[StackTraceElement]]
    // Used to prevent filling stacktraces inside `currentStackTrace` which might lead to infinite loop
    val thread = NativeThread.currentNativeThread
    if (thread.isFillingStackTrace)
      return emptyStackTrace
    if (LinktimeInfo.asanEnabled)
      return emptyStackTrace

    implicit val tlContext: Context = ThreadLocalContext.get()
    val cursor = tlContext.unwindCursor
    val context = tlContext.unwindContext
    val ip = tlContext.ip
    try {
      thread.isFillingStackTrace = true
      val buffer = scala.Array.newBuilder[StackTraceElement]
      if (unwind.get_context(context) < 0)
        return emptyStackTrace
      if (unwind.init_local(cursor, context) < 0)
        return emptyStackTrace
      // JVM limit stack trace to 1024 entries
      var frames = 0
      while (unwind.step(cursor) > 0 && frames < 1024) {
        frames += 1
        if (unwind.get_reg(cursor, unwind.UNW_REG_IP, ip) == 0) {
          val addr =
            Intrinsics.castRawSizeToLongUnsigned(Intrinsics.loadRawSize(ip))
          /* Creates a stack trace element in given unwind context. Finding a
           *  name of the symbol for current function is expensive, so we cache
           *  stack trace elements based on current instruction pointer.
           */
          val elem = tlContext.cache.getOrElseUpdate(
            addr,
            makeStackTraceElement(cursor, addr)
          )
          buffer += elem

          if (frames < 4) {
            if (elem.getClassName.startsWith("scala.scalanative.runtime.")) {
              val shouldClear =
                (elem.getClassName == "scala.scalanative.runtime.StackTrace$" && elem.getMethodName == "currentStackTrace") ||
                  (elem.getClassName == "scala.scalanative.runtime.Throwable" && {
                    elem.getMethodName == "fillInStackTrace" || elem.getMethodName == "<init>"
                  })
              if (shouldClear) buffer.clear()
            }
          }
        }
      }

      buffer.result()
    } finally {
      thread.isFillingStackTrace = false
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
        ffi.strncmp(
          position.linkageName,
          c"__SM",
          Intrinsics.castIntToRawSize(4)
        ) == 0
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
      // Unmangler is going to use strlen on this name and its
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
    final val MethodNameBufferOffset =
      ClassNameBufferOffset + ClassNameMaxLength
    final val FileNameBufferOffset =
      MethodNameBufferOffset + MethodNameMaxLength
    final val UnwindCursorOffset = FileNameBufferOffset + FileNameMaxLength
    final val UnwindContextOffset = UnwindCursorOffset + unwind.sizeOfCursor
    final val IPOffset = UnwindContextOffset + unwind.sizeOfContext
    final val DataSize = IPOffset + 8
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
    def freshClassNameBuffer =
      freshAt(ClassNameBufferOffset, ClassNameMaxLength)
    def freshMethodNameBuffer =
      freshAt(MethodNameBufferOffset, MethodNameMaxLength)
    def freshFileNameBuffer = freshAt(FileNameBufferOffset, FileNameMaxLength)
    def unwindCursor = data.atUnsafe(UnwindCursorOffset)
    def unwindContext = data.atUnsafe(UnwindContextOffset)
    def ip = data.atUnsafe(IPOffset).rawptr
  }
}
