// Ported form Scala.js, revision: 38cf515, dated 26 Aug 2021
package org.scalanative.testsuite.javalib.lang.constant

import java.lang.constant.ConstantDesc

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform.executingInJVM

class ConstantDescTest {

  @Test def knownConstantDescs(): Unit = {
    def test(expected: Boolean, value: Any): Unit =
      assertEquals("" + value, expected, value.isInstanceOf[ConstantDesc])

    test(true, 100000)
    test(true, 5L)
    test(true, 1.5f)
    test(true, 1.4)
    test(true, "foo")

    // In Scala.js Byte and Short is transitively ConstantDesc,
    // in Scala Native we match JVM behaviour
    test(false, 5.toByte)
    test(false, 300.toShort)
    test(false, false)
    test(false, 'A')
    test(false, null)
    test(false, ())
    test(false, List(5))
  }

  @Test
  // Scala.js issue #4545
  def inferConstableOrConstantDesc(): Unit = {
    /* Depending on the JDK version used to compile this test, the types
     * inferred for the arrays will change. On JDK 12+, they will involve
     * Constable and/or ConstantDesc.
     */

    // On JDK 12+, both Constable and ConstantDesc
    val b = Array("foo", java.lang.Integer.valueOf(5))
    assertEquals(2, b.length)
    assertEquals("foo", b(0))
    assertEquals(5, b(1))

    // On JDK 15+, both Constable but Boolean is not a ConstantDesc
    val a = Array("foo", java.lang.Boolean.TRUE)
    assertEquals(2, a.length)
    assertEquals("foo", a(0))
    assertEquals(true, a(1))
  }

}
