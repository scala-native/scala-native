/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.Spliterator
import java.util.concurrent.LinkedBlockingDeque

import org.junit.Assert._
import org.junit.Test

class LinkedBlockingDeque8Test extends JSR166Test {
  import JSR166Test._

  /** Spliterator.getComparator always throws IllegalStateException */
  @Test def testSpliterator_getComparator(): Unit = {
    assertThrows(
      classOf[IllegalStateException],
      () => new LinkedBlockingDeque[Item]().spliterator().getComparator()
    )
  }

  /** Spliterator characteristics are as advertised */
  @Test def testSpliterator_characteristics(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
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
