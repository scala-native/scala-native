package scala

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.runtime.BoxedUnitArray

class IsInstanceOfTest {

  @Test def expectsNewAnyRefAsInstanceOfAnyRefShouldSucceed(): Unit = {
    (new AnyRef).asInstanceOf[AnyRef]
  }

  @Test def expectsAnyRefIsInstanceOfStringEqEqFalse(): Unit = {
    val anyRef = new AnyRef
    assertFalse(anyRef.isInstanceOf[String])
  }

  @Test def expectsLiteralNullIsInstanceOfStringEqEqFalse(): Unit = {
    assertFalse(null.isInstanceOf[String])
  }

  @Test def expectsEmptyStringIsInstanceOfStringEqEqTrue(): Unit = {
    assertTrue("".isInstanceOf[String])
  }

  @Test def expectsEmptyStringIsInstanceOfStringEqEqTrueForEmptyString()
      : Unit = {
    assertIsInstanceOfString("", "")
  }

  @Test def expectsNullIsInstanceOfStringEqEqFalseFroNullString(): Unit = {
    assertNullIsNotInstanceOfString(null, null)
  }

  def assertNullIsNotInstanceOfString(a: AnyRef, b: AnyRef): Unit = {
    assertFalse(a.isInstanceOf[String])
    assertFalse(b.isInstanceOf[String])
  }

  def assertIsInstanceOfString(a: AnyRef, b: AnyRef): Unit = {
    assertTrue(a.isInstanceOf[String])
    assertTrue(b.isInstanceOf[String])
  }

  @Test def boxedUnitArrayIsArrayOfObjects(): Unit = {
    val arr: Array[_] = Array.empty[Unit]
    assertTrue("elems are boxed", arr.getClass == classOf[BoxedUnitArray])
    assertTrue("is Array[Unit]", arr.isInstanceOf[Array[Unit]])
    assertTrue("is Array[Object]", arr.isInstanceOf[Array[Object]])
    assertTrue("is Array[AnyRef]", arr.isInstanceOf[Array[AnyRef]])
  }

}
