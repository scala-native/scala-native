package scala

object ArrayIntCopySuite extends tests.Suite {
  def init(arr: Array[Int], from: Int = 0) = {
    var c = 0
    while (c < arr.length) {
      arr(c) = c + from
      c += 1
    }
  }

  val len      = 10
  val arr      = new Array[Int](len)
  val arr2     = new Array[Int](len + 2)
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
