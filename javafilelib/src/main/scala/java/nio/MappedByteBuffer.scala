package java.nio

import java.nio.channels.{FileChannel, NonWritableChannelException}

abstract class MappedByteBuffer(mode: FileChannel.MapMode,
                                _capacity: Int,
                                _array: Array[Byte],
                                _arrayOffset: Int)
    extends ByteBuffer(_capacity, _array, _arrayOffset) {

  private val underlying = ByteBuffer.wrap(_array, _arrayOffset, _capacity)

  override def isReadOnly(): Boolean =
    mode == FileChannel.MapMode.READ_ONLY

  override def asCharBuffer(): CharBuffer =
    underlying.asCharBuffer()

  override def asDoubleBuffer(): DoubleBuffer =
    underlying.asDoubleBuffer()

  override def asFloatBuffer(): FloatBuffer =
    underlying.asFloatBuffer()

  override def asIntBuffer(): IntBuffer =
    underlying.asIntBuffer()

  override def asLongBuffer(): LongBuffer =
    underlying.asLongBuffer()

  override def asShortBuffer(): ShortBuffer =
    underlying.asShortBuffer()

  override def asReadOnlyBuffer(): ByteBuffer =
    if (isReadOnly()) this
    else underlying.asReadOnlyBuffer()

  override def compact(): ByteBuffer =
    underlying.compact()

  override def duplicate(): ByteBuffer =
    underlying.duplicate()

  override def get(index: Int): Byte =
    underlying.get(index)

  override def get(): Byte =
    underlying.get()

  override def getChar(index: Int): Char =
    underlying.getChar(index)

  override def getChar(): Char =
    underlying.getChar()

  override def getDouble(index: Int): Double =
    underlying.getDouble(index)

  override def getDouble(): Double =
    underlying.getDouble()

  override def getFloat(index: Int): Float =
    underlying.getFloat(index)

  override def getFloat(): Float =
    underlying.getFloat()

  override def getInt(index: Int): Int =
    underlying.getInt(index)

  override def getInt(): Int =
    underlying.getInt()

  override def getLong(index: Int): Long =
    underlying.getLong(index)

  override def getLong(): Long =
    underlying.getLong()

  override def getShort(index: Int): Short =
    underlying.getShort(index)

  override def getShort(): Short =
    underlying.getShort()

  override def isDirect(): Boolean =
    underlying.isDirect()

  private[nio] override def load(index: Int): Byte =
    underlying.load(index)

  override def put(index: Int, b: Byte): ByteBuffer = {
    ensureWritable()
    underlying.put(index, b)
  }

  override def put(b: Byte): ByteBuffer = {
    ensureWritable()
    underlying.put(b)
  }

  override def putChar(index: Int, value: Char): ByteBuffer = {
    ensureWritable()
    underlying.putChar(index, value)
  }

  override def putChar(value: Char): ByteBuffer = {
    ensureWritable()
    underlying.putChar(value)
  }

  override def putDouble(index: Int, value: Double): ByteBuffer = {
    ensureWritable()
    underlying.putDouble(index, value)
  }

  override def putDouble(value: Double): ByteBuffer = {
    ensureWritable()
    underlying.putDouble(value)
  }

  override def putFloat(index: Int, value: Float): ByteBuffer = {
    ensureWritable()
    underlying.putFloat(index, value)
  }

  override def putFloat(value: Float): ByteBuffer = {
    ensureWritable()
    underlying.putFloat(value)
  }

  override def putInt(index: Int, value: Int): ByteBuffer = {
    ensureWritable()
    underlying.putInt(index, value)
  }

  override def putInt(value: Int): ByteBuffer = {
    ensureWritable()
    underlying.putInt(value)
  }

  override def putLong(index: Int, value: Long): ByteBuffer = {
    ensureWritable()
    underlying.putLong(index, value)
  }

  override def putLong(value: Long): ByteBuffer = {
    ensureWritable()
    underlying.putLong(value)
  }

  override def putShort(index: Int, value: Short): ByteBuffer = {
    ensureWritable()
    underlying.putShort(index, value)
  }

  override def putShort(value: Short): ByteBuffer = {
    ensureWritable()
    underlying.putShort(value)
  }

  override def slice(): ByteBuffer =
    underlying.slice()

  private[nio] override def store(index: Int, elem: Byte): Unit =
    underlying.store(index, elem)

  private def ensureWritable(): Unit =
    if (isReadOnly) throw new NonWritableChannelException()
}
