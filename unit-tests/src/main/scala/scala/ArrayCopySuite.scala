package scala

/*
 * tests with Array[Int]
 */
object ArrayIntCopySuite extends tests.Suite {
  def init(arr: Array[Int], from: Int = 0) = {
    var c = 0
    while (c < arr.length) {
      arr(c) = c + from
      c += 1
    }
  }

  val len = 10
  val arr = new Array[Int](len)
  val arr2 = new Array[Int](len + 2)
  val arrEmpty = new Array[Int](0)

  test("array[Int]: init") {
    init(arr, 100)
    assert(
        arr(0) == 100 && arr(1) == 101 && arr(2) == 102 &&
          arr(3) == 103 && arr(4) == 104 && arr(5) == 105 &&
          arr(6) == 106 && arr(7) == 107 && arr(8) == 108 &&
          arr(9) == 109
    )
  }

  test("array[Int]: copy to another array") {
    init(arr, 100)
    scala.Array.copy(arr, 0, arr2, 1, 10)
    assert(
        arr2(0) == 0 && arr2(1) == 100 && arr2(2) == 101 &&
          arr2(3) == 102 && arr2(4) == 103 && arr2(5) == 104 &&
          arr2(6) == 105 && arr2(7) == 106 && arr2(8) == 107 &&
          arr2(9) == 108 && arr2(10) == 109 && arr2(11) == 0
    )
  }

  test("array[Int]: copy zero elements from empty array") {
    init(arr2)
    scala.Array.copy(arrEmpty, 0, arr2, 5, 0)
    assert(
        arr2(0) == 0 && arr2(1) == 1 && arr2(2) == 2 &&
          arr2(3) == 3 && arr2(4) == 4 && arr2(5) == 5 &&
          arr2(6) == 6 && arr2(7) == 7 && arr2(8) == 8 &&
          arr2(9) == 9 && arr2(10) == 10 && arr2(11) == 11
    )
  }

  test("array[Int]: copy to self without overlap (1/2)") {
    init(arr)
    scala.Array.copy(arr, 0, arr, 5, 5)
    assert(
        arr(0) == 0 && arr(1) == 1 && arr(2) == 2 && arr(3) == 3 &&
          arr(4) == 4 && arr(5) == 0 && arr(6) == 1 && arr(7) == 2 &&
          arr(8) == 3 && arr(9) == 4
    )
  }

  test("array[Int]: copy to self without overlap (2/2)") {
    init(arr)
    scala.Array.copy(arr, 6, arr, 4, 2)
    assert(
        arr(0) == 0 && arr(1) == 1 && arr(2) == 2 && arr(3) == 3 &&
          arr(4) == 6 && arr(5) == 7 && arr(6) == 6 && arr(7) == 7 &&
          arr(8) == 8 && arr(9) == 9
    )
  }

  test("array[Int]: copy to self with overlap and backward copy") {
    init(arr)
    scala.Array.copy(arr, 0, arr, 2, 6)
    assert(
        arr(0) == 0 && arr(1) == 1 && arr(2) == 0 && arr(3) == 1 &&
          arr(4) == 2 && arr(5) == 3 && arr(6) == 4 && arr(7) == 5 &&
          arr(8) == 8 && arr(9) == 9
    )
  }

  test("array[Int]: copy to self with overlap and forward copy") {
    init(arr)
    scala.Array.copy(arr, 2, arr, 0, 6)
    assert(
        arr(0) == 2 && arr(1) == 3 && arr(2) == 4 && arr(3) == 5 &&
          arr(4) == 6 && arr(5) == 7 && arr(6) == 6 && arr(7) == 7 &&
          arr(8) == 8 && arr(9) == 9
    )
  }

  test("array[Int]: throws NullPointerException if from is null") {
    assertThrows[java.lang.NullPointerException] {
      scala.Array.copy(null, 0, arr2, 5, 2)
    }
  }

  test("array[Int]: throws NullPointerException if to is null") {
    assertThrows[java.lang.NullPointerException] {
      scala.Array.copy(arr, 0, null, 5, 2)
    }
  }

  test("array[Int]: throws IndexOutOfBoundsException if length is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, 5, -1)
    }
  }

  test(
      "array[Int]: throws IndexOutOfBoundsException if toPos + len > to.length") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, 5, 10)
    }
  }

  test(
      "array[Int]: throws IndexOutOfBoundsException if fromPos + len > from.length") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 5, arr2, 0, 10)
    }
  }

  test("array[Int]: throws IndexOutOfBoundsException if toPos is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, -1, 10)
    }
  }

  test("array[Int]: throws IndexOutOfBoundsException if fromPos is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, -1, arr2, 0, 10)
    }
  }

  test(
      "array[Int]: throws ArrayStoreException if copy to a different type of array") {
    val arrObject = new Array[String](len)
    assertThrows[java.lang.ArrayStoreException] {
      scala.Array.copy(arr, 0, arrObject, 5, 2)
    }
  }
}

/*
 * tests with Array[Double]
 */
