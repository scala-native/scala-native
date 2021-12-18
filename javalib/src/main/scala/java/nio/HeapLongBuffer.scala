package java.nio

// Ported from Scala.js
private[nio] final class HeapLongBuffer private (
    _capacity: Int,
    _array0: Array[Long],
    _arrayOffset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends LongBuffer(_capacity, _array0, null, _arrayOffset0) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[LongBuffer](this)
  private def genHeapBuffer = GenHeapBuffer[LongBuffer](this)
  private implicit def newHeapBuffer
      : GenHeapBuffer.NewHeapBuffer[LongBuffer, Long] =
    HeapLongBuffer.NewHeapLongBuffer

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): LongBuffer =
    genHeapBuffer.generic_slice()

  @noinline
  def duplicate(): LongBuffer =
    genHeapBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    genHeapBuffer.generic_asReadOnlyBuffer()

  @noinline
  def get(): Long =
    genBuffer.generic_get()

  @noinline
  def put(l: Long): LongBuffer =
    genBuffer.generic_put(l)

  @noinline
  def get(index: Int): Long =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, l: Long): LongBuffer =
    genBuffer.generic_put(index, l)

  @noinline
  override def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): LongBuffer =
    genHeapBuffer.generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Long =
    genHeapBuffer.generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    genHeapBuffer.generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Long],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Long],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_store(startIndex, src, offset, length)
}

private[nio] object HeapLongBuffer {
  private[nio] implicit object NewHeapLongBuffer
      extends GenHeapBuffer.NewHeapBuffer[LongBuffer, Long] {
    def apply(
        capacity: Int,
        array: Array[Long],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): LongBuffer = {
      new HeapLongBuffer(
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
      array: Array[Long],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): LongBuffer = {
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
