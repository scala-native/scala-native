// format: off
package java.nio

// Ported from Scala.js
import scala.scalanative.unsafe
import scala.scalanative.unsafe.UnsafeRichArray

object ByteBuffer {
  private final val HashSeed = -547316498 // "java.nio.ByteBuffer".##

  def allocate(capacity: Int): ByteBuffer = wrap(new Array[Byte](capacity))

  def allocateDirect(capacity: Int): ByteBuffer = allocate(capacity)

  def wrap(array: Array[Byte], offset: Int, length: Int): ByteBuffer =
    HeapByteBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Byte]): ByteBuffer =  wrap(array, 0, array.length)


    // Extended API
  def wrapPointerByte(array: unsafe.Ptr[Byte], length: Int): ByteBuffer =
    PointerByteBuffer.wrap(array, length)
}

abstract class ByteBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Byte],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[ByteBuffer] 
  {
  private[nio] type ElementType = Byte
  private[nio] type BufferType = ByteBuffer

  private[nio] var _isBigEndian: Boolean = true

  // TODO: JDK11
  // def mismatch(that: ByteBuffer): Int  = ???

  private def genBuffer = GenBuffer[ByteBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Byte], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Byte], -1, address)

  def slice(): ByteBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): ByteBuffer

  def duplicate(): ByteBuffer

  def asReadOnlyBuffer(): ByteBuffer

  def get(): Byte

  def put(b: Byte): ByteBuffer

  def get(index: Int): Byte = load(validateIndex(index))

  def put(index: Int, elem: Byte): ByteBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Byte], offset: Int, length: Int): ByteBuffer = GenBuffer[ByteBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Byte]): ByteBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Byte], offset: Int, length: Int): ByteBuffer = GenBuffer[ByteBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Byte]): ByteBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Byte], offset: Int, length: Int): ByteBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Byte]): ByteBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: ByteBuffer): ByteBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: ByteBuffer, offset: Int, length: Int) = GenBuffer[ByteBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Byte], offset: Int, length: Int): ByteBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Byte]): ByteBuffer =
    put(src, 0, src.length)


  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Byte] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): ByteBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): ByteBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): ByteBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): ByteBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): ByteBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): ByteBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): ByteBuffer = {
    super.rewind()
    this
  }

  def compact(): ByteBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(ByteBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: ByteBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: ByteBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  final def order(): ByteOrder =
    if (_isBigEndian) ByteOrder.BIG_ENDIAN
    else ByteOrder.LITTLE_ENDIAN
  final def order(bo: ByteOrder): ByteBuffer = {
    if (bo == null)
      throw new NullPointerException
    _isBigEndian = bo == ByteOrder.BIG_ENDIAN
    this
  }
  // Since JDK 9
  final def alignedSlice(unitSize: Int): ByteBuffer = {
    val pos = position()
    val lim = limit()
    val alignedPos = alignmentOffset(pos, unitSize) match {
      case n if n > 0 => pos + (unitSize - n)
      case _ => pos
    }
    val alignedLimit = (lim - alignmentOffset(lim, unitSize))
    if(alignedPos > lim || alignedLimit < pos) slice(pos, 0)
    else slice(alignedPos, alignedLimit - alignedPos)
  }
   // Since JDK 9
  final def alignmentOffset(index: Int, unitSize: Int): Int = {
    require(index >= 0, "Index less then zero: " + index)
    require(unitSize >= 1 && (unitSize & (unitSize - 1)) == 0, "Unit size not a power of two: " + unitSize)
    if(unitSize > 8 && !isDirect()) throw new UnsupportedOperationException("Unit size unsupported for non-direct dufferes: " + unitSize)
    ((this.address.toLong + index) & (unitSize -1)).toInt
  }

  def getChar(): Char
  def putChar(value: Char): ByteBuffer
  def getChar(index: Int): Char
  def putChar(index: Int, value: Char): ByteBuffer

  def asCharBuffer(): CharBuffer

  def getShort(): Short
  def putShort(value: Short): ByteBuffer
  def getShort(index: Int): Short
  def putShort(index: Int, value: Short): ByteBuffer

  def asShortBuffer(): ShortBuffer

  def getInt(): Int
  def putInt(value: Int): ByteBuffer
  def getInt(index: Int): Int
  def putInt(index: Int, value: Int): ByteBuffer

  def asIntBuffer(): IntBuffer

  def getLong(): Long
  def putLong(value: Long): ByteBuffer
  def getLong(index: Int): Long
  def putLong(index: Int, value: Long): ByteBuffer

  def asLongBuffer(): LongBuffer

  def getFloat(): Float
  def putFloat(value: Float): ByteBuffer
  def getFloat(index: Int): Float
  def putFloat(index: Int, value: Float): ByteBuffer

  def asFloatBuffer(): FloatBuffer

  def getDouble(): Double
  def putDouble(value: Double): ByteBuffer
  def getDouble(index: Int): Double
  def putDouble(index: Int, value: Double): ByteBuffer

  def asDoubleBuffer(): DoubleBuffer


  // Internal API
  override private[nio] def isBigEndian: Boolean = _isBigEndian

  // @inline
  private[nio] def load(index: Int): Byte = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Byte): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Byte],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

