// Ported form Scala.js, revision: 38cf515, dated 26 Aug 2021

package org.scalanative.testsuite.javalib.lang.constant

import org.junit.Test
import org.junit.Assert.*

import java.lang.constant.Constable

class ConstableTest {

  @Test def knownConstables(): Unit = {
    def test(expected: Boolean, value: Any): Unit = {
      assertEquals("" + value, expected, value.isInstanceOf[Constable])
    }

    test(true, false)
    test(true, 'A')
    test(true, 5.toByte)
    test(true, 300.toShort)
    test(true, 100000)
    test(true, 5L)
    test(true, 1.5f)
    test(true, 1.4)
    test(true, "foo")

    test(false, null)
    test(false, ())
    test(false, List(5))
  }

}
