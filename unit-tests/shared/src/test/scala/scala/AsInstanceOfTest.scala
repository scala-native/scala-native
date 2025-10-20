package scala

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.scalanative.buildinfo.ScalaNativeBuildInfo.scalaVersion

class AsInstanceOfTest {
  val isScala3 = scalaVersion.startsWith("3.")

  class C
  val c = new C
  @noinline def anyNull: Any = null
  @noinline def any42: Any = 42
  @noinline def anyC: Any = c

  @Test def nullAsInstanceOfObject(): Unit = {
    assertTrue(anyNull.asInstanceOf[Object] == null)
  }

  @Test def nullAsInstanceOfInt(): Unit = {
    assertTrue(anyNull.asInstanceOf[Int] == 0)
  }

  @Test def nullAsInstanceOfC(): Unit = {
    assertTrue(anyNull.asInstanceOf[C] == null)
  }

  @Test def nullAsInstanceOfNull(): Unit = {
    assertTrue((anyNull.asInstanceOf[Null]: Any) == null)
  }

  @Test def nullAsInstanceOfNothing(): Unit = {
    val expected =
      if (isScala3) classOf[ClassCastException]
      else classOf[NullPointerException]
    assertThrows(expected, anyNull.asInstanceOf[Nothing])
  }

  @Test def nullAsInstanceOfUnitNotEqNull(): Unit = {
    assertTrue(anyNull.asInstanceOf[Unit] != anyNull)
  }

  @Test def any42AsInstanceOfObject(): Unit = {
    assertTrue(any42.asInstanceOf[Object] == java.lang.Integer.valueOf(42))
  }

  @Test def any42AsInstanceOfInt(): Unit = {
    assertTrue(any42.asInstanceOf[Int] == 42)
  }

  @Test def any42asInstanceOfC(): Unit = {
    assertThrows(classOf[ClassCastException], any42.asInstanceOf[C])
  }

  @Test def any42AsInstanceOfNull(): Unit = {
    assertThrows(classOf[ClassCastException], any42.asInstanceOf[Null])
  }

  @Test def any42AsInstanceOfNothing(): Unit = {
    assertThrows(classOf[ClassCastException], any42.asInstanceOf[Nothing])
  }

  @Test def any42AsInstanceOfUnitNotNull(): Unit = {
    assertNotNull(any42.asInstanceOf[Unit])
  }

  @Test def cAsInstanceOfObject(): Unit = {
    assertTrue(anyC.asInstanceOf[Object] == anyC)
  }

  @Test def cAsInstanceOfInt(): Unit = {
    assertThrows(classOf[ClassCastException], anyC.asInstanceOf[Int])
  }

  @Test def cAsInstanceOfC(): Unit = {
    assertTrue(anyC.asInstanceOf[C] == anyC)
  }

  @Test def cAsInstanceOfNull(): Unit = {
    assertThrows(classOf[ClassCastException], anyC.asInstanceOf[Null])
  }

  @Test def cAsInstanceOfNothing(): Unit = {
    assertThrows(classOf[ClassCastException], anyC.asInstanceOf[Nothing])
  }

  @Test def cAsInstanceOfUnitNotNull(): Unit = {
    assertNotNull(c.asInstanceOf[Unit])
  }
}
