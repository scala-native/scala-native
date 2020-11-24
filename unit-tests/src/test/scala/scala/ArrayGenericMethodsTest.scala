package scala

import org.junit.Test
import org.junit.Assert.{assertEquals, _}

/** Tests for generic array methods overridden in ScalaRunTime */
class ArrayGenericMethodsTest {

  val defaultLength = 2

  val byteArray   = Array(1.toByte, 2.toByte)
  val charArray   = Array('a', 'b')
  val shortArray  = Array(1.toShort, 2.toShort)
  val intArray    = Array(1, 2)
  val longArray   = Array(1L, 2L)
  val floatArray  = Array(1.0f, 2.0f)
  val doubleArray = Array(1.0, 2.0)

  val booleanArray = Array(false, true)
  val unitArray    = Array((), ())
  val objectArray  = Array(new Object, "hello")

  @Test
  def shouldSupportGenericLength(): Unit = {
    def genericLength[T](a: Array[T]): Int =
      scala.runtime.ScalaRunTime.array_length(a)

    assertEquals(defaultLength, genericLength(byteArray))
    assertEquals(defaultLength, genericLength(charArray))
    assertEquals(defaultLength, genericLength(shortArray))
    assertEquals(defaultLength, genericLength(intArray))
    assertEquals(defaultLength, genericLength(longArray))

    assertEquals(defaultLength, genericLength(floatArray))
    assertEquals(defaultLength, genericLength(doubleArray))
    assertEquals(defaultLength, genericLength(booleanArray))

    assertEquals(defaultLength, genericLength(unitArray))
    assertEquals(defaultLength, genericLength(objectArray))
  }

  @Test
  def shouldSupportGenericClone(): Unit = {
    def testCloned[T](array: Array[T]): Unit = {
      val clone = array.clone()
      assertFalse("same object", array eq clone)
      assertTrue("diff elements", array.corresponds(clone)(_ == _))
    }

    testCloned(byteArray)
    testCloned(charArray)
    testCloned(shortArray)
    testCloned(intArray)
    testCloned(longArray)
    testCloned(floatArray)
    testCloned(doubleArray)
    testCloned(booleanArray)
    testCloned(unitArray)
    testCloned(objectArray)
  }

  @Test
  def shouldSupportGenericApply(): Unit = {
    assertEquals(2.toByte, byteArray(1))
    assertEquals('b', charArray(1))
    assertEquals(2.toShort, shortArray(1))
    assertEquals(2, shortArray(1))
    assertEquals(2L, longArray(1))
    assertEquals(2.0f, floatArray(1), 0.0001f)
    assertEquals(2.0, doubleArray(1), 0.0001)
    assertEquals(true, booleanArray(1))
    assertEquals((), unitArray(1))
    assertEquals("hello", objectArray(1))
  }

  @Test
  def shouldSupportGenericUpdate(): Unit = {
    def testUpdate[T](arr: Array[T], newValue: T): Unit = {
      val idx      = 1
      val oldValue = arr(idx)
      arr.update(idx, newValue)
      assertNotEquals(oldValue, newValue)
      assertEquals(newValue, arr(idx))
    }

    testUpdate(byteArray, 10.toByte)
    testUpdate(charArray, 'N')
    testUpdate(shortArray, 10.toShort)
    testUpdate(intArray, 10)
    testUpdate(longArray, 10L)
    testUpdate(floatArray, 10.0f)
    testUpdate(doubleArray, 10.0)
    testUpdate(booleanArray, false)
    testUpdate(objectArray, "native")
  }
}
