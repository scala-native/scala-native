// format: off

package java.nio

private[nio] final class HeapByteBufferCharView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends CharBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[CharBuffer] =
    HeapByteBufferCharView.NewHeapByteBufferCharView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): CharBuffer =
    GenHeapBufferView[CharBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): CharBuffer =
    GenHeapBufferView[CharBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): CharBuffer =
    GenHeapBufferView[CharBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): CharBuffer =
    GenHeapBufferView[CharBuffer](this).generic_asReadOnlyBuffer()

  def subSequence(start: Int, end: Int): CharBuffer = {
    if (start < 0 || end < start || end > remaining())
      throw new IndexOutOfBoundsException
    new HeapByteBufferCharView(
      capacity(),
      _byteArray,
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
    GenHeapBufferView[CharBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenHeapBufferView[CharBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Char =
    GenHeapBufferView[CharBuffer](this).byteArrayBits.loadChar(index)

  @inline
  private[nio] def store(index: Int, elem: Char): Unit =
    GenHeapBufferView[CharBuffer](this).byteArrayBits.storeChar(index, elem)
}

private[nio] object HeapByteBufferCharView {
  private[nio] implicit object NewHeapByteBufferCharView
      extends GenHeapBufferView.NewHeapBufferView[CharBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): CharBuffer = {
      new HeapByteBufferCharView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): CharBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
private[nio] final class HeapByteBufferShortView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends ShortBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[ShortBuffer] =
    HeapByteBufferShortView.NewHeapByteBufferShortView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ShortBuffer =
    GenHeapBufferView[ShortBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): ShortBuffer =
    GenHeapBufferView[ShortBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): ShortBuffer =
    GenHeapBufferView[ShortBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    GenHeapBufferView[ShortBuffer](this).generic_asReadOnlyBuffer()


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
    GenHeapBufferView[ShortBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenHeapBufferView[ShortBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Short =
    GenHeapBufferView[ShortBuffer](this).byteArrayBits.loadShort(index)

  @inline
  private[nio] def store(index: Int, elem: Short): Unit =
    GenHeapBufferView[ShortBuffer](this).byteArrayBits.storeShort(index, elem)
}

private[nio] object HeapByteBufferShortView {
  private[nio] implicit object NewHeapByteBufferShortView
      extends GenHeapBufferView.NewHeapBufferView[ShortBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): ShortBuffer = {
      new HeapByteBufferShortView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): ShortBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
private[nio] final class HeapByteBufferIntView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends IntBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[IntBuffer] =
    HeapByteBufferIntView.NewHeapByteBufferIntView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): IntBuffer =
    GenHeapBufferView[IntBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): IntBuffer =
    GenHeapBufferView[IntBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): IntBuffer =
    GenHeapBufferView[IntBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    GenHeapBufferView[IntBuffer](this).generic_asReadOnlyBuffer()


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
    GenHeapBufferView[IntBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenHeapBufferView[IntBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Int =
    GenHeapBufferView[IntBuffer](this).byteArrayBits.loadInt(index)

  @inline
  private[nio] def store(index: Int, elem: Int): Unit =
    GenHeapBufferView[IntBuffer](this).byteArrayBits.storeInt(index, elem)
}

private[nio] object HeapByteBufferIntView {
  private[nio] implicit object NewHeapByteBufferIntView
      extends GenHeapBufferView.NewHeapBufferView[IntBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): IntBuffer = {
      new HeapByteBufferIntView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): IntBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
private[nio] final class HeapByteBufferLongView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends LongBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[LongBuffer] =
    HeapByteBufferLongView.NewHeapByteBufferLongView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): LongBuffer =
    GenHeapBufferView[LongBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): LongBuffer =
    GenHeapBufferView[LongBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): LongBuffer =
    GenHeapBufferView[LongBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    GenHeapBufferView[LongBuffer](this).generic_asReadOnlyBuffer()


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
    GenHeapBufferView[LongBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenHeapBufferView[LongBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Long =
    GenHeapBufferView[LongBuffer](this).byteArrayBits.loadLong(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    GenHeapBufferView[LongBuffer](this).byteArrayBits.storeLong(index, elem)
}

private[nio] object HeapByteBufferLongView {
  private[nio] implicit object NewHeapByteBufferLongView
      extends GenHeapBufferView.NewHeapBufferView[LongBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): LongBuffer = {
      new HeapByteBufferLongView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): LongBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
private[nio] final class HeapByteBufferFloatView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends FloatBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[FloatBuffer] =
    HeapByteBufferFloatView.NewHeapByteBufferFloatView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): FloatBuffer =
    GenHeapBufferView[FloatBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): FloatBuffer =
    GenHeapBufferView[FloatBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): FloatBuffer =
    GenHeapBufferView[FloatBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    GenHeapBufferView[FloatBuffer](this).generic_asReadOnlyBuffer()


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
    GenHeapBufferView[FloatBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenHeapBufferView[FloatBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Float =
    GenHeapBufferView[FloatBuffer](this).byteArrayBits.loadFloat(index)

  @inline
  private[nio] def store(index: Int, elem: Float): Unit =
    GenHeapBufferView[FloatBuffer](this).byteArrayBits.storeFloat(index, elem)
}

private[nio] object HeapByteBufferFloatView {
  private[nio] implicit object NewHeapByteBufferFloatView
      extends GenHeapBufferView.NewHeapBufferView[FloatBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): FloatBuffer = {
      new HeapByteBufferFloatView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): FloatBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
private[nio] final class HeapByteBufferDoubleView private (
    _capacity: Int,
    override private[nio] val _byteArray: Array[Byte],
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends DoubleBuffer(_capacity) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newHeapBufferView
      : GenHeapBufferView.NewHeapBufferView[DoubleBuffer] =
    HeapByteBufferDoubleView.NewHeapByteBufferDoubleView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): DoubleBuffer =
    GenHeapBufferView[DoubleBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): DoubleBuffer =
    GenHeapBufferView[DoubleBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): DoubleBuffer =
    GenHeapBufferView[DoubleBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    GenHeapBufferView[DoubleBuffer](this).generic_asReadOnlyBuffer()


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
    GenHeapBufferView[DoubleBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenHeapBufferView[DoubleBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] def load(index: Int): Double =
    GenHeapBufferView[DoubleBuffer](this).byteArrayBits.loadDouble(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    GenHeapBufferView[DoubleBuffer](this).byteArrayBits.storeDouble(index, elem)
}

private[nio] object HeapByteBufferDoubleView {
  private[nio] implicit object NewHeapByteBufferDoubleView
      extends GenHeapBufferView.NewHeapBufferView[DoubleBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        byteArray: Array[Byte],
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): DoubleBuffer = {
      new HeapByteBufferDoubleView(
        capacity,
        byteArray,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromHeapByteBuffer(byteBuffer: HeapByteBuffer): DoubleBuffer =
    GenHeapBufferView.generic_fromHeapByteBuffer(byteBuffer)
}
