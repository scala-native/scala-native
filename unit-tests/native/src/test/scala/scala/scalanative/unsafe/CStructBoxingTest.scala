package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.unsigned.*
import scalanative.libc.stdlib.malloc
import java.lang.Long.toHexString

class CStructBoxingTest {
  var any: Any = null

  @noinline lazy val nullStruct: CStruct2[Int, Int] = null
  @noinline lazy val struct: CStruct2[Int, Int] = !malloc(64.toUSize)
    .asInstanceOf[Ptr[CStruct2[Int, Int]]]
  @noinline lazy val struct2: CStruct2[Int, Int] = !malloc(64.toUSize)
    .asInstanceOf[Ptr[CStruct2[Int, Int]]]

  @noinline def f[T](x: T): T = x
  @noinline def cond(): Boolean = true
  @noinline def retArrAsAny(): Any = struct
  @noinline def retArrAsT[T](): T = struct.asInstanceOf[T]

  @Test def returnAsAny(): Unit = {
    assertTrue(retArrAsAny() == struct)
  }

  @Test def returnAsT(): Unit = {
    assertTrue(retArrAsT[CStruct2[Int, Int]]() == struct)
  }

  @Test def storeToAnyField(): Unit = {
    any = struct
  }

  @Test def readFromAnyField(): Unit = {
    any = struct
    assertTrue(any.asInstanceOf[CStruct2[Int, Int]] == struct)
    any = null
    assertTrue(any.asInstanceOf[CStruct2[Int, Int]] == null)
  }

  @Test def storeToAnyLocal(): Unit = {
    var local: Any = null
    local = struct
  }

  @Test def loadFromAnyLocal(): Unit = {
    var local: Any = struct
    assertTrue(local == struct)
  }

  @Test def lubWithObject(): Unit = {
    val lub =
      if (cond()) {
        struct
      } else {
        new Object
      }
    assertTrue(lub == struct)
  }

  @Test def storeToArrayStruct(): Unit = {
    val structay = new Array[CStruct2[Int, Int]](1)
    structay(0) = struct
  }

  @Test def readFromArrayStruct(): Unit = {
    val structay = Array(struct)
    assertTrue(structay(0) == struct)
  }

  @Test def passToGenericFunction(): Unit = {
    assertTrue(f(struct) == struct)
  }

  @Test def nullAsInstanceOfStruct(): Unit = {
    val nullStruct: CStruct2[Int, Int] = null
    assertTrue(null.asInstanceOf[Ptr[Byte]] == nullStruct)
  }

  @Test def nullCastStruct(): Unit = {
    val nullStruct: Ptr[Byte] = null
    val nullRef: Object = null
    assertTrue(nullRef.asInstanceOf[Ptr[Byte]] == nullStruct)
  }

  @Test def hashCodeOnStruct(): Unit = {
    assertThrows(classOf[NullPointerException], nullStruct.hashCode)
    assertTrue(struct.hashCode == struct.at1.toLong.hashCode)
    assertTrue(struct2.hashCode == struct2.at1.toLong.hashCode)
  }

  @Test def equalsOnSameBox(): Unit = {
    val boxed: Object = struct
    assertTrue(boxed.equals(boxed))
  }

  @Test def equalsOnDifferentBoxes(): Unit = {
    val boxed1: Object = struct
    val boxed2: Object = struct2
    assertFalse(boxed1.equals(boxed2))
  }

  @Test def equalsOnBoxAndNull(): Unit = {
    val boxed1: Object = struct
    val boxed2: Object = nullStruct
    assertFalse(boxed1.equals(boxed2))
  }

  @Test def scalaEqualsOnSameBox(): Unit = {
    val boxed: Object = struct
    assertTrue(boxed == boxed)
    assertFalse((boxed != boxed))
  }

  @Test def scalaEqualsOnDifferentBoxes(): Unit = {
    val boxed1: Object = struct
    val boxed2: Object = struct2
    assertFalse((boxed1 == boxed2))
    assertTrue(boxed1 != boxed2)
  }

  @Test def scalaEqualsOnBoxAndNull(): Unit = {
    val boxed1: Object = struct
    val boxed2: Object = nullStruct
    assertFalse((boxed1 == boxed2))
    assertTrue(boxed1 != boxed2)
  }

  @Test def referenceIdentityOnSameBox(): Unit = {
    val boxed: Object = struct
    assertTrue(boxed.eq(boxed))
    assertFalse(boxed.ne(boxed))
  }

  @Test def referenceIdentityOnDifferentBoxes(): Unit = {
    val boxed1: Object = struct
    val boxed2: Object = struct2
    assertFalse(boxed1.eq(boxed2))
    assertTrue(boxed1.ne(boxed2))
  }

  @Test def referenceIdentityOnBoxAndNull(): Unit = {
    val boxed1: Object = struct
    val boxed2: Object = null
    assertFalse(boxed1.eq(boxed2))
    assertTrue(boxed1.ne(boxed2))
  }

  @Test def boxedStructGetClass(): Unit = {
    val boxed: Any = struct
    assertTrue(boxed.getClass == classOf[CStruct2[?, ?]])
  }

  @Test def testToString(): Unit = {
    assertThrows(
      classOf[NullPointerException], {
        val nullBoxed: Any = nullStruct
        nullBoxed.toString
      }
    )
    val boxed1: Any = struct
    assertTrue(
      boxed1.toString == ("CStruct2@" + toHexString(struct.at1.toLong))
    )
    val boxed2: Any = struct2
    assertTrue(
      boxed2.toString == ("CStruct2@" + toHexString(struct2.at1.toLong))
    )
  }
}
