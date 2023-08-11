package org.scalanative.testsuite.javalib.util.stream

import java.{lang => jl}
import java.{util => ju}
import java.util._

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.CountDownLatch._

import java.util.function._

import java.util.{stream => jus}
import java.util.stream._

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StreamTest {
  /* Design Note:
   *   Scala 2.12 requires the type in many lamba expressions:
   *          (e: String) => { body }
   *
   *   This may look excessive and unnecessary to those used to Scala 2.13
   *   and Scala 3.
   *
   *   Taking a hit on a few style points allows this one file to run
   *   on the full range of version supported by Scala Native. 'Tis
   *   a pity that it reduces its utility as a model for the full power of
   *   streams.
   */

  private def streamOfSingleton[T](single: Object): Stream[T] = {
    /* Scala Native Tests must support a range of Scala Versions, currently:
     * 2.12.13 to 3.2.2 (soon to be 3.3.0).
     * Scala 2.13.* and 3.* can distinguish between singleton and varargs
     * overloads of Stream.of(), allows the use of the simpler:
     *     val s = Stream.of(expected)
     *
     * This tour of Robin Hood's barn allows Scala 2.12 Tests to run
     * without even more complication.
     */

    val arr = new Array[Object](1)
    arr(0) = single
    Arrays.stream(arr).asInstanceOf[Stream[T]]
  }

  // Frequently used data
  private def genHyadesList(): Tuple2[ArrayList[String], Int] = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Phaisyle")
    sisters.add("Coronis")
    sisters.add("Cleeia")
    sisters.add("Phaeo")
    sisters.add("Eudora")
    sisters.add("Ambrosia")
    sisters.add("Dione")
    (sisters, nElements)
  }

// Methods specified in interface BaseStream ----------------------------

  @Test def streamUnorderedOnUnorderedStream(): Unit = {
    val dataSet = new ju.HashSet[String]()
    dataSet.add("T")
    dataSet.add("S")
    dataSet.add("X")
    dataSet.add("Y")

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
    val s = Stream.of("V", "W", "X", "Y", "Z")
    val sSpliter = s.spliterator()

    assertTrue(
      "Expected ORDERED on stream from array",
      sSpliter.hasCharacteristics(Spliterator.ORDERED)
    )

    // s was ordered, 'so' should be same same. Avoid "already used" exception
    val so = Stream.of("V", "W", "X", "Y", "Z")
    val su = so.unordered()
    val suSpliter = su.spliterator()

    assertFalse(
      "ORDERED stream after unordered()",
      suSpliter.hasCharacteristics(Spliterator.ORDERED)
    )
  }

  @Test def streamParallel(): Unit = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val sPar = sisters.parallelStream()

    assertTrue(
      "Expected parallel stream",
      sPar.isParallel()
    )

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED // 0x4050

    val sParSpliterator = sPar.spliterator()
    assertEquals(
      "parallel characteristics",
      expectedCharacteristics,
      sParSpliterator.characteristics()
    )

    val sSeq = sisters.parallelStream().sequential()
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
    sSeqSpliterator.forEachRemaining((e: String) => {
      assertEquals(
        s"sequential stream contents(${count})",
        sisters.get(count),
        e
      )
      count += 1
    })
  }

  @Test def streamSequential(): Unit = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val sSeq = sisters.stream()

    assertFalse(
      "Expected sequential stream",
      sSeq.isParallel()
    )

    val expectedCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED // 0x4050

    val sSeqSpliterator = sSeq.spliterator()
    assertEquals(
      "sequential characteristics",
      expectedCharacteristics,
      sSeqSpliterator.characteristics()
    )

    val sPar = sisters.stream().parallel()
    assertTrue(
      "Expected parallel stream",
      sPar.isParallel()
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
    sParSpliterator.forEachRemaining((e: String) => {
      assertEquals(
        s"parallel stream contents(${count})",
        sisters.get(count),
        e
      )
      count += 1
    })
  }

