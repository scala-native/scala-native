package org.scalanative.testsuite.niobuffer

import scala.scalanative.memory.*

import org.scalanative.testsuite.niobuffer.ByteBufferNativeFactories.WrappedPointerByteBufferFactory
import org.scalanative.testsuite.javalib.nio.BufferFactory
import org.scalanative.testsuite.javalib.nio.BaseBufferTest

import java.nio.*

// scalafmt: { maxColumn = 200}


// format: off
abstract class CharBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.CharBufferFactory

  class ByteBufferCharViewFactory(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends Factory with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    override val createsPointerBuffer: Boolean = byteBufferFactory.createsPointerBuffer

    def baseAllocBuffer(capacity: Int): CharBuffer =
      byteBufferFactory
        .allocBuffer(capacity * 2)
        .order(order)
        .asCharBuffer()
  }
}

// Char views of byte buffers
abstract class CharViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends CharBufferTest {
  val factory: BufferFactory.CharBufferFactory = new ByteBufferCharViewFactory(byteBufferFactory, order)
}

class CharViewOfWrappedByteBufferBigEndianTest    extends CharViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class CharViewOfWrappedByteBufferLittleEndianTest extends CharViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)

// Read only Char views of byte buffers
abstract class ReadOnlyCharViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends CharBufferTest {
  val factory: BufferFactory.CharBufferFactory = new ByteBufferCharViewFactory(byteBufferFactory, order) with BufferFactory.ReadOnlyBufferFactory
}

class ReadOnlyCharViewOfWrappedByteBufferBigEndianTest    extends ReadOnlyCharViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ReadOnlyCharViewOfWrappedByteBufferLittleEndianTest extends ReadOnlyCharViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)
abstract class ShortBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.ShortBufferFactory

  class ByteBufferShortViewFactory(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends Factory with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    override val createsPointerBuffer: Boolean = byteBufferFactory.createsPointerBuffer

    def baseAllocBuffer(capacity: Int): ShortBuffer =
      byteBufferFactory
        .allocBuffer(capacity * 2)
        .order(order)
        .asShortBuffer()
  }
}

// Short views of byte buffers
abstract class ShortViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends ShortBufferTest {
  val factory: BufferFactory.ShortBufferFactory = new ByteBufferShortViewFactory(byteBufferFactory, order)
}

class ShortViewOfWrappedByteBufferBigEndianTest    extends ShortViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ShortViewOfWrappedByteBufferLittleEndianTest extends ShortViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)

// Read only Short views of byte buffers
abstract class ReadOnlyShortViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends ShortBufferTest {
  val factory: BufferFactory.ShortBufferFactory = new ByteBufferShortViewFactory(byteBufferFactory, order) with BufferFactory.ReadOnlyBufferFactory
}

class ReadOnlyShortViewOfWrappedByteBufferBigEndianTest    extends ReadOnlyShortViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ReadOnlyShortViewOfWrappedByteBufferLittleEndianTest extends ReadOnlyShortViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)
abstract class IntBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.IntBufferFactory

  class ByteBufferIntViewFactory(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends Factory with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    override val createsPointerBuffer: Boolean = byteBufferFactory.createsPointerBuffer

    def baseAllocBuffer(capacity: Int): IntBuffer =
      byteBufferFactory
        .allocBuffer(capacity * 4)
        .order(order)
        .asIntBuffer()
  }
}

// Int views of byte buffers
abstract class IntViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends IntBufferTest {
  val factory: BufferFactory.IntBufferFactory = new ByteBufferIntViewFactory(byteBufferFactory, order)
}

class IntViewOfWrappedByteBufferBigEndianTest    extends IntViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class IntViewOfWrappedByteBufferLittleEndianTest extends IntViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)

// Read only Int views of byte buffers
abstract class ReadOnlyIntViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends IntBufferTest {
  val factory: BufferFactory.IntBufferFactory = new ByteBufferIntViewFactory(byteBufferFactory, order) with BufferFactory.ReadOnlyBufferFactory
}

