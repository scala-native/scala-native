package java.nio
import java.nio.ByteBufferFactories._

// Ported from Scala.js
abstract class FloatBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.FloatBufferFactory

  class AllocFloatBufferFactory extends Factory {
    def allocBuffer(capacity: Int): FloatBuffer =
      FloatBuffer.allocate(capacity)
  }

  class WrappedFloatBufferFactory
      extends Factory
      with BufferFactory.WrappedBufferFactory {
    def baseWrap(array: Array[Float]): FloatBuffer =
      FloatBuffer.wrap(array)

    def baseWrap(array: Array[Float], offset: Int, length: Int): FloatBuffer =
      FloatBuffer.wrap(array, offset, length)
  }

  class ByteBufferFloatViewFactory(
      byteBufferFactory: BufferFactory.ByteBufferFactory,
      order: ByteOrder)
      extends Factory
      with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    def baseAllocBuffer(capacity: Int): FloatBuffer =
      byteBufferFactory.allocBuffer(capacity * 4).order(order).asFloatBuffer()
  }

}

object AllocFloatBufferTest extends FloatBufferTest {
  val factory: Factory = new AllocFloatBufferFactory
}

object WrappedFloatBufferTest extends FloatBufferTest {
  val factory: Factory = new WrappedFloatBufferFactory
}

object WrappedFloatReadOnlyBufferTest extends FloatBufferTest {
  val factory: Factory =
    new WrappedFloatBufferFactory with BufferFactory.ReadOnlyBufferFactory
}

object AllocFloatSlicedBufferTest extends FloatBufferTest {
  val factory: Factory =
    new AllocFloatBufferFactory with BufferFactory.SlicedBufferFactory
}

// Float views of byte buffers

abstract class FloatViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends FloatBufferTest {

  val factory: BufferFactory.FloatBufferFactory =
    new ByteBufferFloatViewFactory(byteBufferFactory, order)
}

object FloatViewOfAllocByteBufferBigEndianTest
    extends FloatViewOfByteBufferTest(new AllocByteBufferFactory,
                                      ByteOrder.BIG_ENDIAN)

object FloatViewOfWrappedByteBufferBigEndianTest
    extends FloatViewOfByteBufferTest(new WrappedByteBufferFactory,
                                      ByteOrder.BIG_ENDIAN)

object FloatViewOfSlicedAllocByteBufferBigEndianTest
    extends FloatViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                      ByteOrder.BIG_ENDIAN)

object FloatViewOfAllocByteBufferLittleEndianTest
    extends FloatViewOfByteBufferTest(new AllocByteBufferFactory,
                                      ByteOrder.LITTLE_ENDIAN)

object FloatViewOfWrappedByteBufferLittleEndianTest
    extends FloatViewOfByteBufferTest(new WrappedByteBufferFactory,
                                      ByteOrder.LITTLE_ENDIAN)

object FloatViewOfSlicedAllocByteBufferLittleEndianTest
    extends FloatViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                      ByteOrder.LITTLE_ENDIAN)

// Read only Float views of byte buffers

abstract class ReadOnlyFloatViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends FloatBufferTest {

  val factory: BufferFactory.FloatBufferFactory = {
    new ByteBufferFloatViewFactory(byteBufferFactory, order)
      with BufferFactory.ReadOnlyBufferFactory
  }
}

object ReadOnlyFloatViewOfAllocByteBufferBigEndianTest
    extends ReadOnlyFloatViewOfByteBufferTest(new AllocByteBufferFactory,
                                              ByteOrder.BIG_ENDIAN)

object ReadOnlyFloatViewOfWrappedByteBufferBigEndianTest
    extends ReadOnlyFloatViewOfByteBufferTest(new WrappedByteBufferFactory,
                                              ByteOrder.BIG_ENDIAN)

object ReadOnlyFloatViewOfSlicedAllocByteBufferBigEndianTest
    extends ReadOnlyFloatViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                              ByteOrder.BIG_ENDIAN)

object ReadOnlyFloatViewOfAllocByteBufferLittleEndianTest
    extends ReadOnlyFloatViewOfByteBufferTest(new AllocByteBufferFactory,
                                              ByteOrder.LITTLE_ENDIAN)

object ReadOnlyFloatViewOfWrappedByteBufferLittleEndianTest
    extends ReadOnlyFloatViewOfByteBufferTest(new WrappedByteBufferFactory,
                                              ByteOrder.LITTLE_ENDIAN)

object ReadOnlyFloatViewOfSlicedAllocByteBufferLittleEndianTest
    extends ReadOnlyFloatViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                              ByteOrder.LITTLE_ENDIAN)
