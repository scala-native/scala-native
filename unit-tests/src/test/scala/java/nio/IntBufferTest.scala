package java.nio

import java.nio.ByteBufferFactories._

// Ported from Scala.js
abstract class IntBufferTest extends BaseBufferTest {
  type Factory = BufferFactory.IntBufferFactory

  class AllocIntBufferFactory extends Factory {
    def allocBuffer(capacity: Int): IntBuffer =
      IntBuffer.allocate(capacity)
  }

  class WrappedIntBufferFactory
      extends Factory
      with BufferFactory.WrappedBufferFactory {
    def baseWrap(array: Array[Int]): IntBuffer =
      IntBuffer.wrap(array)

    def baseWrap(array: Array[Int], offset: Int, length: Int): IntBuffer =
      IntBuffer.wrap(array, offset, length)
  }

  class ByteBufferIntViewFactory(
      byteBufferFactory: BufferFactory.ByteBufferFactory,
      order: ByteOrder)
      extends Factory
      with BufferFactory.ByteBufferViewFactory {
    require(!byteBufferFactory.createsReadOnly)

    def baseAllocBuffer(capacity: Int): IntBuffer =
      byteBufferFactory.allocBuffer(capacity * 4).order(order).asIntBuffer()
  }

}

object AllocIntBufferTest extends IntBufferTest {
  val factory: Factory = new AllocIntBufferFactory
}

object WrappedIntBufferTest extends IntBufferTest {
  val factory: Factory = new WrappedIntBufferFactory
}

object WrappedIntReadOnlyBufferTest extends IntBufferTest {
  val factory: Factory =
    new WrappedIntBufferFactory with BufferFactory.ReadOnlyBufferFactory
}

object AllocIntSlicedBufferTest extends IntBufferTest {
  val factory: Factory =
    new AllocIntBufferFactory with BufferFactory.SlicedBufferFactory
}

abstract class IntViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends IntBufferTest {

  val factory: BufferFactory.IntBufferFactory =
    new ByteBufferIntViewFactory(byteBufferFactory, order)
}

object IntViewOfAllocByteBufferBigEndianTest
    extends IntViewOfByteBufferTest(new AllocByteBufferFactory,
                                    ByteOrder.BIG_ENDIAN)

object IntViewOfWrappedByteBufferBigEndianTest
    extends IntViewOfByteBufferTest(new WrappedByteBufferFactory,
                                    ByteOrder.BIG_ENDIAN)

object IntViewOfSlicedAllocByteBufferBigEndianTest
    extends IntViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                    ByteOrder.BIG_ENDIAN)

object IntViewOfAllocByteBufferLittleEndianTest
    extends IntViewOfByteBufferTest(new AllocByteBufferFactory,
                                    ByteOrder.LITTLE_ENDIAN)

object IntViewOfWrappedByteBufferLittleEndianTest
    extends IntViewOfByteBufferTest(new WrappedByteBufferFactory,
                                    ByteOrder.LITTLE_ENDIAN)

object IntViewOfSlicedAllocByteBufferLittleEndianTest
    extends IntViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                    ByteOrder.LITTLE_ENDIAN)

// Read only Int views of byte buffers

abstract class ReadOnlyIntViewOfByteBufferTest(
    byteBufferFactory: BufferFactory.ByteBufferFactory,
    order: ByteOrder)
    extends IntBufferTest {

  val factory: BufferFactory.IntBufferFactory = {
    new ByteBufferIntViewFactory(byteBufferFactory, order)
      with BufferFactory.ReadOnlyBufferFactory
  }
}

object ReadOnlyIntViewOfAllocByteBufferBigEndianTest
    extends ReadOnlyIntViewOfByteBufferTest(new AllocByteBufferFactory,
                                            ByteOrder.BIG_ENDIAN)

object ReadOnlyIntViewOfWrappedByteBufferBigEndianTest
    extends ReadOnlyIntViewOfByteBufferTest(new WrappedByteBufferFactory,
                                            ByteOrder.BIG_ENDIAN)

object ReadOnlyIntViewOfSlicedAllocByteBufferBigEndianTest
    extends ReadOnlyIntViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                            ByteOrder.BIG_ENDIAN)

object ReadOnlyIntViewOfAllocByteBufferFactoryEndianTest
    extends ReadOnlyIntViewOfByteBufferTest(new AllocByteBufferFactory,
                                            ByteOrder.LITTLE_ENDIAN)

object ReadOnlyIntViewOfWrappedByteBufferLittleEndianTest
    extends ReadOnlyIntViewOfByteBufferTest(new WrappedByteBufferFactory,
                                            ByteOrder.LITTLE_ENDIAN)

object ReadOnlyIntViewOfSlicedAllocByteBufferLittleEndianTest
    extends ReadOnlyIntViewOfByteBufferTest(new SlicedAllocByteBufferFactory,
                                            ByteOrder.LITTLE_ENDIAN)
