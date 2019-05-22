package scala.scalanative
package unsafe

import scalanative.runtime.toRawPtr
import scalanative.libc.stdlib.malloc
import java.lang.Long.toHexString

object CStructBoxingSuite extends tests.Suite {
  var any: Any = null

  @noinline lazy val nullStruct: CStruct2[Int, Int] = null
  @noinline lazy val struct: CStruct2[Int, Int] = !malloc(64)
    .asInstanceOf[Ptr[CStruct2[Int, Int]]]
  @noinline lazy val struct2: CStruct2[Int, Int] = !malloc(64)
    .asInstanceOf[Ptr[CStruct2[Int, Int]]]

  @noinline def f[T](x: T): T      = x
  @noinline def cond(): Boolean    = true
  @noinline def retArrAsAny(): Any = struct
  @noinline def retArrAsT[T](): T  = struct.asInstanceOf[T]

  test("return as any") {
    assert(retArrAsAny() == struct)
  }

  test("return as T") {
    assert(retArrAsT[CStruct2[Int, Int]]() == struct)
  }

  test("store to any field") {
    any = struct
  }

  test("read from any field") {
    any = struct
    assert(any.asInstanceOf[CStruct2[Int, Int]] == struct)
    any = null
    assert(any.asInstanceOf[CStruct2[Int, Int]] == null)
  }

  test("store to any local") {
    var local: Any = null
    local = struct
  }

  test("load from any local") {
    var local: Any = struct
    assert(local == struct)
  }

  test("lub with object") {
    val lub =
      if (cond()) {
        struct
      } else {
        new Object
      }
    assert(lub == struct)
  }

  test("store to structay") {
    val structay = new Array[CStruct2[Int, Int]](1)
    structay(0) = struct
  }

  test("read from structay") {
    val structay = Array(struct)
    assert(structay(0) == struct)
  }

  test("pass to generic function") {
    assert(f(struct) == struct)
  }

  test("null as instance of struct") {
    val nullStruct: CStruct2[Int, Int] = null
    assert(null.asInstanceOf[Ptr[Byte]] == nullStruct)
  }

  test("null cast struct") {
    val nullStruct: Ptr[Byte] = null
    val nullRef: Object       = null
    assert(nullRef.asInstanceOf[Ptr[Byte]] == nullStruct)
  }

  test("hash code on struct") {
    assertThrows[NullPointerException] {
      nullStruct.hashCode
    }
    assert(struct.hashCode == struct.at1.toLong.hashCode)
    assert(struct2.hashCode == struct2.at1.toLong.hashCode)
  }

  test("equals on same box") {
    val boxed: Object = struct
    assert(boxed.equals(boxed))
  }

  test("equals on different boxes") {
    val boxed1: Object = struct
    val boxed2: Object = struct2
    assert(!boxed1.equals(boxed2))
  }

  test("equals on box and null") {
    val boxed1: Object = struct
    val boxed2: Object = nullStruct
    assert(!boxed1.equals(boxed2))
  }

  test("scala equals on same box") {
    val boxed: Object = struct
    assert(boxed == boxed)
    assert(!(boxed != boxed))
  }

  test("scala equals on different boxes") {
    val boxed1: Object = struct
    val boxed2: Object = struct2
    assert(!(boxed1 == boxed2))
    assert(boxed1 != boxed2)
  }

  test("scala equals on box and null") {
    val boxed1: Object = struct
    val boxed2: Object = nullStruct
    assert(!(boxed1 == boxed2))
    assert(boxed1 != boxed2)
  }

  test("reference identity on same box") {
    val boxed: Object = struct
    assert(boxed.eq(boxed))
    assert(!boxed.ne(boxed))
  }

  test("reference identity on different boxes") {
    val boxed1: Object = struct
    val boxed2: Object = struct2
    assert(!boxed1.eq(boxed2))
    assert(boxed1.ne(boxed2))
  }

  test("reference identity on box and null") {
    val boxed1: Object = struct
    val boxed2: Object = null
    assert(!boxed1.eq(boxed2))
    assert(boxed1.ne(boxed2))
  }

  test("boxed struct get class") {
    val boxed: Any = struct
    assert(boxed.getClass == classOf[CStruct2[_, _]])
  }

  test("to string") {
    assertThrows[NullPointerException] {
      val nullBoxed: Any = nullStruct
      nullBoxed.toString
    }
    val boxed1: Any = struct
    assert(boxed1.toString == ("CStruct2@" + toHexString(struct.at1.toLong)))
    val boxed2: Any = struct2
    assert(boxed2.toString == ("CStruct2@" + toHexString(struct2.at1.toLong)))
  }
}
