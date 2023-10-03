package java.nio

// Ported from Scala.js
private[nio] final class HeapByteBufferIntView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _byteArrayOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends IntBuffer(_capacity, null, null, -1) {

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[IntBuffer](this)
  private def genHeapBufferView = GenHeapBufferView[IntBuffer](this)
  @inline private def byteArrayBits = genHeapBufferView.byteArrayBits
  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[IntBuffer] =
    HeapByteBufferIntView.NewHeapByteBufferIntView

  override def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): IntBuffer =
    genHeapBufferView.generic_slice()

  @noinline
  def duplicate(): IntBuffer =
    genHeapBufferView.generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    genHeapBufferView.generic_asReadOnlyBuffer()

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
    genHeapBufferView.generic_compact()

  @noinline
  def order(): ByteOrder =
    genHeapBufferView.generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Int =
    byteArrayBits.loadInt(index)

  @inline
  private[nio] def store(index: Int, elem: Int): Unit =
    byteArrayBits.storeInt(index, elem)
}

private[nio] object HeapByteBufferIntView {
  private[nio] implicit object NewHeapByteBufferIntView
      extends GenHeapBufferView.NewHeapBufferView[IntBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): IntBuffer = {
      new HeapByteBufferIntView(
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
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): IntBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