object CharBuffer {
  private final val HashSeed = -182887236 // "java.nio.CharBuffer".##

  def allocate(capacity: Int): CharBuffer = wrap(new Array[Char](capacity))


  def wrap(array: Array[Char], offset: Int, length: Int): CharBuffer =
    HeapCharBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Char]): CharBuffer =  wrap(array, 0, array.length)

  def wrap(csq: CharSequence, start: Int, end: Int): CharBuffer =
    StringCharBuffer.wrap(csq, 0, csq.length(), start, end - start)

  def wrap(csq: CharSequence): CharBuffer = wrap(csq, 0, csq.length())

    // Extended API
}

abstract class CharBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Char],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[CharBuffer] 
    with CharSequence
    with Appendable
    with Readable
  {
  private[nio] type ElementType = Char
  private[nio] type BufferType = CharBuffer


  // TODO: JDK11
  // def mismatch(that: CharBuffer): Int  = ???

  private def genBuffer = GenBuffer[CharBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Char], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Char], -1, address)

  def slice(): CharBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): CharBuffer

  def duplicate(): CharBuffer

  def asReadOnlyBuffer(): CharBuffer

  def get(): Char

  def put(b: Char): CharBuffer

  def get(index: Int): Char = load(validateIndex(index))

  def put(index: Int, elem: Char): CharBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Char], offset: Int, length: Int): CharBuffer = GenBuffer[CharBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Char]): CharBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Char], offset: Int, length: Int): CharBuffer = GenBuffer[CharBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Char]): CharBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Char], offset: Int, length: Int): CharBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Char]): CharBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: CharBuffer): CharBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: CharBuffer, offset: Int, length: Int) = GenBuffer[CharBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Char], offset: Int, length: Int): CharBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Char]): CharBuffer =
    put(src, 0, src.length)

  def put(src: String, start: Int, end: Int): CharBuffer =
    put(CharBuffer.wrap(src, start, end))

  final def put(src: String): CharBuffer =
    put(src, 0, src.length)

  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Char] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): CharBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): CharBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): CharBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): CharBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): CharBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): CharBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): CharBuffer = {
    super.rewind()
    this
  }

  def compact(): CharBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(CharBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: CharBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: CharBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  def order(): ByteOrder

  override def toString(): String = {
    if (_array != null) {
      // even if read-only
      new String(_array, position() + _offset, remaining())
    } else {
      val chars = new Array[Char](remaining())
      val savedPos = position()
      get(chars)
      position(savedPos)
      new String(chars)
    }
  }

  final def length(): Int = remaining()

  final def charAt(index: Int): Char = get(position() + index)

  def subSequence(start: Int, end: Int): CharSequence

  def append(csq: CharSequence): CharBuffer =
    put(csq.toString())

  def append(csq: CharSequence, start: Int, end: Int): CharBuffer =
    put(csq.subSequence(start, end).toString())

  def append(c: Char): CharBuffer =
    put(c)

  def read(target: CharBuffer): Int = {
    // Attention: this method must not change this buffer's position
    val n = remaining()
    if (n == 0) -1
    else if (_array != null) {
      // even if read-only
      genBuffer.generic_put(_array, _offset, n)
      n
    } else {
      val savedPos = position()
      target.put(this)
      position(savedPos)
      n
    }
  }

  // Internal API

  // @inline
  private[nio] def load(index: Int): Char = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Char): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Char],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Char],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

