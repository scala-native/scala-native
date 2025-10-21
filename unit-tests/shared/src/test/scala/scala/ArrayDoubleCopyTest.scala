package scala

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArrayDoubleCopyTest {
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

  @Test def init(): Unit = {
    init(arr, 100.0)
    assertTrue(
      arr(0) == 100.0 && arr(1) == 101.0 && arr(2) == 102.0 &&
      arr(3) == 103.0 && arr(4) == 104.0 && arr(5) == 105.0 &&
      arr(6) == 106.0 && arr(7) == 107.0 && arr(8) == 108.0 &&
      arr(9) == 109.0
    )
  }

  @Test def copyToAnotherArray(): Unit = {
    init(arr, 100.0)
    java.lang.System.arraycopy(arr, 0, arr2, 1, 10)
    assertTrue(
      arr2(0) == 0.0 && arr2(1) == 100.0 && arr2(2) == 101.0 &&
      arr2(3) == 102.0 && arr2(4) == 103.0 && arr2(5) == 104.0 &&
      arr2(6) == 105.0 && arr2(7) == 106.0 && arr2(8) == 107.0 &&
      arr2(9) == 108.0 && arr2(10) == 109.0 && arr2(11) == 0.0
    )
  }

  @Test def copyZeroElementsFromEmptyArray(): Unit = {
    init(arr2)
    java.lang.System.arraycopy(arrEmpty, 0, arr2, 5, 0)
    assertTrue(
      arr2(0) == 0.0 && arr2(1) == 1.0 && arr2(2) == 2.0 &&
      arr2(3) == 3.0 && arr2(4) == 4.0 && arr2(5) == 5.0 &&
      arr2(6) == 6.0 && arr2(7) == 7.0 && arr2(8) == 8.0 &&
      arr2(9) == 9.0 && arr2(10) == 10.0 && arr2(11) == 11.0
    )
  }

  @Test def copyToSelfWithoutOverlapOneOfTwo(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 0, arr, 5, 5)
    assertTrue(
      arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 2.0 && arr(3) == 3.0 &&
      arr(4) == 4.0 && arr(5) == 0.0 && arr(6) == 1.0 && arr(7) == 2.0 &&
      arr(8) == 3.0 && arr(9) == 4.0
    )
  }

  @Test def copyToSelfWithoutOverlapTwoOfTwo(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 6, arr, 4, 2)
    assertTrue(
      arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 2.0 && arr(3) == 3.0 &&
      arr(4) == 6.0 && arr(5) == 7.0 && arr(6) == 6.0 && arr(7) == 7.0 &&
      arr(8) == 8.0 && arr(9) == 9.0
    )
  }

  @Test def copyToSelfWithOverlapAndBackwardCopy(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 0, arr, 2, 6)
    assertTrue(
      arr(0) == 0.0 && arr(1) == 1.0 && arr(2) == 0.0 && arr(3) == 1.0 &&
      arr(4) == 2.0 && arr(5) == 3.0 && arr(6) == 4.0 && arr(7) == 5.0 &&
      arr(8) == 8.0 && arr(9) == 9.0
    )
  }

  @Test def copyToSelfWithOverlapAndForwardCopy(): Unit = {
    init(arr)
    java.lang.System.arraycopy(arr, 2, arr, 0, 6)
    assertTrue(
      arr(0) == 2.0 && arr(1) == 3.0 && arr(2) == 4.0 && arr(3) == 5.0 &&
      arr(4) == 6.0 && arr(5) == 7.0 && arr(6) == 6.0 && arr(7) == 7.0 &&
      arr(8) == 8.0 && arr(9) == 9.0
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
