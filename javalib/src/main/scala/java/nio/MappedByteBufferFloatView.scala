package java.nio

// Based on the code ported from Scala.js,
// see HeapByteBufferFloatView.scala
private[nio] final class MappedByteBufferFloatView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends FloatBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private[this] implicit def newMappedFloatBufferView =
    MappedByteBufferFloatView.NewMappedByteBufferFloatView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): FloatBuffer =
    GenMappedBufferView(this).generic_slice()

  @noinline
  def duplicate(): FloatBuffer =
    GenMappedBufferView(this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    GenMappedBufferView(this).generic_asReadOnlyBuffer()

  @noinline
  def get(): Float =
    GenBuffer(this).generic_get()

  @noinline
  def put(c: Float): FloatBuffer =
    GenBuffer(this).generic_put(c)

  @noinline
  def get(index: Int): Float =
    GenBuffer(this).generic_get(index)

  @noinline
  def put(index: Int, c: Float): FloatBuffer =
    GenBuffer(this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer(this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer(this).generic_put(src, offset, length)

  @noinline
  def compact(): FloatBuffer =
    GenMappedBufferView(this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView(this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Float =
    GenMappedBufferView(this).byteArrayBits.loadFloat(index)

  @inline
  private[nio] def store(index: Int, elem: Float): Unit =
    GenMappedBufferView(this).byteArrayBits.storeFloat(index, elem)
}

private[nio] object MappedByteBufferFloatView {
  private[nio] implicit object NewMappedByteBufferFloatView
      extends GenMappedBufferView.NewMappedBufferView[FloatBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): FloatBuffer = {
      new MappedByteBufferFloatView(
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
  ): FloatBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
