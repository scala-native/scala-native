package java.nio

import scala.scalanative.unsafe

private[nio] final class PointerByteBuffer private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: unsafe.Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends ByteBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerByteBuffer
      : GenPointerBuffer.NewPointerBuffer[ByteBuffer] =
    PointerByteBuffer.NewPointerByteBuffer

  def isReadOnly(): Boolean = _readOnly
  def isDirect(): Boolean = true

  @noinline
  def slice(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_slice()

  @noinline
  def duplicate(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_asReadOnlyBuffer()

  @noinline
  def get(): Byte =
    GenBuffer[ByteBuffer](this).generic_get()

  @noinline
  def put(b: Byte): ByteBuffer =
    GenBuffer[ByteBuffer](this).generic_put(b)

  @noinline
  def get(index: Int): Byte =
    GenBuffer[ByteBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, b: Byte): ByteBuffer =
    GenBuffer[ByteBuffer](this).generic_put(index, b)

  @noinline
  override def get(dst: Array[Byte], offset: Int, length: Int): ByteBuffer =
    GenBuffer[ByteBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Byte], offset: Int, length: Int): ByteBuffer =
    GenBuffer[ByteBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_compact()

  // Here begins the stuff specific to ByteArrays
  @inline private def byteArrayBits: ByteArrayBits =
    ByteArrayBits(_rawDataPointer, _offset, isBigEndian)

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
    PointerByteBufferCharView.fromPointerByteBuffer(this)

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
    PointerByteBufferShortView.fromPointerByteBuffer(this)

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
    PointerByteBufferIntView.fromPointerByteBuffer(this)

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
    PointerByteBufferLongView.fromPointerByteBuffer(this)

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
    PointerByteBufferFloatView.fromPointerByteBuffer(this)

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
    PointerByteBufferDoubleView.fromPointerByteBuffer(this)

  // Internal API

  @inline
  private[nio] def load(index: Int): Byte =
    GenPointerBuffer[ByteBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Byte): Unit =
    GenPointerBuffer[ByteBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    GenPointerBuffer[ByteBuffer](this).generic_load(
      startIndex,
      dst,
      offset,
      length
    )

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    GenPointerBuffer[ByteBuffer](this).generic_store(
      startIndex,
      src,
      offset,
      length
    )
}

private[nio] object PointerByteBuffer {
  private[nio] implicit object NewPointerByteBuffer
      extends GenPointerBuffer.NewPointerBuffer[ByteBuffer] {
    def bytesPerElem: Int = 1

    def apply(
        rawDataPointer: unsafe.Ptr[Byte],
        capacity: Int,
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): PointerByteBuffer = new PointerByteBuffer(
      _capacity = capacity,
      _rawDataPointer = rawDataPointer,
      _offset = arrayOffset,
      _initialPosition = initialPosition,
      _initialLimit = initialLimit,
      _readOnly = readOnly
    )
  }

  def wrap(ptr: unsafe.Ptr[Byte], capacity: Int): ByteBuffer = {
    java.util.Objects.requireNonNull(ptr)
    require(capacity >= 0)
    new PointerByteBuffer(
      _capacity = capacity,
      _rawDataPointer = ptr,
      _offset = 0,
      _initialPosition = 0,
      _initialLimit = capacity,
      _readOnly = false
    )
  }

}
