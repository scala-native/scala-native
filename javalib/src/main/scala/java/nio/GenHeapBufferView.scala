package java.nio

import java.util.Objects

import scala.scalanative.unsafe._

// Ported from Scala.js
private[nio] object GenHeapBufferView {
  def apply[B <: Buffer](self: B): GenHeapBufferView[B] =
    new GenHeapBufferView(self)

  trait NewHeapBufferView[BufferType <: Buffer] {
    def bytesPerElem: Int

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): BufferType
  }

  @inline
  def generic_fromHeapByteBuffer[BufferType <: Buffer](
      byteBuffer: HeapByteBuffer
  )(implicit newHeapBufferView: NewHeapBufferView[BufferType]): BufferType = {
    val byteBufferPos = byteBuffer.position()
    val viewCapacity =
      (byteBuffer.limit() - byteBufferPos) / newHeapBufferView.bytesPerElem
    newHeapBufferView(
      viewCapacity,
      byteBuffer._array,
      byteBuffer._offset + byteBufferPos,
      0,
      viewCapacity,
      byteBuffer.isReadOnly(),
      byteBuffer.isBigEndian
    )
  }
}

private[nio] final class GenHeapBufferView[B <: Buffer](val self: B)
    extends AnyVal {
  import self._

  type NewThisHeapBufferView = GenHeapBufferView.NewHeapBufferView[BufferType]

  @inline
  def generic_slice()(implicit
      newHeapBufferView: NewThisHeapBufferView
  ): BufferType = {
    val newCapacity = remaining()
    val bytesPerElem = newHeapBufferView.bytesPerElem
    newHeapBufferView(
      newCapacity,
      _byteArray,
      _offset + bytesPerElem * position(),
      0,
      newCapacity,
      isReadOnly(),
      isBigEndian
    )
  }

  @inline
  def generic_slice(index: Int, length: Int)(implicit
      newHeapBufferView: NewThisHeapBufferView
  ): BufferType = {
    Objects.checkFromIndexSize(index, length, limit())
    val newCapacity = length
    val bytesPerElem = newHeapBufferView.bytesPerElem
    newHeapBufferView(
      newCapacity,
      _byteArray,
      _offset + bytesPerElem * index,
      0,
      newCapacity,
      isReadOnly(),
      isBigEndian
    )
  }

  @inline
  def generic_duplicate()(implicit
      newHeapBufferView: NewThisHeapBufferView
  ): BufferType = {
    val result = newHeapBufferView(
      capacity(),
      _byteArray,
      _offset,
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
      newHeapBufferView: NewThisHeapBufferView
  ): BufferType = {
    val result = newHeapBufferView(
      capacity(),
      _byteArray,
      _offset,
      position(),
      limit(),
      true,
      isBigEndian
    )
    result._mark = _mark
    result
  }

  @inline
  def generic_compact()(implicit
      newHeapBufferView: NewThisHeapBufferView
  ): BufferType = {
    if (isReadOnly())
      throw new ReadOnlyBufferException

    val len = remaining()
    val bytesPerElem = newHeapBufferView.bytesPerElem
    System.arraycopy(
      _byteArray,
      _offset + bytesPerElem * position(),
      _byteArray,
      _offset,
      bytesPerElem * len
    )
    _mark = -1
    limit(capacity())
    position(len)
    self
  }

  @inline
  def generic_order(): ByteOrder =
    if (isBigEndian) ByteOrder.BIG_ENDIAN
    else ByteOrder.LITTLE_ENDIAN

  @inline
  def byteArrayBits(implicit
      newHeapBufferView: NewThisHeapBufferView
  ): ByteArrayBits = {
    ByteArrayBits(
      _byteArray.at(0),
      _offset,
      isBigEndian,
      newHeapBufferView.bytesPerElem
    )
  }

}
