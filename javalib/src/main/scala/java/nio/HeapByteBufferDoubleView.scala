package java.nio

// Ported from Scala.js
private[nio] final class HeapByteBufferDoubleView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends DoubleBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[DoubleBuffer](this)
  private def genHeapBufferView = GenHeapBufferView[DoubleBuffer](this)
  @inline private def byteArrayBits = genHeapBufferView.byteArrayBits
  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[DoubleBuffer] =
    HeapByteBufferDoubleView.NewHeapByteBufferDoubleView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): DoubleBuffer =
    genHeapBufferView.generic_slice()

  @noinline
  def duplicate(): DoubleBuffer =
    genHeapBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    genHeapBufferView.generic_asReadOnlyBuffer()

  @noinline
  def get(): Double =
    genBuffer.generic_get()

  @noinline
  def put(c: Double): DoubleBuffer =
    genBuffer.generic_put(c)

  @noinline
  def get(index: Int): Double =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, c: Double): DoubleBuffer =
    genBuffer.generic_put(index, c)

  @noinline
  override def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): DoubleBuffer =
    genHeapBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genHeapBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Double =
    byteArrayBits.loadDouble(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    byteArrayBits.storeDouble(index, elem)
}

private[nio] object HeapByteBufferDoubleView {
  private[nio] implicit object NewHeapByteBufferDoubleView
      extends GenHeapBufferView.NewHeapBufferView[DoubleBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): DoubleBuffer = {
      new HeapByteBufferDoubleView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(
      byteBuffer: HeapByteBuffer
  ): DoubleBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