// Methods specified in interface Stream --------------------------------

  @Test def streamBuilderCanBuildAnEmptyStream(): Unit = {
    val s = Stream.builder().build()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def streamBuilderCharacteristics(): Unit = {
    val bldr = Stream.builder[String]()
    bldr
      .add("A")
      .add("B")
      .add("C")

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

  @Test def streamEmptyIsEmpty(): Unit = {
    val s = Stream.empty[Int]()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def streamOf_SingleElement(): Unit = {
    val expected = 7.toString()

    val s = streamOfSingleton[String](expected)
    val it = s.iterator()
    assertTrue("stream should not be empty", it.hasNext())
    assertEquals("unexpected element", it.next(), expected)
    assertFalse("stream should be empty and is not.", it.hasNext())
    assertFalse("stream should be empty and is not.", it.hasNext())
  }

  @Test def streamOf_SingleElementCharacteristics(): Unit = {
    val expected = 7.toString()

    val s = streamOfSingleton[String](expected)
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

  @Test def streamOf_MultipleIntElements(): Unit = {
    val s = Stream.of(1, 2, 3)
    val it = s.iterator()
    assertEquals("element_1", 1, it.next())
    assertEquals("element_2", 2, it.next())
    assertEquals("element_3", 3, it.next())
    assertFalse(it.hasNext())
  }

  @Test def streamOf_MultipleElementsCharacteristics(): Unit = {
    val s = Stream.of(1, 2, 3)
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

  @Test def streamFlatMapWorks(): Unit = {
    val s = Stream.of(1, 2, 3)
    val mapper = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((1 to v): _*)
    }
    val s2 = s.flatMap(mapper)
    val it = s2.iterator()

    assertTrue(it.next() == 1)
    assertTrue(it.next() == 1)
    assertTrue(it.next() == 2)
    assertTrue(it.next() == 1)
    assertTrue(it.next() == 2)
    assertTrue(it.next() == 3)
    assertFalse(it.hasNext())
  }

  @Test def streamForEachWorks(): Unit = {
    val s = Stream.of(1, 2, 3)
    var sum = 0
    val consumer = new Consumer[Int] {
      def accept(i: Int): Unit = sum += i
    }
    s.forEach(consumer)
    assertEquals(6, sum)
  }

  @Test def streamFlatMapWorksTwice(): Unit = {
    val stream = Stream.of(1, 2, 3)
    val mapper1 = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((v to 3): _*)
    }
    val mapper2 = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((5 to v by -1): _*)
    }
    val s2 = stream.flatMap(mapper1).flatMap(mapper2)
    val expected =
      Seq(5, 4, 3, 2, 1, 5, 4, 3, 2, 5, 4, 3, 5, 4, 3, 2, 5, 4, 3, 5, 4, 3)
    val result = scala.collection.mutable.ArrayBuffer.empty[Int]
    val it = s2.iterator()
    while (it.hasNext()) {
      result += it.next()
    }
    assertTrue(result == expected)
  }

  @Test def streamOnCloseWorks(): Unit = {
    var latch = new CountDownLatch(1)

    class Closer(cdLatch: CountDownLatch) extends Runnable {
      override def run(): Unit = cdLatch.countDown()
    }

    val s = Stream.empty[Int]().onClose(new Closer(latch))
    s.close()

    val timeout = 30L
    assertTrue(
      "close handler did not run within ${timeout} seconds",
      latch.await(timeout, TimeUnit.SECONDS)
    )
  }

// Static methods -------------------------------------------------------

  @Test def streamConcat(): Unit = {
    val a = Stream.of("Q", "R", "X", "Y", "Z")
    val b = Stream.of("A", "B", "C")

    val s = Stream.concat(a, b)

    val it = s.iterator()
    assertNotNull("s.iterator() should not be NULL", it)

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"element", "Q", it.next())
    assertEquals(s"element", "R", it.next())
    assertEquals(s"element", "X", it.next())
    assertEquals(s"element", "Y", it.next())
    assertEquals(s"element", "Z", it.next())

    assertEquals(s"element", "A", it.next())
    assertEquals(s"element", "B", it.next())
    assertEquals(s"element", "C", it.next())

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def streamGenerate(): Unit = {
    val nElements = 5
    val data = new ArrayList[String](nElements)
    data.add("Zero")
    data.add("One")
    data.add("Two")
    data.add("Three")
    data.add("Four")

    val src = new Supplier[String]() {
      type T = String
      var count = -1

      def get(): T = {
        count += 1
        data.get(count % nElements)
      }
    }

    val s = Stream.generate(src)

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    for (j <- 0 until nElements)
      assertTrue(s"data(${j})", it.next() == data.get(j))

    assertTrue("stream should not be empty", it.hasNext())
  }

  @Test def streamIterate_Unbounded(): Unit = {
    val nElements = 4
    var count = -1

    val expectedSeed = "Woody Woodpecker"
    val s = Stream.iterate[String](
      expectedSeed,
      (e: String) => {
        count += 1
        count.toString()
      }
    )

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals("seed", expectedSeed, it.next())

    for (j <- 0 until nElements)
      assertEquals(s"element: ${j})", String.valueOf(j), it.next())

    assertTrue("stream should not be empty", it.hasNext())
  }

  @Test def streamIterate_Unbounded_Characteristics(): Unit = {
    val s =
      Stream.iterate[jl.Double](0.0, (n => n + 1): UnaryOperator[jl.Double])
    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.ORDERED, Spliterator.IMMUTABLE)

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

    // If SIZED is really missing, these conditions should hold.
    assertEquals(s"getExactSizeIfKnown", -1L, spliter.getExactSizeIfKnown())
    assertEquals(s"estimateSize", Long.MaxValue, spliter.estimateSize())
  }

  @Test def streamOf_NoItems(): Unit = {
    val s = Stream.of()

    val it = s.iterator()
    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def streamOf_OneItem(): Unit = {
    val expectedString = "Only"

    val s = streamOfSingleton[String](expectedString)

    val it = s.iterator()
    assertTrue("stream should not be empty", it.hasNext())
    assertEquals(s"element", expectedString, it.next())

    assertFalse("stream should be empty", it.hasNext())
  }

  // During development, sometimes two elements were taken to be 1 Tuple2
  // Guard against regression.
  @Test def streamOf_TwoItems(): Unit = {
    val expectedString_1 = "RedSox"
    val expectedString_2 = "Lightening"
    val s = Stream.of(expectedString_1, expectedString_2)

    val it = s.iterator()
    assertTrue("stream should not be empty", it.hasNext())
    assertEquals(s"element_1", expectedString_1, it.next())
    assertEquals(s"element_2", expectedString_2, it.next())

    assertFalse("stream should be empty", it.hasNext())
  }

  // Stream.of() with more than two arguments is exercised in many other
  // places in this file, so no Test for that case here.

// Instance methods -----------------------------------------------------

  @Test def streamAllMatch_EmptyStream(): Unit = {
    val s = Stream.empty[String]
    var predEvaluated = false

    val matched = s.allMatch((e) => { predEvaluated = true; true })
    assertTrue("unexpected match failure", matched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def streamAllMatch_True(): Unit = {

    /* stream.allMatch() will return "true" on an empty stream.
     * Try to distinguish that "true" from an actual all-elements-match "true"
     * Since streams can not be re-used, count s0. If it is non-empty, assume
     * its sibling s is also non-empty, distingishing the two "true"s.
     */
    val s0 = Stream.of("Air", "Earth", "Fire", "Water")
    assertTrue("unexpected empty stream", s0.count > 0)

    val s = Stream.of("Air", "Earth", "Fire", "Water")

    val matched = s.allMatch((e) => { e.contains("a") || e.contains("i") })
    assertTrue("unexpected match failure", matched)
  }

  @Test def streamAllMatch_False(): Unit = {
    val s = Stream.of("Air", "Earth", "Fire", "Water")

    val matched = s.allMatch((e) => e.contains("a"))
    assertFalse("unexpected match", matched)
  }

  @Test def streamAnyMatch_EmptyStream(): Unit = {
    val s = Stream.empty[String]
    var predEvaluated = false

    val matched = s.anyMatch((e) => { predEvaluated = true; true })
    assertFalse("unexpected match", matched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def streamAnyMatch_True(): Unit = {
    val s = Stream.of("Air", "Earth", "Fire", "Water")

    val matched = s.anyMatch((e) => e.contains("a"))
    assertTrue("unexpected predicate failure", matched)
  }

  @Test def streamAnyMatch_False(): Unit = {
    val s = Stream.of("Air", "Earth", "Fire", "Water")

    val matched = s.anyMatch((e) => e.contains("X"))
    assertFalse("unexpected predicate failure", matched)
  }

  @Test def streamCollect_EmptyStreamUsingCollector(): Unit = {
    val sisters = new ArrayList[String](0)

    val s = sisters.stream()

    val collected = s.collect(Collectors.toList())

    // Proper size (empty)
    assertEquals("list size", 0, collected.size())
  }

  @Test def streamCollect_UsingCollector(): Unit = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val s = sisters.stream()

    val collected = s.collect(Collectors.toList())

    // Proper size
    assertEquals("list size", nElements, collected.size())

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("list element", sisters.get(j), collected.get(j))
  }

  @Test def streamCollect_EmptyStreamUsingSupplier(): Unit = {
    type U = ArrayList[String]

    val sisters = new U(0)

    val s = sisters.stream()

    val supplier = new Supplier[U]() {
      def get(): U = new U()
    }

    val collected = s.collect(
      supplier,
      (list: U, e: String) => list.add(e),
      (list1: U, list2: U) => list1.addAll(list2)
    )

    // Proper size
    assertEquals("list size", 0, collected.size())
  }

  @Test def streamCollect_UsingSupplier(): Unit = {
    type U = ArrayList[String]

    val (sisters, nElements) = genHyadesList()

    val s = sisters.stream()

    val supplier = new Supplier[U]() {
      def get(): U = new U()
    }

    val collected = s.collect(
      supplier,
      (list: U, e: String) => list.add(e),
      (list1: U, list2: U) => list1.addAll(list2)
    )

    // Proper size
    assertEquals("list size", nElements, collected.size())

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("list element", sisters.get(j), collected.get(j))
  }

  @Test def streamCount(): Unit = {
    val expectedCount = 4

    val s = jus.Stream.of[String]("A", "B", "C", "D")

    assertEquals(s"unexpected element count", expectedCount, s.count())
  }

  @Test def streamCount_compositeStream(): Unit = {
    // Test that count() works with all substreams of a composite stream.
    val expectedCount = 15

    val n = Stream
      .of(1, 2, 3, 4, 5)
      .flatMap((e) => Stream.of((1 to e): _*))
      .count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Test def streamDistinct(): Unit = {
    val expectedCount = 5
    val range = 0 until expectedCount

    val expectedElements = Array.ofDim[Int](expectedCount)
    for (j <- range)
      expectedElements(j) = j + 1

    val expectedSet = new ju.HashSet[Int]()
    for (j <- range)
      expectedSet.add(expectedElements(j))

    val s = jus.Stream
      .of(expectedElements: _*)
      .flatMap((e) => Stream.of((1 to e): _*))
      .distinct()

    assertEquals(s"unexpected count", expectedCount, s.count())

    // Count is good, now did we get expected elements and only them?

    val s2 = jus.Stream
      .of(expectedElements: _*)
      .flatMap((e) => Stream.of((1 to e): _*))
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

  @Test def streamFindAny_Null(): Unit = {
    val s = Stream.of(null.asInstanceOf[String], "NULL")
    assertThrows(classOf[NullPointerException], s.findAny())
  }

  @Test def streamFindAny_True(): Unit = {
    val s = Stream.of("Air", "Earth", "Fire", "Water")
    val acceptableValues = Arrays.asList("Air", "Earth", "Fire", "Water")

    val optional = s.findAny()

    assertTrue("unexpected empty optional", optional.isPresent())

    val found = optional.get()
    assertTrue(
      s"unexpected value: '${found}'",
      acceptableValues.contains(found)
    )
  }

  @Test def streamFindAny_False(): Unit = {
    val s = Stream.empty[String]()

    val optional = s.findAny()

    assertFalse("unexpected failure", optional.isPresent())
  }

  @Test def streamFindFirst_Null(): Unit = {
    val s = Stream.of(null.asInstanceOf[String], "NULL")
    assertThrows(classOf[NullPointerException], s.findFirst())
  }

  @Test def streamFindFirst_True(): Unit = {
    val expectedFirst = "Air"
    val s = Stream.of(expectedFirst, "Earth", "Fire", "Water")

    val optional = s.findFirst()

    assertTrue("unexpected empty optional", optional.isPresent())
    assertEquals("unexpected mismatch", expectedFirst, optional.get())
  }

  @Test def streamFindFirst_False(): Unit = {
    val s = Stream.empty[String]()

    val optional = s.findFirst()

    assertFalse("unexpected failure", optional.isPresent())
  }

  @Test def streamFilter(): Unit = {
    val expectedCount = 4

    val s0 = jus.Stream.of[String]("AA", "B", "CC", "D", "EE", "F", "G")

    val s1 = s0.filter((e) => e.length() == 1)
    assertEquals(s"unexpected element count", expectedCount, s1.count())
  }

  @Test def streamFlatMapToDouble(): Unit = {
    val expectedSum = 4.5

    val s = jus.Stream.of[String]("AA", "B", "CC", "D", "EE", "F")

    /* Chose the items in S and the mapper function to yield an obviously
     * floating point sum, not something that could be an Int implicitly
     * converted to Double.
     * Let the compiler distinguish Double as Object and Double
     * as primitive. Only DoubleStream will have the sum method.
     */

    val sum = s.flatMapToDouble(e => DoubleStream.of(0.5 * e.length())).sum()

    assertEquals(s"unexpected sum", expectedSum, sum, 0.00001)
  }

  @Test def streamFlatMapToInt(): Unit = {
    // Stream#flatMapToInt is Not Yet Implemented
  }

  @Test def streamFlatMapToLong(): Unit = {
    // Stream#flatMapToLong is Not Yet Implemented
  }

  @Test def streamForeachOrdered(): Unit = {
    val s = Stream.of(1, 2, 3, 4)
    var sum = 0
    val consumer = new Consumer[Int] {
      def accept(i: Int): Unit = sum += i
    }
    s.forEachOrdered(consumer)
    assertEquals(10, sum)
  }

  @Test def streamLimit_NegativeArg(): Unit = {
    val s = Stream.of("X", "Y", "Z")
    assertThrows(classOf[IllegalArgumentException], s.limit(-1))
  }

  @Test def streamLimit(): Unit = {
    val expectedCount = 10
    var data = -1

    val s0 = Stream.iterate[String](
      "seed",
      (e: String) => {
        data += 1
        data.toString()
      }
    )

    val s1 = s0.limit(expectedCount)

    assertEquals(s"unexpected element count", expectedCount, s1.count())
  }

  /*  Note Well: The Issue #3309 tests are written to match Java 8 behavior.
   *  Scala Native javalib currently advertises itself as Java 8 (1.8)
   *  compliant, so these tests match that.
   *
   *  Somewhere after Java 11  and before or at Java 17, the behavior changes
   *  and these tests will begin to fail for parallel ORDERED streams.
   *  See the issue for details.
   */

  // Issue #3309 - 1 of 5
  @Test def streamLimit_Size(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val srcSize = 10

    val spliter =
      Stream
        .iterate[jl.Integer](
          0,
          ((n: jl.Integer) => n + 1): UnaryOperator[jl.Integer]
        )
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
  @Test def streamLimit_Characteristics(): Unit = {
    StreamTestHelpers.requireJDK8CompatibleCharacteristics()

    val zeroCharacteristicsSpliter =
      new Spliterators.AbstractSpliterator[Object](Long.MaxValue, 0x0) {
        def tryAdvance(action: Consumer[_ >: Object]): Boolean = true
      }

    val sZero = StreamSupport.stream(zeroCharacteristicsSpliter, false)
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
      new Spliterators.AbstractSpliterator[Object](Long.MaxValue, 0x5551) {
        def tryAdvance(action: Consumer[_ >: Object]): Boolean = true
      }

    val sAll = StreamSupport.stream(allCharacteristicsSpliter, false)

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
      new Spliterators.AbstractSpliterator[Object](0, 0x5551) {
        def tryAdvance(action: Consumer[_ >: Object]): Boolean = false
      }

    val sAll = StreamSupport.stream(allCharacteristicsSpliter, false)

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

    val unsizedSpliter =
      Stream
        .iterate[jl.Integer](
          0,
          ((n: jl.Integer) => n + 1): UnaryOperator[jl.Integer]
        )
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

    val proofSpliter = Stream.of("Air", "Earth", "Fire", "Water").spliterator()

    val expectedProofCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.ORDERED | Spliterator.IMMUTABLE // 0x4450

    assertEquals(
      "Unexpected origin stream characteristics",
      expectedProofCharacteristics,
      proofSpliter.characteristics()
    )

    val sizedSpliter = Stream
      .of("Air", "Earth", "Fire", "Water")
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

  @Test def streamMap(): Unit = {
    val nElements = 4
    val prefix = "mapped_"
    var count = 0

    val s0 = jus.Stream.of[String]("A", "B", "C", "D")

    /* Scala 2.12 needs the ": Stream[String]" type ascription so it uses
     *  the proper Consumer type.
     *  Scala 2.13.* & 3.* do not need it (and it causes minimal harm).
     */
    val s1: Stream[String] = s0.map((e: String) => {
      count += 1
      s"${prefix}${e}"
    })

    /* Check that the right number of elements, each with the expected form,
     * are generated.
     *
     * "map()" is an intermediate, not terminal operation.
     * Do the terminal "forEach()" first, to force the counting in the mapper.
     */

    s1.forEach((e: String) =>
      assertTrue(
        s"unexpected map element: ${e}",
        e.startsWith(prefix) &&
          (e.endsWith("_A") ||
            e.endsWith("_B") ||
            e.endsWith("_C") ||
            e.endsWith("_D"))
      )
    )

    assertEquals("map has unexpected count", nElements, count)
  }

  /* The mapMulti clade was introduce in Java 16, find Tests in
   * StreamTestOnJDK16.scala
   */

  @Test def streamMapToDouble(): Unit = {
    val expectedSum = 28.26

    val s = jus.Stream.of[String]("AA", "B", "CC", "D", "EE", "F")

    /* Chose the items in S and the mapper function to yield an obviously
     * floating point sum, not something that could be an Int implicitly
     * converted to Double.
     * Let the compiler distinguish Double as Object and Double
     * as primitive. Only DoubleStream will have the sum method.
     */

    val sum = s.mapToDouble(e => 3.14 * e.length()).sum()

    assertEquals(s"unexpected sum", expectedSum, sum, 0.00001)
  }

  @Test def streamNoneMatch_EmptyStream(): Unit = {
    val s = Stream.empty[String]
    var predEvaluated = false

    val noneMatched = s.noneMatch((e) => { predEvaluated = true; true })
    assertTrue("unexpected match", noneMatched)
    assertFalse("predicate should not have been evaluated", predEvaluated)
  }

  @Test def streamNoneMatch_True(): Unit = {
    val s = Stream.of("Air", "Earth", "Fire", "Water")

    val matched = s.noneMatch((e) => e.contains("X"))
    assertTrue("unexpected predicate failure", matched)
  }

  @Test def streamNone_MatchFalse(): Unit = {
    val s = Stream.of("Air", "Earth", "Fire", "Water")

    val matched = s.noneMatch((e) => e.contains("a"))
    assertFalse("unexpected predicate failure", matched)
  }

  @Test def streamMax_ComparatorNaturalOrderString(): Unit = {
    val stream = Stream.of("85", "4", "87", "25", "7")

    val maxOpt = stream.max(Comparator.naturalOrder[String]())

    assertTrue("max not found", maxOpt.isPresent())

    assertEquals(
      "wrong max item found",
      "87",
      maxOpt.get()
    )
  }

  @Test def streamMin_ComparatorNaturalOrderString(): Unit = {
    val stream = Stream.of("85", "4", "87", "25", "7")

    val minOpt = stream.min(Comparator.naturalOrder[String]())

    assertTrue("min not found", minOpt.isPresent())

    assertEquals(
      "wrong min item found",
      "25", // These are string, not primitive, comparisions, so min is not "4"
      minOpt.get()
    )
  }

  case class Item(name: String, upc: Int)

  val itemComparator = new ju.Comparator[Item] {
    def compare(item1: Item, item2: Item): Int =
      item1.upc - item2.upc
  }

  @Test def streamMax_EmptyStream(): Unit = {
    val items = new ArrayList[Item](0)

    val s = items.stream()

    val maxOpt = s.max(itemComparator)

    assertFalse("max optional should be empty", maxOpt.isPresent)
  }

  @Test def streamMax(): Unit = {
    val nElements = 7
    val items = new ArrayList[Item](nElements)
    items.add(Item("Maya", 1))
    items.add(Item("Electra", 2))
    items.add(Item("Taygete", 3))
    items.add(Item("Alcyone", 4))
    items.add(Item("Celaeno", 5))
    items.add(Item("Sterope", 6))
    items.add(Item("Merope", 7))

    val s = items.stream()

    val maxOpt = s.max(itemComparator)

    assertTrue("max not found", maxOpt.isPresent)
    assertEquals(
      "wrong max item found",
      items.get(nElements - 1).name,
      maxOpt.get().name
    )
  }

  @Test def streamMin_EmptyStream(): Unit = {
    val items = new ArrayList[Item](0)

    val s = items.stream()

    val minOpt = s.min(itemComparator)

    assertFalse("min optional should be empty", minOpt.isPresent)
  }

  @Test def streamMin(): Unit = {
    val nElements = 7
    val items = new ArrayList[Item](nElements)
    // Mix up the item.upc field so that first item is not minimum.
    // Some (faulty) algorithms might always report the first item.
    items.add(Item("Maya", 2))
    items.add(Item("Electra", 1))
    items.add(Item("Taygete", 3))
    items.add(Item("Alcyone", 4))
    items.add(Item("Celaeno", 5))
    items.add(Item("Sterope", 6))
    items.add(Item("Merope", 7))

    val s = items.stream()

    val minOpt = s.min(itemComparator)

    assertTrue("min not found", minOpt.isPresent)
    assertEquals("wrong min item found", items.get(1).name, minOpt.get().name)
  }

  /* @Ignore this test and leave it in place. The results are better
   * evaluated visually/manually rather than automatically.
   * JVM documentations suggests that "peek()" be mainly used for debugging.
   */
  @Ignore
  @Test def streamPeek(): Unit = {
    val expectedCount = 3

    val s = Stream.of("Animal", "Vegetable", "Mineral")

    /* The ".count()" is a terminal operation to force the pipeline to
     * evalute. The real interest is if the peek() side-effect happened
     *  correctly.  Currently that can only be evaluated manually/visually.
     */
    val n = s.peek((e) => printf(s"peek: |${e}||\n")).count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Ignore
  @Test def streamPeek_CompositeStream(): Unit = {
    // Test that peek() works with all substreams of a composite stream.
    val expectedCount = 10

    /* See ".count()" comment in streamShouldPeek above.
     *
     * One should see the original data before and then after transformation
     * done by flatmap to each original element. Something like:
     *   before: <1>
     *     after: <1>
     *   before: <2>
     *     after: <1>
     *     after: <2>
     *   before: <3>
     *     after: <1>
     *     after: <2>
     *     after: <3>
     *   before: <4>
     *     after: <1>
     *     after: <2>
     *     after: <3>
     *     after: <4>
     */
    val n = Stream
      .of(1, 2, 3, 4)
      .peek((e) => printf(s"composite peek - before: <${e}>|\n")) // simple str
      .flatMap((e) => Stream.of((1 to e): _*))
      .peek((e) => printf(s"composite peek - after: <${e}>|\n")) // composite
      .count()

    assertEquals(s"unexpected count", expectedCount, n)
  }

  @Test def streamReduce_OneArgEmpty(): Unit = {
    val s = Stream.empty[Int]

    val optional: Optional[Int] = s.reduce((r, e) => r + e)

    assertFalse("unexpected non-empty optional", optional.isPresent())
  }

  @Test def streamReduce_OneArg(): Unit = {
    val s = Stream.of(3, 5, 7, 11)
    val expectedSum = 26

    val optional: Optional[Int] = s.reduce((r, e) => r + e)

    assertTrue("unexpected empty optional", optional.isPresent())
    assertEquals("unexpected reduction result", expectedSum, optional.get())
  }

  @Test def streamReduce_TwoArgEmpty(): Unit = {
    val s = Stream.empty[Int]
    val firstArg = 1

    val product: Int = s.reduce(
      firstArg,
      (r: Int, e: Int) => r * e
    )

    assertEquals("unexpected reduction result", firstArg, product)
  }

  @Test def streamReduce_TwoArg(): Unit = {
    val s = Stream.of(3, 5, 7, 11)
    val expectedProduct = 1155

    val product: Int = s.reduce(
      1,
      (r: Int, e: Int) => r * e
    )

    assertEquals("unexpected reduction result", expectedProduct, product)
  }

  @Test def streamReduce_ThreeArgEmpty(): Unit = {
    val s = Stream.empty[Int]
    val firstArg = Int.MinValue

    val product: Int = s.reduce(
      firstArg,
      (r: Int, e: Int) => Math.max(r, e),
      (r: Int, e: Int) => if (r >= e) r else e
    )

    assertEquals("unexpected reduction result", firstArg, product)
  }

  @Test def streamReduce_ThreeArg(): Unit = {

    val stream = Stream.of(3, 17, 5, 13, 7, 19, 11)
    val expectedMax = 19

    val max: Int = stream.reduce(
      Int.MinValue,
      (r: Int, e: Int) => Math.max(r, e),
      (r: Int, e: Int) => if (r >= e) r else e
    )

    assertEquals("unexpected reduction result", expectedMax, max)
  }

  @Test def streamSkip_NegativeArg(): Unit = {
    val s = Stream.of("X", "Y", "Z")
    assertThrows(classOf[IllegalArgumentException], s.skip(-1))
  }

  @Test def streamSkip_TooMany(): Unit = {
    val s = Stream.of("X", "Y", "Z")

    val isEmptyStream = !s.skip(10).iterator.hasNext()
    assertTrue("expected empty stream", isEmptyStream)
  }

  @Test def streamSkip(): Unit = {
    val expectedValue = "V"
    val s = Stream.of("R", "S", "T", "U", expectedValue, "X", "Y", "Z")

    val iter = s.skip(4).iterator()

    assertTrue("expected non-empty stream", iter.hasNext())
    assertEquals("unexpected first value: ", expectedValue, iter.next())
  }

  @Test def streamSorted(): Unit = {
    val nElements = 8
    val wild = new ArrayList[String](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild.add("Dasher")
    wild.add("Prancer")
    wild.add("Vixen")
    wild.add("Comet")
    wild.add("Cupid")
    wild.add("Donner")
    wild.add("Blitzen")
    wild.add("Rudolph")

    val ordered = new ArrayList(wild)
    ju.Collections.sort(ordered)

    val s = wild.stream()

    val alphabetized = s.sorted()

    var count = 0

    alphabetized.forEachOrdered((e) => {
      assertEquals("mismatched elements", ordered.get(count), e)
      count += 1
    })

    val msg =
      if (count == 0) "unexpected empty stream"
      else "unexpected number of elements"

    assertEquals(msg, nElements, count)
  }

  @Test def streamSorted_Characteristics(): Unit = {
    /* SN sequential, SN parallel, & JVM streams should all return the same
     * characteristics both before (pre) and after (post) sorting.
     *
     * Test both sequential and parallel streams to verify this expectation.
     * Testing 'sorted()' will call 'sorted(comparator)', so this one Test
     * covers both methods.
     */

    val nElements = 8
    val wild = new ArrayList[String](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild.add("Dasher")
    wild.add("Prancer")
    wild.add("Vixen")
    wild.add("Comet")
    wild.add("Cupid")
    wild.add("Donner")
    wild.add("Blitzen")
    wild.add("Rudolph")

    val ordered = new ArrayList(wild)
    ju.Collections.sort(ordered)

    val seqStream = wild.stream()
    assertFalse(
      "Expected sequential stream",
      seqStream.isParallel()
    )

    // same expected values for SN sequential, SN parallel, & JVM streams
    val expectedPreCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED // 0x4050

    val expectedPostCharacteristics =
      expectedPreCharacteristics + Spliterator.SORTED

    val seqSpliter = seqStream.spliterator()

    assertEquals(
      "sequential characteristics",
      expectedPreCharacteristics,
      seqSpliter.characteristics()
    )

    val sortedSeqStream = wild.stream().sorted()
    val sortedSeqSpliter = sortedSeqStream.spliterator()

    assertEquals(
      "sorted sequential characteristics",
      expectedPostCharacteristics,
      sortedSeqSpliter.characteristics()
    )

    val parStream = wild.stream().parallel()
    assertFalse(
      "Expected  parallel stream",
      seqStream.isParallel()
    )

    val parSpliter = parStream.spliterator()

    assertEquals(
      "parallel characteristics",
      expectedPreCharacteristics,
      parSpliter.characteristics()
    )

    val sortedParStream = wild.stream().parallel().sorted()
    val sortedParSpliter = sortedParStream.spliterator()

    assertEquals(
      "sorted parallel characteristics",
      expectedPostCharacteristics,
      sortedParSpliter.characteristics()
    )

  }

  @Test def streamSorted_UsingComparator(): Unit = {
    val nElements = 8
    val wild = new ArrayList[String](nElements)

    // Ensure that the Elements are not inserted in sorted or reverse order.
    wild.add("Dasher")
    wild.add("Prancer")
    wild.add("Vixen")
    wild.add("Comet")
    wild.add("Cupid")
    wild.add("Donner")
    wild.add("Blitzen")
    wild.add("Rudolph")

    val ordered = new ArrayList[String](nElements)
    ordered.add("Blitzen")
    ordered.add("Comet")
    ordered.add("Cupid")
    ordered.add("Dasher")
    ordered.add("Donner")
    ordered.add("Prancer")
    ordered.add("Rudolph")
    ordered.add("Vixen")

    val s = wild.stream()

    val reverseOrdered = s.sorted(Comparator.reverseOrder())

    val startIndex = nElements - 1
    var count = 0

    reverseOrdered.forEachOrdered((e) => {
      val index = startIndex - count
      assertEquals("mismatched elements", ordered.get(index), e)
      count += 1
    })

    val msg =
      if (count == 0) "unexpected empty stream"
      else "unexpected number of elements"

    assertEquals(msg, nElements, count)
  }

  @Test def streamSorted_UsingCustomComparator(): Unit = {

    // A simple class to mix things up.
    // Try something a user in the wild might do and expect to work.
    case class Datum(name: String, expectedOrder: Int)

    val nElements = 8
    val data = new ArrayList[Datum](nElements)
    // Ensure that the Elements are not inserted in sorted or reverse order.

    /* The second field is the expected encounter order in the reverse sorted
     *  stream.
     *  That is, "Vixen" should be first in the output stream, so has 1.
     *  And so on...
     */

    data.add(Datum("Dasher", 5))
    data.add(Datum("Prancer", 3))
    data.add(Datum("Vixen", 1))
    data.add(Datum("Comet", 7))
    data.add(Datum("Cupid", 6))
    data.add(Datum("Donner", 4))
    data.add(Datum("Blitzen", 8))
    data.add(Datum("Rudolph", 2))

    val s = data.stream()

    val reverseOrdered = s.sorted(
      new Comparator[Datum]() {
        def compare(o1: Datum, o2: Datum): Int =
          o2.name.compare(o1.name)
      }
    )

    var count = 0

    reverseOrdered.forEachOrdered((e) => {
      count += 1
      assertEquals(
        s"mismatched element ${e.name} index",
        count,
        e.expectedOrder
      )
    })

    val msg =
      if (count == 0) "unexpected empty stream"
      else "unexpected number of elements"

    assertEquals(msg, nElements, count)
  }

  @Test def streamToArrayObject(): Unit = {
    val (sisters, nElements) = genHyadesList()

    val s = sisters.stream()

    val resultantArray = s.toArray()

    // Proper size
    assertEquals("result size", nElements, resultantArray.size)

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("elements do not match, ", sisters.get(j), resultantArray(j))
  }

  @Test def streamToArrayTypeKnownSize(): Unit = {
    val (sisters, nElements) = genHyadesList()

    val s = sisters.stream()

    val resultantArray = s.toArray(
      new IntFunction[Array[String]]() {
        def apply(value: Int): Array[String] = new Array[String](value)
      }
    )

    // Proper type
    assertTrue(
      "Array element type not String",
      resultantArray.isInstanceOf[Array[String]]
    )

    // Proper size
    assertEquals("result size", nElements, resultantArray.size)

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("elements do not match, ", sisters.get(j), resultantArray(j))
  }

  @Test def streamToArrayTypeUnknownSize(): Unit = {
    val (sisters, nElements) = genHyadesList()

    val spliter = Spliterators.spliteratorUnknownSize(
      sisters.iterator(),
      Spliterator.ORDERED
    )

    val s = StreamSupport.stream(spliter, false)

    val resultantArray = s.toArray(
      new IntFunction[Array[String]]() {
        def apply(value: Int): Array[String] = new Array[String](value)
      }
    )

    // Proper type
    assertTrue(
      "Array element type not String",
      resultantArray.isInstanceOf[Array[String]]
    )

    // Proper size
    assertEquals("result size", nElements, resultantArray.size)

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("elements do not match, ", sisters.get(j), resultantArray(j))
  }

}
