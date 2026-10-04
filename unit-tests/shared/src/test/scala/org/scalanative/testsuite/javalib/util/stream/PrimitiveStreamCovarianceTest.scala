package org.scalanative.testsuite.javalib.util.stream

import java.lang.{Double => JDouble, Integer, Long => JLong}
import java.util.function.{DoubleConsumer, IntConsumer, LongConsumer}
import java.util.stream.{BaseStream, DoubleStream, IntStream, LongStream}
import java.util.{PrimitiveIterator, Spliterator}

import org.junit.Assert._
import org.junit.Test

class PrimitiveStreamCovarianceTest {
  @Test def intIteratorAndSpliterator(): Unit = {
    val iterator: PrimitiveIterator.OfInt = IntStream.of(3, 5).iterator()
    assertEquals(3, iterator.nextInt())
    assertEquals(5, iterator.nextInt())
    assertFalse(iterator.hasNext())
    val spliterator: Spliterator.OfInt = IntStream.of(7).spliterator()
    var value = 0
    assertTrue(spliterator.tryAdvance(((n: Int) => value = n): IntConsumer))
    assertEquals(7, value)
    assertFalse(spliterator.tryAdvance(((n: Int) => value = n): IntConsumer))
    val parent: BaseStream[Integer, IntStream] = IntStream.of(9)
    assertEquals(9, parent.iterator().next().intValue())
    val splitParent: BaseStream[Integer, IntStream] = IntStream.of(11)
    assertEquals(1L, splitParent.spliterator().getExactSizeIfKnown())
  }

  @Test def longIteratorAndSpliterator(): Unit = {
    val iterator: PrimitiveIterator.OfLong = LongStream.of(3L, 5L).iterator()
    assertEquals(3L, iterator.nextLong())
    assertEquals(5L, iterator.nextLong())
    assertFalse(iterator.hasNext())
    val spliterator: Spliterator.OfLong = LongStream.of(7L).spliterator()
    var value = 0L
    assertTrue(spliterator.tryAdvance(((n: Long) => value = n): LongConsumer))
    assertEquals(7L, value)
    assertFalse(spliterator.tryAdvance(((n: Long) => value = n): LongConsumer))
    val parent: BaseStream[JLong, LongStream] = LongStream.of(9L)
    assertEquals(9L, parent.iterator().next().longValue())
    val splitParent: BaseStream[JLong, LongStream] = LongStream.of(11L)
    assertEquals(1L, splitParent.spliterator().getExactSizeIfKnown())
  }

  @Test def doubleIteratorAndSpliterator(): Unit = {
    val iterator: PrimitiveIterator.OfDouble =
      DoubleStream.of(3.0, 5.0).iterator()
    assertEquals(3.0, iterator.nextDouble(), 0.0)
    assertEquals(5.0, iterator.nextDouble(), 0.0)
    assertFalse(iterator.hasNext())
    val spliterator: Spliterator.OfDouble = DoubleStream.of(7.0).spliterator()
    var value = 0.0
    assertTrue(
      spliterator.tryAdvance(((n: Double) => value = n): DoubleConsumer)
    )
    assertEquals(7.0, value, 0.0)
    assertFalse(
      spliterator.tryAdvance(((n: Double) => value = n): DoubleConsumer)
    )
    val parent: BaseStream[JDouble, DoubleStream] = DoubleStream.of(9.0)
    assertEquals(9.0, parent.iterator().next().doubleValue(), 0.0)
    val splitParent: BaseStream[JDouble, DoubleStream] = DoubleStream.of(11.0)
    assertEquals(1L, splitParent.spliterator().getExactSizeIfKnown())
  }

  @Test def intExecutionMode(): Unit = {
    val stream = IntStream.of(1, 2)
    val parallel: IntStream = stream.parallel()
    assertSame(stream, parallel)
    assertTrue(parallel.isParallel())
    val sequential: IntStream = parallel.sequential()
    assertSame(stream, sequential)
    assertFalse(sequential.isParallel())
    val parent: BaseStream[Integer, IntStream] = stream
    assertSame(stream, parent.parallel())
    assertTrue(stream.isParallel())
    assertSame(stream, parent.sequential())
    assertFalse(stream.isParallel())
    assertEquals(3, stream.sum())
  }

  @Test def longExecutionMode(): Unit = {
    val stream = LongStream.of(1L, 2L)
    val parallel: LongStream = stream.parallel()
    assertSame(stream, parallel)
    assertTrue(parallel.isParallel())
    val sequential: LongStream = parallel.sequential()
    assertSame(stream, sequential)
    assertFalse(sequential.isParallel())
    val parent: BaseStream[JLong, LongStream] = stream
    assertSame(stream, parent.parallel())
    assertTrue(stream.isParallel())
    assertSame(stream, parent.sequential())
    assertFalse(stream.isParallel())
    assertEquals(3L, stream.sum())
  }

  @Test def doubleExecutionMode(): Unit = {
    val stream = DoubleStream.of(1.0, 2.0)
    val parallel: DoubleStream = stream.parallel()
    assertSame(stream, parallel)
    assertTrue(parallel.isParallel())
    val sequential: DoubleStream = parallel.sequential()
    assertSame(stream, sequential)
    assertFalse(sequential.isParallel())
    val parent: BaseStream[JDouble, DoubleStream] = stream
    assertSame(stream, parent.parallel())
    assertTrue(stream.isParallel())
    assertSame(stream, parent.sequential())
    assertFalse(stream.isParallel())
    assertEquals(3.0, stream.sum(), 0.0)
  }
}
