package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._
import scalanative.unsigned._

class StackallocTest {

  @Test def stackallocInt(): Unit = {
    val ptr = stackalloc[Int]()

    !ptr = 42

    assertTrue(!ptr == 42)
    assertFalse(ptr.toInt == 42)
  }

  @Test def stackallocInt4(): Unit = {
    val ptr: Ptr[Int] = stackalloc[Int](4)

    ptr(0) = 1
    ptr(1) = 2
    ptr(2) = 3
    ptr(3) = 4

    assertTrue(ptr(0) == 1)
    assertTrue(ptr(1) == 2)
    assertTrue(ptr(2) == 3)
    assertTrue(ptr(3) == 4)
  }

  @Test def stackallocCStruct2IntInt(): Unit = {
    val ptr = stackalloc[CStruct2[Int, Int]]()

    ptr._1 = 1
    ptr._2 = 2

    assertTrue(ptr._1 == 1)
    assertTrue(ptr._2 == 2)
  }

  @Test def stackallocCArrayIntNat4(): Unit = {
    val ptr = stackalloc[CArray[Int, Nat._4]]()
    val arr = !ptr

    arr(0) = 1
    arr(1) = 2
    arr(2) = 3
    arr(3) = 4

    assertTrue(arr(0) == 1)
    assertTrue(arr(1) == 2)
    assertTrue(arr(2) == 3)
    assertTrue(arr(3) == 4)

    val intptr = ptr.asInstanceOf[Ptr[Int]]

    assertTrue(intptr(0) == 1)
    assertTrue(intptr(1) == 2)
    assertTrue(intptr(2) == 3)
    assertTrue(intptr(3) == 4)

    intptr(0) = 10
    intptr(1) = 20
    intptr(2) = 30
    intptr(3) = 40

    assertTrue(arr(0) == 10)
    assertTrue(arr(1) == 20)
    assertTrue(arr(2) == 30)
    assertTrue(arr(3) == 40)
  }

  @Test def stackallocLinkedList(): Unit = {
    import CList._
    var i = 0
    var head: Ptr[Node] = null
    while (i < 4) {
      head = stackalloc[Node]().init(i, head)
      i += 1
    }
    assertTrue(head.sum == 6)
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
    def next = self._2.asInstanceOf[Ptr[Node]]
    def sum: Int = {
      var res = 0
      var head = self
      while (head != null) {
        res += head.value
        head = head.next
      }
      res
    }
  }
}
