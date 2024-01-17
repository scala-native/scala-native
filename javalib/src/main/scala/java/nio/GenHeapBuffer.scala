package java.nio

import java.util.Objects

// Ported from Scala.js

private[nio] object GenHeapBuffer {
  def apply[B <: Buffer](self: B): GenHeapBuffer[B] =
    new GenHeapBuffer(self)

  trait NewHeapBuffer[BufferType <: Buffer, ElementType] {
    def apply(
        capacity: Int,
        array: Array[ElementType],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): BufferType
  }

  @inline
  def generic_wrap[BufferType <: Buffer, ElementType](
      array: Array[ElementType],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  )(implicit
      newHeapBuffer: NewHeapBuffer[BufferType, ElementType]
  ): BufferType = {
    if (capacity < 0) {
      throw new IllegalArgumentException()
    }
    if (arrayOffset < 0 ||
        arrayOffset + capacity > array.length)
      throw new IndexOutOfBoundsException
    val initialLimit = initialPosition + initialLength
    if (initialPosition < 0 || initialLength < 0 || initialLimit > capacity)
      throw new IndexOutOfBoundsException
    newHeapBuffer(
      capacity,
      array,
      arrayOffset,
      initialPosition,
      initialLimit,
      isReadOnly
    )
  }
}

private[nio] final class GenHeapBuffer[B <: Buffer](val self: B)
    extends AnyVal {
  import self._

  type NewThisHeapBuffer = GenHeapBuffer.NewHeapBuffer[BufferType, ElementType]

  @inline
  def generic_slice()(implicit newHeapBuffer: NewThisHeapBuffer): BufferType = {
    val newCapacity = remaining()
    newHeapBuffer(
      newCapacity,
      _array,
      _offset + position(),
      0,
      newCapacity,
      isReadOnly()
    )
  }

  @inline
  def generic_slice(index: Int, length: Int)(implicit
      newHeapBuffer: NewThisHeapBuffer
  ): BufferType = {
    Objects.checkFromIndexSize(index, length, limit())
    val newCapacity = length
    newHeapBuffer(
      newCapacity,
      _array,
      _offset + index,
      0,
      newCapacity,
      isReadOnly()
    )
  }

  @inline
  def generic_duplicate()(implicit
      newHeapBuffer: NewThisHeapBuffer
  ): BufferType = {
    val result =
      newHeapBuffer(
        capacity(),
        _array,
        _offset,
        position(),
        limit(),
        isReadOnly()
      )
    result._mark = _mark
    result
  }

  @inline
  def generic_asReadOnlyBuffer()(implicit
      newHeapBuffer: NewThisHeapBuffer
  ): BufferType = {
    val result =
      newHeapBuffer(capacity(), _array, _offset, position(), limit(), true)
    result._mark = _mark
    result
  }

  @inline
  def generic_compact(): BufferType = {
    ensureNotReadOnly()

    val len = remaining()
    System
      .arraycopy(_array, _offset + position(), _array, _offset, len)
    _mark = -1
    limit(capacity())
    position(len)
    self
  }

  @inline
  def generic_load(index: Int): ElementType =
    _array(_offset + index)

  @inline
  def generic_store(index: Int, elem: ElementType): Unit =
    _array(_offset + index) = elem

  @inline
  def generic_load(
      startIndex: Int,
      dst: Array[ElementType],
      offset: Int,
      length: Int
  ): Unit =
    System.arraycopy(_array, _offset + startIndex, dst, offset, length)

  @inline
  def generic_store(
      startIndex: Int,
      src: Array[ElementType],
      offset: Int,
      length: Int
  ): Unit =
    System.arraycopy(src, offset, _array, _offset + startIndex, length)
}