object ShortBuffer {
  private final val HashSeed = 383731478 // "java.nio.ShortBuffer".##

  def allocate(capacity: Int): ShortBuffer = wrap(new Array[Short](capacity))


  def wrap(array: Array[Short], offset: Int, length: Int): ShortBuffer =
    HeapShortBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Short]): ShortBuffer =  wrap(array, 0, array.length)


    // Extended API
}

abstract class ShortBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Short],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[ShortBuffer] 
  {
  private[nio] type ElementType = Short
  private[nio] type BufferType = ShortBuffer


  // TODO: JDK11
  // def mismatch(that: ShortBuffer): Int  = ???

  private def genBuffer = GenBuffer[ShortBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Short], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Short], -1, address)

  def slice(): ShortBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): ShortBuffer

  def duplicate(): ShortBuffer

  def asReadOnlyBuffer(): ShortBuffer

  def get(): Short

  def put(b: Short): ShortBuffer

  def get(index: Int): Short = load(validateIndex(index))

  def put(index: Int, elem: Short): ShortBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Short], offset: Int, length: Int): ShortBuffer = GenBuffer[ShortBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Short]): ShortBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Short], offset: Int, length: Int): ShortBuffer = GenBuffer[ShortBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Short]): ShortBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Short], offset: Int, length: Int): ShortBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Short]): ShortBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: ShortBuffer): ShortBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: ShortBuffer, offset: Int, length: Int) = GenBuffer[ShortBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Short], offset: Int, length: Int): ShortBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Short]): ShortBuffer =
    put(src, 0, src.length)


  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Short] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): ShortBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): ShortBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): ShortBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): ShortBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): ShortBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): ShortBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): ShortBuffer = {
    super.rewind()
    this
  }

  def compact(): ShortBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(ShortBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: ShortBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: ShortBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  def order(): ByteOrder


  // Internal API

  // @inline
  private[nio] def load(index: Int): Short = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Short): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Short],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Short],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

object IntBuffer {
  private final val HashSeed = 39599817 // "java.nio.IntBuffer".##

  def allocate(capacity: Int): IntBuffer = wrap(new Array[Int](capacity))


  def wrap(array: Array[Int], offset: Int, length: Int): IntBuffer =
    HeapIntBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Int]): IntBuffer =  wrap(array, 0, array.length)


    // Extended API
}

abstract class IntBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Int],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[IntBuffer] 
  {
  private[nio] type ElementType = Int
  private[nio] type BufferType = IntBuffer


  // TODO: JDK11
  // def mismatch(that: IntBuffer): Int  = ???

  private def genBuffer = GenBuffer[IntBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Int], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Int], -1, address)

  def slice(): IntBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): IntBuffer

  def duplicate(): IntBuffer

  def asReadOnlyBuffer(): IntBuffer

  def get(): Int

  def put(b: Int): IntBuffer

  def get(index: Int): Int = load(validateIndex(index))

  def put(index: Int, elem: Int): IntBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Int], offset: Int, length: Int): IntBuffer = GenBuffer[IntBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Int]): IntBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Int], offset: Int, length: Int): IntBuffer = GenBuffer[IntBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Int]): IntBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Int], offset: Int, length: Int): IntBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Int]): IntBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: IntBuffer): IntBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: IntBuffer, offset: Int, length: Int) = GenBuffer[IntBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Int], offset: Int, length: Int): IntBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Int]): IntBuffer =
    put(src, 0, src.length)


  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Int] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): IntBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): IntBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): IntBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): IntBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): IntBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): IntBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): IntBuffer = {
    super.rewind()
    this
  }

  def compact(): IntBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(IntBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: IntBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: IntBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  def order(): ByteOrder


  // Internal API

  // @inline
  private[nio] def load(index: Int): Int = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Int): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Int],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Int],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

