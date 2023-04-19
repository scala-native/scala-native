package java.nio

// Based on the code ported from Scala.js,
// see GenHeapBufferView.scala
private[nio] object GenMappedBufferView {
  def apply[B <: Buffer](self: B): GenMappedBufferView[B] =
    new GenMappedBufferView(self)

  trait NewMappedBufferView[BufferType <: Buffer] {
    def bytesPerElem: Int

    def apply(
        capacity: Int,
        byteArray: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): BufferType
  }

  @inline
  def generic_fromMappedByteBuffer[BufferType <: Buffer](
      byteBuffer: MappedByteBuffer
  )(implicit
      newMappedBufferView: NewMappedBufferView[BufferType]
  ): BufferType = {
    val byteBufferPos = byteBuffer.position()
    val viewCapacity =
      (byteBuffer.limit() - byteBufferPos) / newMappedBufferView.bytesPerElem
    newMappedBufferView(
      viewCapacity,
      byteBuffer._mappedData,
      byteBuffer._arrayOffset + byteBufferPos,
      0,
      viewCapacity,
      byteBuffer.isReadOnly(),
      byteBuffer.isBigEndian
    )
  }
}

private[nio] final class GenMappedBufferView[B <: Buffer](val self: B)
    extends AnyVal {
  import self._

  type NewThisMappedBufferView =
    GenMappedBufferView.NewMappedBufferView[BufferType]

  @inline
  def generic_slice()(implicit
      newMappedBufferView: NewThisMappedBufferView
  ): BufferType = {
    val newCapacity = remaining()
    val bytesPerElem = newMappedBufferView.bytesPerElem
    newMappedBufferView(
      newCapacity,
      _mappedData,
      _byteArrayOffset + bytesPerElem * position(),
      0,
      newCapacity,
      isReadOnly(),
      isBigEndian
    )
  }

  @inline
  def generic_duplicate()(implicit
      newMappedBufferView: NewThisMappedBufferView
  ): BufferType = {
    val result = newMappedBufferView(
      capacity(),
      _mappedData,
      _byteArrayOffset,
      position(),
      limit(),
      isReadOnly(),
      isBigEndian
    )
    result._mark = _mark
    result
  }

  @inline
  def generic_asReadOnlyBuffer()(implicit
      newMappedBufferView: NewThisMappedBufferView
  ): BufferType = {
    val result = newMappedBufferView(
      capacity(),
      _mappedData,
      _byteArrayOffset,
      position(),
      limit(),
      true,
      isBigEndian
    )
    result._mark = _mark
    result
  }

  // Optional operation according to javadoc, does not make sense
  // in the context of MemoryMappedBuffers. Compacting a MemoryMappedBuffer
  // would change the mapped file's internal structure.
  @inline
  def generic_compact(): BufferType =
    throw new UnsupportedOperationException(
      "Not supported in MemoryMappedBuffer"
    )

  @inline
  def generic_order(): ByteOrder =
    if (isBigEndian) ByteOrder.BIG_ENDIAN
    else ByteOrder.LITTLE_ENDIAN

  @inline
  def byteArrayBits(implicit
      newMappedBufferView: NewThisMappedBufferView
  ): ByteArrayBits = {
    ByteArrayBits(
      _mappedData.ptr,
      _byteArrayOffset,
      isBigEndian,
      newMappedBufferView.bytesPerElem
    )
  }

}
