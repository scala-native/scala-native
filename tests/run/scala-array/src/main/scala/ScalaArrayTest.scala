import scalanative.native._
import scalanative.native.stdio._

class A(_i: Int) {
  def i = _i
}
class B(_i: Int, _d: Double) extends A(_i)

/*
 *
 */
object ScalaArrayTest {
  def main(args: Array[String]): Unit = {
    testArraycopyInt()
    testArraycopyDouble()
    testArraycopyObject()
  }

  /*
   * tests with Array[Int]
   */
  def testArraycopyInt() = {
    def init(arr: Array[Int]) = {
      var c = 0

      while (c < arr.length) {
        arr(c) = c
        c += 1
      }
    }

    val len      = 10
    val arr      = new Array[Int](len)
    val arr2     = new Array[Int](len + 2)
    val arrEmpty = new Array[Int](0)

    init(arr)

    // copy to another array
    scala.Array.copy(arr, 0, arr2, 1, 10)
    assert(
        arr2(0) == 0 && arr2(1) == 0 && arr2(2) == 1 &&
          arr2(3) == 2 && arr2(4) == 3 && arr2(5) == 4 &&
          arr2(6) == 5 && arr2(7) == 6 && arr2(8) == 7 &&
          arr2(9) == 8 && arr2(10) == 9 && arr2(11) == 0)

    // copy zero elements from empty array
    scala.Array.copy(arrEmpty, 0, arr2, 5, 0)
    assert(
        arr2(0) == 0 && arr2(1) == 0 && arr2(2) == 1 &&
          arr2(3) == 2 && arr2(4) == 3 && arr2(5) == 4 &&
          arr2(6) == 5 && arr2(7) == 6 && arr2(8) == 7 &&
          arr2(9) == 8 && arr2(10) == 9 && arr2(11) == 0)

    // copy to self without overlap (1/2)
    scala.Array.copy(arr, 0, arr, 5, 5)
    assert(
        arr(0) == 0 && arr(1) == 1 && arr(2) == 2 && arr(3) == 3 &&
          arr(4) == 4 && arr(5) == 0 && arr(6) == 1 && arr(7) == 2 &&
          arr(8) == 3 && arr(9) == 4)

    // copy to self without overlap (2/2)
    scala.Array.copy(arr, 6, arr, 4, 2)
    assert(
        arr(0) == 0 && arr(1) == 1 && arr(2) == 2 && arr(3) == 3 &&
          arr(4) == 1 && arr(5) == 2 && arr(6) == 1 && arr(7) == 2 &&
          arr(8) == 3 && arr(9) == 4)

    init(arr)

    // copy to self with overlap and backward copy
    scala.Array.copy(arr, 0, arr, 2, 6)
    assert(
        arr(0) == 0 && arr(1) == 1 && arr(2) == 0 && arr(3) == 1 &&
          arr(4) == 2 && arr(5) == 3 && arr(6) == 4 && arr(7) == 5 &&
          arr(8) == 8 && arr(9) == 9)

    init(arr)

    // copy to self with overlap and forward copy
    scala.Array.copy(arr, 2, arr, 0, 6)
    assert(
        arr(0) == 2 && arr(1) == 3 && arr(2) == 4 && arr(3) == 5 &&
          arr(4) == 6 && arr(5) == 7 && arr(6) == 6 && arr(7) == 7 &&
          arr(8) == 8 && arr(9) == 9)

    // from is null
    try {
      scala.Array.copy(null, 0, arr2, 5, 2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // to is null
    try {
      scala.Array.copy(arr, 0, null, 5, -2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // length is negative
    try {
      scala.Array.copy(arr, 0, arr2, 5, -1)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // toPos + len > to.length
    try {
      scala.Array.copy(arr, 0, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // fromPos + len > from.length
    try {
      scala.Array.copy(arr, 5, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // toPos is negative
    try {
      scala.Array.copy(arr, 0, arr2, -1, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // fromPos is negative
    try {
      scala.Array.copy(arr, -1, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // Different type of array
    try {
      val arrObject = new Array[String](len)
      scala.Array.copy(arr, 0, arrObject, 5, 2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }
  }

  /*
   * tests with Array[Double]
   */
  def testArraycopyDouble() = {
    def init(arr: Array[Double]) = {
      var c = 0

      while (c < arr.length) {
        arr(c) = c.toDouble
        c += 1
      }
    }

    val len      = 10
    val arr      = new Array[Double](len)
    val arr2     = new Array[Double](len + 2)
    val arrEmpty = new Array[Double](0)

    init(arr)

    // copy to another array
    scala.Array.copy(arr, 0, arr2, 1, 10)
    assert(
        arr2(0) == 0.0 && arr2(1) == 0.0 && arr2(2) == 1.0 &&
          arr2(3) == 2.0 && arr2(4) == 3.0 && arr2(5) == 4.0 &&
          arr2(6) == 5.0 && arr2(7) == 6.0 && arr2(8) == 7.0 &&
          arr2(9) == 8.0 && arr2(10) == 9.0 && arr2(11) == 0.0)

    // copy zero elements from empty array
    scala.Array.copy(arrEmpty, 0, arr2, 5, 0)
    assert(
        arr2(0) == 0.0 && arr2(1) == 0.0 && arr2(2) == 1.0 &&
          arr2(3) == 2.0 && arr2(4) == 3.0 && arr2(5) == 4.0 &&
          arr2(6) == 5.0 && arr2(7) == 6.0 && arr2(8) == 7.0 &&
          arr2(9) == 8.0 && arr2(10) == 9.0 && arr2(11) == 0.0)

    // copy to self without overlap (1/2)
    scala.Array.copy(arr, 0, arr, 5, 5)
    assert(
        arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 2.0 && arr(3) == 3.0 &&
          arr(4) == 4.0 && arr(5) == 0.0 && arr(6) == 1.0 && arr(7) == 2.0 &&
          arr(8) == 3.0 && arr(9) == 4.0)

    // copy to self without overlap (2/2)
    scala.Array.copy(arr, 6, arr, 4, 2)
    assert(
        arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 2.0 && arr(3) == 3.0 &&
          arr(4) == 1.0 && arr(5) == 2.0 && arr(6) == 1.0 && arr(7) == 2.0 &&
          arr(8) == 3.0 && arr(9) == 4.0)

    init(arr)

    // copy to self with overlap and backward copy
    scala.Array.copy(arr, 0, arr, 2, 6)
    assert(
        arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 0.0 && arr(3) == 1.0 &&
          arr(4) == 2.0 && arr(5) == 3.0 && arr(6) == 4.0 && arr(7) == 5.0 &&
          arr(8) == 8.0 && arr(9) == 9.0)

    init(arr)

    // copy to self with overlap and forward copy
    scala.Array.copy(arr, 2, arr, 0, 6)
    assert(
        arr(0) == 2.0 && arr(1) == 3.0 && arr(2) == 4.0 && arr(3) == 5.0 &&
          arr(4) == 6.0 && arr(5) == 7.0 && arr(6) == 6.0 && arr(7) == 7.0 &&
          arr(8) == 8.0 && arr(9) == 9.0)

    // from is null
    try {
      scala.Array.copy(null, 0, arr2, 5, 2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // to is null
    try {
      scala.Array.copy(arr, 0, null, 5, -2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // length is negative
    try {
      scala.Array.copy(arr, 0, arr2, 5, -1)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // toPos + len > to.length
    try {
      scala.Array.copy(arr, 0, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // fromPos + len > from.length
    try {
      scala.Array.copy(arr, 5, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // toPos is negative
    try {
      scala.Array.copy(arr, 0, arr2, -1, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // fromPos is negative
    try {
      scala.Array.copy(arr, -1, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // Different type of array
    try {
      val arrObject = new Array[String](len)
      scala.Array.copy(arr, 0, arrObject, 5, 2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }
  }

  /*
   * tests with Array[Object]
   */
  def testArraycopyObject() = {
    def init(arr: Array[B]) = {
      var c = 0

      while (c < arr.length) {
        arr(c) = new B(c, c.toDouble)
        c += 1
      }
    }

    val len      = 10
    val arr      = new Array[B](len)
    val arr2     = new Array[A](len + 2)
    val arrEmpty = new Array[B](0)

    init(arr)

    // copy to another array [B<:A] -> [A]
    scala.Array.copy(arr, 0, arr2, 1, 10)
    assert(
        arr2(0) == null && arr2(1).i == 0 && arr2(2).i == 1 &&
          arr2(3).i == 2 && arr2(4).i == 3 && arr2(5).i == 4 &&
          arr2(6).i == 5 && arr2(7).i == 6 && arr2(8).i == 7 &&
          arr2(9).i == 8 && arr2(10).i == 9 && arr2(11) == null)

    // copy zero elements from empty array
    scala.Array.copy(arrEmpty, 0, arr2, 5, 0)
    assert(
        arr2(0) == null && arr2(1).i == 0 && arr2(2).i == 1 &&
          arr2(3).i == 2 && arr2(4).i == 3 && arr2(5).i == 4 &&
          arr2(6).i == 5 && arr2(7).i == 6 && arr2(8).i == 7 &&
          arr2(9).i == 8 && arr2(10).i == 9 && arr2(11) == null)

    // copy to self without overlap (1/2)
    scala.Array.copy(arr, 0, arr, 5, 5)
    assert(
        arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 2 && arr(3).i == 3 &&
          arr(4).i == 4 && arr(5).i == 0 && arr(6).i == 1 && arr(7).i == 2 &&
          arr(8).i == 3 && arr(9).i == 4)

    // copy to self without overlap (2/2)
    scala.Array.copy(arr, 6, arr, 4, 2)
    assert(
        arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 2 && arr(3).i == 3 &&
          arr(4).i == 1 && arr(5).i == 2 && arr(6).i == 1 && arr(7).i == 2 &&
          arr(8).i == 3 && arr(9).i == 4)

    init(arr)

    // copy to self with overlap and backward copy
    scala.Array.copy(arr, 0, arr, 2, 6)
    assert(
        arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 0 && arr(3).i == 1 &&
          arr(4).i == 2 && arr(5).i == 3 && arr(6).i == 4 && arr(7).i == 5 &&
          arr(8).i == 8 && arr(9).i == 9)

    init(arr)

    // copy to self with overlap and forward copy
    scala.Array.copy(arr, 2, arr, 0, 6)
    assert(
        arr(0).i == 2 && arr(1).i == 3 && arr(2).i == 4 && arr(3).i == 5 &&
          arr(4).i == 6 && arr(5).i == 7 && arr(6).i == 6 && arr(7).i == 7 &&
          arr(8).i == 8 && arr(9).i == 9)

    // from is null
    try {
      scala.Array.copy(null, 0, arr2, 5, 2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // to is null
    try {
      scala.Array.copy(arr, 0, null, 5, -2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // length is negative
    try {
      scala.Array.copy(arr, 0, arr2, 5, -1)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // toPos + len > to.length
    try {
      scala.Array.copy(arr, 0, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // fromPos + len > from.length
    try {
      scala.Array.copy(arr, 5, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // toPos is negative
    try {
      scala.Array.copy(arr, 0, arr2, -1, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // fromPos is negative
    try {
      scala.Array.copy(arr, -1, arr2, 5, 10)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }

    // Different type of array
    try {
      val arrInt = new Array[Int](len)
      scala.Array.copy(arr, 0, arrInt, 5, 2)
      assert(false)
    } catch {
      case th: Throwable => assert(true)
    }
  }
}