object ArrayDoubleCopySuite extends tests.Suite {
  def init(arr: Array[Double], from: Double = 0.0) = {
    var c = 0

    while (c < arr.length) {
      arr(c) = c.toDouble + from
      c += 1
    }
  }

  val len = 10
  val arr = new Array[Double](len)
  val arr2 = new Array[Double](len + 2)
  val arrEmpty = new Array[Double](0)

  test("array[Double]: init") {
    init(arr, 100.0)
    assert(
        arr(0) == 100.0 && arr(1) == 101.0 && arr(2) == 102.0 &&
          arr(3) == 103.0 && arr(4) == 104.0 && arr(5) == 105.0 &&
          arr(6) == 106.0 && arr(7) == 107.0 && arr(8) == 108.0 &&
          arr(9) == 109.0
    )
  }

  test("array[Double]: copy to another array") {
    init(arr, 100.0)
    scala.Array.copy(arr, 0, arr2, 1, 10)
    assert(
        arr2(0) == 0.0 && arr2(1) == 100.0 && arr2(2) == 101.0 &&
          arr2(3) == 102.0 && arr2(4) == 103.0 && arr2(5) == 104.0 &&
          arr2(6) == 105.0 && arr2(7) == 106.0 && arr2(8) == 107.0 &&
          arr2(9) == 108.0 && arr2(10) == 109.0 && arr2(11) == 0.0
    )
  }

  test("array[Double]: copy zero elements from empty array") {
    init(arr2)
    scala.Array.copy(arrEmpty, 0, arr2, 5, 0)
    assert(
        arr2(0) == 0.0 && arr2(1) == 1.0 && arr2(2) == 2.0 &&
          arr2(3) == 3.0 && arr2(4) == 4.0 && arr2(5) == 5.0 &&
          arr2(6) == 6.0 && arr2(7) == 7.0 && arr2(8) == 8.0 &&
          arr2(9) == 9.0 && arr2(10) == 10.0 && arr2(11) == 11.0
    )
  }

  test("array[Double]: copy to self without overlap (1/2)") {
    init(arr)
    scala.Array.copy(arr, 0, arr, 5, 5)
    assert(
        arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 2.0 && arr(3) == 3.0 &&
          arr(4) == 4.0 && arr(5) == 0.0 && arr(6) == 1.0 && arr(7) == 2.0 &&
          arr(8) == 3.0 && arr(9) == 4.0
    )
  }

  test("array[Double]: copy to self without overlap (2/2)") {
    init(arr)
    scala.Array.copy(arr, 6, arr, 4, 2)
    assert(
        arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 2.0 && arr(3) == 3.0 &&
          arr(4) == 6.0 && arr(5) == 7.0 && arr(6) == 6.0 && arr(7) == 7.0 &&
          arr(8) == 8.0 && arr(9) == 9.0
    )
  }

  test("array[Double]: copy to self with overlap and backward copy") {
    init(arr)
    scala.Array.copy(arr, 0, arr, 2, 6)
    assert(
        arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 0.0 && arr(3) == 1.0 &&
          arr(4) == 2.0 && arr(5) == 3.0 && arr(6) == 4.0 && arr(7) == 5.0 &&
          arr(8) == 8.0 && arr(9) == 9.0
    )
  }

  test("array[Double]: copy to self with overlap and forward copy") {
    init(arr)
    scala.Array.copy(arr, 2, arr, 0, 6)
    assert(
        arr(0) == 2.0 && arr(1) == 3.0 && arr(2) == 4.0 && arr(3) == 5.0 &&
          arr(4) == 6.0 && arr(5) == 7.0 && arr(6) == 6.0 && arr(7) == 7.0 &&
          arr(8) == 8.0 && arr(9) == 9.0
    )
  }

  test("array[Double]: throws NullPointerException if from is null") {
    assertThrows[java.lang.NullPointerException] {
      scala.Array.copy(null, 0, arr2, 5, 2)
    }
  }

  test("array[Double]: throws NullPointerException if to is null") {
    assertThrows[java.lang.NullPointerException] {
      scala.Array.copy(arr, 0, null, 5, 2)
    }
  }

  test("array[Double]: throws IndexOutOfBoundsException if length is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, 5, -1)
    }
  }

  test(
      "array[Double]: throws IndexOutOfBoundsException if toPos + len > to.length") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, 5, 10)
    }
  }

  test(
      "array[Double]: throws IndexOutOfBoundsException if fromPos + len > from.length") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 5, arr2, 0, 10)
    }
  }

  test("array[Double]: throws IndexOutOfBoundsException if toPos is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, -1, 10)
    }
  }

  test(
      "array[Double]: throws IndexOutOfBoundsException if fromPos is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, -1, arr2, 0, 10)
    }
  }

  test(
      "array[Double]: throws ArrayStoreException if copy to a different type of array") {
    val arrObject = new Array[String](len)
    assertThrows[java.lang.ArrayStoreException] {
      scala.Array.copy(arr, 0, arrObject, 5, 2)
    }
  }
}

/*
 * tests with Array[Object]
 */

class A(_i: Int) {
  def i = _i
}
class B(_i: Int, _d: Double) extends A(_i)