object LongBuffer {
  private final val HashSeed = -1709696158 // "java.nio.LongBuffer".##

  def allocate(capacity: Int): LongBuffer = wrap(new Array[Long](capacity))


  def wrap(array: Array[Long], offset: Int, length: Int): LongBuffer =
    HeapLongBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Long]): LongBuffer =  wrap(array, 0, array.length)


    // Extended API
}

abstract class LongBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Long],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[LongBuffer] 
  {
  private[nio] type ElementType = Long
  private[nio] type BufferType = LongBuffer


  // TODO: JDK11
  // def mismatch(that: LongBuffer): Int  = ???

  private def genBuffer = GenBuffer[LongBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Long], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Long], -1, address)

  def slice(): LongBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): LongBuffer

  def duplicate(): LongBuffer

  def asReadOnlyBuffer(): LongBuffer

  def get(): Long

  def put(b: Long): LongBuffer

  def get(index: Int): Long = load(validateIndex(index))

  def put(index: Int, elem: Long): LongBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Long], offset: Int, length: Int): LongBuffer = GenBuffer[LongBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Long]): LongBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Long], offset: Int, length: Int): LongBuffer = GenBuffer[LongBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Long]): LongBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Long]): LongBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: LongBuffer): LongBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: LongBuffer, offset: Int, length: Int) = GenBuffer[LongBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Long]): LongBuffer =
    put(src, 0, src.length)


  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Long] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): LongBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): LongBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): LongBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): LongBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): LongBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): LongBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): LongBuffer = {
    super.rewind()
    this
  }

  def compact(): LongBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(LongBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: LongBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: LongBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  def order(): ByteOrder


  // Internal API

  // @inline
  private[nio] def load(index: Int): Long = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Long): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Long],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Long],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

object FloatBuffer {
  private final val HashSeed = 1920204022 // "java.nio.FloatBuffer".##

  def allocate(capacity: Int): FloatBuffer = wrap(new Array[Float](capacity))


  def wrap(array: Array[Float], offset: Int, length: Int): FloatBuffer =
    HeapFloatBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Float]): FloatBuffer =  wrap(array, 0, array.length)


    // Extended API
}

abstract class FloatBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Float],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[FloatBuffer] 
  {
  private[nio] type ElementType = Float
  private[nio] type BufferType = FloatBuffer


  // TODO: JDK11
  // def mismatch(that: FloatBuffer): Int  = ???

  private def genBuffer = GenBuffer[FloatBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Float], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Float], -1, address)

  def slice(): FloatBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): FloatBuffer

  def duplicate(): FloatBuffer

  def asReadOnlyBuffer(): FloatBuffer

  def get(): Float

  def put(b: Float): FloatBuffer

  def get(index: Int): Float = load(validateIndex(index))

  def put(index: Int, elem: Float): FloatBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Float], offset: Int, length: Int): FloatBuffer = GenBuffer[FloatBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Float]): FloatBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Float], offset: Int, length: Int): FloatBuffer = GenBuffer[FloatBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Float]): FloatBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Float]): FloatBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: FloatBuffer): FloatBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: FloatBuffer, offset: Int, length: Int) = GenBuffer[FloatBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Float]): FloatBuffer =
    put(src, 0, src.length)


  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Float] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): FloatBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): FloatBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): FloatBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): FloatBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): FloatBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): FloatBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): FloatBuffer = {
    super.rewind()
    this
  }

  def compact(): FloatBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(FloatBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: FloatBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: FloatBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  def order(): ByteOrder


  // Internal API

  // @inline
  private[nio] def load(index: Int): Float = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Float): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Float],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Float],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

object DoubleBuffer {
  private final val HashSeed = 2140173175 // "java.nio.DoubleBuffer".##

  def allocate(capacity: Int): DoubleBuffer = wrap(new Array[Double](capacity))


