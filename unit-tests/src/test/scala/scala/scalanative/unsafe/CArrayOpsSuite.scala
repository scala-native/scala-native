package scala.scalanative
package unsafe

import scalanative.unsafe.Nat._

object CArrayOpsSuite extends tests.Suite {

  test("at") {
    val alloc = stackalloc[CArray[Int, Digit2[_3, _2]]]
    val carr  = !alloc
    val ptr   = alloc.asInstanceOf[Ptr[Int]]

    assert(ptr == carr.at(0))
    (1 to 100).foreach { i =>
      assert((ptr + i) == carr.at(i))
    }
  }

  test("apply/update") {
    val alloc = stackalloc[CArray[Int, Digit2[_3, _2]]]
    val carr  = !alloc
    val ptr   = alloc.asInstanceOf[Ptr[Int]]

    (0 until 32).foreach { i =>
      carr(i) = i
      assert(carr(i) == i)
      assert(ptr(i) == i)
      ptr(i) = i * 2
      assert(carr(i) == i * 2)
      assert(ptr(i) == i * 2)
    }
  }

  test("length") {
    val carr0 = stackalloc[CArray[Int, _0]]
    assert(carr0.length == 0)
    val carr8 = stackalloc[CArray[Int, _8]]
    assert(carr8.length == 8)
    val carr16 = stackalloc[CArray[Int, Digit2[_1, _6]]]
    assert(carr16.length == 16)
    val carr32 = stackalloc[CArray[Int, Digit2[_3, _2]]]
    assert(carr32.length == 32)
    val carr128 = stackalloc[CArray[Int, Digit3[_1, _2, _8]]]
    assert(carr128.length == 128)
    val carr256 = stackalloc[CArray[Int, Digit3[_2, _5, _6]]]
    assert(carr256.length == 256)
    val carr1024 = stackalloc[CArray[Int, Digit4[_1, _0, _2, _4]]]
    assert(carr1024.length == 1024)
    val carr4096 = stackalloc[CArray[Int, Digit4[_4, _0, _9, _6]]]
    assert(carr4096.length == 4096)
  }
}
