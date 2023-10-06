package java.nio

import scala.scalanative.runtime.ByteArray
import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc.string
import java.lang.annotation.ElementType

private[nio] object GenPointerBuffer {
  def apply[B <: Buffer](self: B): GenPointerBuffer[B] =
    new GenPointerBuffer(self)

  trait NewPointerBuffer[BufferType <: Buffer] {
    def apply(
        ptr: Ptr[Byte],
        capacity: Int,
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        isReadOnly: Boolean
    ): BufferType
  }
}

private[nio] final class GenPointerBuffer[B <: Buffer](val self: B)
    extends AnyVal {
  import self._

  type NewPointerBuffer =
    GenPointerBuffer.NewPointerBuffer[BufferType]

  @inline
  def generic_slice()(implicit
      newPointerBuffer: NewPointerBuffer
  ): BufferType = {
    val newCapacity = remaining()
    newPointerBuffer(
      ptr = _rawDataPointer,
      capacity = newCapacity,
      arrayOffset = _offset + position(),
      initialPosition = 0,
      initialLimit = newCapacity,
      isReadOnly = isReadOnly()
    )
  }

  @inline
  def generic_duplicate()(implicit
      newPointerBuffer: NewPointerBuffer
  ): BufferType = {
    val result =
      newPointerBuffer(
        ptr = _rawDataPointer,
        capacity = capacity(),
        arrayOffset = _offset,
        initialPosition = position(),
        initialLimit = limit(),
        isReadOnly = isReadOnly()
      )
    result._mark = _mark
    result
  }

  @inline
  def generic_asReadOnlyBuffer()(implicit
      newPointerBuffer: NewPointerBuffer
  ): BufferType = {
    val result =
      newPointerBuffer(
        ptr = _rawDataPointer,
        capacity = capacity(),
        arrayOffset = _offset,
        initialPosition = position(),
        initialLimit = limit(),
        isReadOnly = true
      )
    result._mark = _mark
    result
  }

  @inline
  def generic_compact(): BufferType = {
    ensureNotReadOnly()

    val length = remaining()
    val dstPtr = _rawDataPointer + _offset
    val srcPtr = dstPtr + position()

    string.memcpy(dstPtr, srcPtr, length.toUInt)

    _mark = -1
    limit(capacity())
    position(length)
    self
  }

  @inline
  def generic_load(index: Int): Byte =
    _rawDataPointer(_offset + index)

  @inline
  def generic_store(index: Int, elem: Byte): Unit =
    _rawDataPointer(_offset + index) = elem

  @inline
  def generic_load(
      startIndex: Int,
      dst: Array[Byte],
      offset: Int,
      length: Int
  ): Unit = {
    if (length < 0) {
      throw new ArrayIndexOutOfBoundsException("length is negative")
    } else if (startIndex < 0 || startIndex + length > _capacity) {
      throw new ArrayIndexOutOfBoundsException(startIndex)
    } else if (offset < 0 || offset + length > dst.length) {
      throw new ArrayIndexOutOfBoundsException(offset)
    } else if (length == 0) {
      ()
    } else {
      val dstPtr = dst.atUnsafe(0) + offset
      val srcPtr = _rawDataPointer + startIndex
      string.memcpy(dstPtr, srcPtr, length.toUInt)
    }
  }

  @inline
  def generic_store(
      startIndex: Int,
      src: Array[Byte],
      offset: Int,
      length: Int
  ): Unit = {
    if (length < 0) {
      throw new ArrayIndexOutOfBoundsException("length is negative")
    } else if (startIndex < 0 || startIndex + length > _capacity) {
      throw new ArrayIndexOutOfBoundsException(startIndex)
    } else if (offset < 0 || offset + length > src.length) {
      throw new ArrayIndexOutOfBoundsException(offset)
    } else if (length == 0) {
      ()
    } else {
      val srcPtr = src.atUnsafe(0) + offset
      val dstPtr = _rawDataPointer + startIndex
      string.memcpy(dstPtr, srcPtr, length.toUInt)
    }
  }
}
