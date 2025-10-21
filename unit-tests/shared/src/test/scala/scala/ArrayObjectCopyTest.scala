package scala

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArrayObjectCopyTest {
  class A(_i: Int) {
    def i = _i
  }
  class B(_i: Int, _d: Double) extends A(_i)

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

  @Test def init(): Unit = {
    initB(arr, 100)
    assertTrue(
      arr(0).i == 100 && arr(1).i == 101 && arr(2).i == 102 &&
      arr(3).i == 103 && arr(4).i == 104 && arr(5).i == 105 &&
      arr(6).i == 106 && arr(7).i == 107 && arr(8).i == 108 &&
      arr(9).i == 109
    )
  }

  @Test def copyToAnotherArray(): Unit = {
    initB(arr, 100)
    java.lang.System.arraycopy(arr, 0, arr2, 1, 10)
    assertTrue(
      arr2(0) == null && arr2(1).i == 100 && arr2(2).i == 101 &&
      arr2(3).i == 102 && arr2(4).i == 103 && arr2(5).i == 104 &&
      arr2(6).i == 105 && arr2(7).i == 106 && arr2(8).i == 107 &&
      arr2(9).i == 108 && arr2(10).i == 109 && arr2(11) == null
    )
  }

  @Test def copyZeroElementsFromEmptyArray(): Unit = {
    initA(arr2)
    java.lang.System.arraycopy(arrEmpty, 0, arr2, 5, 0)
    assertTrue(
      arr2(0).i == 0 && arr2(1).i == 1 && arr2(2).i == 2 &&
      arr2(3).i == 3 && arr2(4).i == 4 && arr2(5).i == 5 &&
      arr2(6).i == 6 && arr2(7).i == 7 && arr2(8).i == 8 &&
      arr2(9).i == 9 && arr2(10).i == 10 && arr2(11).i == 11
    )
  }

  @Test def copyToSelfWithoutOverlapOneOfTwo(): Unit = {
    initB(arr)
    java.lang.System.arraycopy(arr, 0, arr, 5, 5)
    assertTrue(
      arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 2 && arr(3).i == 3 &&
      arr(4).i == 4 && arr(5).i == 0 && arr(6).i == 1 && arr(7).i == 2 &&
      arr(8).i == 3 && arr(9).i == 4
    )
  }

  @Test def copyToSelfWithoutOverlapTwoOfTwo(): Unit = {
    initB(arr)
    java.lang.System.arraycopy(arr, 6, arr, 4, 2)
    assertTrue(
      arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 2 && arr(3).i == 3 &&
      arr(4).i == 6 && arr(5).i == 7 && arr(6).i == 6 && arr(7).i == 7 &&
      arr(8).i == 8 && arr(9).i == 9
    )
  }

  @Test def copyToSelfWithOverlapAndBackwardCopy(): Unit = {
    initB(arr)
    java.lang.System.arraycopy(arr, 0, arr, 2, 6)
    assertTrue(
      arr(0).i == 0 && arr(1).i == 1 && arr(2).i == 0 && arr(3).i == 1 &&
      arr(4).i == 2 && arr(5).i == 3 && arr(6).i == 4 && arr(7).i == 5 &&
      arr(8).i == 8 && arr(9).i == 9
    )
  }

  @Test def copyToSelfWithOverlapAndForwardCopy(): Unit = {
    initB(arr)
    java.lang.System.arraycopy(arr, 2, arr, 0, 6)
    assertTrue(
      arr(0).i == 2 && arr(1).i == 3 && arr(2).i == 4 && arr(3).i == 5 &&
      arr(4).i == 6 && arr(5).i == 7 && arr(6).i == 6 && arr(7).i == 7 &&
      arr(8).i == 8 && arr(9).i == 9
    )
  }

  @Test def throwsNullPointerExceptionIfFromIsNull(): Unit = {
    assertThrows(
      classOf[java.lang.NullPointerException],
      java.lang.System.arraycopy(null, 0, arr2, 5, 2)
    )
  }

  @Test def throwsNullPointerExceptionIfToIsNull(): Unit = {
    assertThrows(
      classOf[java.lang.NullPointerException],
      java.lang.System.arraycopy(arr, 0, null, 5, 2)
    )
  }

  @Test def throwsIndexOutOfBoundsExceptionIfLengthIsNegative(): Unit = {
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      java.lang.System.arraycopy(arr, 0, arr2, 5, -1)
    )
  }

  @Test def throwsIndexOutOfBoundsExceptionIfToPosPlusLenGreaterThanToLength()
      : Unit = {
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      java.lang.System.arraycopy(arr, 0, arr2, 5, 10)
    )
  }

  @Test def throwsIndexOutOfBoundsExceptionIfFromPosPlusLenGreaterThanFromLength()
      : Unit = {
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      java.lang.System.arraycopy(arr, 5, arr2, 0, 10)
    )
  }

  @Test def throwsIndexOutOfBoundsExceptionIfToPosIsNegative(): Unit = {
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      java.lang.System.arraycopy(arr, 0, arr2, -1, 10)
    )
  }

  @Test def throwsIndexOutOfBoundsExceptionIfFromPosIsNegative(): Unit = {
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      java.lang.System.arraycopy(arr, -1, arr2, 0, 10)
    )
  }

  @Test def throwsArrayStoreExceptionIfCopyToDifferentTypeOfArray(): Unit = {
    val arrChar = new Array[Char](len)
    assertThrows(
      classOf[java.lang.ArrayStoreException],
      java.lang.System.arraycopy(arr, 0, arrChar, 5, 2)
    )
  }
}
