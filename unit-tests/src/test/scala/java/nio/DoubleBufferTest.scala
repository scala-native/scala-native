package java.nio

import java.nio.ByteBufferFactories._

// Ported from Scala.js
abstract class DoubleBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.DoubleBufferFactory

  class AllocDoubleBufferFactory extends Factory {
    def allocBuffer(capacity: Int): DoubleBuffer =
      DoubleBuffer.allocate(capacity)
  }

  class WrappedDoubleBufferFactory
      extends Factory
      with BufferFactory.WrappedBufferFactory {
    def baseWrap(array: Array[Double]): DoubleBuffer =
      DoubleBuffer.wrap(array)

    def baseWrap(array: Array[Double], offset: Int, length: Int): DoubleBuffer =
      DoubleBuffer.wrap(array, offset, length)
  }

  class ByteBufferDoubleViewFactory(
      byteBufferFactory: BufferFactory.ByteBufferFactory,
      order: ByteOrder)
      extends Factory
      with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    def baseAllocBuffer(capacity: Int): DoubleBuffer =
      byteBufferFactory.allocBuffer(capacity * 8).order(order).asDoubleBuffer()
  }

}

object AllocDoubleBufferTest extends DoubleBufferTest {
  val factory: Factory = new AllocDoubleBufferFactory
}

object WrappedDoubleBufferTest extends DoubleBufferTest {
  val factory: Factory = new WrappedDoubleBufferFactory
}

object WrappedDoubleReadOnlyBufferTest extends DoubleBufferTest {
  val factory: Factory =
    new WrappedDoubleBufferFactory with BufferFactory.ReadOnlyBufferFactory
}

object AllocDoubleSlicedBufferTest extends DoubleBufferTest {
  val factory: Factory =
    new AllocDoubleBufferFactory with BufferFactory.SlicedBufferFactory
}

// Double views of byte buffers

abstract class DoubleViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends DoubleBufferTest {

  val factory: BufferFactory.DoubleBufferFactory =
    new ByteBufferDoubleViewFactory(byteBufferFactory, order)
}

object DoubleViewOfAllocByteBufferBigEndianTest
    extends DoubleViewOfByteBufferTest(new AllocByteBufferFactory,
                                       ByteOrder.BIG_ENDIAN)

object DoubleViewOfWrappedByteBufferBigEndianTest
    extends DoubleViewOfByteBufferTest(new WrappedByteBufferFactory,
                                       ByteOrder.BIG_ENDIAN)

object DoubleViewOfSlicedAllocByteBufferBigEndianTest
    extends DoubleViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                       ByteOrder.BIG_ENDIAN)

object DoubleViewOfAllocByteBufferLittleEndianTest
    extends DoubleViewOfByteBufferTest(new AllocByteBufferFactory,
                                       ByteOrder.LITTLE_ENDIAN)

object DoubleViewOfWrappedByteBufferLittleEndianTest
    extends DoubleViewOfByteBufferTest(new WrappedByteBufferFactory,
                                       ByteOrder.LITTLE_ENDIAN)

object DoubleViewOfSlicedAllocByteBufferLittleEndianTest
    extends DoubleViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                       ByteOrder.LITTLE_ENDIAN)

// Read only Double views of byte buffers

abstract class ReadOnlyDoubleViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends DoubleBufferTest {

  val factory: BufferFactory.DoubleBufferFactory = {
    new ByteBufferDoubleViewFactory(byteBufferFactory, order)
      with BufferFactory.ReadOnlyBufferFactory
  }
}

object ReadOnlyDoubleViewOfAllocByteBufferBigEndianTest
    extends ReadOnlyDoubleViewOfByteBufferTest(new AllocByteBufferFactory,
                                               ByteOrder.BIG_ENDIAN)

object ReadOnlyDoubleViewOfWrappedByteBufferBigEndianTest
    extends ReadOnlyDoubleViewOfByteBufferTest(new WrappedByteBufferFactory,
                                               ByteOrder.BIG_ENDIAN)

object ReadOnlyDoubleViewOfSlicedAllocByteBufferBigEndianTest
    extends ReadOnlyDoubleViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                               ByteOrder.BIG_ENDIAN)

object ReadOnlyDoubleViewOfAllocByteBufferLittleEndianTest
    extends ReadOnlyDoubleViewOfByteBufferTest(new AllocByteBufferFactory,
                                               ByteOrder.LITTLE_ENDIAN)

object ReadOnlyDoubleViewOfWrappedByteBufferLittleEndianTest
    extends ReadOnlyDoubleViewOfByteBufferTest(new WrappedByteBufferFactory,
                                               ByteOrder.LITTLE_ENDIAN)

object ReadOnlyDoubleViewOfSlicedAllocByteBufferLittleEndianTest
    extends ReadOnlyDoubleViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                               ByteOrder.LITTLE_ENDIAN)
