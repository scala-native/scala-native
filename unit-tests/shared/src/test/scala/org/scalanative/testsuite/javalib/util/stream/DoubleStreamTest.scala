package org.scalanative.testsuite.javalib.util.stream

/* It is hard to assure oneself that the desired primitive DoubleStream,
 * LongStream, & IntStream are being used instead of a/an (object) Stream.
 * Create DoubleStream & kin using the methods in Arrays.
 *
 * Do not import ArrayList here, to guard against a Test populating
 * an ArrayList and then inadvertently creating an (object) Stream with it.
 * Use ju.ArrayList surgically at the points of use.
 */

import java.{lang => jl}

import java.{util => ju}
import java.util.Arrays
import java.util.{OptionalDouble, DoubleSummaryStatistics}
import java.util.Spliterator
import java.util.Spliterators

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.CountDownLatch._

import java.util.function.{DoubleConsumer, DoubleFunction, DoubleSupplier}
import java.util.function.Supplier

import java.util.stream._

import org.junit.Test
import org.junit.Assert._
import org.junit.BeforeClass
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Design Note:
 *   Two tests requiring classes which have not been implemented yet
 *   are commented out:
 *     - doubleStreamMapToInt, required IntStream
 *     - doubleStreamMapToLong, requires LongStream
 */

object DoubleStreamTest {
  @BeforeClass def checkLimitMethodCharacteristics(): Unit =
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()
}

class DoubleStreamTest {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

// Methods specified in interface BaseStream ----------------------------

  @Test def streamUnorderedOnUnorderedStream(): Unit = {
    val dataSet = new ju.HashSet[Double]()
    dataSet.add(0.1)
    dataSet.add(1.1)
    dataSet.add(-1.1)
    dataSet.add(2.2)
    dataSet.add(-2.2)

    val s0 = dataSet.stream()
    val s0Spliter = s0.spliterator()
    assertFalse(
      "Unexpected ORDERED stream from hashset",
      s0Spliter.hasCharacteristics(Spliterator.ORDERED)
    )

    val su = dataSet.stream().unordered()
    val suSpliter = su.spliterator()

    assertFalse(
      "Unexpected ORDERED stream",
      suSpliter.hasCharacteristics(Spliterator.ORDERED)
    )
  }

  @Test def streamUnorderedOnOrderedStream(): Unit = {
    val s = DoubleStream.of(0.1, 1.1, -1.1, 2.2, -2.2)
    val sSpliter = s.spliterator()

    assertTrue(
      "Expected ORDERED on stream from array",
      sSpliter.hasCharacteristics(Spliterator.ORDERED)
    )

    // s was ordered, 'so' should be same same. Avoid "already used" exception
    val so = DoubleStream.of(0.1, 1.1, -1.1, 2.2, -2.2)
    val su = so.unordered()
    val suSpliter = su.spliterator()

    assertFalse(
      "ORDERED stream after unordered()",
      suSpliter.hasCharacteristics(Spliterator.ORDERED)
    )
  }

  @Test def streamParallel(): Unit = {
    val nElements = 5

    val wild = new Array[Double](nElements) // holds arbitrarily jumbled data
    wild(0) = 132.45
    wild(1) = 4.21
    wild(2) = 2.11
    wild(3) = 55.31
    wild(4) = 16.68

    val sPar0 =
      StreamSupport.doubleStream(Spliterators.spliterator(wild, 0), true)

    assertTrue(
      "Expected parallel stream",
      sPar0.isParallel()
    )

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED // 0x4040

    val sPar0Spliterator = sPar0.spliterator()
    assertEquals(
      "parallel characteristics",
      expectedCharacteristics,
      sPar0Spliterator.characteristics()
    )

    val sPar =
      StreamSupport.doubleStream(Spliterators.spliterator(wild, 0), true)

    val sSeq = sPar.sequential()
    assertFalse(
      "Expected sequential stream",
      sSeq.isParallel()
    )

    val sSeqSpliterator = sSeq.spliterator()

    assertEquals(
      "sequential characteristics",
      expectedCharacteristics,
      sSeqSpliterator.characteristics()
    )

    assertEquals(
      "Unexpected sequential stream size",
      nElements,
      sSeqSpliterator.estimateSize()
    )

    // sequential stream has expected contents
    var count = 0
    sSeqSpliterator.forEachRemaining((e: Double) => {
      assertEquals(
        s"sequential stream contents(${count})",
        wild(count),
        e,
        epsilon
      )
      count += 1
    })
  }

  @Test def streamSequential(): Unit = {
    val nElements = 5

    val wild = new Array[Double](nElements) // holds arbitrarily jumbled data
    wild(0) = 132.45
    wild(1) = 4.21
    wild(2) = 2.11
    wild(3) = 55.31
    wild(4) = 16.68

    val sSeq0 =
      StreamSupport.doubleStream(Spliterators.spliterator(wild, 0), false)

    assertFalse(
      "Expected sequential stream",
      sSeq0.isParallel()
    )

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED // 0x4040

    val sSeq0Spliterator = sSeq0.spliterator()
    assertEquals(
      "sequential characteristics",
      expectedCharacteristics,
      sSeq0Spliterator.characteristics()
    )

    val sSeq =
      StreamSupport.doubleStream(Spliterators.spliterator(wild, 0), false)

    val sPar = sSeq.parallel()
    assertTrue(
      "Expected parallel stream",
      sSeq.isParallel()
    )

    val sParSpliterator = sPar.spliterator()

    assertEquals(
      "parallel characteristics",
      expectedCharacteristics,
      sParSpliterator.characteristics()
    )

    assertEquals(
      "Unexpected parallel stream size",
      nElements,
      sParSpliterator.estimateSize()
    )

    // parallel stream has expected contents
    var count = 0
    sParSpliterator.forEachRemaining((e: Double) => {
      assertEquals(
        s"parallel stream contents(${count})",
        wild(count),
        e,
        epsilon
      )
      count += 1
    })
  }

// Methods specified in interface Double Stream -------------------------

