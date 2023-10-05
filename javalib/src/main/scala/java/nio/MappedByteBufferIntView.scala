package java.nio

// Based on the code ported from Scala.js,
// see HeapByteBufferIntView.scala
private[nio] final class MappedByteBufferIntView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends IntBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[IntBuffer](this)
  protected def genMappedBufferView = GenMappedBufferView[IntBuffer](this)
  @inline private def byteArrayBits = genMappedBufferView.byteArrayBits
  private implicit def newMappedIntBufferView
      : GenMappedBufferView.NewMappedBufferView[IntBuffer] =
    MappedByteBufferIntView.NewMappedByteBufferIntView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): IntBuffer =
    genMappedBufferView.generic_slice()

  @noinline
  def duplicate(): IntBuffer =
    genMappedBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    genMappedBufferView.generic_asReadOnlyBuffer()

  @noinline
  def get(): Int =
    genBuffer.generic_get()

  @noinline
  def put(c: Int): IntBuffer =
    genBuffer.generic_put(c)

  @noinline
  def get(index: Int): Int =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, c: Int): IntBuffer =
    genBuffer.generic_put(index, c)

  @noinline
  override def get(dst: Array[Int], offset: Int, length: Int): IntBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Int], offset: Int, length: Int): IntBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): IntBuffer =
    genMappedBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genMappedBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Int =
    byteArrayBits.loadInt(index)

  @inline
  private[nio] def store(index: Int, elem: Int): Unit =
    byteArrayBits.storeInt(index, elem)
}

private[nio] object MappedByteBufferIntView {
  private[nio] implicit object NewMappedByteBufferIntView
      extends GenMappedBufferView.NewMappedBufferView[IntBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): IntBuffer = {
      new MappedByteBufferIntView(
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
  ): IntBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
