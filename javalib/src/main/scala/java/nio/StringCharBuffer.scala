package java.nio

import java.util.Objects

// Ported from Scala.js

private[nio] final class StringCharBuffer private (
    _capacity: Int,
    _csq: CharSequence,
    _csqOffset: Int,
    _initialPosition: Int,
    _initialLimit: Int
) extends CharBuffer(_capacity, null) { // TODO: eliminate nulls

  position(_initialPosition)
  limit(_initialLimit)

  private def genBuffer = GenBuffer[CharBuffer](this)

  def isReadOnly(): Boolean = true

  def isDirect(): Boolean = false

  def slice(): CharBuffer = {
    val cap = remaining()
    new StringCharBuffer(cap, _csq, _csqOffset + position(), 0, cap)
  }

  // Since JDK 13
  def slice(index: Int, length: Int): CharBuffer = {
    Objects.checkFromIndexSize(index, length, limit())
    val cap = length
    new StringCharBuffer(cap, _csq, _csqOffset + index, 0, cap)
  }

  def duplicate(): CharBuffer = {
    val result =
      new StringCharBuffer(capacity(), _csq, _csqOffset, position(), limit())
    result._mark = this._mark
    result
  }

  def asReadOnlyBuffer(): CharBuffer = duplicate()

  def subSequence(start: Int, end: Int): CharBuffer = {
    if (start < 0 || end < start || end > remaining())
      throw new IndexOutOfBoundsException
    new StringCharBuffer(
      capacity(),
      _csq,
      _csqOffset,
      position() + start,
      position() + end
    )
  }

  @noinline
  def get(): Char =
    genBuffer.generic_get()

  def put(c: Char): CharBuffer =
    throw new ReadOnlyBufferException

  @noinline
  override def get(index: Int): Char =
    genBuffer.generic_get(index)

  override def put(index: Int, c: Char): CharBuffer =
    throw new ReadOnlyBufferException

  @noinline
  override def get(dst: Array[Char], offset: Int, length: Int): CharBuffer =
    genBuffer.generic_get(dst, offset, length)

  override def put(src: Array[Char], offset: Int, length: Int): CharBuffer =
    throw new ReadOnlyBufferException

  def compact(): CharBuffer =
    throw new ReadOnlyBufferException

  override def toString(): String = {
    val offset = _csqOffset
    _csq.subSequence(position() + offset, limit() + offset).toString()
  }

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] override def load(index: Int): Char =
    _csq.charAt(_csqOffset + index)

  @inline
  private[nio] override def store(index: Int, elem: Char): Unit =
    throw new ReadOnlyBufferException

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Char],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Char],
      offset: Int,
      length: Int
  ): Unit =
    throw new ReadOnlyBufferException
}

private[nio] object StringCharBuffer {
  private[nio] def wrap(
      csq: CharSequence,
      csqOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int
  ): CharBuffer = {
    if (csqOffset < 0 || capacity < 0 || csqOffset + capacity > csq.length())
      throw new IndexOutOfBoundsException
    val initialLimit = initialPosition + initialLength
    if (initialPosition < 0 || initialLength < 0 || initialLimit > capacity)
      throw new IndexOutOfBoundsException
    new StringCharBuffer(
      capacity,
      csq,
      csqOffset,
      initialPosition,
      initialLimit
    )
  }
}