  @Test def doubleStreamBuilderCanBuildAnEmptyStream(): Unit = {
    val s = DoubleStream.builder().build()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def doubleStreamBuilderCharacteristics(): Unit = {
    val bldr = Stream.builder[Double]()
    bldr
      .add(1.1)
      .add(-1.1)
      .add(9.9)

    val s = bldr.build()
    val spliter = s.spliterator()

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED // 0x4050

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      spliter.characteristics()
    )
  }

  @Test def doubleStreamEmptyIsEmpty(): Unit = {
    val s = DoubleStream.empty()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def doubleStreamOf_SingleElement(): Unit = {
    val expected = 7.7
    val s = DoubleStream.of(expected)
    val it = s.iterator()
    assertTrue("DoubleStream should not be empty", it.hasNext())
    assertEquals("unexpected element", it.nextDouble(), expected, epsilon)
    assertFalse("DoubleStream should be empty and is not.", it.hasNext())
  }

  @Test def streamOf_SingleElementCharacteristics(): Unit = {
    val expected = 7.7

    val s = DoubleStream.of(expected)
    val spliter = s.spliterator()

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.ORDERED | Spliterator.IMMUTABLE // 0x4450

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      spliter.characteristics()
    )
  }

  @Test def doubleStreamOf_MultipleElements(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)
    val it = s.iterator()
    assertEquals("element_1", 1.1, it.nextDouble(), epsilon)
    assertEquals("element_2", 2.2, it.nextDouble(), epsilon)
    assertEquals("element_3", 3.3, it.nextDouble(), epsilon)
    assertFalse(it.hasNext())
  }

  @Test def streamOf_MultipleElementsCharacteristics(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)
    val spliter = s.spliterator()

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.ORDERED | Spliterator.IMMUTABLE // 0x4450

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      spliter.characteristics()
    )
  }

  @Test def doubleStreamFlatMapWorks(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)

    val mapper = new DoubleFunction[DoubleStream] {
      override def apply(v: Double): DoubleStream =
        DoubleStream.of(v, v)
    }

    val s2 = s.flatMap(mapper)

    val it = s2.iterator()

    assertEquals(1.1, it.nextDouble(), epsilon)
    assertEquals(1.1, it.nextDouble(), epsilon)

    assertEquals(2.2, it.nextDouble(), epsilon)
    assertEquals(2.2, it.nextDouble(), epsilon)

    assertEquals(3.3, it.nextDouble(), epsilon)
    assertEquals(3.3, it.nextDouble(), epsilon)

    assertFalse(it.hasNext())
  }

  @Test def doubleStreamForEachWorks(): Unit = {
    val s = DoubleStream.of(-1.1, -2.2, -3.3, 0.0)

    var sum = 0.0
    val doubleConsumer = new DoubleConsumer {
      def accept(d: Double): Unit = sum += d
    }

    s.forEach(doubleConsumer)
    assertEquals(-6.6, sum, epsilon)
  }

  @Test def doubleStreamFlatMapWorksTwice(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)

    val mapper1 = new DoubleFunction[DoubleStream] {
      override def apply(v: Double): DoubleStream =
        DoubleStream.of(v, v)
    }

    val mapper2 = new DoubleFunction[DoubleStream] {
      override def apply(v: Double): DoubleStream =
        DoubleStream.of(-v, -v, -v)
    }

    val s2 = s
      .flatMap(mapper1)
      .flatMap(mapper2)

// format: off
    val expected =
      Seq(
        -1.1, -1.1, -1.1, -1.1, -1.1, -1.1,
        -2.2, -2.2, -2.2, -2.2, -2.2, -2.2,
        -3.3, -3.3, -3.3, -3.3, -3.3, -3.3
      )
// format: on

    val result = scala.collection.mutable.ArrayBuffer.empty[Double]
    val it = s2.iterator()

    while (it.hasNext()) {
      result += it.nextDouble()
    }

    assertTrue(result == expected)
  }

  @Test def doubleStreamOnCloseWorks(): Unit = {
    var latch = new CountDownLatch(1)

    class Closer(cdLatch: CountDownLatch) extends Runnable {
      override def run(): Unit = cdLatch.countDown()
    }

    val s = DoubleStream.empty().onClose(new Closer(latch))
    s.close()

    val timeout = 30L
    assertTrue(
      "close handler did not run within ${timeout} seconds",
      latch.await(timeout, TimeUnit.SECONDS)
    )
  }

