package scala.scalanative
package unsafe

import scalanative.unsafe.Nat._
import scalanative.runtime.toRawPtr
import scalanative.libc.stdlib.malloc
import java.lang.Long.toHexString

object CArrayBoxingSuite extends tests.Suite {
  var any: Any = null

  @noinline lazy val nullArr: CArray[Byte, _4] = null
  @noinline lazy val arr: CArray[Byte, _4] = !malloc(64)
    .asInstanceOf[Ptr[CArray[Byte, _4]]]
  @noinline lazy val arr2: CArray[Byte, _4] = !malloc(64)
    .asInstanceOf[Ptr[CArray[Byte, _4]]]

  @noinline def f[T](x: T): T      = x
  @noinline def cond(): Boolean    = true
  @noinline def retArrAsAny(): Any = arr
  @noinline def retArrAsT[T](): T  = arr.asInstanceOf[T]

  test("return as any") {
    assert(retArrAsAny() == arr)
  }

  test("return as T") {
    assert(retArrAsT[CArray[Byte, _4]]() == arr)
  }

  test("store to any field") {
    any = arr
  }

  test("read from any field") {
    any = arr
    assert(any.asInstanceOf[CArray[Byte, _4]] == arr)
    any = null
    assert(any.asInstanceOf[CArray[Byte, _4]] == null)
  }

  test("store to any local") {
    var local: Any = null
    local = arr
  }

  test("load from any local") {
    var local: Any = arr
    assert(local == arr)
  }

  test("lub with object") {
    val lub =
      if (cond()) {
        arr
      } else {
        new Object
      }
    assert(lub == arr)
  }

  test("store to array") {
    val array = new Array[CArray[Byte, _4]](1)
    array(0) = arr
  }

  test("read from array") {
    val array = Array(arr)
    assert(array(0) == arr)
  }

  test("pass to generic function") {
    assert(f(arr) == arr)
  }

  test("null as instance of arr") {
    val nullArr: CArray[Byte, _4] = null
    assert(null.asInstanceOf[Ptr[Byte]] == nullArr)
  }

  test("null cast arr") {
    val nullArr: Ptr[Byte] = null
    val nullRef: Object    = null
    assert(nullRef.asInstanceOf[Ptr[Byte]] == nullArr)
  }

  test("hash code on arr") {
    assertThrows[NullPointerException] {
      nullArr.hashCode
    }
    assert(arr.hashCode == arr.at(0).toLong.hashCode)
    assert(arr2.hashCode == arr2.at(0).toLong.hashCode)
  }

  test("equals on same box") {
    val boxed: Object = arr
    assert(boxed.equals(boxed))
  }

  test("equals on different boxes") {
    val boxed1: Object = arr
    val boxed2: Object = arr2
    assert(!boxed1.equals(boxed2))
  }

  test("equals on box and null") {
    val boxed1: Object = arr
    val boxed2: Object = nullArr
    assert(!boxed1.equals(boxed2))
  }

  test("scala equals on same box") {
    val boxed: Object = arr
    assert(boxed == boxed)
    assert(!(boxed != boxed))
  }

  test("scala equals on different boxes") {
    val boxed1: Object = arr
    val boxed2: Object = arr2
    assert(!(boxed1 == boxed2))
    assert(boxed1 != boxed2)
  }

  test("scala equals on box and null") {
    val boxed1: Object = arr
    val boxed2: Object = nullArr
    assert(!(boxed1 == boxed2))
    assert(boxed1 != boxed2)
  }

  test("reference identity on same box") {
    val boxed: Object = arr
    assert(boxed.eq(boxed))
    assert(!boxed.ne(boxed))
  }

  test("reference identity on different boxes") {
    val boxed1: Object = arr
    val boxed2: Object = arr2
    assert(!boxed1.eq(boxed2))
    assert(boxed1.ne(boxed2))
  }

  test("reference identity on box and null") {
    val boxed1: Object = arr
    val boxed2: Object = null
    assert(!boxed1.eq(boxed2))
    assert(boxed1.ne(boxed2))
  }

  test("boxed arr get class") {
    val boxed: Any = arr
    assert(boxed.getClass == classOf[CArray[_, _]])
  }

  test("to string") {
    assertThrows[NullPointerException] {
      val nullBoxed: Any = nullArr
      nullBoxed.toString
    }
    val boxed1: Any = arr
    assert(boxed1.toString == ("CArray@" + toHexString(arr.at(0).toLong)))
    val boxed2: Any = arr2
    assert(boxed2.toString == ("CArray@" + toHexString(arr2.at(0).toLong)))
  }
}
