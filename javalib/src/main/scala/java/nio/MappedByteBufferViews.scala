// format: off

package java.nio

private[nio] final class MappedByteBufferCharView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends CharBuffer(_capacity, if(_mappedData.data != null) _mappedData.data + _offset else _mappedData.data) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newMappedCharBufferView
      : GenMappedBufferView.NewMappedBufferView[CharBuffer] =
    MappedByteBufferCharView.NewMappedByteBufferCharView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): CharBuffer =
    GenMappedBufferView[CharBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): CharBuffer =
    GenMappedBufferView[CharBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): CharBuffer =
    GenMappedBufferView[CharBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): CharBuffer =
    GenMappedBufferView[CharBuffer](this).generic_asReadOnlyBuffer()

  def subSequence(start: Int, end: Int): CharBuffer = {
    if (start < 0 || end < start || end > remaining())
      throw new IndexOutOfBoundsException
    new MappedByteBufferCharView(
      capacity(),
      _mappedData,
      _offset,
      position() + start,
      position() + end,
      isReadOnly(),
      isBigEndian
    )
  }

  @noinline
  override def get(dst: Array[Char], offset: Int, length: Int): CharBuffer =
    GenBuffer[CharBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Char], offset: Int, length: Int): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): CharBuffer =
    GenMappedBufferView[CharBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView[CharBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] override def load(index: Int): Char =
    GenMappedBufferView[CharBuffer](this).byteArrayBits.loadChar(index)

  @inline
  private[nio] override def store(index: Int, elem: Char): Unit =
    GenMappedBufferView[CharBuffer](this).byteArrayBits.storeChar(index, elem)
}

