package java.nio

// Ported from Scala.js
private[nio] final class HeapDoubleBuffer private (
    _capacity: Int,
    _array0: Array[Double],
    _arrayOffset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends DoubleBuffer(_capacity, _array0, null, _arrayOffset0) {
  private implicit def newHeapBuffer
      : GenHeapBuffer.NewHeapBuffer[DoubleBuffer, Double] =
    HeapDoubleBuffer.NewHeapDoubleBuffer

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[DoubleBuffer](this)
  private def genHeapBuffer = GenHeapBuffer[DoubleBuffer](this)

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): DoubleBuffer =
    genHeapBuffer.generic_slice()

  @noinline
  def duplicate(): DoubleBuffer =
    genHeapBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    genHeapBuffer.generic_asReadOnlyBuffer()

  @noinline
  def get(): Double =
    genBuffer.generic_get()

  @noinline
  def put(d: Double): DoubleBuffer =
    genBuffer.generic_put(d)

  @noinline
  def get(index: Int): Double =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, d: Double): DoubleBuffer =
    genBuffer.generic_put(index, d)

  @noinline
  override def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): DoubleBuffer =
    genHeapBuffer.generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Double =
    genHeapBuffer.generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    genHeapBuffer.generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Double],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Double],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_store(startIndex, src, offset, length)
}

private[nio] object HeapDoubleBuffer {
  private[nio] implicit object NewHeapDoubleBuffer
      extends GenHeapBuffer.NewHeapBuffer[DoubleBuffer, Double] {
    def apply(
        capacity: Int,
        array: Array[Double],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): DoubleBuffer = {
      new HeapDoubleBuffer(
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
      array: Array[Double],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): DoubleBuffer = {
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
