package java.nio

// Based on the code ported from Scala.js,
// see HeapByteBufferShortView.scala
private[nio] final class MappedByteBufferShortView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends ShortBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private[this] implicit def newMappedShortBufferView =
    MappedByteBufferShortView.NewMappedByteBufferShortView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ShortBuffer =
    GenMappedBufferView(this).generic_slice()

  @noinline
  def duplicate(): ShortBuffer =
    GenMappedBufferView(this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    GenMappedBufferView(this).generic_asReadOnlyBuffer()

  @noinline
  def get(): Short =
    GenBuffer(this).generic_get()

  @noinline
  def put(c: Short): ShortBuffer =
    GenBuffer(this).generic_put(c)

  @noinline
  def get(index: Int): Short =
    GenBuffer(this).generic_get(index)

  @noinline
  def put(index: Int, c: Short): ShortBuffer =
    GenBuffer(this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer(this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer(this).generic_put(src, offset, length)

  @noinline
  def compact(): ShortBuffer =
    GenMappedBufferView(this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView(this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Short =
    GenMappedBufferView(this).byteArrayBits.loadShort(index)

  @inline
  private[nio] def store(index: Int, elem: Short): Unit =
    GenMappedBufferView(this).byteArrayBits.storeShort(index, elem)
}

private[nio] object MappedByteBufferShortView {
  private[nio] implicit object NewMappedByteBufferShortView
      extends GenMappedBufferView.NewMappedBufferView[ShortBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): ShortBuffer = {
      new MappedByteBufferShortView(
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
  ): ShortBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
