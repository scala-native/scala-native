package org.scalanative.testsuite.javalib.util

import java.util.{Arrays, Spliterator}

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Test Arrays.spliterator() methods. They were added for Scala Native after
 * the port from Scala.js.
 *
 * These Tests transitively exercise associated methods from
 * Spliterators and Spliterator.
 */

class ArraysSpliteratorTest {

  // characteristics returned by all javalib Arrays.spliterator() methods.
  val stdRequiredPresentCharacteristics = Seq(
    Spliterator.SIZED,
    Spliterator.SUBSIZED,
    Spliterator.ORDERED,
    Spliterator.IMMUTABLE
  )

  // guard getComparator() throw
  val stdRequiredAbsentCharacteristics = Seq(Spliterator.SORTED)

  @Test def spliteratorOfDoubleFromArray: Unit = {
    type T = Double
    val expectedElements = Array(
      0.0, 10.1, 20.2, 30.3, 44.4, 55.5, 66.6
    )

    val expectedSize = expectedElements.size

    // Let compiler check returned type is as expected.
    val spliter: Spliterator.OfDouble = Arrays.spliterator(expectedElements)
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the size & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(0),
          e,
          0.0001
        )
      )
    )

    var count = 1
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
  }

  @Test def spliteratorOfDoubleFromArrayRange: Unit = {
    type T = Double
    val expectedElements = Array(
      1.0, 10.1, 20.2, 30.3, 44.4, 55.5, 66.6
    )

    val sliceStartIndex = 1
    val sliceEndIndex = 5
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    // Let compiler check returned type is as expected.
    val spliter: Spliterator.OfDouble = Arrays.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex
    )
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(sliceStartIndex),
          e,
          0.0001
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e,
        0.0001
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSliceSize, count)
  }

  @Test def spliteratorOfIntFromArray: Unit = {
    type T = Int
    val expectedElements = Array(
      0, 1, 2, 3, 4, 5, 6
    )

    val expectedSize = expectedElements.size

    // Let compiler check returned type is as expected.
    val spliter: Spliterator.OfInt = Arrays.spliterator(expectedElements)
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the size & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(0),
          e
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSize, count)
  }

  @Test def spliteratorOfIntFromArrayRange: Unit = {
    type T = Int
    val expectedElements = Array(
      1, 11, 22, 33, 44, 55, 66
    )

    val sliceStartIndex = 1
    val sliceEndIndex = 5
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    // Let compiler check returned type is as expected.
    val spliter: Spliterator.OfInt = Arrays.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex
    )
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(sliceStartIndex),
          e
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSliceSize, count)
  }

  @Test def spliteratorOfLongFromArray: Unit = {
    type T = Long
    val expectedElements = Array(
      0L, 1L, 2L, 3L, 4L, 5L, 6L
    )

    val expectedSize = expectedElements.size

    // Let compiler check returned type is as expected.
    val spliter: Spliterator.OfLong = Arrays.spliterator(expectedElements)
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the size & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(0),
          e
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSize, count)
  }

  @Test def spliteratorOfLongFromArrayRange: Unit = {
    type T = Long
    val expectedElements = Array(
      0, 11L, 22L, 33L, 44L, 55L, 66L
    )

    val sliceStartIndex = 1
    val sliceEndIndex = 5
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    // Let compiler check returned type is as expected.
    val spliter: Spliterator.OfLong = Arrays.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex
    )
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(sliceStartIndex),
          e
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSliceSize, count)
  }

  @Test def spliteratorOfTypeFromArray: Unit = {
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

    // Let compiler check returned type is as expected.
    val spliter: Spliterator[T] = Arrays.spliterator(expectedElements)
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the size & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(0),
          e
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSize, count)
  }

  @Test def spliteratorOfTypeFromArrayRange: Unit = {
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

    val sliceStartIndex = 1
    val sliceEndIndex = 5
    val expectedSliceSize = sliceEndIndex - sliceStartIndex

    // Let compiler check returned type is as expected.
    val spliter: Spliterator[T] = Arrays.spliterator(
      expectedElements,
      sliceStartIndex,
      sliceEndIndex
    )
    assertNotNull("Null array.spliterator", spliter)

    // check that spliterator has required characteristics and no others.
    SpliteratorsTest.verifyCharacteristics(
      spliter,
      stdRequiredPresentCharacteristics,
      stdRequiredAbsentCharacteristics
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    assertEquals("estimateSize", expectedSliceSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSliceSize,
      spliter.getExactSizeIfKnown()
    )

    // Check that both the end index & each element seen are as expected.

    assertTrue(
      "tryAdvance itself failed",
      spliter.tryAdvance((e: T) =>
        assertEquals(
          "tryAdvance contents do not match,",
          expectedElements(sliceStartIndex),
          e
        )
      )
    )

    var count = 1
    spliter.forEachRemaining((e: T) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(sliceStartIndex + count),
        e
      )
      count += 1
    })
    assertEquals("forEachRemaining count", expectedSliceSize, count)
  }
}
