package java.nio

import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.annotation.alwaysinline

import scala.scalanative.posix.sys.mman._

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.windows.WinBaseApi.CreateFileMappingA
import scala.scalanative.windows.WinBaseApiExt._
import scala.scalanative.windows.MemoryApi._
import scala.scalanative.windows._

import java.io.IOException
import java.io.FileDescriptor

import java.nio.channels.FileChannel.MapMode
import java.nio.channels.FileChannel

private class MappedByteBufferImpl(
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends MappedByteBuffer(
      _capacity,
      _mappedData,
      _byteArrayOffset,
      _initialPosition,
      _initialLimit,
      _readOnly
    ) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[ByteBuffer](this)
  private def genMappedBuffer = GenMappedBuffer[ByteBuffer](this)
  private[this] implicit def newMappedByteBuffer
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
  def duplicate(): ByteBuffer =
    genMappedBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ByteBuffer =
    genMappedBuffer.generic_asReadOnlyBuffer()

  @noinline
  def get(): Byte =
    genBuffer.generic_get()

  @noinline
  def put(b: Byte): ByteBuffer =
    genBuffer.generic_put(b)

  @noinline
  def get(index: Int): Byte =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, b: Byte): ByteBuffer =
    genBuffer.generic_put(index, b)

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

  @inline private def arrayBits: ByteArrayBits =
    ByteArrayBits(_mappedData.ptr, _arrayOffset, isBigEndian)

  @noinline def getChar(): Char =
    arrayBits.loadChar(getPosAndAdvanceRead(2))
  @noinline def putChar(value: Char): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeChar(getPosAndAdvanceWrite(2), value);
    this
  }
  @noinline def getChar(index: Int): Char =
    arrayBits.loadChar(validateIndex(index, 2))
  @noinline def putChar(index: Int, value: Char): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeChar(validateIndex(index, 2), value);
    this
  }

  def asCharBuffer(): CharBuffer =
    MappedByteBufferCharView.fromMappedByteBuffer(this)

  @noinline def getShort(): Short =
    arrayBits.loadShort(getPosAndAdvanceRead(2))
  @noinline def putShort(value: Short): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeShort(getPosAndAdvanceWrite(2), value);
    this
  }
  @noinline def getShort(index: Int): Short =
    arrayBits.loadShort(validateIndex(index, 2))
  @noinline def putShort(index: Int, value: Short): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeShort(validateIndex(index, 2), value);
    this
  }

  def asShortBuffer(): ShortBuffer =
    MappedByteBufferShortView.fromMappedByteBuffer(this)

  @noinline def getInt(): Int =
    arrayBits.loadInt(getPosAndAdvanceRead(4))
  @noinline def putInt(value: Int): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeInt(getPosAndAdvanceWrite(4), value);
    this
  }
  @noinline def getInt(index: Int): Int =
    arrayBits.loadInt(validateIndex(index, 4))
  @noinline def putInt(index: Int, value: Int): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeInt(validateIndex(index, 4), value);
    this
  }

  def asIntBuffer(): IntBuffer =
    MappedByteBufferIntView.fromMappedByteBuffer(this)

  @noinline def getLong(): Long =
    arrayBits.loadLong(getPosAndAdvanceRead(8))
  @noinline def putLong(value: Long): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeLong(getPosAndAdvanceWrite(8), value);
    this
  }
  @noinline def getLong(index: Int): Long =
    arrayBits.loadLong(validateIndex(index, 8))
  @noinline def putLong(index: Int, value: Long): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeLong(validateIndex(index, 8), value);
    this
  }

  def asLongBuffer(): LongBuffer =
    MappedByteBufferLongView.fromMappedByteBuffer(this)

  @noinline def getFloat(): Float =
    arrayBits.loadFloat(getPosAndAdvanceRead(4))
  @noinline def putFloat(value: Float): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeFloat(getPosAndAdvanceWrite(4), value);
    this
  }
  @noinline def getFloat(index: Int): Float =
    arrayBits.loadFloat(validateIndex(index, 4))
  @noinline def putFloat(index: Int, value: Float): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeFloat(validateIndex(index, 4), value);
    this
  }

  def asFloatBuffer(): FloatBuffer =
    MappedByteBufferFloatView.fromMappedByteBuffer(this)

  @noinline def getDouble(): Double =
    arrayBits.loadDouble(getPosAndAdvanceRead(8))
  @noinline def putDouble(value: Double): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeDouble(getPosAndAdvanceWrite(8), value);
    this
  }
  @noinline def getDouble(index: Int): Double =
    arrayBits.loadDouble(validateIndex(index, 8))
  @noinline def putDouble(index: Int, value: Double): ByteBuffer = {
    ensureNotReadOnly(); arrayBits.storeDouble(validateIndex(index, 8), value);
    this
  }

  def asDoubleBuffer(): DoubleBuffer =
    MappedByteBufferDoubleView.fromMappedByteBuffer(this)

  // Internal API

  @inline
  private[nio] def load(index: Int): Byte =
    genMappedBuffer.generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Byte): Unit =
    genMappedBuffer.generic_store(index, elem)

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

  @alwaysinline private def failMapping(): Unit =
    throw new IOException("Could not map file to memory")

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

    val dwFileOffsetHigh = (position >>> 32).toUInt
    val dwFileOffsetLow = position.toUInt

    val ptr = MapViewOfFile(
      mappingHandle,
      dwDesiredAccess,
      dwFileOffsetHigh,
      dwFileOffsetLow,
      size.toUInt
    )
    if (ptr == null) failMapping()

    new MappedByteBufferData(mode, ptr, size, Some(mappingHandle))
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

    val ptr = mmap(
      null,
      size.toUInt,
      prot,
      isPrivate,
      fd.fd,
      position.toSize
    )
    if (ptr.toInt == -1) failMapping()

    new MappedByteBufferData(mode, ptr, size, None)
  }

  def apply(
      mode: MapMode,
      position: Long,
      size: Int,
      fd: FileDescriptor,
      channel: FileChannel
  ): MappedByteBufferImpl = {

    // JVM resizes file to accomodate mapping
    if (mode ne MapMode.READ_ONLY) {
      val prevSize = channel.size()
      val minSize = position + size
      if (minSize > prevSize) {
        val prevPosition = channel.position()
        channel.truncate(minSize)
        if (isWindows) {
          channel.position(prevSize)
          for (i <- prevSize until minSize)
            channel.write(ByteBuffer.wrap(Array[Byte](0.toByte)))
          channel.position(prevPosition)
        }
      }
    }

    val mappedData =
      if (isWindows) mapWindows(position, size, fd, mode)
      else mapUnix(position, size, fd, mode)

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
