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

  test("CArray[Int, _4]") {
    val ptr = stackalloc[CArray[Int, Nat._4]]

    !ptr._1 = 1
    !ptr._2 = 2
    !ptr._3 = 3
    !ptr._4 = 4

    assert(!ptr._1 == 1)
    assert(!ptr._2 == 2)
    assert(!ptr._3 == 3)
    assert(!ptr._4 == 4)

    val intptr = ptr.cast[Ptr[Int]]

    assert(intptr(0) == 1)
    assert(intptr(1) == 2)
    assert(intptr(2) == 3)
    assert(intptr(3) == 4)

    intptr(0) = 10
    intptr(1) = 20
    intptr(2) = 30
    intptr(3) = 40

    assert(!ptr._1 == 10)
    assert(!ptr._2 == 20)
    assert(!ptr._3 == 30)
    assert(!ptr._4 == 40)
  }

  test("stack-allocated list") {
    import CList._
    var i               = 0
    var head: Ptr[Node] = null
    while (i < 4) {
      head = stackalloc[Node].init(i, head)
      i += 1
    }
    assert(head.sum == 6)
  }
}

object CList {
  type Node = CStruct2[Int, Ptr[_]]

  implicit class NodeOps(val self: Ptr[Node]) extends AnyVal {
    def init(value: Int, next: Ptr[Node]) = {
      !self._1 = value
      !self._2 = next
      self
    }
    def value = !self._1
    def next  = (!self._2).cast[Ptr[Node]]
    def sum: Int = {
      var res  = 0
      var head = self
      while (head != null) {
        res += head.value
        head = head.next
      }
      res
    }
  }
}
