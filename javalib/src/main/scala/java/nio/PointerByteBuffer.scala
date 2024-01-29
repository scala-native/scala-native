package java.nio

import scala.scalanative.unsafe

private[nio] final class PointerByteBuffer private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: unsafe.Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends ByteBuffer(_capacity, _rawDataPointer + _offset) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerByteBuffer
      : GenPointerBuffer.NewPointerBuffer[ByteBuffer] =
    PointerByteBuffer.NewPointerByteBuffer

  def isReadOnly(): Boolean = _readOnly
  def isDirect(): Boolean = true

  @noinline
  def slice(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_asReadOnlyBuffer()

  @noinline
  override def get(dst: Array[Byte], offset: Int, length: Int): ByteBuffer =
    GenBuffer[ByteBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Byte], offset: Int, length: Int): ByteBuffer =
    GenBuffer[ByteBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): ByteBuffer =
    GenPointerBuffer[ByteBuffer](this).generic_compact()

  // Here begins the stuff specific to ByteArrays
  def asCharBuffer(): CharBuffer =
    PointerByteBufferCharView.fromPointerByteBuffer(this)

  def asShortBuffer(): ShortBuffer =
    PointerByteBufferShortView.fromPointerByteBuffer(this)

  def asIntBuffer(): IntBuffer =
    PointerByteBufferIntView.fromPointerByteBuffer(this)

  def asLongBuffer(): LongBuffer =
    PointerByteBufferLongView.fromPointerByteBuffer(this)

  def asFloatBuffer(): FloatBuffer =
    PointerByteBufferFloatView.fromPointerByteBuffer(this)

  def asDoubleBuffer(): DoubleBuffer =
    PointerByteBufferDoubleView.fromPointerByteBuffer(this)

  // Internal API
  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    GenPointerBuffer[ByteBuffer](this).generic_load(
      startIndex,
      dst,
      offset,
      length
    )

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    GenPointerBuffer[ByteBuffer](this).generic_store(
      startIndex,
      src,
      offset,
      length
    )
}

private[nio] object PointerByteBuffer {
  private[nio] implicit object NewPointerByteBuffer
      extends GenPointerBuffer.NewPointerBuffer[ByteBuffer] {
    def bytesPerElem: Int = 1

    def apply(
        rawDataPointer: unsafe.Ptr[Byte],
        capacity: Int,
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): PointerByteBuffer = new PointerByteBuffer(
      _capacity = capacity,
      _rawDataPointer = rawDataPointer,
      _offset = arrayOffset,
      _initialPosition = initialPosition,
      _initialLimit = initialLimit,
      _readOnly = readOnly
    )
  }

  def wrap(ptr: unsafe.Ptr[Byte], capacity: Int): ByteBuffer = {
    java.util.Objects.requireNonNull(ptr)
    require(capacity >= 0)
    new PointerByteBuffer(
      _capacity = capacity,
      _rawDataPointer = ptr,
      _offset = 0,
      _initialPosition = 0,
      _initialLimit = capacity,
      _readOnly = false
    )
  }

}
