package java.nio

import scala.scalanative.runtime.ByteArray
import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc.string

// Based on the code ported from Scala.js,
// see GenHeapBuffer.scala
private[nio] object GenMappedBuffer {
  def apply[B <: Buffer](self: B): GenMappedBuffer[B] =
    new GenMappedBuffer(self)

  trait NewMappedBuffer[BufferType <: Buffer, ElementType] {
    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        isReadOnly: Boolean
    ): BufferType
  }
}

private[nio] final class GenMappedBuffer[B <: Buffer](val self: B)
    extends AnyVal {
  import self._

  type NewThisMappedBuffer =
    GenMappedBuffer.NewMappedBuffer[BufferType, ElementType]

  @inline
  def generic_slice()(implicit
      newMappedBuffer: NewThisMappedBuffer
  ): BufferType = {
    val newCapacity = remaining()
    newMappedBuffer(
      newCapacity,
      _mappedData,
      _arrayOffset + position(),
      0,
      newCapacity,
      isReadOnly()
    )
  }

  @inline
  def generic_duplicate()(implicit
      newMappedBuffer: NewThisMappedBuffer
  ): BufferType = {
    val result =
      newMappedBuffer(
        capacity(),
        _mappedData,
        _arrayOffset,
        position(),
        limit(),
        isReadOnly()
      )
    result._mark = _mark
    result
  }

  @inline
  def generic_asReadOnlyBuffer()(implicit
      newMappedBuffer: NewThisMappedBuffer
  ): BufferType = {
    val result =
      newMappedBuffer(
        capacity(),
        _mappedData,
        _arrayOffset,
        position(),
        limit(),
        true
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
  def generic_load(index: Int): Byte =
    _mappedData(_arrayOffset + index)

  @inline
  def generic_store(index: Int, elem: Byte): Unit =
    _mappedData(_arrayOffset + index) = elem

  @inline
  def generic_load(
      startIndex: Int,
      dst: Array[ElementType],
      offset: Int,
      length: Int
  ): Unit = {
    if (length < 0) {
      throw new ArrayIndexOutOfBoundsException("length is negative")
    } else if (startIndex < 0 || startIndex + length > _mappedData.length) {
      throw new ArrayIndexOutOfBoundsException(startIndex)
    } else if (offset < 0 || offset + length > dst.length) {
      throw new ArrayIndexOutOfBoundsException(offset)
    } else if (length == 0) {
      ()
    } else {
      val dstPtr = dst.asInstanceOf[ByteArray].at(0) + offset
      val srcPtr = _mappedData.ptr + startIndex
      string.memcpy(dstPtr, srcPtr, length.toUInt)
    }
  }

  @inline
  def generic_store(
      startIndex: Int,
      src: Array[ElementType],
      offset: Int,
      length: Int
  ): Unit = {
    if (length < 0) {
      throw new ArrayIndexOutOfBoundsException("length is negative")
    } else if (startIndex < 0 || startIndex + length > _mappedData.length) {
      throw new ArrayIndexOutOfBoundsException(startIndex)
    } else if (offset < 0 || offset + length > src.length) {
      throw new ArrayIndexOutOfBoundsException(offset)
    } else if (length == 0) {
      ()
    } else {
      val srcPtr = src.asInstanceOf[ByteArray].at(0) + offset
      val dstPtr = _mappedData.ptr + startIndex
      string.memcpy(dstPtr, srcPtr, length.toUInt)
    }
  }
}
