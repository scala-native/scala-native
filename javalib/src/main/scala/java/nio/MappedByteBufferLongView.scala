package java.nio

// Based on the code ported from Scala.js,
// see HeapByteBufferLongView.scala
private[nio] final class MappedByteBufferLongView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends LongBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[LongBuffer](this)
  protected def genMappedBufferView = GenMappedBufferView[LongBuffer](this)
  @inline private def byteArrayBits = genMappedBufferView.byteArrayBits
  private implicit def newMappedLongBufferView
      : GenMappedBufferView.NewMappedBufferView[LongBuffer] =
    MappedByteBufferLongView.NewMappedByteBufferLongView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): LongBuffer = genMappedBufferView.generic_slice()

  @noinline
  def duplicate(): LongBuffer = genMappedBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    genMappedBufferView.generic_asReadOnlyBuffer()

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
    genMappedBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genMappedBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Long =
    byteArrayBits.loadLong(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    byteArrayBits.storeLong(index, elem)
}

private[nio] object MappedByteBufferLongView {
  private[nio] implicit object NewMappedByteBufferLongView
      extends GenMappedBufferView.NewMappedBufferView[LongBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): LongBuffer = {
      new MappedByteBufferLongView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): LongBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
