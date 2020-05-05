package java.nio

import java.nio.ByteBufferFactories._

// Ported from Scala.js
abstract class ShortBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.ShortBufferFactory

  class AllocShortBufferFactory extends Factory {
    def allocBuffer(capacity: Int): ShortBuffer =
      ShortBuffer.allocate(capacity)
  }

  class WrappedShortBufferFactory
      extends Factory
      with BufferFactory.WrappedBufferFactory {
    def baseWrap(array: Array[Short]): ShortBuffer =
      ShortBuffer.wrap(array)

    def baseWrap(array: Array[Short], offset: Int, length: Int): ShortBuffer =
      ShortBuffer.wrap(array, offset, length)
  }

  class ByteBufferShortViewFactory(
      byteBufferFactory: BufferFactory.ByteBufferFactory,
      order: ByteOrder)
      extends Factory
      with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    def baseAllocBuffer(capacity: Int): ShortBuffer =
      byteBufferFactory.allocBuffer(capacity * 2).order(order).asShortBuffer()
  }

}

object AllocShortBufferTest extends ShortBufferTest {
  val factory: Factory = new AllocShortBufferFactory
}

object WrappedShortBufferTest extends ShortBufferTest {
  val factory: Factory = new WrappedShortBufferFactory
}

object WrappedShortReadOnlyBufferTest extends ShortBufferTest {
  val factory: Factory =
    new WrappedShortBufferFactory with BufferFactory.ReadOnlyBufferFactory
}

object AllocShortSlicedBufferTest extends ShortBufferTest {
  val factory: Factory =
    new AllocShortBufferFactory with BufferFactory.SlicedBufferFactory
}

// Short views of byte buffers

abstract class ShortViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends ShortBufferTest {

  val factory: BufferFactory.ShortBufferFactory =
    new ByteBufferShortViewFactory(byteBufferFactory, order)
}

object ShortViewOfAllocByteBufferBigEndianTest
    extends ShortViewOfByteBufferTest(new AllocByteBufferFactory,
                                      ByteOrder.BIG_ENDIAN)

object ShortViewOfWrappedByteBufferBigEndianTest
    extends ShortViewOfByteBufferTest(new WrappedByteBufferFactory,
                                      ByteOrder.BIG_ENDIAN)

object ShortViewOfSlicedAllocByteBufferBigEndianTest
    extends ShortViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                      ByteOrder.BIG_ENDIAN)

object ShortViewOfAllocByteBufferLittleEndianTest
    extends ShortViewOfByteBufferTest(new AllocByteBufferFactory,
                                      ByteOrder.LITTLE_ENDIAN)

object ShortViewOfWrappedByteBufferLittleEndianTest
    extends ShortViewOfByteBufferTest(new WrappedByteBufferFactory,
                                      ByteOrder.LITTLE_ENDIAN)

object ShortViewOfSlicedAllocByteBufferLittleEndianTest
    extends ShortViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                      ByteOrder.LITTLE_ENDIAN)

// Read only Short views of byte buffers

abstract class ReadOnlyShortViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends ShortBufferTest {

  val factory: BufferFactory.ShortBufferFactory = {
    new ByteBufferShortViewFactory(byteBufferFactory, order)
      with BufferFactory.ReadOnlyBufferFactory
  }
}

object ReadOnlyShortViewOfAllocByteBufferBigEndianTest
    extends ReadOnlyShortViewOfByteBufferTest(new AllocByteBufferFactory,
                                              ByteOrder.BIG_ENDIAN)

object ReadOnlyShortViewOfWrappedByteBufferBigEndianTest
    extends ReadOnlyShortViewOfByteBufferTest(new WrappedByteBufferFactory,
                                              ByteOrder.BIG_ENDIAN)

object ReadOnlyShortViewOfSlicedAllocByteBufferBigEndianTest
    extends ReadOnlyShortViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                              ByteOrder.BIG_ENDIAN)

object ReadOnlyShortViewOfAllocByteBufferLittleEndianTest
    extends ReadOnlyShortViewOfByteBufferTest(new AllocByteBufferFactory,
                                              ByteOrder.LITTLE_ENDIAN)

object ReadOnlyShortViewOfWrappedByteBufferLittleEndianTest
    extends ReadOnlyShortViewOfByteBufferTest(new WrappedByteBufferFactory,
                                              ByteOrder.LITTLE_ENDIAN)

object ReadOnlyShortViewOfSlicedAllocByteBufferLittleEndianTest
    extends ReadOnlyShortViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                              ByteOrder.LITTLE_ENDIAN)
