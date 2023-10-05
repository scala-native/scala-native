package java.nio

// Based on the code ported from Scala.js,
// see HeapByteBufferCharView.scala
private[nio] final class MappedByteBufferCharView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends CharBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[CharBuffer](this)
  private def genHeapBufferView = GenMappedBufferView[CharBuffer](this)
  @inline private def byteArrayBits = genHeapBufferView.byteArrayBits
  private implicit def newMappedCharBufferView
      : GenMappedBufferView.NewMappedBufferView[CharBuffer] =
    MappedByteBufferCharView.NewMappedByteBufferCharView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): CharBuffer =
    genHeapBufferView.generic_slice()

  @noinline
  def duplicate(): CharBuffer =
    genHeapBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): CharBuffer =
    genHeapBufferView.generic_asReadOnlyBuffer()

  def subSequence(start: Int, end: Int): CharBuffer = {
    if (start < 0 || end < start || end > remaining())
      throw new IndexOutOfBoundsException
    new MappedByteBufferCharView(
      capacity(),
      _mappedData,
      _byteArrayOffset,
      position() + start,
      position() + end,
      isReadOnly(),
      isBigEndian
    )
  }

  @noinline
  def get(): Char =
    genBuffer.generic_get()

  @noinline
  def put(c: Char): CharBuffer =
    genBuffer.generic_put(c)

  @noinline
  def get(index: Int): Char =
    genBuffer.generic_get(index)

  @noinline
  def put(index: Int, c: Char): CharBuffer =
    genBuffer.generic_put(index, c)

  @noinline
  override def get(dst: Array[Char], offset: Int, length: Int): CharBuffer =
    genBuffer.generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Char], offset: Int, length: Int): CharBuffer =
    genBuffer.generic_put(src, offset, length)

  @noinline
  def compact(): CharBuffer =
    genHeapBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genHeapBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Char =
    byteArrayBits.loadChar(index)

  @inline
  private[nio] def store(index: Int, elem: Char): Unit =
    byteArrayBits.storeChar(index, elem)
}

private[nio] object MappedByteBufferCharView {
  private[nio] implicit object NewMappedByteBufferCharView
      extends GenMappedBufferView.NewMappedBufferView[CharBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): CharBuffer = {
      new MappedByteBufferCharView(
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
  ): CharBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
