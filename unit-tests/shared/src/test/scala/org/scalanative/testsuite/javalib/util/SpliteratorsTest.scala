package org.scalanative.testsuite.javalib.util

import java.util.{PrimitiveIterator, Spliterator, Spliterators}

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* The vigilant observer will note that there is a SpliteratorsTest, with
 * and 's', but no independent SpliteratorTest, without an 's'.  This is
 * because the generation (Spliterators) and verification (Spliterator)
 * of a spliterator() closely depend on the specified conditions in
 * each other: Tests are best kept close together, in time & space.
 */

/* Java 8 introduced a number of ways of obtaining a spliterator.
 * This file tests the static methods of Spliterator. It passes
 * Arrays & Collections to Spliterator methods rather than calling
 * the spliterator method of the Array or Collection.
 *
 * To get a proper overview, it is worth knowing that other files test
 * other ways of obtaining spliterators. A partial list:
 *   javalib.lang.IterableSpliteratorTest
 *   javalib.util.ArraysSpliteratorTest
 *   javalib.util.CollectionDefaultSpliteratorTest
 *
 * Classes which override the default methods above may/should have
 * their own Tests.
 */

object SpliteratorsTest {
  private val maskNames = Map(
    0x00000001 -> "DISTINCT",
    0x00000004 -> "SORTED",
    0x00000010 -> "ORDERED",
    0x00000040 -> "SIZED",
    0x00000100 -> "NONNULL",
    0x00000400 -> "IMMUTABLE",
    0x00001000 -> "CONCURRENT",
    0x00004000 -> "SUBSIZED"
  )

  // convert characteristics bit mask to its corresponding name, or else hex.
  private def maskToName(mask: Int): String =
    maskNames.getOrElse(mask, s"0x${mask.toHexString.toUpperCase}")

  def verifyCharacteristics[T](
      splItr: Spliterator[T],
      requiredPresent: Seq[Int],
      requiredAbsent: Seq[Int]
  ): Unit = {
    /* The splItr.hasCharacteristics() and splItr.characteristics()
     * sections both seek the same information: Does the spliterator report
     * the required characteristics and no other. They ask the question
     * in slightly different ways to exercise each of the two Spliterator
     * methods. The answers should match, belt & suspenders.
     */

    for (rp <- requiredPresent) {
      assertTrue(
        s"missing requiredPresent characteristic: ${maskToName(rp)}",
        splItr.hasCharacteristics(rp)
      )
    }

    for (rp <- requiredAbsent) {
      assertFalse(
        s"found requiredAbsent characteristic: ${maskToName(rp)}",
        splItr.hasCharacteristics(rp)
      )
    }

    val sc = splItr.characteristics()
    val requiredPresentMask = requiredPresent.fold(0)((x, y) => x | y)

    val unknownBits = sc & ~requiredPresentMask
    val unknownBitsMsg = s"0X${unknownBits.toHexString}"
    assertEquals(
      s"unexpected characteristics, unknown mask: ${unknownBitsMsg}",
      0,
      unknownBits
    )
  }
}

class SpliteratorsTest {
  import SpliteratorsTest._

