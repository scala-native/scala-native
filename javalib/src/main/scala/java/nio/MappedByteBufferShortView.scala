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

  private def genBuffer = GenBuffer[ShortBuffer](this)
  protected def genMappedBufferView =
    GenMappedBufferView[ShortBuffer](this)
  @inline private def byteArrayBits = genMappedBufferView.byteArrayBits
  private implicit def newMappedShortBufferView
      : GenMappedBufferView.NewMappedBufferView[ShortBuffer] =
    MappedByteBufferShortView.NewMappedByteBufferShortView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ShortBuffer =
    genMappedBufferView.generic_slice()

  @noinline
  def duplicate(): ShortBuffer =
    genMappedBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    genMappedBufferView.generic_asReadOnlyBuffer()

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
    genMappedBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genMappedBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Short =
    byteArrayBits.loadShort(index)

  @inline
  private[nio] def store(index: Int, elem: Short): Unit =
    byteArrayBits.storeShort(index, elem)
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
