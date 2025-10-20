package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.unsigned.*
import scalanative.libc.stdlib.malloc
import java.lang.Long.toHexString

class PtrBoxingTest {
  import PtrBoxingTest.*
  var any: Any = null

  @noinline lazy val nullPtr: Ptr[Byte] = null
  @noinline lazy val ptr: Ptr[Byte] = malloc(64)
  @noinline lazy val ptr2: Ptr[Byte] = malloc(64L)

  @noinline def f[T](x: T): T = x
  @noinline def cond(): Boolean = true
  @noinline def retPtrAsAny(): Any = ptr
  @noinline def retPtrAsT[T](): T = ptr.asInstanceOf[T]

  @Test def returnAsAny(): Unit = {
    assertTrue(retPtrAsAny() == ptr)
  }

  @Test def returnAsT(): Unit = {
    assertTrue(retPtrAsT[Ptr[Byte]]() == ptr)
  }

  @Test def storeToAnyField(): Unit = {
    any = ptr
  }

  @Test def readFromAnyField(): Unit = {
    any = ptr
    assertTrue(any.asInstanceOf[Ptr[Byte]] == ptr)
    any = null
    assertTrue(any.asInstanceOf[Ptr[Byte]] == null)
  }

  @Test def storeToAnyLocal(): Unit = {
    var local: Any = null
    local = ptr
  }

  @Test def loadFromAnyLocal(): Unit = {
    var local: Any = ptr
    assertTrue(local == ptr)
  }

  @Test def lubWithObject(): Unit = {
    val lub =
      if (cond()) {
        ptr
      } else {
        new Object
      }
    assertTrue(lub == ptr)
  }

  @Test def storeToArray(): Unit = {
    val arr = new Array[Ptr[Byte]](1)
    arr(0) = ptr
  }

  @Test def readFromArray(): Unit = {
    val arr = Array(ptr)
    assertTrue(arr(0) == ptr)
  }

  @Test def passToGenericFunction(): Unit = {
    assertTrue(f(ptr) == ptr)
  }

  @Test def nullAsInstanceOfPtr(): Unit = {
    val nullPtr: Ptr[Byte] = null
    assertTrue(null.asInstanceOf[Ptr[Byte]] == nullPtr)
  }

  @Test def nullCastPtr(): Unit = {
    val nullPtr: Ptr[Byte] = null
    val nullRef: Object = null
    assertTrue(nullRef.asInstanceOf[Ptr[Byte]] == nullPtr)
  }

  @Test def hashCodeOnPtr(): Unit = {
    assertThrows(classOf[NullPointerException], nullPtr.hashCode)
    assertTrue(ptr.hashCode == ptr.toLong.hashCode)
    assertTrue(ptr2.hashCode == ptr2.toLong.hashCode)
  }

  @Test def equalsOnSameBox(): Unit = {
    val boxedPtr: Object = ptr
    assertTrue(boxedPtr.equals(boxedPtr))
  }

  @Test def equalsOnDifferentBoxes(): Unit = {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = ptr2
    assertFalse(boxedPtr1.equals(boxedPtr2))
  }

  @Test def equalsOnBoxAndNull(): Unit = {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = null
    assertFalse(boxedPtr1.equals(boxedPtr2))
  }

  @Test def scalaEqualsOnSameBox(): Unit = {
    val boxedPtr: Object = ptr
    assertTrue(boxedPtr == boxedPtr)
    assertFalse((boxedPtr != boxedPtr))
  }

  @Test def scalaEqualsOnDifferentBoxes(): Unit = {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = ptr2
    assertFalse((boxedPtr1 == boxedPtr2))
    assertTrue(boxedPtr1 != boxedPtr2)
  }

  @Test def scalaEqualsOnBoxAndNull(): Unit = {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = null
    assertFalse((boxedPtr1 == boxedPtr2))
    assertTrue(boxedPtr1 != boxedPtr2)
  }

  @Test def referenceIdentityOnSameBox(): Unit = {
    val boxedPtr: Object = ptr
    assertTrue(boxedPtr.eq(boxedPtr))
    assertFalse(boxedPtr.ne(boxedPtr))
  }

  @Test def referenceIdentityOnDifferentBoxes(): Unit = {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = ptr2
    assertFalse(boxedPtr1.eq(boxedPtr2))
    assertTrue(boxedPtr1.ne(boxedPtr2))
  }

  @Test def referenceIdentityOnBoxAndNull(): Unit = {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = null
    assertFalse(boxedPtr1.eq(boxedPtr2))
    assertTrue(boxedPtr1.ne(boxedPtr2))
  }

  @Test def boxedPtrGetClass(): Unit = {
    val boxedPtr: Any = ptr
    assertTrue(boxedPtr.getClass == classOf[Ptr[Byte]])
  }

  @Test def testToString(): Unit = {
    assertThrows(
      classOf[NullPointerException], {
        val nullBoxed: Any = nullPtr
        nullBoxed.toString
      }
    )
    val boxed1: Any = ptr
    assertTrue(boxed1.toString == ("Ptr@" + toHexString(ptr.toLong)))
    val boxed2: Any = ptr2
    assertTrue(boxed2.toString == ("Ptr@" + toHexString(ptr2.toLong)))
  }

  @Test def iterateLinkedList(): Unit = {
    type Cons = CStruct2[Int, Ptr[Byte]]

    def cons(value: Int, next: Ptr[Cons])(implicit z: Zone): Ptr[Cons] = {
      val v = alloc[Cons]()
      v._1 = value
      v._2 = next.asInstanceOf[Ptr[Byte]]
      v
    }

    Zone.acquire { implicit z =>
      val out = collection.mutable.ListBuffer.empty[Int]
      var head = cons(10, cons(20, cons(30, null)))
      while (head != null) {
        out += head._1
        head = head._2.asInstanceOf[Ptr[Cons]]
      }
      assertTrue(out.toList == List(10, 20, 30))
    }
  }

  @Test def loadAndStoreCFuncPtr(): Unit = {
    Zone.acquire { implicit z =>
      val x: Ptr[Functions] = stackalloc[Functions]()
      x._1 = CFuncPtr0.fromScalaFunction(getInt _)
      x._2 = CFuncPtr1.fromScalaFunction(stringLength _)

      val loadedGetInt = x._1
      val loadedStringLength = x._2

      val testStr = toCString("hello_native")
      val expectedInt = 42
      val expectedLength = 12.toUSize

      assertEquals(expectedInt, x._1.apply())
      assertEquals(expectedInt, loadedGetInt())

      assertEquals(expectedLength, x._2.apply(testStr))
      assertEquals(expectedLength, loadedStringLength(testStr))
    }
  }
}

object PtrBoxingTest {
  type Functions = CStruct2[CFuncPtr0[Int], CFuncPtr1[CString, CSize]]
  def getInt(): Int = 42
  def stringLength(str: CString): CSize = libc.string.strlen(str)
}
