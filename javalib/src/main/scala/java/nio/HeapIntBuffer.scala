package java.nio

// Ported from Scala.js
private[nio] final class HeapIntBuffer private (
    _capacity: Int,
    _array0: Array[Int],
    _arrayOffset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends IntBuffer(_capacity, _array0, null, _arrayOffset0) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[IntBuffer](this)
  private def genHeapBuffer = GenHeapBuffer[IntBuffer](this)
  private implicit def newHeapBuffer
      : GenHeapBuffer.NewHeapBuffer[IntBuffer, Int] =
    HeapIntBuffer.NewHeapIntBuffer

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): IntBuffer =
    genHeapBuffer.generic_slice()

  @noinline
  def duplicate(): IntBuffer =
    genHeapBuffer.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    genHeapBuffer.generic_asReadOnlyBuffer()

  @noinline
  def get(): Int =
    genBuffer.generic_get()

  @noinline
  def put(i: Int): IntBuffer =
    genBuffer.generic_put(i)

  @noinline
  def get(index: Int): Int =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, i: Int): IntBuffer =
    genBuffer.generic_put(index, i)

  @noinline
  override def get(dst: Array[Int], offset: Int, length: Int): IntBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Int], offset: Int, length: Int): IntBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): IntBuffer =
    genHeapBuffer.generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Int =
    genHeapBuffer.generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Int): Unit =
    genHeapBuffer.generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Int],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Int],
      offset: Int,
      length: Int
  ): Unit =
    genHeapBuffer.generic_store(startIndex, src, offset, length)
}

private[nio] object HeapIntBuffer {
  private[nio] implicit object NewHeapIntBuffer
      extends GenHeapBuffer.NewHeapBuffer[IntBuffer, Int] {
    def apply(
        capacity: Int,
        array: Array[Int],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): IntBuffer = {
      new HeapIntBuffer(
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
      array: Array[Int],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): IntBuffer = {
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
