package org.scalanative.testsuite.javalib.lang

import java.{lang => jl}

import java.util.Arrays

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Test only the Java 8 default methods of java.lang.CharSequence.
 * Abstract methods may get tested in the Tests of classes which
 * implement the CharSequence interface.
 */

class CharSequenceTest {

  @Test def charseqChars(): Unit = {
    val src = "secure the Blessings of Liberty to ourselves and our Posterity"

    val charSeq = new String(src)
    val srcChars = charSeq.toCharArray()
    val srcCharLen = srcChars.length

    assertEquals("srcCharLen", src.length, srcCharLen)

    val csInts = charSeq.chars().toArray

    val csIntsLen = csInts.length
    assertEquals("chars().toArray length", srcChars.length, csIntsLen)

    assertEquals("first character", 's'.toInt, csInts(0))
    assertEquals("last character", 'y'.toInt, csInts(csIntsLen - 1))

    for (j <- 1 until srcCharLen - 1)
      assertEquals("character at index: ${j}", srcChars(j).toInt, csInts(j))
  }

  @Test def charseqCharsPassesSurogatesUnchanged(): Unit = {
    val highSurrogateByte = '\uD8FF'
    val lowSurrogateByte = '\uDCFF'
    val src = s"a${highSurrogateByte}${lowSurrogateByte}z"

    val charSeq = new String(src)
    val srcChars = charSeq.toCharArray()
    val srcCharLen = srcChars.length

    assertEquals("srcCharLen", src.length, srcCharLen)

    val csInts = charSeq.chars().toArray

    val csIntsLen = csInts.length
    assertEquals("chars().toArray length", srcChars.length, csIntsLen)

    assertEquals("first character", 'a'.toInt, csInts(0))

    assertEquals("second character", highSurrogateByte.toInt, csInts(1))
    assertEquals("third character", lowSurrogateByte.toInt, csInts(2))

    assertEquals("last character", 'z'.toInt, csInts(3))
  }

  @Test def charseqCodePoints(): Unit = {
    val src = "secure the Blessings of Liberty to ourselves and our Posterity"

    val charSeq = new String(src)
    val srcChars = charSeq.toCharArray()
    val srcCharLen = srcChars.length

    assertEquals("srcCharLen", src.length, srcCharLen)

    val csCodePoints = charSeq.codePoints().toArray

    val csCodePointsLen = csCodePoints.length
    assertEquals("chars.toArray() length", srcChars.length, csCodePointsLen)

    assertEquals("first character", 's'.toInt, csCodePoints(0))
    assertEquals("last character", 'y'.toInt, csCodePoints(csCodePointsLen - 1))

    for (j <- 1 until srcCharLen - 1)
      assertEquals(
        "character at index: ${j}",
        srcChars(j).toInt,
        csCodePoints(j)
      )
  }

  @Test def charseqCodePointsCombinesSurogatePairs(): Unit = {
    val highSurrogateByte = '\uD8FF'
    val lowSurrogateByte = '\uDCFF'
    val src = s"a${highSurrogateByte}${lowSurrogateByte}z"

    val charSeq = new String(src)
    val srcChars = charSeq.toCharArray()
    val srcCharLen = srcChars.length

    assertEquals("srcCharLen", src.length, srcCharLen)

    val csCodePoints = charSeq.codePoints().toArray

    val csCodePointsLen = csCodePoints.length

    // csCodePointsLen will differ by one if surrogate pair was combined.
    assertEquals("chars().toArray length", srcChars.length - 1, csCodePointsLen)

    assertEquals("first character", 'a'.toInt, csCodePoints(0))

    val combinedCodePoint = charSeq.codePointAt(1)
    assertEquals("combined codePoint", combinedCodePoint, csCodePoints(1))

    assertEquals("last character", 'z'.toInt, csCodePoints(2))
  }

}
