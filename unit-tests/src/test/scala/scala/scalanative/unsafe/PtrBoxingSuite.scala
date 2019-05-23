package scala.scalanative
package unsafe

import scalanative.runtime.toRawPtr
import scalanative.libc.stdlib.malloc
import java.lang.Long.toHexString

object PtrBoxingSuite extends tests.Suite {
  var any: Any = null

  @noinline lazy val nullPtr: Ptr[Byte] = null
  @noinline lazy val ptr: Ptr[Byte]     = malloc(64)
  @noinline lazy val ptr2: Ptr[Byte]    = malloc(64)

  @noinline def f[T](x: T): T      = x
  @noinline def cond(): Boolean    = true
  @noinline def retPtrAsAny(): Any = ptr
  @noinline def retPtrAsT[T](): T  = ptr.asInstanceOf[T]

  test("return as any") {
    assert(retPtrAsAny() == ptr)
  }

  test("return as T") {
    assert(retPtrAsT[Ptr[Byte]]() == ptr)
  }

  test("store to any field") {
    any = ptr
  }

  test("read from any field") {
    any = ptr
    assert(any.asInstanceOf[Ptr[Byte]] == ptr)
    any = null
    assert(any.asInstanceOf[Ptr[Byte]] == null)
  }

  test("store to any local") {
    var local: Any = null
    local = ptr
  }

  test("load from any local") {
    var local: Any = ptr
    assert(local == ptr)
  }

  test("lub with object") {
    val lub =
      if (cond()) {
        ptr
      } else {
        new Object
      }
    assert(lub == ptr)
  }

  test("store to array") {
    val arr = new Array[Ptr[Byte]](1)
    arr(0) = ptr
  }

  test("read from array") {
    val arr = Array(ptr)
    assert(arr(0) == ptr)
  }

  test("pass to generic function") {
    assert(f(ptr) == ptr)
  }

  test("null as instance of ptr") {
    val nullPtr: Ptr[Byte] = null
    assert(null.asInstanceOf[Ptr[Byte]] == nullPtr)
  }

  test("null cast ptr") {
    val nullPtr: Ptr[Byte] = null
    val nullRef: Object    = null
    assert(nullRef.asInstanceOf[Ptr[Byte]] == nullPtr)
  }

  test("hash code on ptr") {
    assertThrows[NullPointerException] {
      nullPtr.hashCode
    }
    assert(ptr.hashCode == ptr.toLong.hashCode)
    assert(ptr2.hashCode == ptr2.toLong.hashCode)
  }

  test("equals on same box") {
    val boxedPtr: Object = ptr
    assert(boxedPtr.equals(boxedPtr))
  }

  test("equals on different boxes") {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = ptr2
    assert(!boxedPtr1.equals(boxedPtr2))
  }

  test("equals on box and null") {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = null
    assert(!boxedPtr1.equals(boxedPtr2))
  }

  test("scala equals on same box") {
    val boxedPtr: Object = ptr
    assert(boxedPtr == boxedPtr)
    assert(!(boxedPtr != boxedPtr))
  }

  test("scala equals on different boxes") {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = ptr2
    assert(!(boxedPtr1 == boxedPtr2))
    assert(boxedPtr1 != boxedPtr2)
  }

  test("scala equals on box and null") {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = null
    assert(!(boxedPtr1 == boxedPtr2))
    assert(boxedPtr1 != boxedPtr2)
  }

  test("reference identity on same box") {
    val boxedPtr: Object = ptr
    assert(boxedPtr.eq(boxedPtr))
    assert(!boxedPtr.ne(boxedPtr))
  }

  test("reference identity on different boxes") {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = ptr2
    assert(!boxedPtr1.eq(boxedPtr2))
    assert(boxedPtr1.ne(boxedPtr2))
  }

  test("reference identity on box and null") {
    val boxedPtr1: Object = ptr
    val boxedPtr2: Object = null
    assert(!boxedPtr1.eq(boxedPtr2))
    assert(boxedPtr1.ne(boxedPtr2))
  }

  test("boxed ptr get class") {
    val boxedPtr: Any = ptr
    assert(boxedPtr.getClass == classOf[Ptr[Byte]])
  }

  test("to string") {
    assertThrows[NullPointerException] {
      val nullBoxed: Any = nullPtr
      nullBoxed.toString
    }
    val boxed1: Any = ptr
    assert(boxed1.toString == ("Ptr@" + toHexString(ptr.toLong)))
    val boxed2: Any = ptr2
    assert(boxed2.toString == ("Ptr@" + toHexString(ptr2.toLong)))
  }

  test("iterate linked list") {
    type Cons = CStruct2[Int, Ptr[Byte]]

    def cons(value: Int, next: Ptr[Cons])(implicit z: Zone): Ptr[Cons] = {
      val v = alloc[Cons]
      v._1 = value
      v._2 = next.asInstanceOf[Ptr[Byte]]
      v
    }

    Zone { implicit z =>
      val out  = collection.mutable.ListBuffer.empty[Int]
      var head = cons(10, cons(20, cons(30, null)))
      while (head != null) {
        out += head._1
        head = head._2.asInstanceOf[Ptr[Cons]]
      }
      assert(out.toList == List(10, 20, 30))
    }
  }
}
