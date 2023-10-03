package java.nio

// Ported from Scala.js
private[nio] final class HeapByteBufferFloatView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends FloatBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[FloatBuffer](this)
  private def genHeapBufferView = GenHeapBufferView[FloatBuffer](this)
  @inline private def byteArrayBits = genHeapBufferView.byteArrayBits
  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[FloatBuffer] =
    HeapByteBufferFloatView.NewHeapByteBufferFloatView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): FloatBuffer =
    genHeapBufferView.generic_slice()

  @noinline
  def duplicate(): FloatBuffer =
    genHeapBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    genHeapBufferView.generic_asReadOnlyBuffer()

  @noinline
  def get(): Float =
    genBuffer.generic_get()

  @noinline
  def put(c: Float): FloatBuffer =
    genBuffer.generic_put(c)

  @noinline
  def get(index: Int): Float =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, c: Float): FloatBuffer =
    genBuffer.generic_put(index, c)

  @noinline
  override def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): FloatBuffer =
    genHeapBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genHeapBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Float =
    byteArrayBits.loadFloat(index)

  @inline
  private[nio] def store(index: Int, elem: Float): Unit =
    byteArrayBits.storeFloat(index, elem)
}

private[nio] object HeapByteBufferFloatView {
  private[nio] implicit object NewHeapByteBufferFloatView
      extends GenHeapBufferView.NewHeapBufferView[FloatBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): FloatBuffer = {
      new HeapByteBufferFloatView(
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
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): FloatBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
