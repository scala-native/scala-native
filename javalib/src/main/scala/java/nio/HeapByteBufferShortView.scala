package java.nio

// Ported from Scala.js
private[nio] final class HeapByteBufferShortView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends ShortBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[ShortBuffer](this)
  private def genHeapBufferView = GenHeapBufferView[ShortBuffer](this)
  @inline private def byteArrayBits = genHeapBufferView.byteArrayBits
  private implicit def newHeapBuffer
      : GenHeapBufferView.NewHeapBufferView[ShortBuffer] =
    HeapByteBufferShortView.NewHeapByteBufferShortView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ShortBuffer =
    genHeapBufferView.generic_slice()

  @noinline
  def duplicate(): ShortBuffer =
    genHeapBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    genHeapBufferView.generic_asReadOnlyBuffer()

  @noinline
  def get(): Short =
    genBuffer.generic_get()

  @noinline
  def put(c: Short): ShortBuffer =
    genBuffer.generic_put(c)

  @noinline
  def get(index: Int): Short =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, c: Short): ShortBuffer =
    genBuffer.generic_put(index, c)

  @noinline
  override def get(dst: Array[Short], offset: Int, length: Int): ShortBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Short], offset: Int, length: Int): ShortBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): ShortBuffer =
    genHeapBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genHeapBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Short =
    byteArrayBits.loadShort(index)

  @inline
  private[nio] def store(index: Int, elem: Short): Unit =
    byteArrayBits.storeShort(index, elem)
}

private[nio] object HeapByteBufferShortView {
  private[nio] implicit object NewHeapByteBufferShortView
      extends GenHeapBufferView.NewHeapBufferView[ShortBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): ShortBuffer = {
      new HeapByteBufferShortView(
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
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): ShortBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
