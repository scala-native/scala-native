package scala.scalanative.native

object StackallocSuite extends tests.Suite {

  test("Int") {
    val ptr = stackalloc[Int]
    !ptr = 42
    assert(!ptr == 42)
  }

  test("Int * 4") {
    val ptr = stackalloc[Int](4)
    ptr(0) = 1
    ptr(1) = 2
    ptr(2) = 3
    ptr(3) = 4
    assert(ptr(0) == 1)
    assert(ptr(1) == 2)
    assert(ptr(2) == 3)
    assert(ptr(3) == 4)
  }

  test("CStruct2[Int, Int]") {
    val ptr = stackalloc[CStruct2[Int, Int]]
    !ptr._1 = 1
    !ptr._2 = 2
    assert(!ptr._1 == 1)
    assert(!ptr._2 == 2)
  }
}
