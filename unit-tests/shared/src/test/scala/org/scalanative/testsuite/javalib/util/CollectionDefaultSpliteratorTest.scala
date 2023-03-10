package org.scalanative.testsuite.javalib.util

import java.util.Spliterator

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CollectionDefaultSpliteratorTest {
  @Test def defaultSpliteratorShouldBeWellFormed(): Unit = {
    val expectedElements = Array(
      "Aiopis",
      "Antheia",
      "Donakis",
      "Calypso",
      "Mermesa",
      "Nelisa",
      "Tara"
    )

    val expectedSize = expectedElements.size
    val foundElements = new Array[String](expectedSize)

    val coll = TrivialImmutableCollection(expectedElements: _*)
    assertEquals(expectedSize, coll.size())

    val spliter = coll.spliterator()
    assertNotNull("Null coll.spliterator", spliter)

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )

    val required = Spliterator.SIZED | Spliterator.SUBSIZED

    assertEquals(
      "characteristics",
      required,
      spliter.characteristics() & required
    )

    assertTrue("hasCharacteristics", spliter.hasCharacteristics(required))

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    // Check that both the count is right and that each element is as expected.

    var count = 0

    // forEachRemaining() exercises tryAdvance() internally.
    spliter.forEachRemaining((str: String) => {
      foundElements(count) = str
      count += 1
    })

    assertEquals("forEachRemaining size", expectedSize, count)

    // Are contents equal?
    for (j <- 0 until expectedSize)
      assertEquals(
        s"forEachRemaining contents(${j})",
        expectedElements(j),
        foundElements(j)
      )
  }
}
