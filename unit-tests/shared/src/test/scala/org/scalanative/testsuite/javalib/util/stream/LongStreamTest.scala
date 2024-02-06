package org.scalanative.testsuite.javalib.util.stream

/* It is hard to assure oneself that the desired primitive DoubleStream,
 * LongStream, & IntStream are being used instead of a/an (object) Stream.
 * Create IntStream & kin using the methods in Arrays.
 *
 * Do not import ArrayList here, to guard against a Test populating
 * an ArrayList and then inadvertently creating an (object) Stream with it.
 * Use ju.ArrayList surgically at the points of use.
 */

import java.{lang => jl}

import java.{util => ju}
import java.util.Arrays
import java.util.LongSummaryStatistics
import java.util.OptionalLong
import java.util.{Spliterator, Spliterators}

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.CountDownLatch._

import java.util.function.{LongConsumer, LongFunction, LongSupplier}
import java.util.function.Supplier

import java.util.stream._

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class LongStreamTest {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

// Methods specified in interface BaseStream ----------------------------

  @Test def streamUnorderedOnUnorderedStream(): Unit = {
    val dataSet = new ju.HashSet[Long]()
    dataSet.add(1L)
    dataSet.add(11)
    dataSet.add(-11)
    dataSet.add(22)
    dataSet.add(-22)

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
    val s = LongStream.of(1L, 11, -11, 22, -22)
    val sSpliter = s.spliterator()

    assertTrue(
      "Expected ORDERED on stream from array",
      sSpliter.hasCharacteristics(Spliterator.ORDERED)
    )

    // s was ordered, 'so' should be same same. Avoid "already used" exception
    val so = LongStream.of(1L, 11, -11, 22, -22)
    val su = so.unordered()
    val suSpliter = su.spliterator()

    assertFalse(
      "ORDERED stream after unordered()",
      suSpliter.hasCharacteristics(Spliterator.ORDERED)
    )
  }

  @Test def streamParallel(): Unit = {
    val nElements = 5

    val wild = new Array[Long](nElements) // holds arbitrarily jumbled data
    wild(0) = 13245L
    wild(1) = 421
    wild(2) = 211
    wild(3) = 5531
    wild(4) = 1668

    val sPar0 =
      StreamSupport.longStream(Spliterators.spliterator(wild, 0), true)

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
      StreamSupport.longStream(Spliterators.spliterator(wild, 0), true)

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
    sSeqSpliterator.forEachRemaining((e: Long) => {
      assertEquals(
        s"sequential stream contents(${count})",
        wild(count),
        e
      )
      count += 1
    })
  }

  @Test def streamSequential(): Unit = {
    val nElements = 5

    val wild = new Array[Long](nElements) // holds arbitrarily jumbled data
    wild(0) = 13245
    wild(1) = 421
    wild(2) = 211
    wild(3) = 5531
    wild(4) = 1668

    val sSeq0 =
      StreamSupport.longStream(Spliterators.spliterator(wild, 0), false)

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
      StreamSupport.longStream(Spliterators.spliterator(wild, 0), false)

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
    sParSpliterator.forEachRemaining((e: Long) => {
      assertEquals(
        s"parallel stream contents(${count})",
        wild(count),
        e
      )
      count += 1
    })
  }

// Methods specified in interface Int Stream -------------------------

  @Test def longStreamBuilderCanBuildAnEmptyStream(): Unit = {
    val s = LongStream.builder().build()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def longStreamBuilderCharacteristics(): Unit = {
    val bldr = Stream.builder[Long]()
    bldr
      .add(11)
      .add(-11)
      .add(99L)

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

  @Test def longStreamEmptyIsEmpty(): Unit = {
    val s = LongStream.empty()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def longStreamOf_SingleElement(): Unit = {
    val expected = 77
    val s = LongStream.of(expected)
    val it = s.iterator()
    assertTrue("LongStream should not be empty", it.hasNext())
    assertEquals("unexpected element", it.nextLong(), expected)
    assertFalse("LongStream should be empty and is not.", it.hasNext())
  }

  @Test def streamOf_SingleElementCharacteristics(): Unit = {
    val expected = 77

    val s = LongStream.of(expected)
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

  @Test def longStreamOf_MultipleElements(): Unit = {
    val s = LongStream.of(11, 22, 33)
    val it = s.iterator()
    assertEquals("element_1", 11, it.nextLong())
    assertEquals("element_2", 22, it.nextLong())
    assertEquals("element_3", 33, it.nextLong())
    assertFalse(it.hasNext())
  }

  @Test def streamOf_MultipleElementsCharacteristics(): Unit = {
    val s = LongStream.of(11, 22, 33)
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

  @Test def longStreamFlatMapWorks(): Unit = {
    val s = LongStream.of(11, 22, 33)

    val mapper = new LongFunction[LongStream] {
      override def apply(v: Long): LongStream =
        LongStream.of(v, v)
    }

    val s2 = s.flatMap(mapper)

    val it = s2.iterator()

    assertEquals(11, it.nextLong())
    assertEquals(11, it.nextLong())

    assertEquals(22, it.nextLong())
    assertEquals(22, it.nextLong())

    assertEquals(33, it.nextLong())
    assertEquals(33, it.nextLong())

    assertFalse(it.hasNext())
  }

  @Test def longStreamForEachWorks(): Unit = {
    val s = LongStream.of(-11, -22, -33, 0)

    var sum = 0L
    val longConsumer = new LongConsumer {
      def accept(i: Long): Unit = sum += i
    }

    s.forEach(longConsumer)
    assertEquals(-66, sum)
  }

  @Test def longStreamFlatMapWorksTwice(): Unit = {
    val s = LongStream.of(11, 22, 33)

    val mapper1 = new LongFunction[LongStream] {
      override def apply(v: Long): LongStream =
        LongStream.of(v, v)
    }

    val mapper2 = new LongFunction[LongStream] {
      override def apply(v: Long): LongStream =
        LongStream.of(-v, -v, -v)
    }

    val s2 = s
      .flatMap(mapper1)
      .flatMap(mapper2)

// format: off
    val expected =
      Seq(
        -11, -11, -11, -11, -11, -11,
        -22, -22, -22, -22, -22, -22,
        -33, -33, -33, -33, -33, -33
      )
// format: on

    val result = scala.collection.mutable.ArrayBuffer.empty[Long]
    val it = s2.iterator()

    while (it.hasNext()) {
      result += it.nextLong()
    }

    assertTrue(result == expected)
  }

  @Test def longStreamOnCloseWorks(): Unit = {
    var latch = new CountDownLatch(1)

    class Closer(cdLatch: CountDownLatch) extends Runnable {
      override def run(): Unit = cdLatch.countDown()
    }

    val s = LongStream.empty().onClose(new Closer(latch))
    s.close()

    val timeout = 30L
    assertTrue(
      "close handler did not run within ${timeout} seconds",
      latch.await(timeout, TimeUnit.SECONDS)
    )
  }

// Static methods -------------------------------------------------------

  @Test def longStreamConcat(): Unit = {
    val a = LongStream.of(99, 88, 66, 77, 55)
    val b = LongStream.of(0, 33, 22)

    val s = LongStream.concat(a, b)

    val it = s.iterator()
    assertNotNull("s.iterator() should not be NULL", it)
    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"element", 99, it.nextLong())
    assertEquals(s"element", 88, it.nextLong())
    assertEquals(s"element", 66, it.nextLong())
    assertEquals(s"element", 77, it.nextLong())
    assertEquals(s"element", 55, it.nextLong())

    assertEquals(s"element", 0, it.nextLong())
    assertEquals(s"element", 33, it.nextLong())
    assertEquals(s"element", 22, it.nextLong())

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def doubleStreamGenerate(): Unit = {
    val nElements = 5
    val data = new Array[Long](nElements)
    data(0) = 0
    data(1) = 11
    data(2) = 22
    data(3) = 33
    data(4) = 44

    val src = new LongSupplier() {
      var count = -1

      def getAsLong(): Long = {
        count += 1
        data(count % nElements)
      }
    }

    val s = LongStream.generate(src)

    val it = s.iterator()

    assertTrue("LongStream should not be empty", it.hasNext())

    for (j <- 0 until nElements)
      assertEquals(s"data(${j})", it.nextLong(), data(j))

    assertTrue("LongStream should not be empty", it.hasNext())
  }

  @Test def longStreamIterate_Unbounded(): Unit = {
    val nElements = 4
    var count = -1.0

    val expectedSeed = 1775

    val expected = Seq(expectedSeed, 1786, 1797, 1808)

    val s = LongStream.iterate(
      expectedSeed,
      e => e + 11
    )

    val it = s.iterator()

    assertTrue("LongStream should not be empty", it.hasNext())

    for (j <- 0 until nElements)
      assertEquals(s"element: ${j})", expected(j), it.nextLong())

    assertTrue("LongStream should not be empty", it.hasNext())
  }

  @Test def longStreamIterate_Unbounded_Characteristics(): Unit = {
    val s = LongStream.iterate(0, n => n + 11)
    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    // Note: LongStream requires NONNULL, whereas Stream[T] does not.
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

  @Test def longStreamOf_NoItems(): Unit = {
    val s = LongStream.of()

    val it = s.iterator()
    assertFalse("LongStream should be empty", it.hasNext())
  }

  @Test def longStreamOf_OneItem(): Unit = {
    val expected = 667
    val s = LongStream.of(expected)

    val it = s.iterator()
    assertTrue("stream should not be empty", it.hasNext())
    assertEquals(s"element", expected, it.nextLong())

    assertFalse("LongStream should be empty", it.hasNext())
  }

  // LongStream.of() with more than two arguments is exercised in many other
  // places in this file, so no Test for that case here.

  @Test def longStreamRange(): Unit = {
    val startInclusive = 5
    val endExclusive = 15
    val expectedCount = endExclusive - startInclusive

    val s = LongStream.range(startInclusive, endExclusive)

    var count = 0

    s.spliterator()
      .forEachRemaining((e: Long) => {
        assertEquals(
          s"range contents",
          count + startInclusive,
          e
        )
        count += 1
      })

    assertEquals(s"unexpected range count", expectedCount, count)
  }

  @Test def intStreamRangeClosed(): Unit = {

    val startInclusive = 5
    val endInclusive = 15
    val expectedCount = endInclusive - startInclusive + 1

    val s = LongStream.rangeClosed(startInclusive, endInclusive)

    var count = 0

    s.spliterator()
      .forEachRemaining((e: Long) => {
        assertEquals(
          s"rangeClosed contents",
          count + startInclusive,
          e
        )
        count += 1
      })

    assertEquals(s"unexpected rangeClosed count", expectedCount, count)
  }

// Instance methods -----------------------------------------------------

  @Test def longStreamAllMatch_EmptyStream(): Unit = {
    val s = LongStream.empty()
    var predEvaluated = false

    val matched = s.allMatch((e) => { predEvaluated = true; true })
    assertTrue("unexpected match failure", matched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def longStreamAllMatch_True(): Unit = {

    /* LongStream.allMatch() will return "true" on an empty stream.
     * Try to distinguish that "true" from an actual all-elements-match "true"
     * Since streams can not be re-used, count s0. If it is non-empty, assume
     * its sibling s is also non-empty, distingishing the two "true"s.
     */
    val s0 = LongStream.of(0, 11, 22, 33)
    assertTrue("unexpected empty stream", s0.count > 0)

    val s = LongStream.of(0, 11, 22, 33)

    val matched = s.allMatch((e) => { (e >= 0) && (e < 90) })
    assertTrue("unexpected match failure", matched)
  }

  @Test def longStreamAllMatch_False(): Unit = {
    val s = LongStream.of(0, 11, 22, 33)

    val matched = s.allMatch((e) => e > 22)
    assertFalse("unexpected match", matched)
  }

  @Test def longStreamAnyMatch_EmptyStream(): Unit = {
    val s = LongStream.empty()
    var predEvaluated = false

    val matched = s.anyMatch((e) => { predEvaluated = true; true })
    assertFalse("unexpected match", matched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def longStreamAnyMatch_True(): Unit = {
    val s = LongStream.of(0, 11, 22, 33)

    val matched = s.anyMatch((e) => (e > 10) && (e < 20))
    assertTrue("unexpected predicate failure", matched)
  }

  @Test def longStreamAnyMatch_False(): Unit = {
    val s = LongStream.of(0, 11, 22, 33)

    val matched = s.anyMatch((e) => e > 90)
    assertFalse("unexpected predicate failure", matched)
  }

  @Test def intStreamAsDoubleStream(): Unit = {
    val nElements = 4
    var count = 0

    val s0 = LongStream.of(11, 22, 33, 44L)

    val s1 = s0.asDoubleStream()

    // Right resultant types
    s1.forEach(e => {
      count += 1
      assertEquals(s"unexpected type", classOf[Double], e.getClass())
    })

    // Right count
    assertEquals("unexpected count", nElements, count)

    // Right content
    val s2 = LongStream.of(11, 22, 33, 44L)

    val s3 = s2.asDoubleStream()

    val it = s3.iterator()

    for (j <- 1 to nElements)
      assertEquals(
        "unexpected element",
        (j * 11).toDouble,
        it.nextDouble(),
        epsilon
      )
  }

  @Test def longStreamAverage_EmptyStream(): Unit = {
    val s = LongStream.empty()

    val optional = s.average()

    assertFalse(s"expected empty optional, got value", optional.isPresent())
  }

  @Test def longStreamAverage(): Unit = {
    val nElements = 8

    val wild = new Array[Long](nElements) // holds arbitrarily jumbled data
    wild(0) = 13245
    wild(1) = 421
    wild(2) = 211
    wild(3) = 5531
    wild(4) = 1668
    wild(5) = 773
    wild(6) = 4461
    wild(7) = 609

    val expectedAverage = 3364.875 // test against known value, not calculated.

    val s = LongStream.of(wild: _*)

    val optional = s.average()

    assertTrue("unexpected empty optional", optional.isPresent())

    assertEquals(
      "unexpected average",
      expectedAverage,
      optional.getAsDouble(),
      epsilon
    )
  }

  @Test def longStreamBoxed(): Unit = {
    val nElements = 5
    val data = new Array[Long](nElements)
    data(0) = 0
    data(1) = 11
    data(2) = 22
    data(3) = 33
    data(4) = 44

    val sd = Arrays.stream(data)

    assertTrue(
      "stream should be a LongStream",
      sd.isInstanceOf[LongStream]
    )

    val sBoxed = sd.boxed()

    assertTrue(
      "resultant stream should be boxed Stream[Long]",
      sBoxed.isInstanceOf[Stream[_]]
    )

    assertFalse(
      "resultant stream should not be a LongStream",
      sBoxed.isInstanceOf[LongStream]
    )
  }

  @Test def longStreamCollect_EmptyStreamUsingSupplier(): Unit = {
    type U = ju.ArrayList[Long]

    val s = LongStream.empty()

    val supplier = new Supplier[U]() {
      def get(): U = new U
    }

    val collected = s.collect(
      supplier,
      (list: U, e: Long) => list.add(e),
      (list1: U, list2: U) => list1.addAll(list2)
    )

    // Proper size
    assertEquals("list size", 0, collected.size())
  }

  @Test def longStreamCollect_UsingSupplier(): Unit = {
    type U = ju.ArrayList[Long]

    val nElements = 5
    val data = new Array[Long](nElements)
    data(0) = 0
    data(1) = 11
    data(2) = 22
    data(3) = 33
    data(4) = 44

    val s = Arrays.stream(data)

    val supplier = new Supplier[U]() {
      def get(): U = new U
    }

    val collected = s.collect(
      supplier,
      (list: U, e: Long) => list.add(e),
      (list1: U, list2: U) => list1.addAll(list2)
    )

    // Proper size
    assertEquals("list size", nElements, collected.size())

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("list element", data(j), collected.get(j))
  }

  @Test def longStreamCollect_UsingSummaryStatistics(): Unit = {
    /* This is the example given at the top of the JVM
     *  DoubleSummaryStatistics description, translate to Scala & Int.
     *
     *  It tests LongStream.collect() using user-designated arguments.
     *
     *  Along the way, it shows a succinct way of using collect() in Scala.
     */

    type U = LongSummaryStatistics

    val nElements = 6
    val expectedSum = 165L
    val expectedMin = 0L
    val expectedAverage = expectedSum.toDouble / nElements
    val expectedMax = 55L

    val data = new Array[Long](nElements)
    data(0) = 11
    data(1) = 22
    data(2) = expectedMin
    data(3) = 33
    data(4) = expectedMax
    data(5) = 44

    val s = Arrays.stream(data)

    val collected = s.collect(
      () => new U,
      (summary: U, e: Long) => summary.accept(e),
      (summary1: U, summary2: U) => summary1.combine(summary2)
    )

    // Proper stats
    assertEquals("count", nElements, collected.getCount())
    assertEquals("sum", expectedSum, collected.getSum())
    assertEquals("min", expectedMin, collected.getMin())
    assertEquals("average", expectedAverage, collected.getAverage(), epsilon)
    assertEquals("max", expectedMax, collected.getMax())
  }

  @Test def longStreamCount(): Unit = {
    val expectedCount = 5

    val s = LongStream.of(0, 11, 22, 33, 44)

    assertEquals(s"unexpected element count", expectedCount, s.count())
  }

  @Test def longStreamDistinct(): Unit = {

    // There must be a harder way of doing this setup.
    // Using " scala.jdk.CollectionConverters._" and futzing with it
    // having a different name in Scala 2.12 might just be a greater
    // time suck.

    val expectedCount = 5
    val range = 0 until expectedCount

    val expectedElements = new Array[Long](expectedCount)
    for (j <- range)
      expectedElements(j) = j * 2

    val expectedSet = new ju.HashSet[Long]()
    for (j <- range)
      expectedSet.add(expectedElements(j))

    val s = LongStream
      .of(expectedElements: _*)
      .flatMap((e) => LongStream.of(e, e, e))
      .distinct()

    assertEquals(s"unexpected count", expectedCount, s.count())

    // Count is good, now did we get expected elements and only them?

    // count() exhausted s1, so create second stream, s2

    val s2 = LongStream
      .of(expectedElements: _*)
      .flatMap((e) => LongStream.of(e, e, e))
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

  @Test def longStreamFindAny_Null(): Unit = {
    val s = LongStream.of(null.asInstanceOf[Long])
    // Long nulls get seen as 0
    val optional = s.findAny()
    assertTrue("unexpected failure to findAny", optional.isPresent())
    assertEquals("unexpected element", 0, optional.getAsLong())
  }

  @Test def longStreamFindAny_True(): Unit = {
    val s = LongStream.of(0, 11, 22, 33)
    val acceptableValues = List(0, 11, 22, 33)

    val optional = s.findAny()

    assertTrue("unexpected empty optional", optional.isPresent())

    val found = optional.getAsLong()
    assertTrue(
      s"unexpected value: '${found}'",
      acceptableValues.contains(found)
    )
  }

  @Test def longStreamFindAny_False(): Unit = {
    val s = LongStream.empty()

    val optional = s.findAny()

    assertFalse("unexpected failure", optional.isPresent())
  }

  @Test def longStreamFindFirst_True(): Unit = {
    val expectedFirst = 0
    val s = LongStream.of(expectedFirst, 11, 22, 33)

    val optional = s.findFirst()

    assertTrue("unexpected empty optional", optional.isPresent())
    assertEquals(
      "unexpected mismatch",
      expectedFirst,
      optional.getAsLong()
    )
  }

  @Test def longStreamFindFirst_False(): Unit = {
    val s = LongStream.empty()

    val optional = s.findFirst()

    assertFalse("unexpected failure", optional.isPresent())
  }

  @Test def longStreamFilter(): Unit = {
    val expectedCount = 4

    val s0 = LongStream.of(
      1011, 11, 1022, 22, 1032, 33, 44
    )

    val s1 = s0.filter(e => e < 1000)
    assertEquals(s"unexpected element count", expectedCount, s1.count())
  }

  @Test def longStreamForeachOrdered(): Unit = {
    val s = LongStream.of(11, 22, 33)

    var sum = 0L
    val consumer = new LongConsumer {
      def accept(i: Long): Unit = { sum = sum + i }
    }
    s.forEachOrdered(consumer)
    assertEquals("unexpected sum", 66, sum)
  }

  @Test def longStreamLimit_NegativeArg(): Unit = {
    val s = LongStream.of(11, 22, 33)
    assertThrows(classOf[IllegalArgumentException], s.limit(-1))
  }

  @Test def longStreamLimit(): Unit = {
    val expectedCount = 10
    var data = -1

    val s0 = LongStream.iterate(
      161803,
      e => e + 10
    )

    val s1 = s0.limit(expectedCount)

    assertEquals(s"unexpected element count", expectedCount, s1.count())
  }

  /*  Note Well: See Issue #3309 comments in StreamTest.scala and
   *             in original issue.
   */

  // Issue #3309 - 1 of 5
  @Test def longStreamLimit_Size(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val srcSize = 10

    val spliter = LongStream
      .iterate(271828, e => e + 10)
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
  @Test def longStreamLimit_Characteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val zeroCharacteristicsSpliter =
      new Spliterators.AbstractLongSpliterator(Long.MaxValue, 0x0) {
        def tryAdvance(action: LongConsumer): Boolean = true
      }

    val sZero = StreamSupport.longStream(zeroCharacteristicsSpliter, false)
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
      new Spliterators.AbstractLongSpliterator(Long.MaxValue, 0x5551) {
        def tryAdvance(action: LongConsumer): Boolean = true
      }

    val sAll = StreamSupport.longStream(allCharacteristicsSpliter, false)

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
  @Test def longStreamLimit_SortedCharacteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    /* Address issues with SORTED described in Test
     * streamLimit_sequentialAlwaysCharacteristics
     */
    val allCharacteristicsSpliter =
      new Spliterators.AbstractLongSpliterator(0, 0x5551) {
        def tryAdvance(action: LongConsumer): Boolean = false
      }

    val sAll = StreamSupport.longStream(allCharacteristicsSpliter, false)

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

    val unsizedSpliter = LongStream
      .iterate(12, n => n + 11)
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

    val proofSpliter = LongStream.of(112, 223, 334, -112).spliterator()

    val expectedProofCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.ORDERED | Spliterator.IMMUTABLE // 0x4450

    assertEquals(
      "Unexpected origin stream characteristics",
      expectedProofCharacteristics,
      proofSpliter.characteristics()
    )

    val sizedSpliter = LongStream
      .of(112, 223, 334, -112)
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

  @Test def longStreamMap(): Unit = {
    val nElements = 4
    val prefix = "mapped_"
    var count = 0

    val s0 = LongStream.of(11, 22, 33, 44)

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
        (e > 100) && (e < 450)
      )
    )
    assertEquals("unexpected count", nElements, count)
  }

  @Test def longStreamMapToDouble(): Unit = {
    val nElements = 4
    var count = 0

    val s0 = LongStream.of(11, 22, 33, 44L)

    val s1 = s0.mapToDouble((e) => e.toDouble)

    // Right resultant types
    s1.forEach(e => {
      count += 1
      assertEquals(s"unexpected type", classOf[Double], e.getClass())
    })

    // Right count
    assertEquals("unexpected count", nElements, count)

    // Right content
    val s2 = LongStream.of(11, 22, 33, 44)

    val s3 = s2.mapToDouble((e) => e.toDouble)

    val it = s3.iterator()

    for (j <- 1 to nElements)
      assertEquals(
        "unexpected element",
        (j * 11).toDouble,
        it.nextDouble(),
        epsilon
      )
  }

  @Test def longStreamMapToInt: Unit = {
    val nElements = 4
    var count = 0

    val s0 = LongStream.of(11, 22, 33, 44L)

    val s1 = s0.mapToInt((e: Long) => e.toInt)

    // Right resultant types
    s1.forEach(e => {
      count += 1
      assertEquals(s"unexpected type", classOf[Int], e.getClass())
    })

    // Right count
    assertEquals("unexpected count", nElements, count)

    // Right content
    val s2 = LongStream.of(11, 22, 33, 44L)

    val s3 = s2.mapToInt((e: Long) => e.toInt)

    val it = s3.iterator()

    for (j <- 1 to nElements)
      assertEquals("unexpected element", (j * 11), it.nextInt())
  }

  @Test def longStreamMapToObj(): Unit = {
    val nElements = 4
    val prefix = "mapped_"
    var count = 0

    val s0 = LongStream.of(11, 22, 33, 44)

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

  @Test def longStreamNoneMatch_EmptyStream(): Unit = {
    val s = LongStream.empty()
    var predEvaluated = false

    val noneMatched = s.noneMatch((e) => { predEvaluated = true; true })
    assertTrue("unexpected match", noneMatched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def longStreamNoneMatch_True(): Unit = {
    val s = LongStream.of(0, 11, 22, 33)

    val matched = s.noneMatch((e) => e < 0)
    assertTrue("unexpected predicate failure", matched)
  }

  @Test def longStreamNone_MatchFalse(): Unit = {
    val s = LongStream.of(0, 11, 22, 33)

    val matched = s.noneMatch((e) => e > 22)
    assertFalse("unexpected predicate failure", matched)
  }

  @Test def longStreamMax_EmptyStream(): Unit = {
    val s = LongStream.empty()

    val max = s.max()

    assertFalse("max optional should be empty", max.isPresent)
  }

  @Test def longStreamMax(): Unit = {
    val stream = LongStream.of(8585, 44, 8787, 2525, 77)

    val maxOpt = stream.max()

    assertTrue("max not found", maxOpt.isPresent())

    assertEquals(
      "wrong max item found",
      8787,
      maxOpt.getAsLong()
    )
  }

  @Test def longStreamMax_NegativeZero(): Unit = {
    val stream = LongStream.of(-8585, -0, -8787, -2525, -77)

    val maxOpt = stream.max()

    assertTrue("max not found", maxOpt.isPresent())

    assertEquals(
      s"wrong max item found: '${maxOpt.getAsLong()}'",
      0,
      maxOpt.getAsLong()
    )
  }

  @Test def longStreamMin_EmptyStream(): Unit = {
    val s = LongStream.empty()

    val minOpt = s.min()

    assertFalse("min optional should be empty", minOpt.isPresent)
  }

  @Test def longStreamMin(): Unit = {
    val stream = LongStream.of(8585, 44, 8787, 2525, 77)

    val minOpt = stream.min()

    assertTrue("min not found", minOpt.isPresent())

    assertEquals(
      "wrong min item found",
      44,
      minOpt.getAsLong()
    )
  }

  @Test def longStreamMin_NegativeZero(): Unit = {
    val stream = LongStream.of(8585, -0, 8787, 0, 2525, 77)

    val minOpt = stream.min()

    assertTrue("min not found", minOpt.isPresent())

    assertEquals(
      s"wrong min item found: '${minOpt.getAsLong()}'",
      0,
      minOpt.getAsLong()
    )
  }

  /* @Ignore this test and leave it in place. The results are better evaluated
   * visually/manually rather than automatically.
   * JVM documentations suggests that "peek()" be mainly used for debugging.
   */
  @Ignore
  @Test def longStreamPeek(): Unit = {
    val expectedCount = 3

    val s = LongStream.of(13L, 17L, 19L)

    // The ".count()" is a terminal operation to force the pipeline to
    // evalute. The real interest is if the peek() side-effect happened
    // correctly.  Currently that can only be evaluated manually/visually.
    val n = s.peek((e: Long) => printf(s"peek: |${e}|\n")).count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Ignore // see @Ignore comment above "streamShouldPeek()" above.
  @Test def longStreamPeek_CompositeStream(): Unit = {
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

    val n = LongStream
      .of(1, 2, 3, 4L)
      .peek((e: Long) =>
        printf(s"composite peek - before: <${e}>|\n")
      ) // simple str
      .flatMap((e: Long) => LongStream.of((1L to e): _*))
      .peek((e: Long) =>
        printf(s"composite peek - after: <${e}>|\n")
      ) // composite
      .count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Test def longStreamReduce_OneArgEmpty(): Unit = {
    val s = LongStream.empty()

    val optional: OptionalLong = s.reduce((r, e) => r + e)

    assertFalse("unexpected non-empty optional", optional.isPresent())
  }

  @Test def longStreamReduce_OneArg(): Unit = {
    val s = LongStream.of(33, 55, 77, 1111)
    val expectedSum = 1276

    val optional: OptionalLong = s.reduce((r, e) => r + e)

    assertTrue("unexpected empty optional", optional.isPresent())
    assertEquals(
      "unexpected reduction result",
      expectedSum,
      optional.getAsLong()
    )
  }

  @Test def longStreamReduce_TwoArgEmpty(): Unit = {
    val s = LongStream.empty()

    val firstArg = 11L

    val product: Long = s.reduce(firstArg, (r, e) => r * e)

    assertEquals("unexpected reduction result", firstArg, product)
  }

  @Test def longStreamReduce_TwoArg(): Unit = {
    val s = LongStream.of(33, 55, 77, 1111L)
    val expectedProduct = 155267805L

    val product: Long = s.reduce(1L, (r, e) => r * e)

    assertEquals(
      "unexpected reduction result",
      expectedProduct,
      product
    )
  }

  @Test def longStreamSkip_NegativeArg(): Unit = {
    val s = LongStream.of(11, 22, 33)
    assertThrows(classOf[IllegalArgumentException], s.skip(-1))
  }

  @Test def longStreamSkip_TooMany(): Unit = {
    val s = LongStream.of(11, 22, 33)

    val isEmptyStream = !s.skip(10).iterator.hasNext()
    assertTrue("expected empty stream", isEmptyStream)
  }

  @Test def longStreamSkip(): Unit = {
    val expectedValue = 9999
    val s = LongStream.of(11, 22, 33, 44, expectedValue, 66, 77)

    val iter = s.skip(4).iterator()

    assertTrue("expected non-empty stream", iter.hasNext())
    assertEquals(
      "unexpected first value: ",
      expectedValue,
      iter.nextLong()
    )
  }

  @Test def longStreamSorted(): Unit = {
    val nElements = 8
    val wild = new Array[Long](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild(0) = 4532
    wild(1) = 214
    wild(2) = 112
    wild(3) = 315
    wild(4) = 6816
    wild(5) = 377
    wild(6) = 6144
    wild(7) = 960L

    val ordered = new Array[Long](nElements)
    ordered(0) = 112
    ordered(1) = 214
    ordered(2) = 315
    ordered(3) = 377
    ordered(4) = 960
    ordered(5) = 4532
    ordered(6) = 6144
    ordered(7) = 6816L

    val s = LongStream.of(wild: _*)

    val sorted = s.sorted()

    var count = 0

    sorted.forEachOrdered((e) => {
      assertEquals("mismatched elements", ordered(count), e)
      count += 1
    })

    val msg =
      if (count == 0) "unexpected empty stream"
      else "unexpected number of elements"

    assertEquals(msg, nElements, count)
  }

  @Test def longStreamSorted_Characteristics(): Unit = {
    // See comments in StreamTest#streamSorted_Characteristics

    val nElements = 8
    val wild = new Array[Long](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild(0) = 4532
    wild(1) = 214
    wild(2) = 112
    wild(3) = 315
    wild(4) = 6816
    wild(5) = 377
    wild(6) = 6144
    wild(7) = 960

    val seqLongStream = LongStream.of(wild: _*)
    assertFalse(
      "Expected sequential stream",
      seqLongStream.isParallel()
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

    val seqIntSpliter = seqLongStream.spliterator()

    assertEquals(
      "sequential characteristics",
      expectedPreCharacteristics,
      seqIntSpliter.characteristics()
    )

    val sortedSeqLongStream = LongStream.of(wild: _*).sorted()
    val sortedSeqSpliter = sortedSeqLongStream.spliterator()

    assertEquals(
      "sorted sequential characteristics",
      expectedPostCharacteristics,
      sortedSeqSpliter.characteristics()
    )
  }

  @Test def longStreamSortedUnknownSizeButSmall(): Unit = {

    /* To fit array, nElements should be <= Integer.MAX_VALUE.
     * Machine must have sufficient memory to support chosen number of
     * elements.
     */
    val nElements = 20 // Use a few more than usual 2 or 8.

    // Are the characteristics correct?
    val rng = new ju.Random(567890123)

    val wild = rng
      .longs(nElements, 0L, jl.Long.MAX_VALUE)
      .toArray()

    val ordered = wild.clone()
    Arrays.sort(ordered)

    // do some contortions to get an stream with unknown size.
    val iter0 = Spliterators.iterator(Spliterators.spliterator(wild, 0))
    val spliter0 = Spliterators.spliteratorUnknownSize(iter0, 0)

    val s0 = StreamSupport.longStream(spliter0, false)

    val s0Spliter = s0.spliterator()
    assertFalse(
      "Unexpected SIZED stream",
      s0Spliter.hasCharacteristics(Spliterator.SIZED)
    )

    // Validating un-SIZED terminated s0, so need fresh similar stream
    val iter1 = Spliterators.iterator(Spliterators.spliterator(wild, 0))
    val spliter1 = Spliterators.spliteratorUnknownSize(iter1, 0)

    val s = StreamSupport.longStream(spliter1, false)

    val ascending = s.sorted()

    var count = 0

    ascending.forEachOrdered((e) => {
      assertEquals("mismatched elements", ordered(count), e)
      count += 1
    })

    val msg =
      if (count == 0) "unexpected empty stream"
      else "unexpected number of elements"

    assertEquals(msg, nElements, count)

  }

  @Ignore
  @Test def longStreamSortedUnknownSizeButHuge(): Unit = {
    /* This test is for development and Issue verification.
     * It is Ignored in normal Continuous Integration because it takes
     * a long time.
     *
     * See note for similar Test in StreamTest.scala for details.
     * No sense copying same text to DoubleStreamTest, LongStreamTest,
     * & LongStreamTest.
     */

    val rng = new ju.Random(567890123)

    // Are the characteristics correct?
    val rs0 = rng
      .longs(0L, jl.Long.MAX_VALUE) // "Infinite" stream

    val iter0 = rs0.iterator()
    val spliter0 = Spliterators.spliteratorUnknownSize(iter0, 0)
    val s0 = StreamSupport.longStream(spliter0, false)

    val s0Spliter = s0.spliterator()
    assertFalse(
      "Unexpected SIZED stream",
      s0Spliter.hasCharacteristics(Spliterator.SIZED)
    )

    // Validating un-SIZED terminated s0, so need fresh similar stream.
    val rs1 = rng
      .longs(0L, jl.Long.MAX_VALUE) // "Infinite" stream

    val spliter1 = Spliterators.spliteratorUnknownSize(iter0, 0)
    val s = StreamSupport.longStream(spliter1, false)

    val uut = s.sorted() // unit-under-test

    // May take tens of seconds or more to get to Exception.
    assertThrows(classOf[OutOfMemoryError], uut.findFirst())
  }

  @Test def longStreamSortedZeroSize(): Unit = {
    val nElements = 0

    val rng = new ju.Random(567890123)

    val wild = rng
      .longs(nElements, 0L, jl.Long.MAX_VALUE)
      .toArray()

    val ordered = wild.clone()
    Arrays.sort(ordered)

    val spliter = Spliterators.spliterator(wild, 0)

    val s = StreamSupport.longStream(spliter, false)

    val sorted = s.sorted()
    val count = sorted.count()

    assertEquals("expected an empty stream", 0, count)
  }

  // Issue 3378
  @Test def longStreamSortedLongSize(): Unit = {
    /* This tests streams with the SIZED characteristics and a
     *  know length is larger than the largest possible Java array:
     *  approximately Integer.MAX_VALUE.
     */
    val rng = new ju.Random(1234567890)

    val s = rng
      .longs(0, jl.Long.MAX_VALUE) // "Infinite" stream

    /* The sorted() implementation should be a late binding, intermediate
     * operation. Expect no "max array size" error here, but later.
     */

    val uut = s.sorted() // unit-under-test

    /* Stream#findFirst() is a terminal operation, so expect any errors
     * to happen here, not earlier.  In particular, expect code being tested
     * to detect and report the huge size rather than taking a long time
     * and then running out of memory.
     */

    assertThrows(classOf[IllegalArgumentException], uut.findFirst())
  }

  @Test def longStreamSum(): Unit = {
    val nElements = 9

    val wild = new Array[Long](nElements) // holds arbitrarily jumbled data
    wild(0) = 4532
    wild(1) = 214
    wild(2) = 112
    wild(3) = 315
    wild(4) = 6816
    wild(5) = 377
    wild(6) = 6144
    wild(7) = 960

    val expectedSum = 19470

    val s = LongStream.of(wild: _*)

    val sum = s.sum()

    assertEquals("unexpected sum", expectedSum, sum)
  }

  @Test def longStreamSummaryStatistics(): Unit = {
    val nElements = 8

    val wild = new Array[Long](nElements) // holds arbitrarily jumbled data
    wild(0) = 4532
    wild(1) = 214
    wild(2) = 112
    wild(3) = 315
    wild(4) = 6816
    wild(5) = 377
    wild(6) = 6144
    wild(7) = 960

    val expectedCount = nElements
    val expectedMax = 6816
    val expectedMin = 112
    val expectedSum = 19470
    val expectedAverage = expectedSum.toDouble / nElements

    val s = LongStream.of(wild: _*)

    val stats = s.summaryStatistics()

    assertEquals(
      "unexpected average",
      expectedAverage,
      stats.getAverage(),
      epsilon
    )

    assertEquals("unexpected count", expectedCount, stats.getCount())

    assertEquals("unexpected max", expectedMax, stats.getMax())

    assertEquals("unexpected min", expectedMin, stats.getMin())

    assertEquals("unexpected sum", expectedSum, stats.getSum())
  }

  @Test def longStreamToArray(): Unit = {
    val nElements = 9

    val wild = new Array[Long](nElements) // holds arbitrarily jumbled data
    wild(0) = 4532
    wild(1) = 214
    wild(2) = 112
    wild(3) = 315
    wild(4) = 6816
    wild(5) = 377
    wild(6) = 6144
    wild(7) = 960

    val s = LongStream.of(wild: _*)

    val resultantArray = s.toArray()

    // Proper size
    assertEquals("result size", nElements, resultantArray.size)

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("elements do not match", wild(j), resultantArray(j))
  }

}
