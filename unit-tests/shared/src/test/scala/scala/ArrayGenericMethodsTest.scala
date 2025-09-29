package scala

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/** Tests for generic array methods overridden in ScalaRunTime */
class ArrayGenericMethodsTest {

  val arrayLength = 2

  val byteArray = Array(1.toByte, 2.toByte)
  val charArray = Array('a', 'b')
  val shortArray = Array(1.toShort, 2.toShort)
  val intArray = Array(1, 2)
  val longArray = Array(1L, 2L)
  val floatArray = Array(1.0f, 2.0f)
  val doubleArray = Array(1.0, 2.0)

  val booleanArray = Array(false, true)
  val unitArray = Array((), ())
  val objectArray = Array(new Object, "hello")

  @Test
  def shouldSupportGenericLength(): Unit = {
    def genericLength[T](a: Array[T]): Int =
      scala.runtime.ScalaRunTime.array_length(a)

    assertEquals(arrayLength, genericLength(byteArray))
    assertEquals(arrayLength, genericLength(charArray))
    assertEquals(arrayLength, genericLength(shortArray))
    assertEquals(arrayLength, genericLength(intArray))
    assertEquals(arrayLength, genericLength(longArray))

    assertEquals(arrayLength, genericLength(floatArray))
    assertEquals(arrayLength, genericLength(doubleArray))
    assertEquals(arrayLength, genericLength(booleanArray))

    assertEquals(arrayLength, genericLength(unitArray))
    assertEquals(arrayLength, genericLength(objectArray))
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
      val idx = 1
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

  @Test
  def shouldThrowExceptionOnAccessOutOfBounds(): Unit = {
    def testBounds[T](arr: Array[T]): Unit = {
      val elem = arr(0)

      List(-1, arrayLength + 1).foreach { idx =>
        assertThrows(classOf[ArrayIndexOutOfBoundsException], arr(idx))
        assertThrows(
          classOf[ArrayIndexOutOfBoundsException],
          arr.update(idx, elem)
        )
      }

      val targetArray = arr.clone()
      List(
        (-1, 0, arrayLength),
        (0, -1, arrayLength),
        (0, 0, -1),
        (arrayLength + 1, 0, arrayLength),
        (0, arrayLength + 1, arrayLength),
        (0, 0, arrayLength + 1)
      ).foreach {
        case (fromPos, toPos, length) =>
          assertThrows(
            classOf[ArrayIndexOutOfBoundsException],
            System.arraycopy(arr, fromPos, targetArray, toPos, length)
          )
      }
    }

    testBounds(byteArray)
    testBounds(charArray)
    testBounds(shortArray)
    testBounds(intArray)
    testBounds(longArray)
    testBounds(floatArray)
    testBounds(doubleArray)
    testBounds(booleanArray)
    testBounds(unitArray)
    testBounds(objectArray)
  }
}
