package java.nio

// Ported from Scala.js

abstract class Buffer private[nio] (val _capacity: Int) {
  private[nio] type ElementType

  private[nio] type BufferType >: this.type <: Buffer {
    type ElementType = Buffer.this.ElementType
  }

  // Normal implementation of Buffer

  private var _limit: Int     = capacity()
  private var _position: Int  = 0
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

  override def toString(): String =
    s"${getClass.getName}[pos=${position()} lim=${limit()} cap=${capacity()}]"

  /* Generic access to methods declared in subclasses.
   * These methods allow to write generic algorithms on any kind of Buffer.
   * The optimizer will get rid of all the overhead.
   * We only declare the methods we need somewhere.
   */

  private[nio] def _array: Array[ElementType]
  private[nio] def _arrayOffset: Int

  /** Loads an element at the given absolute, unchecked index. */
  private[nio] def load(index: Int): ElementType

  /** Stores an element at the given absolute, unchecked index. */
  private[nio] def store(index: Int, elem: ElementType): Unit

  /** Loads a range of elements with absolute, unchecked indices. */
  private[nio] def load(startIndex: Int,
                        dst: Array[ElementType],
                        offset: Int,
                        length: Int): Unit

  /** Stores a range of elements with absolute, unchecked indices. */
  private[nio] def store(startIndex: Int,
                         src: Array[ElementType],
                         offset: Int,
                         length: Int): Unit

  /* Only for HeapByteBufferViews -- but that's the only place we can put it.
   * For all other types, it will be dce'ed.
   */
  private[nio] def _byteArray: Array[Byte] =
    throw new UnsupportedOperationException
  private[nio] def _byteArrayOffset: Int =
    throw new UnsupportedOperationException
  private[nio] def isBigEndian: Boolean =
    throw new UnsupportedOperationException

  // Helpers

  @inline private[nio] def ensureNotReadOnly(): Unit = {
    if (isReadOnly())
      throw new ReadOnlyBufferException
  }

  @inline private[nio] def validateArrayIndexRange(array: Array[_],
                                                   offset: Int,
                                                   length: Int): Unit = {
    if (offset < 0 || length < 0 || offset > array.length - length)
      throw new IndexOutOfBoundsException
  }

  @inline private[nio] def getPosAndAdvanceRead(): Int = {
    val p = _position
    if (p == limit())
      throw new BufferUnderflowException
    _position = p + 1
    p
  }

  @inline private[nio] def getPosAndAdvanceRead(length: Int): Int = {
    val p      = _position
    val newPos = p + length
    if (newPos > limit())
      throw new BufferUnderflowException
    _position = newPos
    p
  }

  @inline private[nio] def getPosAndAdvanceWrite(): Int = {
    val p = _position
    if (p == limit())
      throw new BufferOverflowException
    _position = p + 1
    p
  }

  @inline private[nio] def getPosAndAdvanceWrite(length: Int): Int = {
    val p      = _position
    val newPos = p + length
    if (newPos > limit())
      throw new BufferOverflowException
    _position = newPos
    p
  }

  @inline private[nio] def validateIndex(index: Int): Int = {
    if (index < 0 || index >= limit())
      throw new IndexOutOfBoundsException
    index
  }

  @inline private[nio] def validateIndex(index: Int, length: Int): Int = {
    if (index < 0 || index + length > limit())
      throw new IndexOutOfBoundsException
    index
  }
}
