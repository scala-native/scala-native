package scala

object ArrayDoubleCopySuite extends tests.Suite {
  def init(arr: Array[Double], from: Double = 0.0) = {
    var c = 0

    while (c < arr.length) {
      arr(c) = c.toDouble + from
      c += 1
    }
  }

  val len      = 10
  val arr      = new Array[Double](len)
  val arr2     = new Array[Double](len + 2)
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
