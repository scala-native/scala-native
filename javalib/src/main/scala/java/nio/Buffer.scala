package java.nio

import scala.scalanative.runtime.{fromRawPtr, toRawPtr}
// Ported from Scala.js
import scala.scalanative.unsafe

abstract class Buffer private[nio] (
    val _capacity: Int,
    _address: unsafe.CVoidPtr
) {
  private[nio] type ElementType

  private[nio] type BufferType >: this.type <: Buffer {
    type ElementType = Buffer.this.ElementType
  }

  // TODO: Teach optimizer to convert Ptr[A].asInstanceOf[Ptr[B]] as identity
  // Keep only RawPtr as field, this way optimizer would erase boxed variant
  protected val _rawAddress = toRawPtr(_address)
  private[nio] def address: unsafe.Ptr[Byte] = fromRawPtr(_rawAddress)
  private[nio] def data: unsafe.Ptr[ElementType] = fromRawPtr(_rawAddress)

  // Normal implementation of Buffer

  private var _limit: Int = capacity()
  private var _position: Int = 0
  private[nio] var _mark: Int = -1

  final def capacity(): Int = _capacity

  final def position(): Int = _position

  def position(newPosition: Int): Buffer = {
    if (newPosition < 0 || newPosition > limit())
      throw new IllegalArgumentException
    _position = newPosition
    if (_mark > newPosition)
      _mark = -1
    this
  }

  final def limit(): Int = _limit

  def limit(newLimit: Int): Buffer = {
    if (newLimit < 0 || newLimit > capacity())
      throw new IllegalArgumentException
    _limit = newLimit
    if (_position > newLimit) {
      _position = newLimit
      if (_mark > newLimit)
        _mark = -1
    }
    this
  }

  def mark(): Buffer = {
    _mark = _position
    this
  }

  def reset(): Buffer = {
    if (_mark == -1)
      throw new InvalidMarkException
    _position = _mark
    this
  }

  def clear(): Buffer = {
    _mark = -1
    _position = 0
    _limit = capacity()
    this
  }

  def flip(): Buffer = {
    _mark = -1
    _limit = _position
    _position = 0
    this
  }

  def rewind(): Buffer = {
    _mark = -1
    _position = 0
    this
  }

  @inline final def remaining(): Int = limit() - position()

  @inline final def hasRemaining(): Boolean = position() != limit()

  def isReadOnly(): Boolean

  def hasArray(): Boolean

  /* Note: in the JDK, this returns Object.
   * But Array[ElementType] erases to Object so this is binary compatible.
   */
  def array(): Array[ElementType]

  def arrayOffset(): Int

  def isDirect(): Boolean

  // Since JDK 9
  def slice(): Buffer
  // Since JDK 13
  def slice(index: Int, length: Int): Buffer

  // Since JDK 9
  def duplicate(): Buffer

  override def toString(): String =
    s"${getClass.getName}[pos=${position()} lim=${limit()} cap=${capacity()}]"

  // Extended API
  final def hasPointer(): Boolean = _rawDataPointer != null && !isReadOnly()

  final def pointer(): unsafe.Ptr[Byte] = {
    val ptr = _rawDataPointer
    if (ptr == null || isReadOnly())
      throw new UnsupportedOperationException
    ptr
  }

  /* Generic access to methods declared in subclasses.
   * These methods allow to write generic algorithms on any kind of Buffer.
   * The optimizer will get rid of all the overhead.
   * We only declare the methods we need somewhere.
   */

  private[nio] def _array: Array[ElementType] = null
  private[nio] def _offset: Int

  // MappedByteBuffer specific
  private[nio] def _mappedData: MappedByteBufferData = null

  // PointerByteBuffer specific
  private[nio] def _rawDataPointer: unsafe.Ptr[Byte] = null

  // HeapByteBuffer specific
  private[nio] def _byteArray: Array[Byte] =
    throw new UnsupportedOperationException
  private[nio] def isBigEndian: Boolean =
    throw new UnsupportedOperationException

  /** Loads an element at the given absolute, unchecked index. */
  private[nio] def load(index: Int): ElementType

  /** Stores an element at the given absolute, unchecked index. */
  private[nio] def store(index: Int, elem: ElementType): Unit

  /** Loads a range of elements with absolute, unchecked indices. */
  private[nio] def load(
      startIndex: Int,
      dst: Array[ElementType],
      offset: Int,
      length: Int
  ): Unit

  /** Stores a range of elements with absolute, unchecked indices. */
  private[nio] def store(
      startIndex: Int,
      src: Array[ElementType],
      offset: Int,
      length: Int
  ): Unit

  // Helpers

  @inline private[nio] def ensureNotReadOnly(): Unit = {
    if (isReadOnly())
      throw new ReadOnlyBufferException
  }

  @inline private[nio] def validateArrayIndexRange(
      array: Array[_],
      offset: Int,
      length: Int
  ): Unit = {
    if (offset < 0 || length < 0 || offset > array.length - length)
      throwOutOfBounds(offset)
  }

  @inline private[nio] def getPosAndAdvanceRead(): Int = {
    val p = _position
    if (p >= limit()) throwBufferUnderflow(p)
    _position = p + 1
    p
  }

  @inline private[nio] def getPosAndAdvanceRead(length: Int): Int = {
    val p = _position
    val newPos = p + length
    if (newPos > limit()) throwBufferUnderflow(newPos)
    _position = newPos
    p
  }

  @inline private[nio] def getPosAndAdvanceWrite(): Int = {
    val p = _position
    if (p >= limit()) throwBufferOverflow(p)
    _position = p + 1
    p
  }

  @inline private[nio] def getPosAndAdvanceWrite(length: Int): Int = {
    val p = _position
    val newPos = p + length
    if (newPos > limit()) throwBufferOverflow(newPos)
    _position = newPos
    p
  }

  @inline private[nio] def validateIndex(index: Int): Int = {
    if (index < 0 || index >= limit()) throwOutOfBounds(index)
    else index
  }

  @inline private[nio] def validateIndex(index: Int, length: Int): Int = {
    if (index < 0) throwOutOfBounds(index)
    else if (index + length > limit()) throwOutOfBounds(index + length)
    else index
  }

  private def throwOutOfBounds(index: Int): Nothing =
    throw new IndexOutOfBoundsException(
      s"Index $index out of bounds for length ${limit()}"
    )
  private def throwBufferUnderflow(index: Int): Nothing =
    throw new BufferUnderflowException(
      s"Access at index $index underflows buffer of length ${limit()}"
    )
  private def throwBufferOverflow(index: Int): Nothing =
    throw new BufferOverflowException(
      s"Access at index $index overflows buffer of length ${limit()}"
    )
}
