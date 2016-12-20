package java.util

object IteratorSuite extends tests.Suite {
  test("empty") {
    val it = new Iterator[String] {
      def hasNext(): Boolean = false
      def next: String       = throw new NoSuchElementException
    }

    assertThrows[NoSuchElementException](it.next())
  }

  test("three elements") {
    val it = new Iterator[String] {
      var remainingElements = 3

      def hasNext(): Boolean = remainingElements > 0
      def next: String =
        if (remainingElements > 0) {
          val result = "elem: " + remainingElements
          remainingElements -= 1
          result
        } else throw new NoSuchElementException
    }

    assert(it.next() == "elem: 3")
    assert(it.next() == "elem: 2")
    assert(it.next() == "elem: 1")
    assertNot(it.hasNext)
    assertThrows[NoSuchElementException](it.next())
  }
}