object ArrayObjectCopySuite extends tests.Suite {
  def initB(arr: Array[B], from: Int = 0) = {
    var c = 0

    while (c < arr.length) {
      arr(c) = new B(c + from, c.toDouble)
      c += 1
    }
  }

  def initA(arr: Array[A]) = {
    var c = 0

    while (c < arr.length) {
      arr(c) = new A(c)
      c += 1
    }
  }

  val len = 10
  val arr = new Array[B](len)
  val arr2 = new Array[A](len + 2)
  val arrEmpty = new Array[B](0)

  test("array[Object]: init") {
    initB(arr, 100)
    assert(
        arr(0).i == 100 && arr(1).i == 101 && arr(2).i == 102 &&
          arr(3).i == 103 && arr(4).i == 104 && arr(5).i == 105 &&
          arr(6).i == 106 && arr(7).i == 107 && arr(8).i == 108 &&
          arr(9).i == 109
    )
  }

  test("array[Object]: copy to another array") {
    initB(arr, 100)
    scala.Array.copy(arr, 0, arr2, 1, 10)
    assert(
        arr2(0) == null && arr2(1).i == 100 && arr2(2).i == 101 &&
          arr2(3).i == 102 && arr2(4).i == 103 && arr2(5).i == 104 &&
          arr2(6).i == 105 && arr2(7).i == 106 && arr2(8).i == 107 &&
          arr2(9).i == 108 && arr2(10).i == 109 && arr2(11) == null
    )
  }

  test("array[Object]: copy zero elements from empty array") {
    initA(arr2)
    scala.Array.copy(arrEmpty, 0, arr2, 5, 0)
    assert(
        arr2(0).i == 0 && arr2(1).i == 1 && arr2(2).i == 2 &&
          arr2(3).i == 3 && arr2(4).i == 4 && arr2(5).i == 5 &&
          arr2(6).i == 6 && arr2(7).i == 7 && arr2(8).i == 8 &&
          arr2(9).i == 9 && arr2(10).i == 10 && arr2(11).i == 11
    )
  }

  test("array[Object]: copy to self without overlap (1/2)") {
    initB(arr)
    scala.Array.copy(arr, 0, arr, 5, 5)
    assert(
        arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 2 && arr(3).i == 3 &&
          arr(4).i == 4 && arr(5).i == 0 && arr(6).i == 1 && arr(7).i == 2 &&
          arr(8).i == 3 && arr(9).i == 4
    )
  }

  test("array[Object]: copy to self without overlap (2/2)") {
    initB(arr)
    scala.Array.copy(arr, 6, arr, 4, 2)
    assert(
        arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 2 && arr(3).i == 3 &&
          arr(4).i == 6 && arr(5).i == 7 && arr(6).i == 6 && arr(7).i == 7 &&
          arr(8).i == 8 && arr(9).i == 9
    )
  }

  test("array[Object]: copy to self with overlap and backward copy") {
    initB(arr)
    scala.Array.copy(arr, 0, arr, 2, 6)
    assert(
        arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 0 && arr(3).i == 1 &&
          arr(4).i == 2 && arr(5).i == 3 && arr(6).i == 4 && arr(7).i == 5 &&
          arr(8).i == 8 && arr(9).i == 9
    )
  }

  test("array[Object]: copy to self with overlap and forward copy") {
    initB(arr)
    scala.Array.copy(arr, 2, arr, 0, 6)
    assert(
        arr(0).i == 2 && arr(1).i == 3 && arr(2).i == 4 && arr(3).i == 5 &&
          arr(4).i == 6 && arr(5).i == 7 && arr(6).i == 6 && arr(7).i == 7 &&
          arr(8).i == 8 && arr(9).i == 9
    )
  }

  test("array[Object]: throws NullPointerException if from is null") {
    assertThrows[java.lang.NullPointerException] {
      scala.Array.copy(null, 0, arr2, 5, 2)
    }
  }

  test("array[Object]: throws NullPointerException if to is null") {
    assertThrows[java.lang.NullPointerException] {
      scala.Array.copy(arr, 0, null, 5, 2)
    }
  }

  test("array[Object]: throws IndexOutOfBoundsException if length is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, 5, -1)
    }
  }

  test(
      "array[Object]: throws IndexOutOfBoundsException if toPos + len > to.length") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, 5, 10)
    }
  }

  test(
      "array[Object]: throws IndexOutOfBoundsException if fromPos + len > from.length") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 5, arr2, 0, 10)
    }
  }

  test("array[Object]: throws IndexOutOfBoundsException if toPos is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, 0, arr2, -1, 10)
    }
  }

  test(
      "array[Object]: throws IndexOutOfBoundsException if fromPos is negative") {
    assertThrows[java.lang.IndexOutOfBoundsException] {
      scala.Array.copy(arr, -1, arr2, 0, 10)
    }
  }

  test(
      "array[Object]: throws ArrayStoreException if copy to a different type of array") {
    val arrChar = new Array[Char](len)
    assertThrows[java.lang.ArrayStoreException] {
      scala.Array.copy(arr, 0, arrChar, 5, 2)
    }
  }
}
