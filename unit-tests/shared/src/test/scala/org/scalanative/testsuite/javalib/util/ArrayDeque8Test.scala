/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util

import java.util.{ArrayDeque, Collections, Spliterator}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArrayDeque8Test extends JSR166Test {
  import JSR166Test._

  @Test def testSpliterator_getComparator(): Unit = {
    assertThrows(
      classOf[IllegalStateException],
      new ArrayDeque[Item]().spliterator().getComparator()
    )
  }

  @Test def testSpliterator_characteristics(): Unit = {
    val q = new ArrayDeque[Item]()
    val s = q.spliterator()
    val characteristics = s.characteristics()
    val required =
      Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED |
        Spliterator.SUBSIZED
    mustEqual(required, characteristics & required)
    assertTrue(s.hasCharacteristics(required))
    mustEqual(
      0,
      characteristics &
        (Spliterator.CONCURRENT | Spliterator.DISTINCT |
          Spliterator.IMMUTABLE | Spliterator.SORTED)
    )
  }

  @Test def testHugeCapacity(): Unit = {
    if (!(testImplementationDetails && expensiveTests)) return

    val e = fortytwo.asInstanceOf[Object]
    val maxArraySize = Integer.MAX_VALUE - 8

    assertThrows(
      classOf[OutOfMemoryError],
      new ArrayDeque[Item](Integer.MAX_VALUE)
    )

    {
      val q = new ArrayDeque[Object](maxArraySize - 1)
      mustEqual(0, q.size())
      assertTrue(q.isEmpty())
    }

    {
      val q = new ArrayDeque[Object]()
      assertTrue(q.addAll(Collections.nCopies(maxArraySize - 3, e)))
      mustEqual(e, q.peekFirst())
      mustEqual(e, q.peekLast())
      mustEqual(maxArraySize - 3, q.size())
      q.addFirst(zero)
      q.addLast(one)
      mustEqual(zero, q.peekFirst())
      mustEqual(one, q.peekLast())
      mustEqual(maxArraySize - 1, q.size())

      val smallish = new ArrayDeque[Object](
        Collections.nCopies(Integer.MAX_VALUE - q.size() + 1, e)
      )
      assertThrows(classOf[IllegalStateException], q.addAll(q))
      assertThrows(classOf[IllegalStateException], q.addAll(smallish))
      assertThrows(classOf[IllegalStateException], smallish.addAll(q))
    }
  }
}
