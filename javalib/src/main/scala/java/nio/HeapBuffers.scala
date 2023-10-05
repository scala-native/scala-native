// format: off

package java.nio

private[nio] final class HeapCharBuffer private (
    _capacity: Int,
    _array0: Array[Char],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends CharBuffer(_capacity, _array0, _offset0) {
  
  position(_initialPosition)
  limit(_initialLimit)
   
  private implicit def newHeapCharBuffer
      : GenHeapBuffer.NewHeapBuffer[CharBuffer, Char] =
    HeapCharBuffer.NewHeapCharBuffer
    
  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): CharBuffer =
    GenHeapBuffer[CharBuffer](this).generic_slice()

  @noinline
  def duplicate(): CharBuffer =
    GenHeapBuffer[CharBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): CharBuffer =
    GenHeapBuffer[CharBuffer](this).generic_asReadOnlyBuffer()

  def subSequence(start: Int, end: Int): CharBuffer = {
    if (start < 0 || end < start || end > remaining())
      throw new IndexOutOfBoundsException
    new HeapCharBuffer(
      capacity(),
      _array,
      _offset,
      position() + start,
      position() + end,
      isReadOnly()
    )
  }

  @noinline
  def get(): Char =
    GenBuffer[CharBuffer](this).generic_get()

  @noinline
  def put(v: Char): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(v)

  @noinline
  def get(index: Int): Char =
    GenBuffer[CharBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, v: Char): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(index, v)

  @noinline
  override def get(dst: Array[Char], offset: Int, length: Int): CharBuffer =
    GenBuffer[CharBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Char], offset: Int, length: Int): CharBuffer =
    GenBuffer[CharBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): CharBuffer =
    GenHeapBuffer[CharBuffer](this).generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Char =
    GenHeapBuffer[CharBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Char): Unit =
    GenHeapBuffer[CharBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Char],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[CharBuffer](this).generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Char],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[CharBuffer](this).generic_store(startIndex, src, offset, length)
}

private[nio] object HeapCharBuffer {
  private[nio] implicit object NewHeapCharBuffer
      extends GenHeapBuffer.NewHeapBuffer[CharBuffer, Char] {
    def apply(
        capacity: Int,
        array: Array[Char],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): CharBuffer = {
      new HeapCharBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Char],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): CharBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}

private[nio] final class HeapShortBuffer private (
    _capacity: Int,
    _array0: Array[Short],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends ShortBuffer(_capacity, _array0, _offset0) {
  
  position(_initialPosition)
  limit(_initialLimit)
   
  private implicit def newHeapShortBuffer
      : GenHeapBuffer.NewHeapBuffer[ShortBuffer, Short] =
    HeapShortBuffer.NewHeapShortBuffer
    
  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): ShortBuffer =
    GenHeapBuffer[ShortBuffer](this).generic_slice()

  @noinline
  def duplicate(): ShortBuffer =
    GenHeapBuffer[ShortBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): ShortBuffer =
    GenHeapBuffer[ShortBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Short =
    GenBuffer[ShortBuffer](this).generic_get()

  @noinline
  def put(v: Short): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(v)

  @noinline
  def get(index: Int): Short =
    GenBuffer[ShortBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, v: Short): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(index, v)

  @noinline
  override def get(dst: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Short], offset: Int, length: Int): ShortBuffer =
    GenBuffer[ShortBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): ShortBuffer =
    GenHeapBuffer[ShortBuffer](this).generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Short =
    GenHeapBuffer[ShortBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Short): Unit =
    GenHeapBuffer[ShortBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Short],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[ShortBuffer](this).generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Short],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[ShortBuffer](this).generic_store(startIndex, src, offset, length)
}

private[nio] object HeapShortBuffer {
  private[nio] implicit object NewHeapShortBuffer
      extends GenHeapBuffer.NewHeapBuffer[ShortBuffer, Short] {
    def apply(
        capacity: Int,
        array: Array[Short],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): ShortBuffer = {
      new HeapShortBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Short],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): ShortBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}

