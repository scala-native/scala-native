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

  private def genBuffer = GenBuffer[DoubleBuffer](this)
  protected lazy val genMappedBufferView =
    GenMappedBufferView[DoubleBuffer](this)
  @inline private def byteArrayBits = genMappedBufferView.byteArrayBits
  private implicit def newMappedDoubleBufferView
      : GenMappedBufferView.NewMappedBufferView[DoubleBuffer] =
    MappedByteBufferDoubleView.NewMappedByteBufferDoubleView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): DoubleBuffer =
    genMappedBufferView.generic_slice()

  @noinline
  def duplicate(): DoubleBuffer =
    genMappedBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    genMappedBufferView.generic_asReadOnlyBuffer()

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
    genMappedBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genMappedBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Double =
    byteArrayBits.loadDouble(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    byteArrayBits.storeDouble(index, elem)
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