// Static methods -------------------------------------------------------

  @Test def doubleStreamConcat(): Unit = {
    val a = DoubleStream.of(9.9, 8.8, 6.6, 7.7, 5.5)
    val b = DoubleStream.of(0.0, 3.3, 2.2)

    val s = DoubleStream.concat(a, b)

    val it = s.iterator()
    assertNotNull("s.iterator() should not be NULL", it)
    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"element", 9.9, it.nextDouble(), epsilon)
    assertEquals(s"element", 8.8, it.nextDouble(), epsilon)
    assertEquals(s"element", 6.6, it.nextDouble(), epsilon)
    assertEquals(s"element", 7.7, it.nextDouble(), epsilon)
    assertEquals(s"element", 5.5, it.nextDouble(), epsilon)

    assertEquals(s"element", 0.0, it.nextDouble(), epsilon)
    assertEquals(s"element", 3.3, it.nextDouble(), epsilon)
    assertEquals(s"element", 2.2, it.nextDouble(), epsilon)

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def doubleStreamGenerate(): Unit = {
    val nElements = 5
    val data = new Array[Double](nElements)
    data(0) = 0.0
    data(1) = 1.1
    data(2) = 2.2
    data(3) = 3.3
    data(4) = 4.4

    val src = new DoubleSupplier() {
      var count = -1

      def getAsDouble(): Double = {
        count += 1
        data(count % nElements)
      }
    }

    val s = DoubleStream.generate(src)

    val it = s.iterator()

    assertTrue("DoubleStream should not be empty", it.hasNext())

    for (j <- 0 until nElements)
      assertEquals(s"data(${j})", it.nextDouble(), data(j), epsilon)

    assertTrue("DoubleStream should not be empty", it.hasNext())
  }

  @Test def doubleStreamIterate_Unbounded(): Unit = {
    val nElements = 4
    var count = -1.0

    val expectedSeed = 3.14

    val expected = Seq(expectedSeed, 4.24, 5.34, 6.44)

    val s = DoubleStream.iterate(
      expectedSeed,
      e => e + 1.1
    )

    val it = s.iterator()

    assertTrue("DoubleStream should not be empty", it.hasNext())

    for (j <- 0 until nElements)
      assertEquals(s"element: ${j})", expected(j), it.nextDouble(), epsilon)

    assertTrue("DoubleStream should not be empty", it.hasNext())
  }

  @Test def doubleStreamIterate_Unbounded_Characteristics(): Unit = {
    val s = DoubleStream.iterate(0.0, n => n + 1.1)
    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    // Note: DoubleStream requires NONNULL, whereas Stream[T] does not.
    val requiredPresent =
      Seq(Spliterator.ORDERED, Spliterator.IMMUTABLE, Spliterator.NONNULL)

    val requiredAbsent = Seq(
      Spliterator.SORTED,
      Spliterator.SIZED,
      Spliterator.SUBSIZED
    )

    StreamTestHelpers.verifyCharacteristics(
      spliter,
      requiredPresent,
      requiredAbsent
    )

    // If SIZED is indeed missing, as expected, these conditions should hold.
    assertEquals(s"getExactSizeIfKnown", -1L, spliter.getExactSizeIfKnown())
    assertEquals(s"estimateSize", Long.MaxValue, spliter.estimateSize())
  }

  @Test def doubleStreamOf_NoItems(): Unit = {
    val s = DoubleStream.of()

    val it = s.iterator()
    assertFalse("DoubleStream should be empty", it.hasNext())
  }

  @Test def doubleStreamOf_OneItem(): Unit = {
    val expected = 6.67
    val s = DoubleStream.of(expected)

    val it = s.iterator()
    assertTrue("stream should not be empty", it.hasNext())
    assertEquals(s"element", expected, it.nextDouble(), epsilon)

    assertFalse("DoubleStream should be empty", it.hasNext())
  }

  // DoubleStream.of() with more than two arguments is exercised in many other
  // places in this file, so no Test for that case here.

