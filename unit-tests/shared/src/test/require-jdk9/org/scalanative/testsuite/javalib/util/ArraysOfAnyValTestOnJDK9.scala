package org.scalanative.testsuite.javalib.util

/* This code is manually generated from ArraysOfAnyValTestOnJDK9.scala.gyb.
 * Any edits here and not in the .gyb may be lost the next time this file is
 * generated.
 */

/* The name of this file is ArraysOfAnyValTestOnJDK9.scala. That
 * correspond ArraysOfObjectTestOnJDK9.scala, since Objects are AnyRefs.
 *
 * That name and the practice in ArraysOfObjectTestOnJDK9.scala would lead
 * one to believe that an 'ArraysOfAnyValTestOnJDK9' exists but it does not.
 * One must run tests by type, say 'ArraysOfByteTestOnJDK9'.
 *
 * Having all of the 'ArrayOf<type>TestOnJDK9' classes in one file
 * greatly simplifies the .gyb file.
 *
 * Having separate classes, by type, avoids having one unwieldy, humongous
 * 'ArraysOfAnyValTestOnJDK9' class.
 *
 * Scala Native 'testsuite' does not allow a Suite of Test classes under
 * one enclosing class. Room for improvement.
 */

import java.{lang => jl}
import java.{util => ju}

