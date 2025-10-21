package scala.scalanative
package unsafe

import java.lang.Long.toHexString

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.libc.stdlib.malloc
import scalanative.unsafe.Nat._
import scalanative.unsigned._

class CArrayBoxingTest {
  var any: Any = null

  @noinline lazy val nullArr: CArray[Byte, _4] = null
  @noinline lazy val arr: CArray[Byte, _4] = !malloc(64.toUSize)
    .asInstanceOf[Ptr[CArray[Byte, _4]]]
  @noinline lazy val arr2: CArray[Byte, _4] = !malloc(64.toUSize)
    .asInstanceOf[Ptr[CArray[Byte, _4]]]

  @noinline def f[T](x: T): T = x
  @noinline def cond(): Boolean = true
  @noinline def retArrAsAny(): Any = arr
  @noinline def retArrAsT[T](): T = arr.asInstanceOf[T]

  @Test def returnAsAny(): Unit = {
    assertTrue(retArrAsAny() == arr)
  }

  @Test def returnAsT(): Unit = {
    assertTrue(retArrAsT[CArray[Byte, _4]]() == arr)
  }

  @Test def storeToAnyField(): Unit = {
    any = arr
  }

  @Test def readFromAnyField(): Unit = {
    any = arr
    assertTrue(any.asInstanceOf[CArray[Byte, _4]] == arr)
    any = null
    assertTrue(any.asInstanceOf[CArray[Byte, _4]] == null)
  }

  @Test def storeToAnyLocal(): Unit = {
    var local: Any = null
    local = arr
  }

  @Test def loadFromAnyLocal(): Unit = {
    var local: Any = arr
    assertTrue(local == arr)
  }

  @Test def lubWithObject(): Unit = {
    val lub =
      if (cond()) {
        arr
      } else {
        new Object
      }
    assertTrue(lub == arr)
  }

  @Test def storeToArray(): Unit = {
    val array = new Array[CArray[Byte, _4]](1)
    array(0) = arr
  }

  @Test def readFromArray(): Unit = {
    val array = Array(arr)
    assertTrue(array(0) == arr)
  }

  @Test def passToGenericFunction(): Unit = {
    assertTrue(f(arr) == arr)
  }

  @Test def nullAsInstanceOfArr(): Unit = {
    val nullArr: CArray[Byte, _4] = null
    assertTrue(null.asInstanceOf[Ptr[Byte]] == nullArr)
  }

  @Test def nullCastArr(): Unit = {
    val nullArr: Ptr[Byte] = null
    val nullRef: Object = null
    assertTrue(nullRef.asInstanceOf[Ptr[Byte]] == nullArr)
  }

  @Test def hashCodeOnArr(): Unit = {
    assertThrows(classOf[NullPointerException], nullArr.hashCode)
    assertTrue(arr.hashCode == arr.at(0).toLong.hashCode)
    assertTrue(arr2.hashCode == arr2.at(0).toLong.hashCode)
  }

  @Test def equalsOnSameBox(): Unit = {
    val boxed: Object = arr
    assertTrue(boxed.equals(boxed))
  }

  @Test def equalsOnDifferentBoxes(): Unit = {
    val boxed1: Object = arr
    val boxed2: Object = arr2
    assertFalse(boxed1.equals(boxed2))
  }

  @Test def equalsOnBoxAndNull(): Unit = {
    val boxed1: Object = arr
    val boxed2: Object = nullArr
    assertFalse(boxed1.equals(boxed2))
  }

  @Test def scalaEqualsOnSameBox(): Unit = {
    val boxed: Object = arr
    assertTrue(boxed == boxed)
    assertFalse((boxed != boxed))
  }

  @Test def scalaEqualsOnDifferentBoxes(): Unit = {
    val boxed1: Object = arr
    val boxed2: Object = arr2
    assertFalse((boxed1 == boxed2))
    assertTrue(boxed1 != boxed2)
  }

  @Test def scalaEqualsOnBoxAndNull(): Unit = {
    val boxed1: Object = arr
    val boxed2: Object = nullArr
    assertFalse((boxed1 == boxed2))
    assertTrue(boxed1 != boxed2)
  }

  @Test def referenceIdentityOnSameBox(): Unit = {
    val boxed: Object = arr
    assertTrue(boxed.eq(boxed))
    assertFalse(boxed.ne(boxed))
  }

  @Test def referenceIdentityOnDifferentBoxes(): Unit = {
    val boxed1: Object = arr
    val boxed2: Object = arr2
    assertFalse(boxed1.eq(boxed2))
    assertTrue(boxed1.ne(boxed2))
  }

  @Test def referenceIdentityOnBoxAndNull(): Unit = {
    val boxed1: Object = arr
    val boxed2: Object = null
    assertFalse(boxed1.eq(boxed2))
    assertTrue(boxed1.ne(boxed2))
  }

  @Test def boxedArrGetClass(): Unit = {
    val boxed: Any = arr
    assertTrue(boxed.getClass == classOf[CArray[_, _]])
  }

  @Test def testToString(): Unit = {
    assertThrows(
      classOf[NullPointerException], {
        val nullBoxed: Any = nullArr
        nullBoxed.toString
      }
    )
    val boxed1: Any = arr
    assertTrue(boxed1.toString == ("CArray@" + toHexString(arr.at(0).toLong)))
    val boxed2: Any = arr2
    assertTrue(boxed2.toString == ("CArray@" + toHexString(arr2.at(0).toLong)))
  }
}
