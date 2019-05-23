package scala.scalanative
package unsafe

object CStructOpsSuite extends tests.Suite {

  test("at") {
    val alloc  = stackalloc[CStruct4[Byte, Short, Int, Long]]
    val struct = !alloc
    val ptr    = alloc.asInstanceOf[Ptr[Byte]]

    assert(ptr == struct.at1)
    assert(ptr + 2 == struct.at2)
    assert(ptr + 4 == struct.at3)
    assert(ptr + 8 == struct.at4)
  }

  test("apply/update") {
    val alloc  = stackalloc[CStruct4[Int, Int, Int, Int]]
    val struct = !alloc
    val ptr    = alloc.asInstanceOf[Ptr[Int]]

    struct._1 = 1
    assert(struct._1 == 1)
    assert(ptr(0) == 1)
    ptr(0) = 10
    assert(struct._1 == 10)
    assert(ptr(0) == 10)

    struct._2 = 2
    assert(struct._2 == 2)
    assert(ptr(1) == 2)
    ptr(1) = 20
    assert(struct._2 == 20)
    assert(ptr(1) == 20)

    struct._3 = 3
    assert(struct._3 == 3)
    assert(ptr(2) == 3)
    ptr(2) = 30
    assert(struct._3 == 30)
    assert(ptr(2) == 30)

    struct._4 = 4
    assert(struct._4 == 4)
    assert(ptr(3) == 4)
    ptr(3) = 40
    assert(struct._4 == 40)
    assert(ptr(3) == 40)
  }
}