import java.util.Arrays

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class ArraysOfBooleanTestOnJDK9 {

  @Test def compare_Boolean_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      false

    val arrA = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = true

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Boolean_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    val arrB = new Array[scala.Boolean](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Boolean_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = true

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      false

    val arrB = new Array[scala.Boolean](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = true

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    arrB(t1FromIdx) = !arrA(t1FromIdx) // force a mismatch at the low end

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Boolean_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    val arrB = new Array[scala.Boolean](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Boolean_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = false

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Boolean](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = false

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    arrB(t1FromIdx) = !arrA(t1FromIdx) // force a mismatch at the low end

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = true

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Boolean_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    val arrB = new Array[scala.Boolean](srcSize)

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

  @Test def mismatch_Boolean_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = false

    val arrAPrime = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = false

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Boolean](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = false

    val arrB = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = false

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = true

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

  @Test def mismatch_Boolean_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    val arrB = new Array[scala.Boolean](srcSize)

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

  @Test def mismatch_Boolean_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Boolean](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = false

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Boolean](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = false

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    arrB(t1FromIdx) = !arrA(t1FromIdx) // force a mismatch at the low end

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
    arrB(changeBAt) = true

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfByteTestOnJDK9 {

  @Test def compare_Byte_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Byte.MIN_VALUE

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Byte_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Byte](srcSize)
    val arrB = new Array[scala.Byte](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Byte_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Byte.MIN_VALUE

    val arrB = new Array[scala.Byte](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toByte

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def compareUnsigned_Byte_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 12
    val changeTo = jl.Byte.MIN_VALUE

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compareUnsigned(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a < b",
      Arrays.compareUnsigned(arrA, arrB) < 0
    )
  }

  @Test def compareUnsigned_Byte_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 4 // an arbitrary site
    val changeTo = jl.Byte.MIN_VALUE

    val arrB = new Array[scala.Byte](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toByte

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // Test that signed & unsigned comparison results differ.
    assertTrue(
      s"signed: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    assertTrue(
      s"unsigned: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx
      ) < 0
    )

    // ranges which used to match no longer do when content in one has changed.
    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Byte_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Byte](srcSize)
    val arrB = new Array[scala.Byte](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Byte_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Byte](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toByte

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toByte

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Byte_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Byte](srcSize)
    val arrB = new Array[scala.Byte](srcSize)

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

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val arrAPrime = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toByte

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Byte](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toByte

    val arrB = new Array[scala.Byte](srcSize)
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

    val arrA = new Array[scala.Byte](srcSize)
    val arrB = new Array[scala.Byte](srcSize)

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

    val arrA = new Array[scala.Byte](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toByte

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Byte](srcSize)
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

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfCharTestOnJDK9 {

  @Test def compare_Char_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Character.MIN_VALUE

    val arrA = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toChar

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Char_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    val arrB = new Array[scala.Char](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Char_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toChar

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Character.MIN_VALUE

    val arrB = new Array[scala.Char](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toChar

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Char_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    val arrB = new Array[scala.Char](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Char_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toChar

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Char](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toChar

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toChar

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Char_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    val arrB = new Array[scala.Char](srcSize)

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

  @Test def mismatch_Char_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toChar

    val arrAPrime = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toChar

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Char](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toChar

    val arrB = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toChar

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toChar

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

  @Test def mismatch_Char_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    val arrB = new Array[scala.Char](srcSize)

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

  @Test def mismatch_Char_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toChar

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Char](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toChar

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
    arrB(changeBAt) = 5.toChar

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfDoubleTestOnJDK9 {

  @Test def compare_Double_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Double.MIN_VALUE

    val arrA = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toDouble

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Double_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    val arrB = new Array[scala.Double](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Double_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toDouble

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Double.MIN_VALUE

    val arrB = new Array[scala.Double](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toDouble

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx),
      -0.0d
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Double_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    val arrB = new Array[scala.Double](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Double_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toDouble

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Double](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toDouble

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx),
      -0.0d
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toDouble

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Double_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    val arrB = new Array[scala.Double](srcSize)

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

  @Test def mismatch_Double_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toDouble

    val arrAPrime = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toDouble

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Double](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toDouble

    val arrB = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toDouble

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toDouble

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

  @Test def mismatch_Double_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    val arrB = new Array[scala.Double](srcSize)

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

  @Test def mismatch_Double_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Double](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toDouble

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Double](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toDouble

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx),
      -0.0d
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
    arrB(changeBAt) = 5.toDouble

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfFloatTestOnJDK9 {

  @Test def compare_Float_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Float.MIN_VALUE

    val arrA = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toFloat

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Float_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    val arrB = new Array[scala.Float](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Float_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toFloat

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Float.MIN_VALUE

    val arrB = new Array[scala.Float](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toFloat

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx),
      -0.0f
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Float_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    val arrB = new Array[scala.Float](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Float_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toFloat

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Float](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toFloat

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx),
      -0.0f
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toFloat

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Float_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    val arrB = new Array[scala.Float](srcSize)

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

  @Test def mismatch_Float_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toFloat

    val arrAPrime = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toFloat

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Float](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toFloat

    val arrB = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toFloat

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toFloat

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

  @Test def mismatch_Float_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    val arrB = new Array[scala.Float](srcSize)

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

  @Test def mismatch_Float_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Float](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toFloat

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Float](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toFloat

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx),
      -0.0f
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
    arrB(changeBAt) = 5.toFloat

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfIntTestOnJDK9 {

  @Test def compare_Int_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Integer.MIN_VALUE

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Int_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    val arrB = new Array[scala.Int](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Int_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Integer.MIN_VALUE

    val arrB = new Array[scala.Int](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toInt

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def compareUnsigned_Int_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 12
    val changeTo = jl.Integer.MIN_VALUE

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compareUnsigned(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a < b",
      Arrays.compareUnsigned(arrA, arrB) < 0
    )
  }

  @Test def compareUnsigned_Int_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 4 // an arbitrary site
    val changeTo = jl.Integer.MIN_VALUE

    val arrB = new Array[scala.Int](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toInt

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // Test that signed & unsigned comparison results differ.
    assertTrue(
      s"signed: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    assertTrue(
      s"unsigned: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx
      ) < 0
    )

    // ranges which used to match no longer do when content in one has changed.
    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Int_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    val arrB = new Array[scala.Int](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Int_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Int](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toInt

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toInt

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Int_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    val arrB = new Array[scala.Int](srcSize)

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

  @Test def mismatch_Int_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val arrAPrime = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toInt

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Int](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toInt

    val arrB = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toInt

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toInt

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

  @Test def mismatch_Int_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    val arrB = new Array[scala.Int](srcSize)

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

  @Test def mismatch_Int_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Int](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toInt

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Int](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toInt

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
    arrB(changeBAt) = 5.toInt

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfLongTestOnJDK9 {

  @Test def compare_Long_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Long.MIN_VALUE

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Long_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    val arrB = new Array[scala.Long](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Long_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Long.MIN_VALUE

    val arrB = new Array[scala.Long](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toLong

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def compareUnsigned_Long_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 12
    val changeTo = jl.Long.MIN_VALUE

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compareUnsigned(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a < b",
      Arrays.compareUnsigned(arrA, arrB) < 0
    )
  }

  @Test def compareUnsigned_Long_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 4 // an arbitrary site
    val changeTo = jl.Long.MIN_VALUE

    val arrB = new Array[scala.Long](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toLong

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // Test that signed & unsigned comparison results differ.
    assertTrue(
      s"signed: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    assertTrue(
      s"unsigned: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx
      ) < 0
    )

    // ranges which used to match no longer do when content in one has changed.
    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Long_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    val arrB = new Array[scala.Long](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Long_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Long](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toLong

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toLong

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Long_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    val arrB = new Array[scala.Long](srcSize)

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

  @Test def mismatch_Long_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val arrAPrime = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toLong

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Long](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toLong

    val arrB = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toLong

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toLong

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

  @Test def mismatch_Long_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    val arrB = new Array[scala.Long](srcSize)

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

  @Test def mismatch_Long_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Long](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toLong

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Long](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toLong

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
    arrB(changeBAt) = 5.toLong

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfShortTestOnJDK9 {

  @Test def compare_Short_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 3
    val changeTo =
      jl.Short.MIN_VALUE

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compare(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a > b",
      Arrays.compare(arrA, arrB) > 0
    )
  }

  @Test def compare_Short_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    val arrB = new Array[scala.Short](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.compare(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.compare(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.compare(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.compare(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def compare_Short_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    val changeTo =
      jl.Short.MIN_VALUE

    val arrB = new Array[scala.Short](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toShort

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compare(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def compareUnsigned_Short_2Arg(): Unit = {
    val srcSize = 16

    val changeAt = 12
    val changeTo = jl.Short.MIN_VALUE

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val arrA2 = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      s"a == aClone",
      0,
      Arrays.compareUnsigned(arrA, arrA2)
    )

    val arrB = Arrays.copyOf(arrA, srcSize)
    arrB(changeAt) = changeTo

    assertTrue(
      s"a < b",
      Arrays.compareUnsigned(arrA, arrB) < 0
    )
  }

  @Test def compareUnsigned_Short_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val changeBAt = t2FromIdx + 4 // an arbitrary site
    val changeTo = jl.Short.MIN_VALUE

    val arrB = new Array[scala.Short](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toShort

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertNotEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      0,
      Arrays.compareUnsigned(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    arrB(changeBAt) = changeTo

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // Test that signed & unsigned comparison results differ.
    assertTrue(
      s"signed: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx) > 0
    )

    assertTrue(
      s"unsigned: a[${t1FromIdx}, ${t1ToIdx}) < b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx
      ) < 0
    )

    // ranges which used to match no longer do when content in one has changed.
    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertTrue(
      "common prefix but a.length < b.length, return former",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length, return later",
      Arrays.compareUnsigned(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def equals_Short_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    val arrB = new Array[scala.Short](srcSize)

    assertThrows(
      "null arg1",
      classOf[NullPointerException],
      Arrays.equals(null, 0, srcSize, arrB, 0, srcSize)
    )

    assertThrows(
      "invalid arg, aFromIndex > aToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 10, 2, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, -1, srcSize, arrB, 1, srcSize)
    )

    assertThrows(
      "invalid aToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize * 2, arrB, 1, srcSize >> 2)
    )

    assertThrows(
      "null arg2",
      classOf[NullPointerException],
      Arrays.equals(arrA, 0, srcSize, null, 0, srcSize)
    )

    assertThrows(
      "invalid arg, bFromIndex > bToIndex",
      classOf[IllegalArgumentException],
      Arrays.equals(arrA, 0, srcSize, arrB, srcSize, 1)
    )

    assertThrows(
      "invalid bFromIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 3, srcSize, arrB, -3, srcSize)
    )

    assertThrows(
      "invalid bToIndex",
      classOf[ArrayIndexOutOfBoundsException],
      Arrays.equals(arrA, 4, srcSize - 2, arrB, 1, srcSize * 2)
    )
  }

  @Test def equals_Short_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Short](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toShort

    assertArrayEquals(
      s"array range contents",
      Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
      Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t1FromIdx, t1ToIdx)
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val changeBAt = t2FromIdx + 2 // an arbitrary site
    arrB(changeBAt) = 5.toShort

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(arrA, t1FromIdx, t1ToIdx, arrB, t2FromIdx, t2ToIdx)
    )

    val t3ToIdx = t1ToIdx - 3 // arbitrary slot

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Short_2Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    val arrB = new Array[scala.Short](srcSize)

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

  @Test def mismatch_Short_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val arrAPrime = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrAPrime(idx) = (idx + 1).toShort

    val expectedShortSize = srcSize >> 2
    val arrAShort = new Array[scala.Short](expectedShortSize)
    for (idx <- 0 until expectedShortSize)
      arrAShort(idx) = (idx + 1).toShort

    val arrB = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrB(idx) = (idx + 1).toShort

    val mismatchAt = 33 // 33 is just somewhere in the middle
    arrB(mismatchAt) = 255.toShort

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

  @Test def mismatch_Short_6Arg_InvalidArgs(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    val arrB = new Array[scala.Short](srcSize)

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

  @Test def mismatch_Short_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[scala.Short](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = (idx + 1).toShort

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[scala.Short](srcSize)
    for (idx <- t2FromIdx until srcSize) // fill extra length to entice bugs
      arrB(idx) = (idx - t1Shift + 1).toShort

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
    arrB(changeBAt) = 5.toShort

    assertFalse(
      s"changed array range contents",
      Arrays.equals(
        Arrays.copyOfRange(arrA, t1FromIdx, t1ToIdx),
        Arrays.copyOfRange(arrB, t2FromIdx, t2ToIdx)
      )
    )

    // ranges which used to match no longer do when content in one has changed.
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

class ArraysOfCharCornerCasesTestOnJDK9 {
  /* Arrays.equals() in JDK 8 takes two arguments.
   * Arrays.compare() and Arrays.mismatch() appear in JDK9 and have
   * overloads with varying number of arguments.
   *
   * Tests here for JDK 9 Arrays.equals() should not use the 2 argument
   * form unless explicitly intending to exercise the JDK 8 implementation.
   *
   * The six argument form of Arrays.equals() is used in this class to
   * exercise the JDK 9 implementation.
   *
   * Even though the two argument overloads of Arrays.compare() and
   * Arrays.mismatch() are suitable, use the apparently excessive
   * six argument overlay to calls to equals() similar. This makes it
   * harder to inadvertently use the two argument form of equals().
   * Can you say "Cut & Paste" error?
   */

  @Test def charConsidersUpperByte: Unit = {
    /* An implementation naively using memcmp() will fail at least the
     * compare() case.
     */

    val srcSize = 64

    val arrA = new Array[scala.Char](srcSize)

    arrA(srcSize - 8) = '\u0F00' // 8 is arbitrary, so arrA not zero everywhere

    val arrB = Arrays.copyOf(arrA, srcSize)

    assertFalse("a.eq(b)", arrA.eq(arrB))

    assertEquals(
      "a.equals(b)",
      0,
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    val changeAt = 62
    arrA(changeAt) = '\u00FF'
    arrB(changeAt) = '\u0100'

    assertTrue(
      "a < b",
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length) < 0
    )

    assertFalse(
      "a != b",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    assertEquals(
      "mismatch position,",
      changeAt,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }

  private def newArrayOfGreekCharacters(size: Int): Array[Char] = {
    // There are probably more direct and/or efficient ways of doing this.

    val sb = new jl.StringBuilder(size)

    val seed = 20250329 // An arbitrary value
    val rng = new ju.Random(seed)

// format: off
    /* 0x0370 to 0x03FF is the Unicode "Greek and Coptic" block
     * Some of the codepoints in the block are reserved and do not
     * display well when debugging. Otherwise, throw a broad assortment
     * of letters, punctuation marks, and others at the
     * implementation under test.
     */
    val reservedCodePoints = ju.List.of(
        0x0378, 0x0379,
        0x0380, 0x0381, 0x0382, 0x0383, 0x038b, 0x038d,
        0x03a2
      )
// format: on

    rng
      .ints(0x0370, 0x03ff)
      .filter(cp => !reservedCodePoints.contains(cp))
      .limit(size)
      .forEach(cp => sb.append(Character.toChars(cp)))

    sb.toString.toCharArray()
  }

  @Test def exerciseGreekCharacters: Unit = {
    /* Reviewer JD557 recommended exercising Non-Latin-1 characters
     * to strengthen coverage. A suggeston well worth implementing.
     *
     * People familiar with other Non-Latin-1 character sets are
     * encouraged to add additional Tests for that set.
     */

    val srcSize = 256
    val changeAt = srcSize - 2

    val arrA = newArrayOfGreekCharacters(srcSize)

    arrA(changeAt) = 'Γ' // establish arbitrary but known value before change

    val arrB = Arrays.copyOf(arrA, srcSize)

    assertFalse("!a.eq(b)", arrA.eq(arrB))
    assertTrue("a == b", Arrays.compare(arrA, arrB) == 0)

    arrB(changeAt) = 'Δ' // something Greek and not capital gamma

    assertTrue(
      "a < b",
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length) < 0
    )

    assertFalse(
      "a != b",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    assertEquals(
      "mismatch position,",
      changeAt,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }

  /* Tests of Non-BMP (basic multilingual plan) characters
   */

  @Test def exerciseHighSurrogateCharacters: Unit = {
    val srcSize = 9
    val changeAt = srcSize - 3

    val arrA = new Array[scala.Char](srcSize)
    val arrB = new Array[scala.Char](srcSize)

    // High surrogates (first or index-0 byte) differ
    Character.toChars(0x1f308, arrA, changeAt) // Rainbow emoji
    Character.toChars(0x1f648, arrB, changeAt) // See-no-evil monkey emoji

    assertTrue(
      "a < b",
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length) < 0
    )

    assertFalse(
      "a != b",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    assertEquals(
      "mismatch position,",
      changeAt,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }

  @Test def exerciseLowSurrogateCharacters: Unit = {
    val srcSize = 11
    val changeAt = srcSize - 4
    val mismatchAt = changeAt + 1

    val arrA = new Array[scala.Char](srcSize)
    val arrB = new Array[scala.Char](srcSize)

    // High surrogates (first or index-0 byte) same, low surrogates differ
    Character.toChars(0x1f64a, arrA, changeAt) // Hear-no-evil monkey emoji
    Character.toChars(0x1f648, arrB, changeAt) // See-no-evil monkey emoji

    assertTrue(
      "a > b",
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length) > 0
    )

    assertFalse(
      "a != b",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    assertEquals(
      "mismatch position,",
      mismatchAt,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }
}

class ArraysOfDoubleCornerCasesTestOnJDK9 {
  /* The exerciseDouble* tests check that the JDK behavior for compareTo is
   * being followed, not IEEE 754 specification.
   *
   * Similar tests could, and someday should, be written to exercise Float,
   * which shares similar Java definitions.
   */

  @Test def exerciseDoubleNegativeZero: Unit = {
    /* See Scala Native Issues #3982 & #3986 re SN bugs with -0.0.
     *
     * Also there is a bug in the SN JDK8 implementation of
     * Arrays.equals(a, b) for Double and probably also for Float.
     */

    val srcSize = 16

    val arrA = new Array[scala.Double](srcSize)
    val arrB = new Array[scala.Double](srcSize)

    // convoluted initialization works around suspected SN bugs
    val negativeZero = jl.Double.longBitsToDouble(0x8000000000000000L)

    val changeAt = 10
    val changeTo = negativeZero

    arrB(changeAt) = changeTo

    // Increase confidence that we are using a true negative zero at Array pos
    assertEquals(
      "have true negative zero",
      1.0 / arrB(changeAt),
      jl.Double.NEGATIVE_INFINITY,
      -0.0
    )

    assertTrue(
      "a(changeAt) == b(changeAt)",
      arrA(changeAt) == arrB(changeAt)
    )

    assertFalse(
      "a(changeAt).equals(b(changeAt))",
      arrA(changeAt).equals(arrB(changeAt))
    )

    assertTrue(
      "a(changeAt).compareTo(b(changeAt)) > 0",
      arrA(changeAt).compareTo(arrB(changeAt)) > 0
    )

    assertFalse(
      "a.equals(b), 6 Arg",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    /* Use the 2 argument overload to cross check the JDK 9 six argument
     * implementation and the historical method: results should be the same.
     *
     * Expand coverage to Scala Native once its 2 argument overload is
     * corrected.
     */

    if (Platform.executingInJVM)
      assertFalse("a.equals(b), 2 arg", Arrays.equals(arrA, arrB))

    assertTrue(
      "a > b",
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length) > 0
    )

    assertEquals(
      "mismatch position,",
      changeAt,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }

  @Test def exerciseDoubleJavaNaNs: Unit = {
    val srcSize = 16

    // The simplest IEEE 754 NaN
    val javaNaN = jl.Double.NaN

    val changeAt = 10
    val changeTo = javaNaN
    val arrA = new Array[scala.Double](srcSize)

    arrA(changeAt) = changeTo

    val arrB = Arrays.copyOf(arrA, srcSize)

    assertEquals(
      "have java NaN",
      arrB(changeAt),
      jl.Double.NaN,
      0.0
    )

    assertFalse("a(changeAt) == b(changeAt)", arrA(changeAt) == arrB(changeAt))

    // Java equals() specifies true for NaN. IEEE 754 would be false, as above.
    assertTrue(
      "a(changeAt).equals(b(changeAt))",
      arrA(changeAt).equals(arrB(changeAt))
    )

    assertTrue(
      "a(changeAt).compareTo(b(changeAt)) == 0",
      arrA(changeAt).compareTo(arrB(changeAt)) == 0
    )

    assertTrue(
      "a.equals(b), 6 Arg",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    /* Use the 2 argument overload to cross check the JDK 9 six argument
     * implementation and the historical method: results should be the same.
     */

    if (Platform.executingInJVM)
      assertTrue("a.equals(b), 2 Arg", Arrays.equals(arrA, arrB))

    assertEquals(
      "a.compareTo(b)",
      0,
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    assertEquals(
      "mismatch position,",
      -1,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }

  @Test def exerciseDoublePayloadNaNs: Unit = {
    /* Ensure that the implementation is either not doing bitwise testing
     * of IEEE 754 values or is doing it in a sufficiently clever way.
     */

    val srcSize = 15

    val javaNaNRawLongBits = 0x7ff8000000000000L

    // 0xF and 0xF0 are arbitrary valid values, to make bit patterns differ.
    val payloadNaN_1 = jl.Double.longBitsToDouble(javaNaNRawLongBits | 0xf)

    val payloadNaN_2 = jl.Double.longBitsToDouble(javaNaNRawLongBits | 0xf0)

    val arrA = new Array[scala.Double](srcSize)
    val arrB = new Array[scala.Double](srcSize)

    val changeAt = 9

    arrA(changeAt) = payloadNaN_1
    arrB(changeAt) = payloadNaN_2

    assertEquals("payloadNaN_1", arrA(changeAt), jl.Double.NaN, 0.0)

    assertEquals("payloadNaN_2", arrB(changeAt), jl.Double.NaN, 0.0)

    assertFalse(
      "a(changeAt) == b(changeAt)",
      arrA(changeAt) == arrB(changeAt)
    )

    // Java equals() specifies true for NaN. IEEE 754 would be false, as above.
    assertTrue(
      "a(changeAt).equals(b(changeAt))",
      arrA(changeAt).equals(arrB(changeAt))
    )

    assertTrue(
      "a(changeAt).compareTo(b(changeAt)) == 0",
      arrA(changeAt).compareTo(arrB(changeAt)) == 0
    )

    assertTrue(
      "a.equals(b), 6 Arg",
      Arrays.equals(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    /* Use the 2 argument overload to cross check the JDK 9 six argument
     * implementation and the historical method: results should be the same.
     */

    if (Platform.executingInJVM)
      assertTrue("a.equals(b), 2 Arg", Arrays.equals(arrA, arrB))

    assertEquals(
      "a.compareTo(b)",
      0,
      Arrays.compare(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )

    assertEquals(
      "mismatch position,",
      -1,
      Arrays.mismatch(arrA, 0, arrA.length, arrB, 0, arrB.length)
    )
  }
}
