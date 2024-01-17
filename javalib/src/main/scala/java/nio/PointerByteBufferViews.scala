// format: off

package java.nio

import scala.scalanative.unsafe._

private[nio] final class PointerByteBufferCharView private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends CharBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerCharBufferView
      : GenPointerBufferView.NewPointerBufferView[CharBuffer] =
    PointerByteBufferCharView.NewPointerByteBufferCharView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): CharBuffer =
    GenPointerBufferView[CharBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): CharBuffer =
    GenPointerBufferView[CharBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): CharBuffer =
    GenPointerBufferView[CharBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): CharBuffer =
    GenPointerBufferView[CharBuffer](this).generic_asReadOnlyBuffer()

  def subSequence(start: Int, end: Int): CharBuffer = {
    if (start < 0 || end < start || end > remaining())
      throw new IndexOutOfBoundsException
    new PointerByteBufferCharView(
      capacity(),
      _rawDataPointer,
      _offset,
      position() + start,
      position() + end,
      isReadOnly(),
      isBigEndian
    )
  }

  @noinline
  def get(): Char =
    GenBuffer[CharBuffer](this).generic_get()

  @noinline
  def put(c: Char): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(c)

  @noinline
  def get(index: Int): Char =
    GenBuffer[CharBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, c: Char): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Char], offset: Int, length: Int): CharBuffer =
    GenBuffer[CharBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Char], offset: Int, length: Int): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): CharBuffer =
    GenPointerBufferView[CharBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenPointerBufferView[CharBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Char =
    GenPointerBufferView[CharBuffer](this).byteArrayBits.loadChar(index)

  @inline
  private[nio] def store(index: Int, elem: Char): Unit =
    GenPointerBufferView[CharBuffer](this).byteArrayBits.storeChar(index, elem)
}

private[nio] object PointerByteBufferCharView {
  private[nio] implicit object NewPointerByteBufferCharView
      extends GenPointerBufferView.NewPointerBufferView[CharBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        arrayPtr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): CharBuffer = {
      new PointerByteBufferCharView(
        capacity,
        arrayPtr,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromPointerByteBuffer(
      byteBuffer: PointerByteBuffer
  ): CharBuffer =
    GenPointerBufferView.generic_fromPointerByteBuffer(byteBuffer)
}
private[nio] final class PointerByteBufferShortView private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends ShortBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerShortBufferView
      : GenPointerBufferView.NewPointerBufferView[ShortBuffer] =
    PointerByteBufferShortView.NewPointerByteBufferShortView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ShortBuffer =
    GenPointerBufferView[ShortBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): ShortBuffer =
    GenPointerBufferView[ShortBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): ShortBuffer =
    GenPointerBufferView[ShortBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    GenPointerBufferView[ShortBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Short =
    GenBuffer[ShortBuffer](this).generic_get()

  @noinline
  def put(c: Short): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(c)

  @noinline
  def get(index: Int): Short =
    GenBuffer[ShortBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, c: Short): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): ShortBuffer =
    GenPointerBufferView[ShortBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenPointerBufferView[ShortBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Short =
    GenPointerBufferView[ShortBuffer](this).byteArrayBits.loadShort(index)

  @inline
  private[nio] def store(index: Int, elem: Short): Unit =
    GenPointerBufferView[ShortBuffer](this).byteArrayBits.storeShort(index, elem)
}

private[nio] object PointerByteBufferShortView {
  private[nio] implicit object NewPointerByteBufferShortView
      extends GenPointerBufferView.NewPointerBufferView[ShortBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        arrayPtr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): ShortBuffer = {
      new PointerByteBufferShortView(
        capacity,
        arrayPtr,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromPointerByteBuffer(
      byteBuffer: PointerByteBuffer
  ): ShortBuffer =
    GenPointerBufferView.generic_fromPointerByteBuffer(byteBuffer)
}
private[nio] final class PointerByteBufferIntView private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends IntBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerIntBufferView
      : GenPointerBufferView.NewPointerBufferView[IntBuffer] =
    PointerByteBufferIntView.NewPointerByteBufferIntView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): IntBuffer =
    GenPointerBufferView[IntBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): IntBuffer =
    GenPointerBufferView[IntBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): IntBuffer =
    GenPointerBufferView[IntBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    GenPointerBufferView[IntBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Int =
    GenBuffer[IntBuffer](this).generic_get()

  @noinline
  def put(c: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(c)

  @noinline
  def get(index: Int): Int =
    GenBuffer[IntBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, c: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Int], offset: Int, length: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Int], offset: Int, length: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): IntBuffer =
    GenPointerBufferView[IntBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenPointerBufferView[IntBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Int =
    GenPointerBufferView[IntBuffer](this).byteArrayBits.loadInt(index)

  @inline
  private[nio] def store(index: Int, elem: Int): Unit =
    GenPointerBufferView[IntBuffer](this).byteArrayBits.storeInt(index, elem)
}

private[nio] object PointerByteBufferIntView {
  private[nio] implicit object NewPointerByteBufferIntView
      extends GenPointerBufferView.NewPointerBufferView[IntBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        arrayPtr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): IntBuffer = {
      new PointerByteBufferIntView(
        capacity,
        arrayPtr,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromPointerByteBuffer(
      byteBuffer: PointerByteBuffer
  ): IntBuffer =
    GenPointerBufferView.generic_fromPointerByteBuffer(byteBuffer)
}
private[nio] final class PointerByteBufferLongView private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends LongBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerLongBufferView
      : GenPointerBufferView.NewPointerBufferView[LongBuffer] =
    PointerByteBufferLongView.NewPointerByteBufferLongView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): LongBuffer =
    GenPointerBufferView[LongBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): LongBuffer =
    GenPointerBufferView[LongBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): LongBuffer =
    GenPointerBufferView[LongBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    GenPointerBufferView[LongBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Long =
    GenBuffer[LongBuffer](this).generic_get()

  @noinline
  def put(c: Long): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(c)

  @noinline
  def get(index: Int): Long =
    GenBuffer[LongBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, c: Long): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer[LongBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): LongBuffer =
    GenPointerBufferView[LongBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenPointerBufferView[LongBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Long =
    GenPointerBufferView[LongBuffer](this).byteArrayBits.loadLong(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    GenPointerBufferView[LongBuffer](this).byteArrayBits.storeLong(index, elem)
}

private[nio] object PointerByteBufferLongView {
  private[nio] implicit object NewPointerByteBufferLongView
      extends GenPointerBufferView.NewPointerBufferView[LongBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        arrayPtr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): LongBuffer = {
      new PointerByteBufferLongView(
        capacity,
        arrayPtr,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromPointerByteBuffer(
      byteBuffer: PointerByteBuffer
  ): LongBuffer =
    GenPointerBufferView.generic_fromPointerByteBuffer(byteBuffer)
}
private[nio] final class PointerByteBufferFloatView private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends FloatBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerFloatBufferView
      : GenPointerBufferView.NewPointerBufferView[FloatBuffer] =
    PointerByteBufferFloatView.NewPointerByteBufferFloatView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): FloatBuffer =
    GenPointerBufferView[FloatBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): FloatBuffer =
    GenPointerBufferView[FloatBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): FloatBuffer =
    GenPointerBufferView[FloatBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    GenPointerBufferView[FloatBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Float =
    GenBuffer[FloatBuffer](this).generic_get()

  @noinline
  def put(c: Float): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(c)

  @noinline
  def get(index: Int): Float =
    GenBuffer[FloatBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, c: Float): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): FloatBuffer =
    GenPointerBufferView[FloatBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenPointerBufferView[FloatBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Float =
    GenPointerBufferView[FloatBuffer](this).byteArrayBits.loadFloat(index)

  @inline
  private[nio] def store(index: Int, elem: Float): Unit =
    GenPointerBufferView[FloatBuffer](this).byteArrayBits.storeFloat(index, elem)
}

private[nio] object PointerByteBufferFloatView {
  private[nio] implicit object NewPointerByteBufferFloatView
      extends GenPointerBufferView.NewPointerBufferView[FloatBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        arrayPtr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): FloatBuffer = {
      new PointerByteBufferFloatView(
        capacity,
        arrayPtr,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromPointerByteBuffer(
      byteBuffer: PointerByteBuffer
  ): FloatBuffer =
    GenPointerBufferView.generic_fromPointerByteBuffer(byteBuffer)
}
private[nio] final class PointerByteBufferDoubleView private (
    _capacity: Int,
    override private[nio] val _rawDataPointer: Ptr[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends DoubleBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newPointerDoubleBufferView
      : GenPointerBufferView.NewPointerBufferView[DoubleBuffer] =
    PointerByteBufferDoubleView.NewPointerByteBufferDoubleView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): DoubleBuffer =
    GenPointerBufferView[DoubleBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): DoubleBuffer =
    GenPointerBufferView[DoubleBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): DoubleBuffer =
    GenPointerBufferView[DoubleBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    GenPointerBufferView[DoubleBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Double =
    GenBuffer[DoubleBuffer](this).generic_get()

  @noinline
  def put(c: Double): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(c)

  @noinline
  def get(index: Int): Double =
    GenBuffer[DoubleBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, c: Double): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(index, c)

  @noinline
  override def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): DoubleBuffer =
    GenPointerBufferView[DoubleBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenPointerBufferView[DoubleBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Double =
    GenPointerBufferView[DoubleBuffer](this).byteArrayBits.loadDouble(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    GenPointerBufferView[DoubleBuffer](this).byteArrayBits.storeDouble(index, elem)
}

private[nio] object PointerByteBufferDoubleView {
  private[nio] implicit object NewPointerByteBufferDoubleView
      extends GenPointerBufferView.NewPointerBufferView[DoubleBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        arrayPtr: Ptr[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): DoubleBuffer = {
      new PointerByteBufferDoubleView(
        capacity,
        arrayPtr,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromPointerByteBuffer(
      byteBuffer: PointerByteBuffer
  ): DoubleBuffer =
    GenPointerBufferView.generic_fromPointerByteBuffer(byteBuffer)
}