  @Test def emptyDoubleSpliterator(): Unit = {
    type T = Double
    val expectedSize = 0

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfDouble = Spliterators.emptyDoubleSpliterator()
    assertNotNull("Null coll.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    assertFalse("tryAdvance", spliter.tryAdvance((_: T) => ()))

    var count = 0
    spliter.forEachRemaining((_: T) => { count += 1 })
    assertEquals("forEachRemaining size", expectedSize, count)
  }

  @Test def emptyIntSpliterator(): Unit = {
    type T = Int
    val expectedSize = 0

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfInt = Spliterators.emptyIntSpliterator()
    assertNotNull("Null coll.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    assertFalse("tryAdvance", spliter.tryAdvance((_: T) => ()))

    var count = 0
    spliter.forEachRemaining((_: T) => { count += 1 })
    assertEquals("forEachRemaining size", expectedSize, count)
  }

  @Test def emptyLongSpliterator(): Unit = {
    type T = Long
    val expectedSize = 0

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfLong = Spliterators.emptyLongSpliterator()
    assertNotNull("Null coll.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    assertFalse("tryAdvance", spliter.tryAdvance((_: T) => ()))

    var count = 0
    spliter.forEachRemaining((_: T) => { count += 1 })
    assertEquals("forEachRemaining size", expectedSize, count)
  }

  @Test def emptySpliterator(): Unit = {
    type T = String
    val expectedSize = 0

    // Let compiler check type returned is expected.
    val spliter: Spliterator[T] = Spliterators.emptySpliterator[T]()
    assertNotNull("Null coll.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    assertFalse("tryAdvance", spliter.tryAdvance((_: T) => ()))

    var count = 0
    spliter.forEachRemaining((_: T) => { count += 1 })
    assertEquals("forEachRemaining size", expectedSize, count)
  }

  @Test def primitiveIteratorFromSpliteratorDouble(): Unit = {
    val expectedElements = Array(
      0.0, 1.1, 2.2, 3.3, 4.4, 5.5, 6.6
    )
    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfDouble = Spliterators.spliterator(
      expectedElements,
      Spliterator.SIZED | Spliterator.SUBSIZED
    )
    assertNotNull("Null array.spliterator", spliter)

    // Check that iterator() call returns expected type.
    val pItrDouble: PrimitiveIterator.OfDouble = Spliterators.iterator(spliter)
    assertNotNull("Null PrimitiveIterator.OfDouble", pItrDouble)

    // Now verify the PrimitiveIterator.OfDouble

    assertTrue(
      s"unexpected empty PrimitiveIterator",
      pItrDouble.hasNext()
    )

    var count = 0
    pItrDouble.forEachRemaining((e: Double) => {
      assertEquals(s"failed match", expectedElements(count), e, 0.0001)
      count += 1
    })
  }

  @Test def primitiveIteratorFromSpliteratorInt(): Unit = {
    val expectedElements = Array(
      0, 1, 2, 3, 4, 5, 6
    )
    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfInt = Spliterators.spliterator(
      expectedElements,
      Spliterator.SIZED | Spliterator.SUBSIZED
    )
    assertNotNull("Null array.spliterator", spliter)

    // Check that iterator() call returns expected type.
    val pItrInt: PrimitiveIterator.OfInt = Spliterators.iterator(spliter)
    assertNotNull("Null PrimitiveIterator.OfInt", pItrInt)

    // Now verify the PrimitiveIterator.OfInt

    assertTrue(
      s"unexpected empty PrimitiveIterator",
      pItrInt.hasNext()
    )

    var count = 0
    pItrInt.forEachRemaining((e: Int) => {
      assertEquals(s"failed match", expectedElements(count), e)
      count += 1
    })
  }

  @Test def primitiveIteratorFromSpliteratorLong(): Unit = {
    val expectedElements = Array(0, 11L, 22L, 33L, 44L, 55L, 66L)
    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfLong = Spliterators.spliterator(
      expectedElements,
      Spliterator.SIZED | Spliterator.SUBSIZED
    )
    assertNotNull("Null array.spliterator", spliter)

    // Check that iterator() call returns expected type.
    val pItrLong: PrimitiveIterator.OfLong = Spliterators.iterator(spliter)
    assertNotNull("Null PrimitiveIterator.OfLong", pItrLong)

    // Now verify the PrimitiveIterator.OfLong

    assertTrue(
      s"unexpected empty PrimitiveIterator",
      pItrLong.hasNext()
    )

    var count = 0
    pItrLong.forEachRemaining((e: Long) => {
      assertEquals(s"failed match", expectedElements(count), e)
      count += 1
    })
  }

  @Test def iteratorFromSpliteratorType(): Unit = {
    type T = String
    val expectedElements = Array(
      "lliu",
      "hwi",
      "kre",
      "sei",
      "mne",
      "rhi",
      "fve"
    )

    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator[T] = Spliterators.spliterator(
      expectedElements.asInstanceOf[Array[Object]],
      Spliterator.SIZED | Spliterator.SUBSIZED
    )
    assertNotNull("Null array.spliterator", spliter)

    // Check that iterator() call returns expected type.
    val itr: java.util.Iterator[T] = Spliterators.iterator(spliter)
    assertNotNull("Null Iterator", itr)

    // Now verify the Iterator

    assertTrue(
      s"unexpected empty Iterator",
      itr.hasNext()
    )

    var count = 0
    itr.forEachRemaining((e: T) => {
      assertEquals(s"failed match", expectedElements(count), e)
      count += 1
    })
  }

  @Test def spliteratorOfTypeFromCollection(): Unit = {
    type T = String
    val expectedElements = Array(
      "Bertha von Suttner",
      "Jane Addams",
      "Emily Greene Balch",
      "Betty Williams",
      "Mairead Corrigan",
      "Alva Myrdal"
    )

    val expectedSize = expectedElements.size

    val coll = TrivialImmutableCollection(expectedElements: _*)
    assertEquals(expectedSize, coll.size())

    // Example values used at the time of this writing by ArrayBlockingQueue
    val requiredPresent = Seq(
      Spliterator.ORDERED,
      Spliterator.NONNULL,
      Spliterator.CONCURRENT
    )
    val requiredPresentMask = requiredPresent.fold(0)((x, y) => x | y)

    // Since CONCURRENT is given in requiredPresent, SIZED and SUBSIZED
    // should not be turned on by constructor.
    val requiredAbsent = Seq(
      Spliterator.SIZED,
      Spliterator.SUBSIZED,
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val spliter: Spliterator[T] =
      Spliterators.spliterator(coll, requiredPresentMask)
    assertNotNull("Null coll.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1, // Because CONCURRENT, exact size is not known.
      spliter.getExactSizeIfKnown()
    )

    // Check that both count & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize, // on JVM estimateSize always returns initial expectedSize
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining size", expectedSize, count)
    // on JVM estimateSize always returns initial expectedSize
    assertEquals(
      "forEachRemaining estimateSize",
      expectedSize,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorOfDoubleFromArray(): Unit = {
    type T = Double
    val expectedElements = Array(
      0.0, 10.1, 20.2, 30.3, 44.4, 55.5, 66.6
    )

    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfDouble = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e,
        0.0001
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e,
        0.0001
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorOfDoubleFromArrayRange(): Unit = {
    type T = Double
    val expectedElements = Array(
      0.0, 10.1, 20.2, 30.3, 44.4, 55.5, 66.6
    )

    val sliceStartIndex = 1
    val sliceEndIndex = 5
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfDouble = Spliterators.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(sliceStartIndex + count),
        e,
        0.0001
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSliceSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e,
        0.0001
      )
      count += 1
    })
    assertEquals("forEachRemaining cursor", expectedSliceSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorOfIntFromArray(): Unit = {
    type T = Int
    val expectedElements = Array(
      0, 1, 2, 3, 4, 5, 6
    )

    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfInt = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorOfIntFromArrayRange(): Unit = {
    type T = Int
    val expectedElements = Array(
      1, 11, 22, 33, 44, 55, 66
    )

    val sliceStartIndex = 1
    val sliceEndIndex = 4
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    val coll = TrivialImmutableCollection(expectedElements: _*)
    assertEquals(expectedElements.size, coll.size())

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfInt = Spliterators.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSliceSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining cursor", expectedSliceSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorFromIteratorType(): Unit = {
    type T = String
    val expectedElements = Array(
      "Arctic",
      "North Atlantic",
      "South Atlantic",
      "Indian",
      "North Pacific",
      "South Pacific",
      "Antarctic"
    )

    val expectedSize = expectedElements.size

    val coll = TrivialImmutableCollection(expectedElements: _*)
    assertEquals(expectedSize, coll.size())

    /* Test only the "astonishing" case, estimatedSize always return the
     * initial size. No need to test CONCURRENT and SIZED separately.
     */
    val requiredPresent = Seq(Spliterator.CONCURRENT)
    val requiredPresentMask = requiredPresent.fold(0)((x, y) => x | y)

    // Since CONCURRENT specified as required, SIZED and SUBSIZED should be off.
    val requiredAbsent = Seq(
      Spliterator.SIZED,
      Spliterator.SUBSIZED,
      Spliterator.SORTED // guard getComparator() throw
    )

    /* Create spliterator specifying SIZED and SUBSIZED then check
     * that the spliterator always reports them as absent, as documented.
     */
    // Let compiler check type returned is expected.
    val spliter: Spliterator[T] = Spliterators.spliterator(
      coll.iterator,
      expectedSize,
      requiredPresentMask
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the count & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize, // on JVM estimateSize always returns initial expectedSize
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedSize, count)
    // on JVM estimateSize always returns initial expectedSize
    assertEquals(
      "forEachRemaining estimateSize",
      expectedSize,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorOfLongFromArray(): Unit = {
    type T = Long
    val expectedElements = Array(
      0L, 1L, 2L, 3L, 4L, 5L, 6L
    )

    val expectedSize = expectedElements.size

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfLong = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorOfLongFromArrayRange(): Unit = {
    type T = Long
    val expectedElements = Array(
      1L, 11L, 22L, 33L, 44L, 55L, 66L
    )

    val sliceStartIndex = 1
    val sliceEndIndex = 4
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfLong = Spliterators.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(Spliterator.SORTED) // guard getComparator() throw
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSliceSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining cursor", expectedSliceSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorOfTypeFromArrayRange(): Unit = {
    type T = String
    val expectedElements = Array(
      "nul'",
      "odin",
      "dva",
      "tri",
      "cotiri",
      "p'at",
      "sist'"
    )

    val sliceStartIndex = 2
    val sliceEndIndex = 6
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    /* InitialPresent are the values used, at the time of this writing,
     * by LinkedBlockingQueue.
     *
     * The spliterator-under-test is expected to always supply SIZED and
     * SUBSIZED. Current implementation does not automatically add the
     * "possibly more", from the documentation. If that ever changes,
     * this test will begin to fail (unexpected bits set).
     *
     * Yes, having CONCURRENT and SIZED both set is unusual. Done here
     * just to test wierd corner cases that are _bound_ to happen in the wild.
     */

    val initialPresent = Seq(
      Spliterator.ORDERED,
      Spliterator.NONNULL,
      Spliterator.CONCURRENT
    )
    val initialPresentMask = initialPresent.fold(0)((x, y) => x | y)

    val requiredPresent = initialPresent ++
      Seq(Spliterator.SIZED, Spliterator.SUBSIZED)

    val requiredAbsent = Seq.empty[Int]

    // Let compiler check type returned is expected.
    val spliter: Spliterator[T] = Spliterators.spliterator(
      expectedElements.asInstanceOf[Array[AnyRef]],
      sliceStartIndex,
      sliceEndIndex,
      initialPresentMask
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSliceSize - 1,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining cursor", expectedSliceSize, count)
    assertEquals("forEachRemaining estimateSize", 0, spliter.estimateSize())
  }

  @Test def spliteratorFromPrimitiveIteratorOfDouble(): Unit = {
    type T = Double
    val expectedElements = Array(
      0.0, 10.1, 20.2, 30.3, 44.4, 55.5, 66.6
    )
    val expectedSize = expectedElements.size

    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val siOfDouble: Spliterator.OfDouble = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", siOfDouble)

    //
    // Let compiler check type returned is expected.
    val piOfDouble: PrimitiveIterator.OfDouble =
      Spliterators.iterator(siOfDouble)
    assertNotNull("Null array.spliterator", piOfDouble)

    /* Create spliterator with characteristics of 0, then check
     * that the spliterator always reports SIZED and SUBSIZED, unless
     * CONCURRENT, as documented.
     *
     * Someday have a similar Test specifying CONCURRENT.
     */
    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfDouble = Spliterators.spliterator(
      piOfDouble,
      expectedSize,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e,
        0.0001
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize, // on JVM estimateSize always returns initial expectedSize
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e,
        0.0001
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    // on JVM estimateSize always returns initial expectedSize
    assertEquals(
      "forEachRemaining estimateSize",
      expectedSize,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorFromPrimitiveIteratorOfInt(): Unit = {
    type T = Int
    val expectedElements = Array(
      0, 1, 2, 3, 4, 5, 6
    )
    val expectedSize = expectedElements.size

    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val siOfInt: Spliterator.OfInt = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", siOfInt)

    // Let compiler check type returned is expected.
    val piOfInt: PrimitiveIterator.OfInt = Spliterators.iterator(siOfInt)
    assertNotNull("Null array.spliterator", piOfInt)

    /* Create spliterator with characteristics of 0, then check
     * that the spliterator always reports SIZED and SUBSIZED, unless
     * CONCURRENT, as documented.
     *
     * Someday have a similar Test specifying CONCURRENT.
     */

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfInt = Spliterators.spliterator(
      piOfInt,
      expectedSize,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize, // on JVM estimateSize always returns initial expectedSize
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    // on JVM estimateSize always returns initial expectedSize
    assertEquals(
      "forEachRemaining estimateSize",
      expectedSize,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorFromPrimitiveIteratorOfLong(): Unit = {
    type T = Long
    val expectedElements = Array(
      0L, 1L, 2L, 3L, 4L, 5L, 6L
    )
    val expectedSize = expectedElements.size

    val requiredPresent = Seq(Spliterator.SIZED, Spliterator.SUBSIZED)
    val requiredAbsent = Seq(
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val siOfLong: Spliterator.OfLong = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", siOfLong)

    // Let compiler check type returned is expected.
    val piOfLong: PrimitiveIterator.OfLong = Spliterators.iterator(siOfLong)
    assertNotNull("Null array.spliterator", piOfLong)

    /* Create spliterator with characteristics of 0, then check
     * that the spliterator always reports SIZED and SUBSIZED, unless
     * CONCURRENT, as documented.
     *
     * Someday have a similar Test specifying CONCURRENT.
     */

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfLong = Spliterators.spliterator(
      piOfLong,
      expectedSize,
      0
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      expectedSize, // on JVM estimateSize always returns initial expectedSize
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    // on JVM estimateSize always returns initial expectedSize
    assertEquals(
      "forEachRemaining estimateSize",
      expectedSize,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorUnknownSizeFromIteratorType(): Unit = {
    type T = String
    val expectedElements = Array(
      "pride",
      "greed",
      "wrath",
      "envy",
      "lust",
      "gluttony",
      "sloth"
    )

    val coll = TrivialImmutableCollection(expectedElements: _*)
    assertEquals(expectedElements.size, coll.size())

    val requiredPresent = Seq.empty[Int]
    val requiredAbsent = Seq(
      Spliterator.SIZED,
      Spliterator.SUBSIZED,
      Spliterator.SORTED // guard getComparator() throw
    )

    /* Create spliterator specifying SIZED and SUBSIZED then check
     * that the spliterator always reports them as absent, as documented.
     */

    // Let compiler check type returned is expected.
    val spliter: Spliterator[T] = Spliterators.spliteratorUnknownSize(
      coll.iterator,
      requiredAbsent.take(2).fold(0)((x, y) => x | y)
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", Long.MaxValue, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1, // By definition, size is Unknown.
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    assertEquals(
      "forEachRemaining estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorUnknownSizeFromPrimitiveIteratorOfDouble(): Unit = {
    type T = Double
    val expectedElements = Array(
      0.0, 10.1, 20.2, 30.3, 44.4, 55.5, 66.6
    )

    val requiredPresent = Seq.empty[Int]
    val requiredAbsent = Seq(
      Spliterator.SIZED,
      Spliterator.SUBSIZED,
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val siOfDouble: Spliterator.OfDouble = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", siOfDouble)

    // Let compiler check type returned is expected.
    val piOfDouble: PrimitiveIterator.OfDouble =
      Spliterators.iterator(siOfDouble)
    assertNotNull("Null array.spliterator", piOfDouble)

    /* Create spliterator specifying SIZED and SUBSIZED then check
     * that the spliterator always reports them as absent, as documented.
     */

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfDouble = Spliterators.spliteratorUnknownSize(
      piOfDouble,
      requiredAbsent.take(2).fold(0)((x, y) => x | y)
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", Long.MaxValue, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1, // By definition, size is Unknown.
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e,
        0.0001
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e,
        0.0001
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    assertEquals(
      "forEachRemaining estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorUnknownSizeFromPrimitiveIteratorOfInt(): Unit = {
    type T = Int
    val expectedElements = Array(
      0, 1, 2, 3, 4, 5, 6
    )

    val requiredPresent = Seq.empty[Int]
    val requiredAbsent = Seq(
      Spliterator.SIZED,
      Spliterator.SUBSIZED,
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val siOfInt: Spliterator.OfInt = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", siOfInt)

    // Let compiler check type returned is expected.
    val piOfInt: PrimitiveIterator.OfInt = Spliterators.iterator(siOfInt)
    assertNotNull("Null array.spliterator", piOfInt)

    /* Create spliterator specifying SIZED and SUBSIZED then check
     * that the spliterator always reports them as absent, as documented.
     */

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfInt = Spliterators.spliteratorUnknownSize(
      piOfInt,
      requiredAbsent.take(2).fold(0)((x, y) => x | y)
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", Long.MaxValue, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1, // By definition, size is Unknown.
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    assertEquals(
      "forEachRemaining estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )
  }

  @Test def spliteratorUnknownSizeFromPrimitiveIteratorOfLong(): Unit = {
    type T = Long
    val expectedElements = Array(
      0L, 1L, 2L, 3L, 4L, 5L, 6L
    )

    val requiredPresent = Seq.empty[Int]
    val requiredAbsent = Seq(
      Spliterator.SIZED,
      Spliterator.SUBSIZED,
      Spliterator.SORTED // guard getComparator() throw
    )

    // Let compiler check type returned is expected.
    val siOfLong: Spliterator.OfLong = Spliterators.spliterator(
      expectedElements,
      0
    )
    assertNotNull("Null array.spliterator", siOfLong)

    // Let compiler check type returned is expected.
    val piOfLong: PrimitiveIterator.OfLong =
      Spliterators.iterator(siOfLong)
    assertNotNull("Null array.spliterator", piOfLong)

    /* Create spliterator specifying SIZED and SUBSIZED then check
     * that the spliterator always reports them as absent, as documented.
     */

    // Let compiler check type returned is expected.
    val spliter: Spliterator.OfLong = Spliterators.spliteratorUnknownSize(
      piOfLong,
      requiredAbsent.take(2).fold(0)((x, y) => x | y)
    )
    assertNotNull("Null array.spliterator", spliter)

    // spliterator should have required characteristics and no others.
    verifyCharacteristics(spliter, requiredPresent, requiredAbsent)

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", Long.MaxValue, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1, // By definition, size is Unknown.
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    var count = 0

    spliter.tryAdvance((e: T) => {
      assertEquals(
        s"tryAdvance contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })

    assertEquals(
      "tryAdvance estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )

    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining", expectedElements.size, count)
    assertEquals(
      "forEachRemaining estimateSize",
      Long.MaxValue,
      spliter.estimateSize()
    )
  }
}
