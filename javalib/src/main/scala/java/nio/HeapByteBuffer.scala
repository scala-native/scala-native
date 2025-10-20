package java.nio

import scala.scalanative.unsafe.*

// Ported from Scala.js

private[nio] class HeapByteBuffer(
    _capacity: Int,
    _array0: Array[Byte],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends ByteBuffer(_capacity, _array0, _offset0) {

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
  def slice(index: Int, length: Int): ByteBuffer =
    genHeapBuffer.generic_slice(index, length)

  @noinline
  def duplicate(): ByteBuffer =
    genHeapBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ByteBuffer =
    genHeapBuffer.generic_asReadOnlyBuffer()

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
  def asCharBuffer(): CharBuffer =
    HeapByteBufferCharView.fromHeapByteBuffer(this)

  def asShortBuffer(): ShortBuffer =
    HeapByteBufferShortView.fromHeapByteBuffer(this)

  def asIntBuffer(): IntBuffer =
    HeapByteBufferIntView.fromHeapByteBuffer(this)

  def asLongBuffer(): LongBuffer =
    HeapByteBufferLongView.fromHeapByteBuffer(this)

  def asFloatBuffer(): FloatBuffer =
    HeapByteBufferFloatView.fromHeapByteBuffer(this)

  def asDoubleBuffer(): DoubleBuffer =
    HeapByteBufferDoubleView.fromHeapByteBuffer(this)

  // Internal API

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
