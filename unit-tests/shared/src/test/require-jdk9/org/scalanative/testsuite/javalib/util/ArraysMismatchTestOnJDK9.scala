package org.scalanative.testsuite.javalib.util

import java.util.Arrays

import org.junit.Assert.*
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArraysMismatchTestOnJDK9 {

  @Test def mismatch_Byte_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[Byte](srcSize)
    val arrB = new Array[Byte](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.mismatch(null, arrB)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.mismatch(arrA, null)
    )
  }

  @Test def mismatch_Byte_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val arrAPrime = new Array[Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toByte

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[Byte](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toByte

    val arrB = new Array[Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toByte

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toByte

    assertEquals(
      "a == aPrime",
      -1, // No mismatch found
      Arrays.mismatch(arrA, arrAPrime)
    )

    assertEquals(
      "a != b",
      mismatchAt,
      Arrays.mismatch(arrA, arrB)
    )

    assertEquals(
      "common prefix but a.length < b.length",
      expectedShortSize,
      Arrays.mismatch(arrAShort, arrA)
    )

    assertEquals(
      "common prefix but a.length > b.length",
      expectedShortSize,
      Arrays.mismatch(arrAShort, arrA)
    )
  }

  @Test def mismatch_Byte_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[Byte](srcSize)
    val arrB = new Array[Byte](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.mismatch(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.mismatch(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.mismatch(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.mismatch(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.mismatch(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.mismatch(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.mismatch(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.mismatch(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def mismatch_Byte_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Byte](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toByte

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.mismatch(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      -1, // No mismatch found
      Arrays.mismatch(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt) = 5.toByte

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when byte in one has changed.
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      expectedMismatchAtAIdx,
      Arrays.mismatch(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertEquals(
      "common prefix but a.length < b.length",
      expectedShortMismatchAtIdx,
      Arrays.mismatch(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertEquals(
      "common prefix but a.length > b.length",
      expectedShortMismatchAtIdx,
      Arrays.mismatch(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

}
