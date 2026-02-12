package scala.scalanative.runtime

import java.util.Arrays

import scala.collection.mutable

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[runtime] object StackTrace {
  @noinline def stackTraceIterator(): Iterator[StackTraceElement] = {
    val rawPCs = currentRawStackTrace()
    val tlContext = ThreadLocalContext.get()
    implicit val localContext: Context = tlContext

    rawPCs.iterator
      .map { addr =>
        tlContext.cache.getOrElseUpdate(
          addr,
          makeStackTraceElement(addr)
        )
      }
      .filter(_ != null)
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
    if (null eq thread)
      return emptyStackTrace

    if (thread.isFillingStackTrace)
      return emptyStackTrace

    if (LinktimeInfo.asanEnabled)
      return emptyStackTrace

    try {
      thread.isFillingStackTrace = true

      Backtrace.ensureInitialized()
      val maxFrames = 1024
      val rawBuf: Ptr[CSize] =
        fromRawPtr(
          Intrinsics.stackalloc[CSize](
            Intrinsics.castIntToRawSizeUnsigned(maxFrames)
          )
        )
      // skip=1 to skip this currentRawStackTrace frame itself
      val count = backtrace_ffi.collect(1, rawBuf, maxFrames)
      if (count <= 0) return emptyStackTrace

      val result = new scala.Array[Long](count)
      var i = 0
      while (i < count) {
        result(i) = (!(rawBuf + i)).toLong
        i += 1
      }
      result
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

    val buffer = scala.Array.newBuilder[StackTraceElement]
    buffer.sizeHint(raw.length)

    var ipIdx = 0
    while (ipIdx < raw.length) {
      val addr = raw(ipIdx)

      val elem = tlContext.cache.getOrElseUpdate(
        addr,
        makeStackTraceElement(addr)
      )
      if (elem != null) {
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
      }
      ipIdx += 1
    }

    buffer.result()
  }

  private def makeStackTraceElement(
      ip: Long
  )(implicit tlContext: Context): StackTraceElement = {
    val position = Backtrace.decodePosition(ip)

    if (position.linkageName != null) {
      parseStackTraceElement(position.linkageName, position)
    } else {
      // No symbol found — still show the frame with whatever info we have
      new StackTraceElement(
        "<unknown>",
        "<unknown>",
        position.filename,
        position.line
      )
    }
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
    val classNameStr = fromCString(className)

    // Non-Scala frame (e.g. C function) — use raw symbol as method name
    if (classNameStr == SymbolFormatter.NoClassNameStr) {
      new StackTraceElement(
        "<none>",
        fromCString(sym),
        position.filename,
        position.line
      )
    } else {
      val filename =
        if (position.filename != null || fileName == null) position.filename
        else fromCString(fileName).trim()
      val line =
        if (position.line > 0 || filename == null) position.line
        else !lineOut

      new StackTraceElement(
        classNameStr,
        fromCString(methodName),
        filename,
        line
      )
    }
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
    final val DataSize = FileNameBufferOffset + FileNameMaxLength
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
  }
}
