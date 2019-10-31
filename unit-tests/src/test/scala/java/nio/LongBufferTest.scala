package java.nio

import java.nio.ByteBufferFactories._

// Ported from Scala.js
abstract class LongBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.LongBufferFactory

  class AllocLongBufferFactory extends Factory {
    def allocBuffer(capacity: Int): LongBuffer =
      LongBuffer.allocate(capacity)
  }

  class WrappedLongBufferFactory
      extends Factory
      with BufferFactory.WrappedBufferFactory {
    def baseWrap(array: Array[Long]): LongBuffer =
      LongBuffer.wrap(array)

    def baseWrap(array: Array[Long], offset: Int, length: Int): LongBuffer =
      LongBuffer.wrap(array, offset, length)
  }

  class ByteBufferLongViewFactory(
      byteBufferFactory: BufferFactory.ByteBufferFactory,
      order: ByteOrder)
      extends Factory
      with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    def baseAllocBuffer(capacity: Int): LongBuffer =
      byteBufferFactory.allocBuffer(capacity * 8).order(order).asLongBuffer()
  }

}

object AllocLongBufferTest extends LongBufferTest {
  val factory: Factory = new AllocLongBufferFactory
}

object WrappedLongBufferTest extends LongBufferTest {
  val factory: Factory = new WrappedLongBufferFactory
}

object WrappedLongReadOnlyBufferTest extends LongBufferTest {
  val factory: Factory =
    new WrappedLongBufferFactory with BufferFactory.ReadOnlyBufferFactory
}

object AllocLongSlicedBufferTest extends LongBufferTest {
  val factory: Factory =
    new AllocLongBufferFactory with BufferFactory.SlicedBufferFactory
}

// Long views of byte buffers

abstract class LongViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends LongBufferTest {

  val factory: BufferFactory.LongBufferFactory =
    new ByteBufferLongViewFactory(byteBufferFactory, order)
}

object LongViewOfAllocByteBufferBigEndianTest
    extends LongViewOfByteBufferTest(new AllocByteBufferFactory,
                                     ByteOrder.BIG_ENDIAN)

object LongViewOfWrappedByteBufferBigEndianTest
    extends LongViewOfByteBufferTest(new WrappedByteBufferFactory,
                                     ByteOrder.BIG_ENDIAN)

object LongViewOfSlicedAllocByteBufferBigEndianTest
    extends LongViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                     ByteOrder.BIG_ENDIAN)

object LongViewOfAllocByteBufferLittleEndianTest
    extends LongViewOfByteBufferTest(new AllocByteBufferFactory,
                                     ByteOrder.LITTLE_ENDIAN)

object LongViewOfWrappedByteBufferLittleEndianTest
    extends LongViewOfByteBufferTest(new WrappedByteBufferFactory,
                                     ByteOrder.LITTLE_ENDIAN)

object LongViewOfSlicedAllocByteBufferLittleEndianTest
    extends LongViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                     ByteOrder.LITTLE_ENDIAN)

// Read only Long views of byte buffers

abstract class ReadOnlyLongViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends LongBufferTest {

  val factory: BufferFactory.LongBufferFactory = {
    new ByteBufferLongViewFactory(byteBufferFactory, order)
      with BufferFactory.ReadOnlyBufferFactory
  }
}

object ReadOnlyLongViewOfAllocByteBufferBigEndianTest
    extends ReadOnlyLongViewOfByteBufferTest(new AllocByteBufferFactory,
                                             ByteOrder.BIG_ENDIAN)

object ReadOnlyLongViewOfWrappedByteBufferBigEndianTest
    extends ReadOnlyLongViewOfByteBufferTest(new WrappedByteBufferFactory,
                                             ByteOrder.BIG_ENDIAN)

object ReadOnlyLongViewOfSlicedAllocByteBufferBigEndianTest
    extends ReadOnlyLongViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                             ByteOrder.BIG_ENDIAN)

object ReadOnlyLongViewOfAllocByteBufferLittleEndianTest
    extends ReadOnlyLongViewOfByteBufferTest(new AllocByteBufferFactory,
                                             ByteOrder.LITTLE_ENDIAN)

object ReadOnlyLongViewOfWrappedByteBufferLittleEndianTest
    extends ReadOnlyLongViewOfByteBufferTest(new WrappedByteBufferFactory,
                                             ByteOrder.LITTLE_ENDIAN)

object ReadOnlyLongViewOfSlicedAllocByteBufferLittleEndianTest
    extends ReadOnlyLongViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                             ByteOrder.LITTLE_ENDIAN)
