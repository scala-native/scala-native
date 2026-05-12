/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util

import java.util.{ArrayList, Arrays}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}

class ArrayListTest extends JSR166Test {
  import JSR166Test._

  @Test def testClone(): Unit = {
    val x = new ArrayList[Item]()
    x.add(one)
    x.add(two)
    x.add(three)

    val y = x.clone().asInstanceOf[ArrayList[Item]]
    assertNotSame(y, x)
    mustEqual(x, y)
    mustEqual(y, x)
    mustEqual(x.size(), y.size())
    mustEqual(x.toString(), y.toString())
    assertTrue(Arrays.equals(x.toArray(), y.toArray()))
    while (!x.isEmpty()) {
      assertFalse(y.isEmpty())
      mustEqual(x.remove(0), y.remove(0))
    }
    assertTrue(y.isEmpty())
  }
}
