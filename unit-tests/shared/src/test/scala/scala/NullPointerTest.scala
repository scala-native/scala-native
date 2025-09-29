package scala

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class NullPointerTest {
  class E extends Exception
  class C { var x: Int = 42; def f(): Int = x }

  @noinline def notNullE: E = new E
  @noinline def nullE: E = null
  @noinline def throwNotNullE: Nothing = throw notNullE
  @noinline def throwNullE: Nothing = throw nullE

  @noinline def nullC: C = null
  @noinline def notNullC: C = new C

  @noinline def notNullArray: Array[Int] = Array(1, 2, 3)
  @noinline def nullArray: Array[Int] = null

  @Test def callMethodOnNonNullObject(): Unit = {
    assertTrue(notNullC.f() == 42)
  }

  @Test def callMethodOnNullObject(): Unit = {
    assertThrows(classOf[NullPointerException], nullC.f())
  }

  @Test def loadFieldOnNonNullObject(): Unit = {
    assertTrue(notNullC.x == 42)
  }

  @Test def loadFieldOnNullObject(): Unit = {
    assertThrows(classOf[NullPointerException], nullC.x)
  }

  @Test def storeFieldOnNonNullObject(): Unit = {
    val c = notNullC
    c.x = 84
    assertTrue(c.x == 84)
  }

  @Test def storeFieldOnNullObject(): Unit = {
    assertThrows(classOf[NullPointerException], nullC.x = 84)
  }

  @Test def loadElementFromNonNullArray(): Unit = {
    assertTrue(notNullArray(0) == 1)
  }

  @Test def loadElementFromNullArray(): Unit = {
    assertThrows(classOf[NullPointerException], nullArray(0))
  }

  @Test def storeElementToNonNullArray(): Unit = {
    val arr = notNullArray
    arr(0) = 42
    assertTrue(arr(0) == 42)
  }

  @Test def storeElementToNullArray(): Unit = {
    val arr = nullArray
    assertThrows(classOf[NullPointerException], arr(0) == 42)
  }

  @Test def loadLengthFromNonNullArray(): Unit = {
    assertTrue(notNullArray.length == 3)
  }

  @Test def loadLengthFromNullArray(): Unit = {
    assertThrows(classOf[NullPointerException], nullArray.length)
  }

  @Test def throwException(): Unit = {
    assertThrows(classOf[E], throwNotNullE)
  }

  @Test def throwNull(): Unit = {
    assertThrows(classOf[NullPointerException], throwNullE)
  }
}
