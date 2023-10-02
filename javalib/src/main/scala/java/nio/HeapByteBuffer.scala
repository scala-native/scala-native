package java.nio

import scala.scalanative.unsafe._

// Ported from Scala.js

private[nio] class HeapByteBuffer(
    _capacity: Int,
    _array0: Array[Byte],
    _arrayOffset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends ByteBuffer(_capacity, _array0, null, _arrayOffset0) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[ByteBuffer](this)
  private def genHeapBuffer = GenHeapBuffer[ByteBuffer](this)
  private implicit def newHeapByteBuffer
      : GenHeapBuffer.NewHeapBuffer[ByteBuffer, Byte] =
    HeapByteBuffer.NewHeapByteBuffer

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ByteBuffer =
    genHeapBuffer.generic_slice()

  @noinline
  def duplicate(): ByteBuffer =
    genHeapBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ByteBuffer =
    genHeapBuffer.generic_asReadOnlyBuffer()

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
    genHeapBuffer.generic_compact()

  // Here begins the stuff specific to ByteArrays

  @inline private def byteArrayBits: ByteArrayBits =
    ByteArrayBits(
      _array.at(0),
      _arrayOffset,
      isBigEndian
    )

  @noinline def getChar(): Char =
    byteArrayBits.loadChar(getPosAndAdvanceRead(2))
  @noinline def putChar(value: Char): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeChar(getPosAndAdvanceWrite(2), value);
    this
  }
  @noinline def getChar(index: Int): Char =
    byteArrayBits.loadChar(validateIndex(index, 2))
  @noinline def putChar(index: Int, value: Char): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeChar(validateIndex(index, 2), value);
    this
  }

  def asCharBuffer(): CharBuffer =
    HeapByteBufferCharView.fromHeapByteBuffer(this)

  @noinline def getShort(): Short =
    byteArrayBits.loadShort(getPosAndAdvanceRead(2))
  @noinline def putShort(value: Short): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeShort(getPosAndAdvanceWrite(2), value);
    this
  }
  @noinline def getShort(index: Int): Short =
    byteArrayBits.loadShort(validateIndex(index, 2))
  @noinline def putShort(index: Int, value: Short): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeShort(validateIndex(index, 2), value);
    this
  }

  def asShortBuffer(): ShortBuffer =
    HeapByteBufferShortView.fromHeapByteBuffer(this)

  @noinline def getInt(): Int =
    byteArrayBits.loadInt(getPosAndAdvanceRead(4))
  @noinline def putInt(value: Int): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeInt(getPosAndAdvanceWrite(4), value);
    this
  }
  @noinline def getInt(index: Int): Int =
    byteArrayBits.loadInt(validateIndex(index, 4))
  @noinline def putInt(index: Int, value: Int): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeInt(validateIndex(index, 4), value);
    this
  }

  def asIntBuffer(): IntBuffer =
    HeapByteBufferIntView.fromHeapByteBuffer(this)

  @noinline def getLong(): Long =
    byteArrayBits.loadLong(getPosAndAdvanceRead(8))
  @noinline def putLong(value: Long): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeLong(getPosAndAdvanceWrite(8), value);
    this
  }
  @noinline def getLong(index: Int): Long =
    byteArrayBits.loadLong(validateIndex(index, 8))
  @noinline def putLong(index: Int, value: Long): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeLong(validateIndex(index, 8), value);
    this
  }

  def asLongBuffer(): LongBuffer =
    HeapByteBufferLongView.fromHeapByteBuffer(this)

  @noinline def getFloat(): Float =
    byteArrayBits.loadFloat(getPosAndAdvanceRead(4))
  @noinline def putFloat(value: Float): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeFloat(getPosAndAdvanceWrite(4), value);
    this
  }
  @noinline def getFloat(index: Int): Float =
    byteArrayBits.loadFloat(validateIndex(index, 4))
  @noinline def putFloat(index: Int, value: Float): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeFloat(validateIndex(index, 4), value);
    this
  }

  def asFloatBuffer(): FloatBuffer =
    HeapByteBufferFloatView.fromHeapByteBuffer(this)

  @noinline def getDouble(): Double =
    byteArrayBits.loadDouble(getPosAndAdvanceRead(8))
  @noinline def putDouble(value: Double): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeDouble(getPosAndAdvanceWrite(8), value);
    this
  }
  @noinline def getDouble(index: Int): Double =
    byteArrayBits.loadDouble(validateIndex(index, 8))
  @noinline def putDouble(index: Int, value: Double): ByteBuffer = {
    ensureNotReadOnly(); byteArrayBits.storeDouble(validateIndex(index, 8), value);
    this
  }

  def asDoubleBuffer(): DoubleBuffer =
    HeapByteBufferDoubleView.fromHeapByteBuffer(this)

  // Internal API

  @inline
  private[nio] def load(index: Int): Byte =
    genHeapBuffer.generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Byte): Unit =
    genHeapBuffer.generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_store(startIndex, src, offset, length)
}

private[nio] object HeapByteBuffer {
  private[nio] implicit object NewHeapByteBuffer
      extends GenHeapBuffer.NewHeapBuffer[ByteBuffer, Byte] {
    def apply(
        capacity: Int,
        array: Array[Byte],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): ByteBuffer = {
      new HeapByteBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Byte],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): ByteBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}