  def wrap(array: Array[Double], offset: Int, length: Int): DoubleBuffer =
    HeapDoubleBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Double]): DoubleBuffer =  wrap(array, 0, array.length)


    // Extended API
}

abstract class DoubleBuffer private[nio] (
    _capacity: Int,
    override private[nio] val _array: Array[Double],
    private[nio] val _offset: Int,
    _address: unsafe.Ptr[_],
) extends Buffer(_capacity, _address)
    with Comparable[DoubleBuffer] 
  {
  private[nio] type ElementType = Double
  private[nio] type BufferType = DoubleBuffer


  // TODO: JDK11
  // def mismatch(that: DoubleBuffer): Int  = ???

  private def genBuffer = GenBuffer[DoubleBuffer](this)

  private[nio] def this(_capacity: Int, _array: Array[Double], _offset: Int) = this(_capacity, _array, _offset, _array.atUnsafe(_offset))
  private[nio] def this(_capacity: Int, address: unsafe.Ptr[_]) = this(_capacity, null: Array[Double], -1, address)

  def slice(): DoubleBuffer
  // Since JDK 13
  def slice(index: Int, length: Int): DoubleBuffer

  def duplicate(): DoubleBuffer

  def asReadOnlyBuffer(): DoubleBuffer

  def get(): Double

  def put(b: Double): DoubleBuffer

  def get(index: Int): Double = load(validateIndex(index))

  def put(index: Int, elem: Double): DoubleBuffer = {
    ensureNotReadOnly()
    store(validateIndex(index), elem)
    this
  }
  
  // Since: JDK 13
  def get(index: Int, dst: Array[Double], offset: Int, length: Int): DoubleBuffer = GenBuffer[DoubleBuffer](this).generic_get(index, dst, offset, length)
  def get(index: Int, dst: Array[Double]): DoubleBuffer = get(index, dst, 0, dst.length) 

  // Since: JDK13
  def put(index: Int, src: Array[Double], offset: Int, length: Int): DoubleBuffer = GenBuffer[DoubleBuffer](this).generic_put(index, src, offset, length)
  def put(index: Int, src: Array[Double]): DoubleBuffer = put(index, src, 0, src.length)  

  @noinline
  def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    genBuffer.generic_get(dst, offset, length)

  def get(dst: Array[Double]): DoubleBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: DoubleBuffer): DoubleBuffer =
    genBuffer.generic_put(src)
    // Since: JDK16
  def put(index: Int, src: DoubleBuffer, offset: Int, length: Int) = GenBuffer[DoubleBuffer](this).generic_put(index, src, offset, length)
    
  @noinline
  def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    genBuffer.generic_put(src, offset, length)

  final def put(src: Array[Double]): DoubleBuffer =
    put(src, 0, src.length)


  @inline final def hasArray(): Boolean =
    genBuffer.generic_hasArray()

  @inline final def array(): Array[Double] =
    genBuffer.generic_array()

  @inline final def arrayOffset(): Int =
    genBuffer.generic_offset()

  @inline override def position(newPosition: Int): DoubleBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): DoubleBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): DoubleBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): DoubleBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): DoubleBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): DoubleBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): DoubleBuffer = {
    super.rewind()
    this
  }

  def compact(): DoubleBuffer

  def isDirect(): Boolean

  // Since JDK 15
  final def isEmpty(): Boolean = remaining() == 0

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    genBuffer.generic_hashCode(DoubleBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: DoubleBuffer => compareTo(that) == 0
    case _                => false
  }

  @noinline
  def compareTo(that: DoubleBuffer): Int =
    genBuffer.generic_compareTo(that)(_.compareTo(_))

  def order(): ByteOrder


  // Internal API

  // @inline
  private[nio] def load(index: Int): Double = this.data(index)

  // @inline
  private[nio] def store(index: Int, elem: Double): Unit = this.data(index) = elem

  @inline
  private[nio] def load(
      startIndex: Int,
      dst: Array[Double],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(
      startIndex: Int,
      src: Array[Double],
      offset: Int,
      length: Int
  ): Unit =
    genBuffer.generic_store(startIndex, src, offset, length)
}