// Instance methods -----------------------------------------------------

  @Test def doubleStreamAllMatch_EmptyStream(): Unit = {
    val s = DoubleStream.empty()
    var predEvaluated = false

    val matched = s.allMatch((e) => { predEvaluated = true; true })
    assertTrue("unexpected match failure", matched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def doubleStreamAllMatch_True(): Unit = {

    /* DoubleStream.allMatch() will return "true" on an empty stream.
     *  Try to distinguish that "true" from an actual all-elements-match "true"
     *  Since streams can not be re-used, count s0. If it is non-empty, assume
     * its sibling s is also non-empty, distingishing the two "true"s.
     */
    val s0 = DoubleStream.of(0.0, 1.1, 2.2, 3.3)
    assertTrue("unexpected empty stream", s0.count > 0)

    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)

    val matched = s.allMatch((e) => { (e >= 0.0) && (e < 10.0) })
    assertTrue("unexpected match failure", matched)
  }

  @Test def doubleStreamAllMatch_False(): Unit = {
    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)

    val matched = s.allMatch((e) => e > 2.2)
    assertFalse("unexpected match", matched)
  }

  @Test def doubleStreamAnyMatch_EmptyStream(): Unit = {
    val s = DoubleStream.empty()
    var predEvaluated = false

    val matched = s.anyMatch((e) => { predEvaluated = true; true })
    assertFalse("unexpected match", matched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def doubleStreamAnyMatch_True(): Unit = {
    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)

    val matched = s.anyMatch((e) => (e > 1.0) && (e < 2.0))
    assertTrue("unexpected predicate failure", matched)
  }

  @Test def doubleStreamAnyMatch_False(): Unit = {
    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)

    val matched = s.anyMatch((e) => e > 10.0)
    assertFalse("unexpected predicate failure", matched)
  }

  @Test def doubleStreamAverage_EmptyStream(): Unit = {
    val s = DoubleStream.empty()

    val optional = s.average()

    assertFalse(s"expected empty optional, got value", optional.isPresent())
  }

  @Test def doubleStreamAverage(): Unit = {
    val nElements = 8

    val wild = new Array[Double](nElements) // holds arbitrarily jumbled data
    wild(0) = 132.45
    wild(1) = 4.21
    wild(2) = 2.11
    wild(3) = 55.31
    wild(4) = 16.68
    wild(5) = 77.3
    wild(6) = 44.61
    wild(7) = 60.9

    val expectedAverage = 49.19625

    val s = DoubleStream.of(wild: _*)

    val optional = s.average()

    assertTrue("unexpected empty optional", optional.isPresent())

    assertEquals(
      "unexpected average",
      expectedAverage,
      optional.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamBoxed(): Unit = {
    val nElements = 5
    val data = new Array[Double](nElements)
    data(0) = 0.0
    data(1) = 1.1
    data(2) = 2.2
    data(3) = 3.3
    data(4) = 4.4

    val sd = Arrays.stream(data)

    assertTrue(
      "stream should be a DoubleStream",
      sd.isInstanceOf[DoubleStream]
    )

    val sBoxed = sd.boxed()

    assertTrue(
      "resultant stream should be boxed Stream[Double]",
      sBoxed.isInstanceOf[Stream[_]]
    )

    assertFalse(
      "resultant stream should not be a DoubleStream",
      sBoxed.isInstanceOf[DoubleStream]
    )
  }

  @Test def doubleStreamCollect_EmptyStreamUsingSupplier(): Unit = {
    type U = ju.ArrayList[Double]

    val s = DoubleStream.empty()

    val supplier = new Supplier[U]() {
      def get(): U = new U
    }

    val collected = s.collect(
      supplier,
      (list: U, e: Double) => list.add(e),
      (list1: U, list2: U) => list1.addAll(list2)
    )

    // Proper size
    assertEquals("list size", 0, collected.size())
  }

  @Test def doubleStreamCollect_UsingSupplier(): Unit = {
    type U = ju.ArrayList[Double]

    val nElements = 5
    val data = new Array[Double](nElements)
    data(0) = 0.0
    data(1) = 1.1
    data(2) = 2.2
    data(3) = 3.3
    data(4) = 4.4

    val s = Arrays.stream(data)

    val supplier = new Supplier[U]() {
      def get(): U = new U
    }

    val collected = s.collect(
      supplier,
      (list: U, e: Double) => list.add(e),
      (list1: U, list2: U) => list1.addAll(list2)
    )

    // Proper size
    assertEquals("list size", nElements, collected.size())

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("list element", data(j), collected.get(j), epsilon)
  }

  @Test def doubleStreamCollect_UsingSummaryStatistics(): Unit = {
    /* This is the example given at the top of the JVM
     *  DoubleSummaryStatistics description, translate to Scala.
     *
     *  It tests DoubleStream.collect() using user-designated arguments.
     *
     *  Along the way, it shows a succinct way of using collect() in Scala.
     */

    type U = DoubleSummaryStatistics

    val nElements = 6
    val expectedSum = 16.5
    val expectedMin = 0.0
    val expectedAverage = expectedSum / nElements
    val expectedMax = 5.5

    val data = new Array[Double](nElements)
    data(0) = 1.1
    data(1) = 2.2
    data(2) = expectedMin
    data(3) = 3.3
    data(4) = expectedMax
    data(5) = 4.4

    val s = Arrays.stream(data)

    val collected = s.collect(
      () => new U,
      (summary: U, e: Double) => summary.accept(e),
      (summary1: U, summary2: U) => summary1.combine(summary2)
    )

    // Proper stats
    assertEquals("count", nElements, collected.getCount())
    assertEquals("sum", expectedSum, collected.getSum(), epsilon)
    assertEquals("min", expectedMin, collected.getMin(), epsilon)
    assertEquals("average", expectedAverage, collected.getAverage(), epsilon)
    assertEquals("max", expectedMax, collected.getMax(), epsilon)
  }

  @Test def doubleStreamCount(): Unit = {
    val expectedCount = 5

    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3, 4.4)

    assertEquals(s"unexpected element count", expectedCount, s.count())
  }

  @Test def doubleStreamDistinct(): Unit = {

    // There must be a harder way of doing this setup.
    // Using " scala.jdk.CollectionConverters._" and futzing with it
    // having a different name in Scala 2.12 might just be a greater
    // time suck.

    val expectedCount = 5
    val range = 0 until expectedCount

    val expectedElements = new Array[Double](expectedCount)
    for (j <- range)
      expectedElements(j) = j * 2.0

    val expectedSet = new ju.HashSet[Double]()
    for (j <- range)
      expectedSet.add(expectedElements(j))

    val s = DoubleStream
      .of(expectedElements: _*)
      .flatMap((e) => DoubleStream.of(e, e, e))
      .distinct()

    assertEquals(s"unexpected count", expectedCount, s.count())

    // Count is good, now did we get expected elements and only them?

    // count() exhausted s1, so create second stream, s2

    val s2 = DoubleStream
      .of(expectedElements: _*)
      .flatMap((e) => DoubleStream.of(e, e, e))
      .distinct()

    s2.forEach((e) => {
      val inSet = expectedSet.remove(e)
      // Detect both unknown elements and
      // occurances of unwanted, non-distinct elements
      assertTrue(s"element ${e} not in expectedSet", inSet)
    })

    // Iff the stream was proper & distinct, the expected set should be empty.
    assertTrue("expectedSet has remaining elements", expectedSet.isEmpty())
  }

  @Test def doubleStreamFindAny_Null(): Unit = {
    val s = DoubleStream.of(null.asInstanceOf[Double])
    // Double nulls get seen as 0.0
    val optional = s.findAny()
    assertTrue("unexpected failure to findAny", optional.isPresent())
    assertEquals("unexpected element", 0.0, optional.getAsDouble(), epsilon)
  }

  @Test def doubleStreamFindAny_True(): Unit = {
    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)
    val acceptableValues = List(0.0, 1.1, 2.2, 3.3)

    val optional = s.findAny()

    assertTrue("unexpected empty optional", optional.isPresent())

    val found = optional.getAsDouble()
    assertTrue(
      s"unexpected value: '${found}'",
      acceptableValues.contains(found)
    )
  }

  @Test def doubleStreamFindAny_False(): Unit = {
    val s = DoubleStream.empty()

    val optional = s.findAny()

    assertFalse("unexpected failure", optional.isPresent())
  }

  @Test def doubleStreamFindFirst_True(): Unit = {
    val expectedFirst = 0.0
    val s = DoubleStream.of(expectedFirst, 1.1, 2.2, 3.3)

    val optional = s.findFirst()

    assertTrue("unexpected empty optional", optional.isPresent())
    assertEquals(
      "unexpected mismatch",
      expectedFirst,
      optional.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamFindFirst_False(): Unit = {
    val s = DoubleStream.empty()

    val optional = s.findFirst()

    assertFalse("unexpected failure", optional.isPresent())
  }

  @Test def doubleStreamFilter(): Unit = {
    val expectedCount = 4

    val s0 = DoubleStream.of(
      101.1, 1.1, 102.2, 2.2, 103.2, 3.3, 4.4
    )

    val s1 = s0.filter(e => e < 100.0)
    assertEquals(s"unexpected element count", expectedCount, s1.count())
  }

  @Test def doubleStreamForeachOrdered(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)

    var sum = 0.0
    val consumer = new DoubleConsumer {
      def accept(i: Double): Unit = { sum = sum + i }
    }
    s.forEachOrdered(consumer)
    assertEquals("unexpected sum", 6.6, sum, epsilon)
  }

  @Test def doubleStreamLimit_NegativeArg(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)
    assertThrows(classOf[IllegalArgumentException], s.limit(-1))
  }

  @Test def doubleStreamLimit(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val expectedCount = 10
    var data = -1

    val s0 = DoubleStream.iterate(
      1.61803,
      e => e + 1.0
    )

    val s1 = s0.limit(expectedCount)

    assertEquals(s"unexpected element count", expectedCount, s1.count())
  }

  /*  Note Well: See Issue #3309 comments in StreamTest.scala and
   *             in original issue.
   */

  // Issue #3309 - 1 of 5
  @Test def doubleSstreamLimit_Size(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val srcSize = 10

    val spliter = DoubleStream
      .iterate(2.71828, e => e + 1.0)
      .limit(srcSize)
      .spliterator()

    val expectedExactSize = -1
    assertEquals(
      "expected exact size",
      expectedExactSize,
      spliter.getExactSizeIfKnown()
    )

    val expectedEstimatedSize = Long.MaxValue
    assertEquals(
      "expected estimated size",
      expectedEstimatedSize,
      spliter.estimateSize()
    )
  }

  // Issue #3309 - 2 of 5
  @Test def doubleStreamLimit_Characteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val zeroCharacteristicsSpliter =
      new Spliterators.AbstractDoubleSpliterator(Long.MaxValue, 0x0) {
        def tryAdvance(action: DoubleConsumer): Boolean = true
      }

    val sZero = StreamSupport.doubleStream(zeroCharacteristicsSpliter, false)
    val sZeroLimited = sZero.limit(9)

    val sZeroLimitedSpliter = sZeroLimited.spliterator()

    val expectedSZeroLimitedCharacteristics = 0x0

    assertEquals(
      "Unexpected characteristics for zero characteristics stream",
      expectedSZeroLimitedCharacteristics,
      sZeroLimitedSpliter.characteristics()
    )

    /* JVM fails the StreamSupport.stream() call with IllegalStateException
     * when SORTED is specified. Top of stack traceback is:
     *    at java.util.Spliterator.getComparator(Spliterator.java:471)
     *
     * Test the bits we can here and let Test
     * streamLimit_SortedCharacteristics() handle SORTED.
     */
    val allCharacteristicsSpliter =
      new Spliterators.AbstractDoubleSpliterator(Long.MaxValue, 0x5551) {
        def tryAdvance(action: DoubleConsumer): Boolean = true
      }

    val sAll = StreamSupport.doubleStream(allCharacteristicsSpliter, false)

    val sAllLimited = sAll.limit(9)
    val sAllLimitedSpliter = sAllLimited.spliterator()

    // JVM 8 expects 0x11 (decimal 17), JVM >= 17 expects 0x4051 (Dec 16465)
    val expectedSAllLimitedCharacteristics =
      Spliterator.ORDERED | Spliterator.DISTINCT // 0x11
      // Drop SIZED, SUBSIZED, CONCURRENT, IMMUTABLE, & NONNULL.
      // SORTED was not there to drop.

    assertEquals(
      "Unexpected characteristics for all characteristics stream",
      expectedSAllLimitedCharacteristics,
      sAllLimitedSpliter.characteristics()
    )
  }

  // Issue #3309 - 3 of 5
  @Test def streamLimit_SortedCharacteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    /* Address issues with SORTED described in Test
     * streamLimit_sequentialAlwaysCharacteristics
     */
    val allCharacteristicsSpliter =
      new Spliterators.AbstractDoubleSpliterator(0, 0x5551) {
        def tryAdvance(action: DoubleConsumer): Boolean = false
      }

    val sAll = StreamSupport.doubleStream(allCharacteristicsSpliter, false)

    val sAllLimited = sAll.sorted().limit(9)
    val sAllLimitedSpliter = sAllLimited.spliterator()

    // JVM 8 expects 0x15 (decimal 21), JVM >= 17 expects 0x4055 (Dec 16469)
    val expectedSAllLimitedCharacteristics =
      Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED // 0x15        // Drop SIZED, SUBSIZED, CONCURRENT, IMMUTABLE, & NONNULL.

    assertEquals(
      "Unexpected characteristics for all characteristics sorted stream",
      expectedSAllLimitedCharacteristics,
      sAllLimitedSpliter.characteristics()
    )
  }

  // Issue #3309 - 4 of 5
  @Test def streamLimit_UnsizedCharacteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val srcSize = 20

    val unsizedSpliter = DoubleStream
      .iterate(1.2, n => n + 1.1)
      .limit(srcSize)
      .spliterator()

    val expectedUnsizedCharacteristics = Spliterator.ORDERED // 0x10

    assertEquals(
      "Unexpected unsized characteristics",
      expectedUnsizedCharacteristics,
      unsizedSpliter.characteristics()
    )
  }

  // Issue #3309 - 5 of 5
  @Test def streamLimit_SizedCharacteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val proofSpliter = DoubleStream.of(1.12, 2.23, 3.34, -1.12).spliterator()

    val expectedProofCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.ORDERED | Spliterator.IMMUTABLE // 0x4450

    assertEquals(
      "Unexpected origin stream characteristics",
      expectedProofCharacteristics,
      proofSpliter.characteristics()
    )

    val sizedSpliter = DoubleStream
      .of(1.12, 2.23, 3.34, -1.12)
      .limit(3)
      .spliterator()

    // JVM 8 expects 0x10 (decimal 16), JVM >= 17 expects 0x4050 (Dec 16464)
    val expectedSizedLimitCharacteristics = Spliterator.ORDERED

    assertEquals(
      "Unexpected characteristics for SIZED stream",
      expectedSizedLimitCharacteristics,
      sizedSpliter.characteristics()
    )
  }

  @Test def doubleStreamMap(): Unit = {
    val nElements = 4
    val prefix = "mapped_"
    var count = 0

    val s0 = DoubleStream.of(1.1, 2.2, 3.3, 4.4)

    val s1 = s0.map((e) => {
      count += 1
      s"${prefix}${e}"
      e * 10
    })

    /* Check that the right number of elements, each with the expected form,
     * are generated.
     *
     * "map()" is an intermediate, not terminal operation.
     * Do the terminal "forEach()" first, to force the counting in the mapper.
     */
    s1.forEach((e) =>
      assertTrue(
        s"unexpected map element: ${e}",
        (e > 10.0) && (e < 45.0)
      )
    )
    assertEquals("unexpected count", nElements, count)
  }

  /* // Not Yet Implemented, needs IntStream first
  @Test def doubleStreamMapToInt(): Unit = {
    val nElements = 4
    var count = 0

    val s0 = DoubleStream.of(1.1, 2.2, 3.3, 4.4)

    val s1 = s0.mapToInt((e) => e.toInt)

    // Right resultant types
    s1.forEach(e =>
      count += 1
        assertEquals (s"unexpected type",
        classOf[Int],
        e.getClass())
    )

    // Right count
    assertEquals("unexpected count", nElements, count)

    // Right content
    val s2 = DoubleStream.of(1.1, 2.2, 3.3, 4.4)

    val s3 = s2.mapToInt((e) => e.toInt)

    val it = s3.iterator()

    for (j <- 1 to nElements)
      assertEquals("unexpected element", j, it.nextDouble())
  }
   */ // Not Yet Implemented

  /* // Not Yet Implemented, needs LongStream first
  @Test def doubleStreamMapToLong: Unit = {
    val nElements = 4
    var count = 0

    val s0 = DoubleStream.of(1.1, 2.2, 3.3, 4.4)

    /val s1 = s0.mapToLong((e) => e.toLong)

    // Right resultant types
    s1.forEach(e =>
      count += 1
        assertEquals (s"unexpected type",
        classOf[Long],
        e.getClass())
    )

    // Right count
    assertEquals("unexpected count", nElements, count)

    // Right content
    val s2 = DoubleStream.of(1.1, 2.2, 3.3, 4.4)

    val s3 = s2.mapToLong((e) => e.toLong)

    val it = s3.iterator()

    for (j <- 1 to nElements)
      assertEquals("unexpected element", j.toLong, it.nextDouble())
  }
   */ // Not Yet Implemented

  @Test def doubleStreamMapToObj(): Unit = {
    val nElements = 4
    val prefix = "mapped_"
    var count = 0

    val s0 = DoubleStream.of(1.1, 2.2, 3.3, 4.4)

    val s1 = s0.mapToObj[String]((e) => {
      count += 1
      s"${prefix}${e}"
    })

    /* Check that the right number of elements, each with the expected form,
     * are generated.
     *
     * "mapToObj()" is an intermediate, not terminal operation.
     * Do the terminal "forEach()" first, to force the counting in the mapper.
     */

    s1.forEach((e) =>
      /* Type check logic:
       *    The compiler expects the resultant element type to be String
       *    or else it would not allow the "startsWith()" below.
       *    Simlarly, if the runtime type is not String, that call would
       *    error. A pretty convincing case for having Strings here.
       */

      assertTrue(
        s"unexpected map element: ${e}",
        e.startsWith(prefix)
      )
    )
    assertEquals("unexpected count", nElements, count)
  }

  @Test def doubleStreamNoneMatch_EmptyStream(): Unit = {
    val s = DoubleStream.empty()
    var predEvaluated = false

    val noneMatched = s.noneMatch((e) => { predEvaluated = true; true })
    assertTrue("unexpected match", noneMatched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def doubleStreamNoneMatch_True(): Unit = {
    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)

    val matched = s.noneMatch((e) => e < 0.0)
    assertTrue("unexpected predicate failure", matched)
  }

  @Test def doubleStreamNone_MatchFalse(): Unit = {
    val s = DoubleStream.of(0.0, 1.1, 2.2, 3.3)

    val matched = s.noneMatch((e) => e > 2.2)
    assertFalse("unexpected predicate failure", matched)
  }

  @Test def doubleStreamMax_EmptyStream(): Unit = {
    val s = DoubleStream.empty()

    val max = s.max()

    assertFalse("max optional should be empty", max.isPresent)
  }

  @Test def doubleStreamMax(): Unit = {
    val stream = DoubleStream.of(85.85, 4.4, 87.87, 25.25, 7.7)

    val maxOpt = stream.max()

    assertTrue("max not found", maxOpt.isPresent())

    assertEquals(
      "wrong max item found",
      87.87,
      maxOpt.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamMax_NaN(): Unit = {
    val stream = DoubleStream.of(85.85, Double.NaN, 87.87, 25.25, 7.7)

    val maxOpt = stream.max()

    assertTrue("max not found", maxOpt.isPresent())

    assertEquals(
      "wrong max item found",
      Double.NaN,
      maxOpt.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamMax_NegativeZero(): Unit = {
    val stream = DoubleStream.of(-85.85, -0.0, -87.87, -25.25, -7.7)

    val maxOpt = stream.max()

    assertTrue("max not found", maxOpt.isPresent())

    /* This Test expects a -0.0, exactly, not a -0.0 squashed to 0.0.
     * ==, <, and > will conflate -0.0 and 0.0: i.e. -0.0 == 0.0.
     * Double.compare will distinguish them: i.e. -0.0 != 0.0.
     */
    assertEquals(
      s"wrong max item found: '${maxOpt.getAsDouble()}'",
      0,
      jl.Double.compare(-0.0, maxOpt.getAsDouble()) // distinguish -0.0
    )
  }

  @Test def doubleStreamMin_EmptyStream(): Unit = {
    val s = DoubleStream.empty()

    val minOpt = s.min()

    assertFalse("min optional should be empty", minOpt.isPresent)
  }

  @Test def doubleStreamMin(): Unit = {
    val stream = DoubleStream.of(85.85, 4.4, 87.87, 25.25, 7.7)

    val minOpt = stream.min()

    assertTrue("min not found", minOpt.isPresent())

    assertEquals(
      "wrong min item found",
      4.4,
      minOpt.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamMin_NaN(): Unit = {
    val stream = DoubleStream.of(85.85, Double.NaN, 87.87, 25.25, 7.7)

    val minOpt = stream.min()

    assertTrue("min not found", minOpt.isPresent())

    assertEquals(
      "wrong min item found",
      Double.NaN,
      minOpt.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamMin_NegativeZero(): Unit = {
    val stream = DoubleStream.of(85.85, -0.0, 87.87, 0.0, 25.25, 7.7)

    val minOpt = stream.min()

    assertTrue("min not found", minOpt.isPresent())

    /* This Test expects a -0.0, exactly, not a -0.0 squashed to 0.0.
     * ==, <, and > will conflate -0.0 and 0.0: i.e. -0.0 == 0.0.
     * Double.compare will distinguish them: i.e. -0.0 != 0.0.
     */
    assertEquals(
      s"wrong min item found: '${minOpt.getAsDouble()}'",
      0,
      jl.Double.compare(-0.0, minOpt.getAsDouble()) // distinguish -0.0
    )
  }

  /* @Ignore this test and leave it in place. The results are better evaluated
   * visually/manually rather than automatically.
   * JVM documentations suggests that "peek()" be mainly used for debugging.
   */
  @Ignore
  @Test def doubleStreamPeek(): Unit = {
    val expectedCount = 3

    val s = Stream.of("Animal", "Vegetable", "Mineral")

    // The ".count()" is a terminal operation to force the pipeline to
    // evalute. The real interest is if the peek() side-effect happened
    // correctly.  Currently that can only be evaluated manually/visually.
    val n = s.peek((e) => printf(s"peek: |${e}||\n")).count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Ignore // see @Ignore comment above "streamShouldPeek()" above.
  @Test def doubleStreamPeek_CompositeStream(): Unit = {
    // Test that peek() works with all substreams of a composite stream.
    val expectedCount = 10

    // See ".count()" comment in streamShouldPeek above.

    // One should see the original data before and then after transformation
    // done by flatmap to each original element. Something like:
    //   before: <1>
    //     after: <1>
    //   before: <2>
    //     after: <1>
    //     after: <2>
    //   before: <3>
    //     after: <1>
    //     after: <2>
    //     after: <3>
    //   before: <4>
    //     after: <1>
    //     after: <2>
    //     after: <3>
    //     after: <4>

    val n = Stream
      .of(1, 2, 3, 4)
      .peek((e) => printf(s"composite peek - before: <${e}>|\n")) // simple str
      .flatMap((e) => Stream.of((1 to e): _*))
      .peek((e) => printf(s"composite peek - after: <${e}>|\n")) // composite
      .count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Test def doubleStreamReduce_OneArgEmpty(): Unit = {
    val s = DoubleStream.empty()

    val optional: OptionalDouble = s.reduce((r, e) => r + e)

    assertFalse("unexpected non-empty optional", optional.isPresent())
  }

  @Test def doubleStreamReduce_OneArg(): Unit = {
    val s = DoubleStream.of(3.3, 5.5, 7.7, 11.11)
    val expectedSum = 27.61

    val optional: OptionalDouble = s.reduce((r, e) => r + e)

    assertTrue("unexpected empty optional", optional.isPresent())
    assertEquals(
      "unexpected reduction result",
      expectedSum,
      optional.getAsDouble(),
      epsilon
    )
  }

  @Test def doubleStreamReduce_TwoArgEmpty(): Unit = {
    val s = DoubleStream.empty()

    val firstArg = 1.1

    val product: Double = s.reduce(firstArg, (r, e) => r * e)

    assertEquals("unexpected reduction result", firstArg, product, epsilon)
  }

  @Test def doubleStreamReduce_TwoArg(): Unit = {
    val s = DoubleStream.of(3.3, 5.5, 7.7, 11.11)
    val expectedProduct = 1552.67805

    val product: Double = s.reduce(1, (r, e) => r * e)

    assertEquals(
      "unexpected reduction result",
      expectedProduct,
      product,
      epsilon
    )
  }

  @Test def doubleStreamSkip_NegativeArg(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)
    assertThrows(classOf[IllegalArgumentException], s.skip(-1))
  }

  @Test def doubleStreamSkip_TooMany(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 3.3)

    val isEmptyStream = !s.skip(10).iterator.hasNext()
    assertTrue("expected empty stream", isEmptyStream)
  }

  @Test def doubleStreamSkip(): Unit = {
    val expectedValue = 99.99
    val s = DoubleStream.of(1.1, 2.2, 3.3, 4.4, expectedValue, 6.6, 7.7)

    val iter = s.skip(4).iterator()

    assertTrue("expected non-empty stream", iter.hasNext())
    assertEquals(
      "unexpected first value: ",
      expectedValue,
      iter.nextDouble(),
      epsilon
    )
  }

  @Test def doubleStreamSorted(): Unit = {
    val nElements = 8
    val wild = new Array[Double](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild(0) = 45.32
    wild(1) = 21.4
    wild(2) = 11.2
    wild(3) = 31.5
    wild(4) = 68.16
    wild(5) = 3.77
    wild(6) = 61.44
    wild(7) = 9.60

    val ordered = new Array[Double](nElements)
    ordered(0) = 3.77
    ordered(1) = 9.60
    ordered(2) = 11.2
    ordered(3) = 21.4
    ordered(4) = 31.5
    ordered(5) = 45.32
    ordered(6) = 61.44
    ordered(7) = 68.16

    val s = DoubleStream.of(wild: _*)

    val sorted = s.sorted()

    var count = 0

    sorted.forEachOrdered((e) => {
      assertEquals("mismatched elements", ordered(count), e, epsilon)
      count += 1
    })

    val msg =
      if (count == 0) "unexpected empty stream"
      else "unexpected number of elements"

    assertEquals(msg, nElements, count)
  }

  @Test def doubleStreamSorted_Characteristics(): Unit = {
    // See comments in StreamTest#streamSorted_Characteristics

    val nElements = 8
    val wild = new Array[Double](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild(0) = 45.32
    wild(1) = 21.4
    wild(2) = 11.2
    wild(3) = 31.5
    wild(4) = 68.16
    wild(5) = 3.77
    wild(6) = 61.44
    wild(7) = 9.60

    val seqDoubleStream = DoubleStream.of(wild: _*)
    assertFalse(
      "Expected sequential stream",
      seqDoubleStream.isParallel()
    )

    // same expected values for SN sequential, SN parallel, & JVM streams
    /* The characteristics here differ from those of the corresponding
     * StreamTest because of the way the streams are constructed.
     * StreamTest reports 0x4050, while this adds IMMUTABLE yeilding 0x4450.
     * This stream is constructed using "of()" which is indeed IMMUTABLE.
     * Mix things up, for variety and  to keep people trying to follow along
     * at home on their toes.
     */
    val expectedPreCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.ORDERED | Spliterator.IMMUTABLE

    // Drop IMMUTABLE, add SORTED
    val expectedPostCharacteristics =
      (expectedPreCharacteristics & ~Spliterator.IMMUTABLE) +
        Spliterator.SORTED

    val seqDoubleSpliter = seqDoubleStream.spliterator()

    assertEquals(
      "sequential characteristics",
      expectedPreCharacteristics,
      seqDoubleSpliter.characteristics()
    )

    val sortedSeqDoubleStream = DoubleStream.of(wild: _*).sorted()
    val sortedSeqSpliter = sortedSeqDoubleStream.spliterator()

    assertEquals(
      "sorted sequential characteristics",
      expectedPostCharacteristics,
      sortedSeqSpliter.characteristics()
    )

  }

  @Test def doubleStreamSum(): Unit = {
    val nElements = 9

    val wild = new Array[Double](nElements) // holds arbitrarily jumbled data
    wild(0) = 45.32
    wild(1) = 21.4
    wild(2) = 11.2
    wild(3) = 31.5
    wild(4) = 68.16
    wild(5) = 3.77
    wild(6) = 61.44
    wild(7) = 9.60

    val expectedSum = 252.39

    val s = DoubleStream.of(wild: _*)

    val sum = s.sum()

    assertEquals("unexpected sum", expectedSum, sum, epsilon)
  }

  @Test def doubleStreamSummaryStatistics(): Unit = {
    val nElements = 8

    val wild = new Array[Double](nElements) // holds arbitrarily jumbled data
    wild(0) = 45.32
    wild(1) = 21.4
    wild(2) = 11.2
    wild(3) = 31.5
    wild(4) = 68.16
    wild(5) = 3.77
    wild(6) = 61.44
    wild(7) = 9.60

    val expectedAverage = 31.54875
    val expectedCount = nElements
    val expectedMax = 68.16
    val expectedMin = 3.77
    val expectedSum = 252.39

    val s = DoubleStream.of(wild: _*)

    val stats = s.summaryStatistics()

    assertEquals(
      "unexpected average",
      expectedAverage,
      stats.getAverage(),
      epsilon
    )

    assertEquals("unexpected count", expectedCount, stats.getCount())

    assertEquals("unexpected max", expectedMax, stats.getMax(), epsilon)

    assertEquals("unexpected min", expectedMin, stats.getMin(), epsilon)

    assertEquals("unexpected sum", expectedSum, stats.getSum(), epsilon)
  }

  @Test def doubleStreamToArray(): Unit = {
    val nElements = 9

    val wild = new Array[Double](nElements) // holds arbitrarily jumbled data
    wild(0) = 45.32
    wild(1) = 21.4
    wild(2) = 11.2
    wild(3) = 31.5
    wild(4) = 68.16
    wild(5) = 3.77
    wild(6) = 61.44
    wild(7) = 9.60

    val s = DoubleStream.of(wild: _*)

    val resultantArray = s.toArray()

    // Proper size
    assertEquals("result size", nElements, resultantArray.size)

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("elements do not match", wild(j), resultantArray(j), epsilon)
  }

}
