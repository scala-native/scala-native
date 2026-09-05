/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.Spliterator
import java.util.concurrent.LinkedBlockingQueue

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class LinkedBlockingQueue8Test extends JSR166Test {
  import JSR166Test._

  @Test def testSpliterator_getComparator(): Unit = {
    assertThrows(
      classOf[IllegalStateException],
      new LinkedBlockingQueue[Item]().spliterator().getComparator()
    )
  }

  @Test def testSpliterator_characteristics(): Unit = {
    val q = new LinkedBlockingQueue[Item]()
    val s = q.spliterator()
    val characteristics = s.characteristics()
    val required =
      Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED
    mustEqual(required, characteristics & required)
    assertTrue(s.hasCharacteristics(required))
    mustEqual(
      0,
      characteristics &
        (Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.SORTED)
    )
  }
}