private[nio] final class HeapIntBuffer private (
    _capacity: Int,
    _array0: Array[Int],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends IntBuffer(_capacity, _array0, _offset0) {
  
  position(_initialPosition)
  limit(_initialLimit)
   
  private implicit def newHeapIntBuffer
      : GenHeapBuffer.NewHeapBuffer[IntBuffer, Int] =
    HeapIntBuffer.NewHeapIntBuffer
    
  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): IntBuffer =
    GenHeapBuffer[IntBuffer](this).generic_slice()

  @noinline
  def duplicate(): IntBuffer =
    GenHeapBuffer[IntBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): IntBuffer =
    GenHeapBuffer[IntBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Int =
    GenBuffer[IntBuffer](this).generic_get()

  @noinline
  def put(v: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(v)

  @noinline
  def get(index: Int): Int =
    GenBuffer[IntBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, v: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(index, v)

  @noinline
  override def get(dst: Array[Int], offset: Int, length: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Int], offset: Int, length: Int): IntBuffer =
    GenBuffer[IntBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): IntBuffer =
    GenHeapBuffer[IntBuffer](this).generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Int =
    GenHeapBuffer[IntBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Int): Unit =
    GenHeapBuffer[IntBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Int],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[IntBuffer](this).generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Int],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[IntBuffer](this).generic_store(startIndex, src, offset, length)
}

private[nio] object HeapIntBuffer {
  private[nio] implicit object NewHeapIntBuffer
      extends GenHeapBuffer.NewHeapBuffer[IntBuffer, Int] {
    def apply(
        capacity: Int,
        array: Array[Int],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): IntBuffer = {
      new HeapIntBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Int],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): IntBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}

private[nio] final class HeapLongBuffer private (
    _capacity: Int,
    _array0: Array[Long],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends LongBuffer(_capacity, _array0, _offset0) {
  
  position(_initialPosition)
  limit(_initialLimit)
   
  private implicit def newHeapLongBuffer
      : GenHeapBuffer.NewHeapBuffer[LongBuffer, Long] =
    HeapLongBuffer.NewHeapLongBuffer
    
  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): LongBuffer =
    GenHeapBuffer[LongBuffer](this).generic_slice()

  @noinline
  def duplicate(): LongBuffer =
    GenHeapBuffer[LongBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): LongBuffer =
    GenHeapBuffer[LongBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Long =
    GenBuffer[LongBuffer](this).generic_get()

  @noinline
  def put(v: Long): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(v)

  @noinline
  def get(index: Int): Long =
    GenBuffer[LongBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, v: Long): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(index, v)

  @noinline
  override def get(dst: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer[LongBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Long], offset: Int, length: Int): LongBuffer =
    GenBuffer[LongBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): LongBuffer =
    GenHeapBuffer[LongBuffer](this).generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Long =
    GenHeapBuffer[LongBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Long): Unit =
    GenHeapBuffer[LongBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Long],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[LongBuffer](this).generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Long],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[LongBuffer](this).generic_store(startIndex, src, offset, length)
}

private[nio] object HeapLongBuffer {
  private[nio] implicit object NewHeapLongBuffer
      extends GenHeapBuffer.NewHeapBuffer[LongBuffer, Long] {
    def apply(
        capacity: Int,
        array: Array[Long],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): LongBuffer = {
      new HeapLongBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Long],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): LongBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}

private[nio] final class HeapFloatBuffer private (
    _capacity: Int,
    _array0: Array[Float],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends FloatBuffer(_capacity, _array0, _offset0) {
  
  position(_initialPosition)
  limit(_initialLimit)
   
  private implicit def newHeapFloatBuffer
      : GenHeapBuffer.NewHeapBuffer[FloatBuffer, Float] =
    HeapFloatBuffer.NewHeapFloatBuffer
    
  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): FloatBuffer =
    GenHeapBuffer[FloatBuffer](this).generic_slice()

  @noinline
  def duplicate(): FloatBuffer =
    GenHeapBuffer[FloatBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): FloatBuffer =
    GenHeapBuffer[FloatBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Float =
    GenBuffer[FloatBuffer](this).generic_get()

  @noinline
  def put(v: Float): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(v)

  @noinline
  def get(index: Int): Float =
    GenBuffer[FloatBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, v: Float): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(index, v)

  @noinline
  override def get(dst: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Float], offset: Int, length: Int): FloatBuffer =
    GenBuffer[FloatBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): FloatBuffer =
    GenHeapBuffer[FloatBuffer](this).generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Float =
    GenHeapBuffer[FloatBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Float): Unit =
    GenHeapBuffer[FloatBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Float],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[FloatBuffer](this).generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Float],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[FloatBuffer](this).generic_store(startIndex, src, offset, length)
}

