package scala

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scalanative.junit.utils.AssertThrows.assertThrows

import scala.scalanative.buildinfo.ScalaNativeBuildInfo.scalaVersion

class AsInstanceOfTest {
  val isScala211 = scalaVersion.startsWith("2.11.")

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
    assertThrows(classOf[NullPointerException], anyNull.asInstanceOf[Nothing])
  }

  @Test def nullAsInstanceOfUnitEqNull(): Unit = {
    assumeTrue(isScala211)
    assertTrue(anyNull.asInstanceOf[Unit] == anyNull)
  }

  @Test def nullAsInstanceOfUnitNotEqNull(): Unit = {
    assumeFalse(isScala211)
    assertTrue(anyNull.asInstanceOf[Unit] != anyNull)
  }

  @Test def any42AsInstanceOfObject(): Unit = {
    assertTrue(any42.asInstanceOf[Object] == 42)
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

  @Test def any42AsInstanceOfUnitThrows(): Unit = {
    assumeTrue(isScala211)
    assertThrows(classOf[ClassCastException], any42.asInstanceOf[Unit])
  }

  @Test def any42AsInstanceOfUnitNotNull(): Unit = {
    assumeFalse(isScala211)
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

  @Test def cAsInstanceOfUnitThrows(): Unit = {
    assumeTrue(isScala211)
    assertThrows(classOf[ClassCastException], anyC.asInstanceOf[Unit])
  }

  @Test def cAsInstanceOfUnitNotNull(): Unit = {
    assumeFalse(isScala211)
    assertNotNull(c.asInstanceOf[Unit])
  }
}
