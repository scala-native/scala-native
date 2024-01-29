package java.nio

import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.annotation.alwaysinline

import scala.scalanative.libc.errno
import scala.scalanative.libc.string
import scala.scalanative.posix.sys.mman._
import scala.scalanative.posix.unistd.{sysconf, _SC_PAGESIZE}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.windows.WinBaseApi.CreateFileMappingA
import scala.scalanative.windows.WinBaseApiExt._
import scala.scalanative.windows.MemoryApi._
import scala.scalanative.windows.ErrorHandlingApi.GetLastError
import scala.scalanative.windows.SysInfoApi._
import scala.scalanative.windows.SysInfoApiOps._
import scala.scalanative.windows._

import java.io.IOException
import java.io.FileDescriptor

import java.nio.channels.FileChannel.MapMode
import java.nio.channels.FileChannel
import scala.scalanative.windows.SysInfoApi.GetSystemInfo

private class MappedByteBufferImpl(
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends MappedByteBuffer(
      _capacity,
      _mappedData,
      _offset,
      _initialPosition,
      _initialLimit,
      _readOnly
    ) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[ByteBuffer](this)
  private def genMappedBuffer = GenMappedBuffer[ByteBuffer](this)
  private implicit def newMappedByteBuffer
      : GenMappedBuffer.NewMappedBuffer[ByteBuffer, Byte] =
    MappedByteBufferImpl.NewMappedByteBuffer

  override def force(): MappedByteBuffer = {
    _mappedData.force()
    this
  }

  override def isLoaded(): Boolean = true

  override def load(): MappedByteBuffer = this

  override def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ByteBuffer =
    genMappedBuffer.generic_slice()

  @noinline
  def slice(index: Int, length: Int): ByteBuffer =
    genMappedBuffer.generic_slice(index, length)

  @noinline
  def duplicate(): ByteBuffer =
    genMappedBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ByteBuffer =
    genMappedBuffer.generic_asReadOnlyBuffer()

  @noinline
  override def get(dst: Array[Byte], offset: Int, length: Int): ByteBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Byte], offset: Int, length: Int): ByteBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): ByteBuffer =
    genMappedBuffer.generic_compact()

  // Here begins the stuff specific to ByteArrays

  @inline @inline private def byteArrayBits: ByteArrayBits =
    ByteArrayBits(_mappedData.data, _offset, isBigEndian)

  @noinline def getChar(): Char =
    byteArrayBits.loadChar(getPosAndAdvanceRead(2))
  @noinline def putChar(value: Char): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeChar(getPosAndAdvanceWrite(2), value);
    this
  }
  @noinline def getChar(index: Int): Char =
    byteArrayBits.loadChar(validateIndex(index, 2))
  @noinline def putChar(index: Int, value: Char): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeChar(validateIndex(index, 2), value);
    this
  }

  def asCharBuffer(): CharBuffer =
    MappedByteBufferCharView.fromMappedByteBuffer(this)

  @noinline def getShort(): Short =
    byteArrayBits.loadShort(getPosAndAdvanceRead(2))
  @noinline def putShort(value: Short): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeShort(getPosAndAdvanceWrite(2), value);
    this
  }
  @noinline def getShort(index: Int): Short =
    byteArrayBits.loadShort(validateIndex(index, 2))
  @noinline def putShort(index: Int, value: Short): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeShort(validateIndex(index, 2), value);
    this
  }

  def asShortBuffer(): ShortBuffer =
    MappedByteBufferShortView.fromMappedByteBuffer(this)

  @noinline def getInt(): Int =
    byteArrayBits.loadInt(getPosAndAdvanceRead(4))
  @noinline def putInt(value: Int): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeInt(getPosAndAdvanceWrite(4), value);
    this
  }
  @noinline def getInt(index: Int): Int =
    byteArrayBits.loadInt(validateIndex(index, 4))
  @noinline def putInt(index: Int, value: Int): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeInt(validateIndex(index, 4), value);
    this
  }

  def asIntBuffer(): IntBuffer =
    MappedByteBufferIntView.fromMappedByteBuffer(this)

  @noinline def getLong(): Long =
    byteArrayBits.loadLong(getPosAndAdvanceRead(8))
  @noinline def putLong(value: Long): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeLong(getPosAndAdvanceWrite(8), value);
    this
  }
  @noinline def getLong(index: Int): Long =
    byteArrayBits.loadLong(validateIndex(index, 8))
  @noinline def putLong(index: Int, value: Long): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeLong(validateIndex(index, 8), value);
    this
  }

  def asLongBuffer(): LongBuffer =
    MappedByteBufferLongView.fromMappedByteBuffer(this)

  @noinline def getFloat(): Float =
    byteArrayBits.loadFloat(getPosAndAdvanceRead(4))
  @noinline def putFloat(value: Float): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeFloat(getPosAndAdvanceWrite(4), value);
    this
  }
  @noinline def getFloat(index: Int): Float =
    byteArrayBits.loadFloat(validateIndex(index, 4))
  @noinline def putFloat(index: Int, value: Float): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeFloat(validateIndex(index, 4), value);
    this
  }

  def asFloatBuffer(): FloatBuffer =
    MappedByteBufferFloatView.fromMappedByteBuffer(this)

  @noinline def getDouble(): Double =
    byteArrayBits.loadDouble(getPosAndAdvanceRead(8))
  @noinline def putDouble(value: Double): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeDouble(getPosAndAdvanceWrite(8), value);
    this
  }
  @noinline def getDouble(index: Int): Double =
    byteArrayBits.loadDouble(validateIndex(index, 8))
  @noinline def putDouble(index: Int, value: Double): ByteBuffer = {
    ensureNotReadOnly();
    byteArrayBits.storeDouble(validateIndex(index, 8), value);
    this
  }

  def asDoubleBuffer(): DoubleBuffer =
    MappedByteBufferDoubleView.fromMappedByteBuffer(this)

  // Internal API

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    genMappedBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    genMappedBuffer.generic_store(startIndex, src, offset, length)
}