private[nio] object HeapFloatBuffer {
  private[nio] implicit object NewHeapFloatBuffer
      extends GenHeapBuffer.NewHeapBuffer[FloatBuffer, Float] {
    def apply(
        capacity: Int,
        array: Array[Float],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): FloatBuffer = {
      new HeapFloatBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Float],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): FloatBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}

private[nio] final class HeapDoubleBuffer private (
    _capacity: Int,
    _array0: Array[Double],
    _offset0: Int,
    _initialPosition: Int,
    _initialLimit: Int,
    _readOnly: Boolean
) extends DoubleBuffer(_capacity, _array0, _offset0) {
  
  position(_initialPosition)
  limit(_initialLimit)
   
  private implicit def newHeapDoubleBuffer
      : GenHeapBuffer.NewHeapBuffer[DoubleBuffer, Double] =
    HeapDoubleBuffer.NewHeapDoubleBuffer
    
  def isReadOnly(): Boolean = _readOnly

  def isDirect(): Boolean = false

  @noinline
  def slice(): DoubleBuffer =
    GenHeapBuffer[DoubleBuffer](this).generic_slice()

  @noinline
  def duplicate(): DoubleBuffer =
    GenHeapBuffer[DoubleBuffer](this).generic_duplicate()

  @noinline
  def asReadOnlyBuffer(): DoubleBuffer =
    GenHeapBuffer[DoubleBuffer](this).generic_asReadOnlyBuffer()


  @noinline
  def get(): Double =
    GenBuffer[DoubleBuffer](this).generic_get()

  @noinline
  def put(v: Double): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(v)

  @noinline
  def get(index: Int): Double =
    GenBuffer[DoubleBuffer](this).generic_get(index)

  @noinline
  def put(index: Int, v: Double): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(index, v)

  @noinline
  override def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_get(dst, offset, length)

  @noinline
  override def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer[DoubleBuffer](this).generic_put(src, offset, length)

  @noinline
  def compact(): DoubleBuffer =
    GenHeapBuffer[DoubleBuffer](this).generic_compact()

  def order(): ByteOrder = ByteOrder.nativeOrder()

  // Internal API

  @inline
  private[nio] def load(index: Int): Double =
    GenHeapBuffer[DoubleBuffer](this).generic_load(index)

  @inline
  private[nio] def store(index: Int, elem: Double): Unit =
    GenHeapBuffer[DoubleBuffer](this).generic_store(index, elem)

  @inline
  override private[nio] def load(
      startIndex: Int,
      dst: Array[Double],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[DoubleBuffer](this).generic_load(startIndex, dst, offset, length)

  @inline
  override private[nio] def store(
      startIndex: Int,
      src: Array[Double],
      offset: Int,
      length: Int
  ): Unit =
    GenHeapBuffer[DoubleBuffer](this).generic_store(startIndex, src, offset, length)
}

private[nio] object HeapDoubleBuffer {
  private[nio] implicit object NewHeapDoubleBuffer
      extends GenHeapBuffer.NewHeapBuffer[DoubleBuffer, Double] {
    def apply(
        capacity: Int,
        array: Array[Double],
        arrayOffset: Int,
        initialPosition: Int,
        initialLimit: Int,
        readOnly: Boolean
    ): DoubleBuffer = {
      new HeapDoubleBuffer(
        capacity,
        array,
        arrayOffset,
        initialPosition,
        initialLimit,
        readOnly
      )
    }
  }

  @noinline
  private[nio] def wrap(
      array: Array[Double],
      arrayOffset: Int,
      capacity: Int,
      initialPosition: Int,
      initialLength: Int,
      isReadOnly: Boolean
  ): DoubleBuffer = {
    GenHeapBuffer.generic_wrap(
      array,
      arrayOffset,
      capacity,
      initialPosition,
      initialLength,
      isReadOnly
    )
  }
}

