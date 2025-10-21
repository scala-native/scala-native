package scala

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArrayIntCopyTest {
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

  @Test def init(): Unit = {
    init(arr, 100)
    assertTrue(
      arr(0) == 100 && arr(1) == 101 && arr(2) == 102 &&
      arr(3) == 103 && arr(4) == 104 && arr(5) == 105 &&
      arr(6) == 106 && arr(7) == 107 && arr(8) == 108 &&
      arr(9) == 109
    )
  }

  @Test def copyToAnotherArray(): Unit = {
    init(arr, 100)
    java.lang.System.arraycopy(arr, 0, arr2, 1, 10)
    assertTrue(
      arr2(0) == 0 && arr2(1) == 100 && arr2(2) == 101 &&
      arr2(3) == 102 && arr2(4) == 103 && arr2(5) == 104 &&
      arr2(6) == 105 && arr2(7) == 106 && arr2(8) == 107 &&
      arr2(9) == 108 && arr2(10) == 109 && arr2(11) == 0
    )
  }

  @Test def copyZeroElementsFromEmptyArray(): Unit = {
    init(arr2)
    java.lang.System.arraycopy(arrEmpty, 0, arr2, 5, 0)
    assertTrue(
      arr2(0) == 0 && arr2(1) == 1 && arr2(2) == 2 &&
      arr2(3) == 3 && arr2(4) == 4 && arr2(5) == 5 &&
      arr2(6) == 6 && arr2(7) == 7 && arr2(8) == 8 &&
      arr2(9) == 9 && arr2(10) == 10 && arr2(11) == 11
    )
  }

  @Test def copyToSelfWithoutOverlapOneOfTwo(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 0, arr, 5, 5)
    assertTrue(
      arr(0) == 0 && arr(1) == 1 && arr(2) == 2 && arr(3) == 3 &&
      arr(4) == 4 && arr(5) == 0 && arr(6) == 1 && arr(7) == 2 &&
      arr(8) == 3 && arr(9) == 4
    )
  }

  @Test def copyToSelfWithoutOverlapTwoOfTwo(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 6, arr, 4, 2)
    assertTrue(
      arr(0) == 0 && arr(1) == 1 && arr(2) == 2 && arr(3) == 3 &&
      arr(4) == 6 && arr(5) == 7 && arr(6) == 6 && arr(7) == 7 &&
      arr(8) == 8 && arr(9) == 9
    )
  }

  @Test def copyToSelfWithOverlapAndBackwardCopy(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 0, arr, 2, 6)
    assertTrue(
      arr(0) == 0 && arr(1) == 1 && arr(2) == 0 && arr(3) == 1 &&
      arr(4) == 2 && arr(5) == 3 && arr(6) == 4 && arr(7) == 5 &&
      arr(8) == 8 && arr(9) == 9
    )
  }

  @Test def copyToSelfWithOverlapAndForwardCopy(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 2, arr, 0, 6)
    assertTrue(
      arr(0) == 2 && arr(1) == 3 && arr(2) == 4 && arr(3) == 5 &&
      arr(4) == 6 && arr(5) == 7 && arr(6) == 6 && arr(7) == 7 &&
      arr(8) == 8 && arr(9) == 9
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
    val arrObject = new Array[String](len)
    assertThrows(
      classOf[java.lang.ArrayStoreException],
      java.lang.System.arraycopy(arr, 0, arrObject, 5, 2)
    )
  }
}