private[nio] object MappedByteBufferCharView {
  private[nio] implicit object NewMappedByteBufferCharView
      extends GenMappedBufferView.NewMappedBufferView[CharBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): CharBuffer = {
      new MappedByteBufferCharView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): CharBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
private[nio] final class MappedByteBufferShortView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends ShortBuffer(_capacity, if(_mappedData.data != null) _mappedData.data + _offset else _mappedData.data) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newMappedShortBufferView
      : GenMappedBufferView.NewMappedBufferView[ShortBuffer] =
    MappedByteBufferShortView.NewMappedByteBufferShortView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): ShortBuffer =
    GenMappedBufferView[ShortBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): ShortBuffer =
    GenMappedBufferView[ShortBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): ShortBuffer =
    GenMappedBufferView[ShortBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    GenMappedBufferView[ShortBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  override def get(dst: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): ShortBuffer =
    GenMappedBufferView[ShortBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView[ShortBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] override def load(index: Int): Short =
    GenMappedBufferView[ShortBuffer](this).byteArrayBits.loadShort(index)

  @inline
  private[nio] override def store(index: Int, elem: Short): Unit =
    GenMappedBufferView[ShortBuffer](this).byteArrayBits.storeShort(index, elem)
}

private[nio] object MappedByteBufferShortView {
  private[nio] implicit object NewMappedByteBufferShortView
      extends GenMappedBufferView.NewMappedBufferView[ShortBuffer] {
    def bytesPerElem: Int = 2

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): ShortBuffer = {
      new MappedByteBufferShortView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): ShortBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
private[nio] final class MappedByteBufferIntView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends IntBuffer(_capacity, if(_mappedData.data != null) _mappedData.data + _offset else _mappedData.data) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newMappedIntBufferView
      : GenMappedBufferView.NewMappedBufferView[IntBuffer] =
    MappedByteBufferIntView.NewMappedByteBufferIntView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): IntBuffer =
    GenMappedBufferView[IntBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): IntBuffer =
    GenMappedBufferView[IntBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): IntBuffer =
    GenMappedBufferView[IntBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    GenMappedBufferView[IntBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  override def get(dst: Array[Int], offset: Int, length: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Int], offset: Int, length: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): IntBuffer =
    GenMappedBufferView[IntBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView[IntBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] override def load(index: Int): Int =
    GenMappedBufferView[IntBuffer](this).byteArrayBits.loadInt(index)

  @inline
  private[nio] override def store(index: Int, elem: Int): Unit =
    GenMappedBufferView[IntBuffer](this).byteArrayBits.storeInt(index, elem)
}

private[nio] object MappedByteBufferIntView {
  private[nio] implicit object NewMappedByteBufferIntView
      extends GenMappedBufferView.NewMappedBufferView[IntBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): IntBuffer = {
      new MappedByteBufferIntView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): IntBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
private[nio] final class MappedByteBufferLongView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends LongBuffer(_capacity, if(_mappedData.data != null) _mappedData.data + _offset else _mappedData.data) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newMappedLongBufferView
      : GenMappedBufferView.NewMappedBufferView[LongBuffer] =
    MappedByteBufferLongView.NewMappedByteBufferLongView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): LongBuffer =
    GenMappedBufferView[LongBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): LongBuffer =
    GenMappedBufferView[LongBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): LongBuffer =
    GenMappedBufferView[LongBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    GenMappedBufferView[LongBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  override def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer[LongBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): LongBuffer =
    GenMappedBufferView[LongBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView[LongBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] override def load(index: Int): Long =
    GenMappedBufferView[LongBuffer](this).byteArrayBits.loadLong(index)

  @inline
  private[nio] override def store(index: Int, elem: Long): Unit =
    GenMappedBufferView[LongBuffer](this).byteArrayBits.storeLong(index, elem)
}

private[nio] object MappedByteBufferLongView {
  private[nio] implicit object NewMappedByteBufferLongView
      extends GenMappedBufferView.NewMappedBufferView[LongBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): LongBuffer = {
      new MappedByteBufferLongView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): LongBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
private[nio] final class MappedByteBufferFloatView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends FloatBuffer(_capacity, if(_mappedData.data != null) _mappedData.data + _offset else _mappedData.data) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newMappedFloatBufferView
      : GenMappedBufferView.NewMappedBufferView[FloatBuffer] =
    MappedByteBufferFloatView.NewMappedByteBufferFloatView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): FloatBuffer =
    GenMappedBufferView[FloatBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): FloatBuffer =
    GenMappedBufferView[FloatBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): FloatBuffer =
    GenMappedBufferView[FloatBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    GenMappedBufferView[FloatBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  override def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): FloatBuffer =
    GenMappedBufferView[FloatBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView[FloatBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] override def load(index: Int): Float =
    GenMappedBufferView[FloatBuffer](this).byteArrayBits.loadFloat(index)

  @inline
  private[nio] override def store(index: Int, elem: Float): Unit =
    GenMappedBufferView[FloatBuffer](this).byteArrayBits.storeFloat(index, elem)
}

private[nio] object MappedByteBufferFloatView {
  private[nio] implicit object NewMappedByteBufferFloatView
      extends GenMappedBufferView.NewMappedBufferView[FloatBuffer] {
    def bytesPerElem: Int = 4

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): FloatBuffer = {
      new MappedByteBufferFloatView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): FloatBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
private[nio] final class MappedByteBufferDoubleView private (
    _capacity: Int,
    override private[nio] val _mappedData: MappedByteBufferData,
    override private[nio] val _offset: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean,
    override private[nio] val isBigEndian: Boolean
) extends DoubleBuffer(_capacity, if(_mappedData.data != null) _mappedData.data + _offset else _mappedData.data) {

  position(_initialPosition)
  limit(_initialLimit)

  private implicit def newMappedDoubleBufferView
      : GenMappedBufferView.NewMappedBufferView[DoubleBuffer] =
    MappedByteBufferDoubleView.NewMappedByteBufferDoubleView

  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = true

  @noinline
  def slice(): DoubleBuffer =
    GenMappedBufferView[DoubleBuffer](this).generic_slice()

  @noinline
  def slice(index: Int, length: Int): DoubleBuffer =
    GenMappedBufferView[DoubleBuffer](this).generic_slice(index, length)

  @noinline
  def duplicate(): DoubleBuffer =
    GenMappedBufferView[DoubleBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    GenMappedBufferView[DoubleBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  override def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): DoubleBuffer =
    GenMappedBufferView[DoubleBuffer](this).generic_compact()

  @noinline
  def order(): ByteOrder =
    GenMappedBufferView[DoubleBuffer](this).generic_order()

  // Private API

  @inline
  private[nio] override def load(index: Int): Double =
    GenMappedBufferView[DoubleBuffer](this).byteArrayBits.loadDouble(index)

  @inline
  private[nio] override def store(index: Int, elem: Double): Unit =
    GenMappedBufferView[DoubleBuffer](this).byteArrayBits.storeDouble(index, elem)
}

private[nio] object MappedByteBufferDoubleView {
  private[nio] implicit object NewMappedByteBufferDoubleView
      extends GenMappedBufferView.NewMappedBufferView[DoubleBuffer] {
    def bytesPerElem: Int = 8

    def apply(
        capacity: Int,
        mappedData: MappedByteBufferData,
        byteArrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean,
        isBigEndian: Boolean
    ): DoubleBuffer = {
      new MappedByteBufferDoubleView(
        capacity,
        mappedData,
        byteArrayOffset,
        initialPosition,
        initialLimit,
        readOnly,
        isBigEndian
      )
    }
  }

  @inline
  private[nio] def fromMappedByteBuffer(
      byteBuffer: MappedByteBuffer
  ): DoubleBuffer =
    GenMappedBufferView.generic_fromMappedByteBuffer(byteBuffer)
}
