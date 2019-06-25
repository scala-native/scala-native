package scala.scalanative
package unsafe

object StackallocSuite extends tests.Suite {

  test("stackalloc Int") {
    val ptr = stackalloc[Int]

    !ptr = 42

    assert(!ptr == 42)
  }

  test("stackalloc Int, 4") {
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

  test("stackalloc CStruct2[Int, Int]") {
    val ptr = stackalloc[CStruct2[Int, Int]]

    ptr._1 = 1
    ptr._2 = 2

    assert(ptr._1 == 1)
    assert(ptr._2 == 2)
  }

  test("stackalloc CArray[Int, _4]") {
    val ptr = stackalloc[CArray[Int, Nat._4]]
    val arr = !ptr

    arr(0) = 1
    arr(1) = 2
    arr(2) = 3
    arr(3) = 4

    assert(arr(0) == 1)
    assert(arr(1) == 2)
    assert(arr(2) == 3)
    assert(arr(3) == 4)

    val intptr = ptr.asInstanceOf[Ptr[Int]]

    assert(intptr(0) == 1)
    assert(intptr(1) == 2)
    assert(intptr(2) == 3)
    assert(intptr(3) == 4)

    intptr(0) = 10
    intptr(1) = 20
    intptr(2) = 30
    intptr(3) = 40

    assert(arr(0) == 10)
    assert(arr(1) == 20)
    assert(arr(2) == 30)
    assert(arr(3) == 40)
  }

  test("stackalloc linked list") {
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
      self._1 = value
      self._2 = next
      self
    }
    def value = self._1
    def next  = self._2.asInstanceOf[Ptr[Node]]
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
