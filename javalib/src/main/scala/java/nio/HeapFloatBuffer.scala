package java.nio

// Ported from Scala.js
private[nio] final class HeapFloatBuffer private (
    _capacity: Int,
    _array0: Array[Float],
    _arrayOffset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends FloatBuffer(_capacity, _array0, null, _arrayOffset0) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[FloatBuffer](this)
  private def genHeapBuffer = GenHeapBuffer[FloatBuffer](this)
  private implicit def newHeapFloatBuffer
      : HeapFloatBuffer.NewHeapFloatBuffer.type =
    HeapFloatBuffer.NewHeapFloatBuffer

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): FloatBuffer =
    genHeapBuffer.generic_slice()

  @noinline
  def duplicate(): FloatBuffer =
    genHeapBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    genHeapBuffer.generic_asReadOnlyBuffer()

  @noinline
  def get(): Float =
    genBuffer.generic_get()

  @noinline
  def put(f: Float): FloatBuffer =
    genBuffer.generic_put(f)

  @noinline
  def get(index: Int): Float =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, f: Float): FloatBuffer =
    genBuffer.generic_put(index, f)

  @noinline
  override def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): FloatBuffer =
    genHeapBuffer.generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Float =
    genHeapBuffer.generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Float): Unit =
    genHeapBuffer.generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Float],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Float],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_store(startIndex, src, offset, length)
}

private[nio] object HeapFloatBuffer {
  private[nio] implicit object NewHeapFloatBuffer
      extends GenHeapBuffer.NewHeapBuffer[FloatBuffer, Float] {
    def apply(
        capacity: Int,
        array: Array[Float],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): FloatBuffer = {
      new HeapFloatBuffer(
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
      array: Array[Float],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): FloatBuffer = {
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