private[nio] object MappedByteBufferImpl {
  private[nio] implicit object NewMappedByteBuffer
      extends GenMappedBuffer.NewMappedBuffer[ByteBuffer, Byte] {
    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): ByteBuffer =
      new MappedByteBufferImpl(
        capacity,
        mappedData,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
  }

  @alwaysinline private def failMapping(): Unit = {
    val reason =
      if (isWindows) ErrorHandlingApiOps.errorMessage(GetLastError())
      else fromCString(string.strerror(errno.errno))
    throw new IOException(s"Could not map file to memory: $reason")
  }

  private def mapWindows(
      position: Long,
      size: Int,
      fd: FileDescriptor,
      mode: MapMode
  ): MappedByteBufferData = {
    val (flProtect: DWord, dwDesiredAccess: DWord) = mode match {
      case MapMode.PRIVATE    => (PAGE_WRITECOPY, FILE_MAP_COPY)
      case MapMode.READ_ONLY  => (PAGE_READONLY, FILE_MAP_READ)
      case MapMode.READ_WRITE => (PAGE_READWRITE, FILE_MAP_WRITE)
      case _ => throw new IllegalStateException("Unknown MapMode")
    }

    val sysInfo = stackalloc[SystemInfo]()
    GetSystemInfo(sysInfo)
    val pageSize = sysInfo.allocationGranularity.toInt
    if (pageSize <= 0) failMapping()
    val pagePosition = (position % pageSize).toInt
    val offset = position - pagePosition
    val length = size + pagePosition

    val mappingHandle =
      CreateFileMappingA(
        fd.handle,
        null,
        flProtect,
        0.toUInt,
        0.toUInt,
        null
      )
    if (mappingHandle == null) failMapping()

    val dwFileOffsetHigh = (offset >>> 32).toUInt
    val dwFileOffsetLow = offset.toUInt

    val ptr = MapViewOfFile(
      mappingHandle,
      dwDesiredAccess,
      dwFileOffsetHigh,
      dwFileOffsetLow,
      length.toUInt
    )
    if (ptr == null) failMapping()

    new MappedByteBufferData(
      mode = mode,
      mapAddress = ptr,
      length = size,
      pagePosition = pagePosition,
      windowsMappingHandle = Some(mappingHandle)
    )
  }

  private def mapUnix(
      position: Long,
      size: Int,
      fd: FileDescriptor,
      mode: MapMode
  ): MappedByteBufferData = {

    /* FreeBSD requires that PROT_READ be explicit with MAP_SHARED.
     * Linux, macOS, & FreeBSD MAP_PRIVATE allow PROT_WRITE to imply
     * PROT_READ. Make PROT_READ explicit in all these cases to document
     * the intention.
     */
    val (prot: Int, isPrivate: Int) = mode match {
      case MapMode.PRIVATE    => (PROT_READ | PROT_WRITE, MAP_PRIVATE)
      case MapMode.READ_ONLY  => (PROT_READ, MAP_SHARED)
      case MapMode.READ_WRITE => (PROT_READ | PROT_WRITE, MAP_SHARED)
      case _ => throw new IllegalStateException("Unknown MapMode")
    }

    val pageSize = sysconf(_SC_PAGESIZE).toInt
    if (pageSize <= 0) failMapping()
    val pagePosition = (position % pageSize).toInt
    val offset = position - pagePosition
    val length = size + pagePosition

    val ptr = mmap(
      addr = null,
      length = length.toUSize,
      prot = prot,
      flags = isPrivate,
      fd = fd.fd,
      offset = offset.toSize
    )
    if (ptr.toInt == -1) failMapping()

    new MappedByteBufferData(mode, ptr, size, pagePosition, None)
  }

  private def mapData(
      position: Long,
      size: Int,
      fd: FileDescriptor,
      mode: MapMode
  ): MappedByteBufferData = {

    if (size > 0) {
      if (isWindows) mapWindows(position, size, fd, mode)
      else mapUnix(position, size, fd, mode)
    } else {
      /* Issue #3340
       *   JVM silently succeeds on MappedByteBuffer creation and
       *   throws "IndexOutOfBoundsException" on access; get or put.
       *
       *   Create and use an "empty" MappedByteBuffer so that Scala Native
       *   matches the JVM behavior.
       *
       *   POSIX and most (all?) unix-like systems explicitly do not
       *   allow mapping zero bytes and mapUnix() will throw an Exception.
       *
       *   On Windows, a request to map zero bytes causes the entire
       *   file to be mapped. At the least, expensive in I/O and memory
       *   for bytes which will never be used. The call to MapViewOfFile()
       *   in mapWindows() may or may not use the same semantics. Someone
       *   with Windows skills would have to check. Knowing the zero size,
       *   it is easier to match the JDK by creating an empty
       *   MappedByteBufferData on the Windows branch also.
       */
      MappedByteBufferData.empty
    }
  }

  def apply(
      mode: MapMode,
      position: Long,
      size: Int,
      fd: FileDescriptor
  ): MappedByteBufferImpl = {

    val mappedData = mapData(position, size, fd, mode)

    new MappedByteBufferImpl(
      mappedData.length,
      mappedData,
      0,
      0,
      size,
      mode == FileChannel.MapMode.READ_ONLY
    )
  }
}
