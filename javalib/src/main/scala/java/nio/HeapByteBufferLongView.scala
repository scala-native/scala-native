package java.nio

// Ported from Scala.js
private[nio] final class HeapByteBufferLongView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends LongBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[LongBuffer](this)
  private def genHeapBufferView = GenHeapBufferView[LongBuffer](this)
  @inline private def byteArrayBits = genHeapBufferView.byteArrayBits
  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[LongBuffer] =
    HeapByteBufferLongView.NewHeapByteBufferLongView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): LongBuffer =
    genHeapBufferView.generic_slice()

  @noinline
  def duplicate(): LongBuffer =
    genHeapBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    genHeapBufferView.generic_asReadOnlyBuffer()

  @noinline
  def get(): Long =
    genBuffer.generic_get()

  @noinline
  def put(c: Long): LongBuffer =
    genBuffer.generic_put(c)

  @noinline
  def get(index: Int): Long =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, c: Long): LongBuffer =
    genBuffer.generic_put(index, c)

  @noinline
  override def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): LongBuffer =
    genHeapBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genHeapBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Long =
    byteArrayBits.loadLong(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    byteArrayBits.storeLong(index, elem)
}

private[nio] object HeapByteBufferLongView {
  private[nio] implicit object NewHeapByteBufferLongView
      extends GenHeapBufferView.NewHeapBufferView[LongBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): LongBuffer = {
      new HeapByteBufferLongView(
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
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): LongBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
