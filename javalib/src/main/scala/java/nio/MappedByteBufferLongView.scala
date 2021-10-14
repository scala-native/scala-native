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

  private[this] implicit def newMappedLongBufferView =
    MappedByteBufferLongView.NewMappedByteBufferLongView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): LongBuffer =
    GenMappedBufferView(this).generic_slice()

  @noinline
  def duplicate(): LongBuffer =
    GenMappedBufferView(this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    GenMappedBufferView(this).generic_asReadOnlyBuffer()

  @noinline
  def get(): Long =
    GenBuffer(this).generic_get()

  @noinline
  def put(c: Long): LongBuffer =
    GenBuffer(this).generic_put(c)

  @noinline
  def get(index: Int): Long =
    GenBuffer(this).generic_get(index)

  @noinline
  def put(index: Int, c: Long): LongBuffer =
    GenBuffer(this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer(this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer(this).generic_put(src, offset, length)

  @noinline
  def compact(): LongBuffer =
    GenMappedBufferView(this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView(this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Long =
    GenMappedBufferView(this).byteArrayBits.loadLong(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    GenMappedBufferView(this).byteArrayBits.storeLong(index, elem)
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
