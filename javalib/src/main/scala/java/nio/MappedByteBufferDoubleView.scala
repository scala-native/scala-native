package java.nio

// Based on the code ported from Scala.js,
// see HeapByteBufferDoubleView.scala
private[nio] final class MappedByteBufferDoubleView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends DoubleBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private[this] implicit def newMappedDoubleBufferView =
    MappedByteBufferDoubleView.NewMappedByteBufferDoubleView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): DoubleBuffer =
    GenMappedBufferView(this).generic_slice()

  @noinline
  def duplicate(): DoubleBuffer =
    GenMappedBufferView(this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    GenMappedBufferView(this).generic_asReadOnlyBuffer()

  @noinline
  def get(): Double =
    GenBuffer(this).generic_get()

  @noinline
  def put(c: Double): DoubleBuffer =
    GenBuffer(this).generic_put(c)

  @noinline
  def get(index: Int): Double =
    GenBuffer(this).generic_get(index)

  @noinline
  def put(index: Int, c: Double): DoubleBuffer =
    GenBuffer(this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer(this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer(this).generic_put(src, offset, length)

  @noinline
  def compact(): DoubleBuffer =
    GenMappedBufferView(this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView(this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Double =
    GenMappedBufferView(this).byteArrayBits.loadDouble(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    GenMappedBufferView(this).byteArrayBits.storeDouble(index, elem)
}

private[nio] object MappedByteBufferDoubleView {
  private[nio] implicit object NewMappedByteBufferDoubleView
      extends GenMappedBufferView.NewMappedBufferView[DoubleBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): DoubleBuffer = {
      new MappedByteBufferDoubleView(
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
  ): DoubleBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