class ReadOnlyIntViewOfWrappedByteBufferBigEndianTest    extends ReadOnlyIntViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ReadOnlyIntViewOfWrappedByteBufferLittleEndianTest extends ReadOnlyIntViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)
abstract class LongBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.LongBufferFactory

  class ByteBufferLongViewFactory(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends Factory with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    override val createsPointerBuffer: Boolean = byteBufferFactory.createsPointerBuffer

    def baseAllocBuffer(capacity: Int): LongBuffer =
      byteBufferFactory
        .allocBuffer(capacity * 8)
        .order(order)
        .asLongBuffer()
  }
}

// Long views of byte buffers
abstract class LongViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends LongBufferTest {
  val factory: BufferFactory.LongBufferFactory = new ByteBufferLongViewFactory(byteBufferFactory, order)
}

class LongViewOfWrappedByteBufferBigEndianTest    extends LongViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class LongViewOfWrappedByteBufferLittleEndianTest extends LongViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)

// Read only Long views of byte buffers
abstract class ReadOnlyLongViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends LongBufferTest {
  val factory: BufferFactory.LongBufferFactory = new ByteBufferLongViewFactory(byteBufferFactory, order) with BufferFactory.ReadOnlyBufferFactory
}

class ReadOnlyLongViewOfWrappedByteBufferBigEndianTest    extends ReadOnlyLongViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ReadOnlyLongViewOfWrappedByteBufferLittleEndianTest extends ReadOnlyLongViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)
abstract class FloatBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.FloatBufferFactory

  class ByteBufferFloatViewFactory(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends Factory with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    override val createsPointerBuffer: Boolean = byteBufferFactory.createsPointerBuffer

    def baseAllocBuffer(capacity: Int): FloatBuffer =
      byteBufferFactory
        .allocBuffer(capacity * 4)
        .order(order)
        .asFloatBuffer()
  }
}

// Float views of byte buffers
abstract class FloatViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends FloatBufferTest {
  val factory: BufferFactory.FloatBufferFactory = new ByteBufferFloatViewFactory(byteBufferFactory, order)
}

class FloatViewOfWrappedByteBufferBigEndianTest    extends FloatViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class FloatViewOfWrappedByteBufferLittleEndianTest extends FloatViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)

// Read only Float views of byte buffers
abstract class ReadOnlyFloatViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends FloatBufferTest {
  val factory: BufferFactory.FloatBufferFactory = new ByteBufferFloatViewFactory(byteBufferFactory, order) with BufferFactory.ReadOnlyBufferFactory
}

class ReadOnlyFloatViewOfWrappedByteBufferBigEndianTest    extends ReadOnlyFloatViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ReadOnlyFloatViewOfWrappedByteBufferLittleEndianTest extends ReadOnlyFloatViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)
abstract class DoubleBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.DoubleBufferFactory

  class ByteBufferDoubleViewFactory(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends Factory with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    override val createsPointerBuffer: Boolean = byteBufferFactory.createsPointerBuffer

    def baseAllocBuffer(capacity: Int): DoubleBuffer =
      byteBufferFactory
        .allocBuffer(capacity * 8)
        .order(order)
        .asDoubleBuffer()
  }
}

// Double views of byte buffers
abstract class DoubleViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends DoubleBufferTest {
  val factory: BufferFactory.DoubleBufferFactory = new ByteBufferDoubleViewFactory(byteBufferFactory, order)
}

class DoubleViewOfWrappedByteBufferBigEndianTest    extends DoubleViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class DoubleViewOfWrappedByteBufferLittleEndianTest extends DoubleViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)

// Read only Double views of byte buffers
abstract class ReadOnlyDoubleViewOfByteBufferTest(byteBufferFactory: BufferFactory.ByteBufferFactory, order: ByteOrder) extends DoubleBufferTest {
  val factory: BufferFactory.DoubleBufferFactory = new ByteBufferDoubleViewFactory(byteBufferFactory, order) with BufferFactory.ReadOnlyBufferFactory
}

class ReadOnlyDoubleViewOfWrappedByteBufferBigEndianTest    extends ReadOnlyDoubleViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.BIG_ENDIAN)
class ReadOnlyDoubleViewOfWrappedByteBufferLittleEndianTest extends ReadOnlyDoubleViewOfByteBufferTest(new WrappedPointerByteBufferFactory, ByteOrder.LITTLE_ENDIAN)
