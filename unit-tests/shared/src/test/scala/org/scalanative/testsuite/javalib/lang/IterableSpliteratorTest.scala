package org.scalanative.testsuite.javalib.lang

import java.util.Spliterator
import java.nio.file.{Path, Paths}

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class IterableSpliteratorTest {
  /* nio.Path extends Iterable and does not override Iterable's default
   * spliterator() method. Use that knowledge to test the default
   * implementation of Iterable#spliterator.
   *
   * Do not use a class in the Collection hierarchy because Collection
   * overrides the Iterable#spliterator method under test.
   */
  @Test def defaultSpliteratorShouldBeWellFormed(): Unit = {

    // Let compiler check type returned is as expected.
    val spliter: Spliterator[Path] = Paths.get(".").spliterator()
    assertNotNull("Null coll.spliterator", spliter)

    assertEquals("estimateSize", Long.MaxValue, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      -1, // spliterator is known to not have SIZED characteristic
      spliter.getExactSizeIfKnown()
    )

    // Default method always reports NO characteristics set.
    assertEquals(
      "characteristics",
      0,
      spliter.characteristics()
    )

    assertThrows(classOf[IllegalStateException], spliter.getComparator())

    // Check that both the count is right and that each element is as expected.

    var count = 0

    spliter.forEachRemaining((p: Path) => count += 1)

    assertEquals("forEachRemaining size", 1, count)

  }
}
