package java.nio

import java.util.Objects

import scala.scalanative.libc.string
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

// Based on the code ported from Scala.js,
// see GenHeapBufferView.scala

private[nio] object GenPointerBufferView {
  def apply[B <: Buffer](self: B): GenPointerBufferView[B] =
    new GenPointerBufferView(self)

  trait NewPointerBufferView[BufferType <: Buffer] {
    def bytesPerElem: Int

    def apply(
        capacity: Int,
        ptr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): BufferType
  }

  @inline
  def generic_fromPointerByteBuffer[BufferType <: Buffer](
      byteBuffer: PointerByteBuffer
  )(implicit
      newPointerBufferView: NewPointerBufferView[BufferType]
  ): BufferType = {
    val byteBufferPos = byteBuffer.position()
    val viewCapacity =
      (byteBuffer.limit() - byteBufferPos) / newPointerBufferView.bytesPerElem
    newPointerBufferView(
      viewCapacity,
      byteBuffer._rawDataPointer,
      byteBuffer._offset + byteBufferPos,
      0,
      viewCapacity,
      byteBuffer.isReadOnly(),
      byteBuffer.isBigEndian
    )
  }
}

private[nio] final class GenPointerBufferView[B <: Buffer](val self: B)
    extends AnyVal {
  import self._

  type NewThisPointerBufferView =
    GenPointerBufferView.NewPointerBufferView[BufferType]

  @inline
  def generic_slice()(implicit
      newPointerBufferView: NewThisPointerBufferView
  ): BufferType = {
    val newCapacity = remaining()
    val bytesPerElem = newPointerBufferView.bytesPerElem
    newPointerBufferView(
      newCapacity,
      _rawDataPointer,
      _offset + bytesPerElem * position(),
      0,
      newCapacity,
      isReadOnly(),
      isBigEndian
    )
  }

  @inline
  def generic_slice(index: Int, length: Int)(implicit
      newPointerBufferView: NewThisPointerBufferView
  ): BufferType = {
    Objects.checkFromIndexSize(index, length, limit())
    val newCapacity = length
    val bytesPerElem = newPointerBufferView.bytesPerElem
    newPointerBufferView(
      newCapacity,
      _rawDataPointer,
      _offset + bytesPerElem * index,
      0,
      newCapacity,
      isReadOnly(),
      isBigEndian
    )
  }

  @inline
  def generic_duplicate()(implicit
      newPointerBufferView: NewThisPointerBufferView
  ): BufferType = {
    val result = newPointerBufferView(
      capacity(),
      _rawDataPointer,
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
      newPointerBufferView: NewThisPointerBufferView
  ): BufferType = {
    val result = newPointerBufferView(
      capacity(),
      _rawDataPointer,
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
      newPointerBufferView: NewThisPointerBufferView
  ): BufferType = {
    ensureNotReadOnly()

    val length = remaining()
    val bytesPerElem = newPointerBufferView.bytesPerElem
    val dstPtr = _rawDataPointer + _offset * bytesPerElem
    val srcPtr = dstPtr + position() * bytesPerElem

    string.memcpy(dstPtr, srcPtr, (length * bytesPerElem).toUInt)

    _mark = -1
    limit(capacity())
    position(length)
    self
  }

  @inline
  def generic_order(): ByteOrder =
    if (isBigEndian) ByteOrder.BIG_ENDIAN
    else ByteOrder.LITTLE_ENDIAN

  @inline
  def byteArrayBits(implicit
      newPointerBufferView: NewThisPointerBufferView
  ): ByteArrayBits = {
    ByteArrayBits(
      _rawDataPointer,
      _offset,
      isBigEndian,
      newPointerBufferView.bytesPerElem
    )
  }

}
