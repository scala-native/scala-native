package org.scalanative.testsuite.javalib.nio.file

/* This code is generated from BuffersMismatchTestOnJDK11.scala.gyb.
 * Any edits here and not in the .gyb will be lost when this file is next
 * generated.
 */


// format: off

import java.nio.ByteOrder

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.nio.ShortBuffer

import java.{util => ju}

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class BuffersMismatchTestOnJDK11 {

  private val fullData =
    ju.List.of('-', '|', 'a', 'b', 'c', 'd', 'e', '|', '-', '$')

  private val matchData =
    ju.List.of('a', 'b', 'c', 'd', 'e')

  @Test def byteBuffersMismatchBE(): Unit = {

    val byteBufA = ByteBuffer.allocate(matchData.size())
    matchData.forEach(e => byteBufA.put(e.toByte))
    byteBufA.flip()

    /* Try to trip things up and expose bugs.
     * Skew byteBufferB so that its position is not zero and it has both
     *	a matching range and some characters in the buffer after that range.
     */
    val byteBufB = ByteBuffer.allocate(fullData.size())
    fullData.forEach(e => byteBufB.put(e.toByte))

    val startPositionByteBufB = 2
    val endLimitByteBufB = byteBufB.limit() - 3
    byteBufB
      .flip()
      .position(startPositionByteBufB)
      .limit(endLimitByteBufB)

    val byteBufC = ByteBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => byteBufC.put(e.toByte))
    byteBufC.put('M'.toByte)
    byteBufC.flip()

    assertEquals("bufA should match bufB", -1, byteBufA.mismatch(byteBufB))

    assertEquals("byteBufA position should not move", 0, byteBufA.position())
    assertEquals(
      "byteBufB position should not move",
      startPositionByteBufB,
      byteBufB.position()
    )

    val changeAt = 3
    byteBufB.put(startPositionByteBufB + changeAt, 'z'.toByte)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      byteBufA.mismatch(byteBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      byteBufA.mismatch(byteBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      byteBufC.mismatch(byteBufA)
    )
  }

  @Test def byteBuffersMismatchLE(): Unit = {

    val byteBufA = ByteBuffer
      .allocate(matchData.size())
      .order(ByteOrder.LITTLE_ENDIAN)
    matchData.forEach(e => byteBufA.put(e.toByte))
    byteBufA.flip()

    /* Try to trip things up and expose bugs.
     * Skew byteBufferB so that its position is not zero and it has both
     *	a matching range and some characters in the buffer after that range.
     */
    val byteBufB = ByteBuffer
      .allocate(fullData.size())
      .order(ByteOrder.LITTLE_ENDIAN)
    fullData.forEach(e => byteBufB.put(e.toByte))

    val startPositionByteBufB = 2
    val endLimitByteBufB = byteBufB.limit() - 3
    byteBufB
      .flip()
      .position(startPositionByteBufB)
      .limit(endLimitByteBufB)

    val byteBufC = ByteBuffer
      .allocate(matchData.size() + 1)
      .order(ByteOrder.LITTLE_ENDIAN)
    matchData.forEach(e => byteBufC.put(e.toByte))
    byteBufC.put('M'.toByte)
    byteBufC.flip()

    assertEquals("bufA should match bufB", -1, byteBufA.mismatch(byteBufB))

    assertEquals("byteBufA position should not move", 0, byteBufA.position())
    assertEquals(
      "byteBufB position should not move",
      startPositionByteBufB,
      byteBufB.position()
    )

    val changeAt = 3
    byteBufB.put(startPositionByteBufB + changeAt, 'z'.toByte)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      byteBufA.mismatch(byteBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      byteBufA.mismatch(byteBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      byteBufC.mismatch(byteBufA)
    )
  }

  @Test def byteBuffersMismatchReadOnly(): Unit = {

    val byteBufA = {
      val bb = ByteBuffer.allocate(matchData.size())
      matchData.forEach(e => bb.put(e.toByte))
      bb.asReadOnlyBuffer()
    }

    byteBufA.flip()

    val byteBufB = {
      val bb = ByteBuffer.allocate(matchData.size())
      matchData.forEach(e => bb.put(e.toByte))
      bb.asReadOnlyBuffer()
    }

    byteBufB.flip()

    val mismatchAt = 3

    val byteBufC = {
      val bb = ByteBuffer.allocate(matchData.size())
      matchData.forEach(e => bb.put(e.toByte))
      bb.put(mismatchAt, 'z'.toByte).asReadOnlyBuffer()
    }

    byteBufC.flip()

    val byteBufD = {
      val bb = ByteBuffer.allocate(matchData.size() + 1)
      matchData.forEach(e => bb.put(e.toByte))
      bb.put('M'.toByte).asReadOnlyBuffer()
    }

    byteBufD.flip()

    assertEquals("bufA should match bufB", -1, byteBufA.mismatch(byteBufB))

    assertEquals("byteBufA position should not move", 0, byteBufA.position())
    assertEquals("byteBufB position should not move", 0, byteBufB.position())

    assertEquals(
      "bufA should mismatch bufC",
      mismatchAt,
      byteBufA.mismatch(byteBufC)
    )

    assertEquals(
      "bufA < bufD",
      matchData.size(),
      byteBufA.mismatch(byteBufD)
    )

    assertEquals(
      "bufD < bufA",
      matchData.size(),
      byteBufD.mismatch(byteBufA)
    )
  }


  @Test def CharBuffersMismatchNativeOrder(): Unit = {

    val charBufA = CharBuffer.allocate(matchData.size())
    matchData.forEach(e => charBufA.put(e.toChar))
    charBufA.flip()

    val charBufB = CharBuffer.allocate(matchData.size())
    matchData.forEach(e => charBufB.put(e.toChar))
    charBufB.flip()

    val charBufC = CharBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => charBufC.put(e.toChar))
    charBufC.put('M'.toChar)
    charBufC.flip()

    assertEquals("bufA should match bufB",
                 -1,
                 charBufA.mismatch(charBufB))

    assertEquals("charBufA position should not move",
                 0,
                 charBufA.position())
    assertEquals("charBufB position should not move",
                 0,
                 charBufB.position())

    val changeAt = 4
    charBufB.put(changeAt, 'z'.toChar)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      charBufA.mismatch(charBufB)
    )

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      charBufA.mismatch(charBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      charBufA.mismatch(charBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      charBufC.mismatch(charBufA)
    )
  }


  @Test def DoubleBuffersMismatchNativeOrder(): Unit = {

    val doubleBufA = DoubleBuffer.allocate(matchData.size())
    matchData.forEach(e => doubleBufA.put(e.toDouble))
    doubleBufA.flip()

    val doubleBufB = DoubleBuffer.allocate(matchData.size())
    matchData.forEach(e => doubleBufB.put(e.toDouble))
    doubleBufB.flip()

    val doubleBufC = DoubleBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => doubleBufC.put(e.toDouble))
    doubleBufC.put('M'.toDouble)
    doubleBufC.flip()

    assertEquals("bufA should match bufB",
                 -1,
                 doubleBufA.mismatch(doubleBufB))

    assertEquals("doubleBufA position should not move",
                 0,
                 doubleBufA.position())
    assertEquals("doubleBufB position should not move",
                 0,
                 doubleBufB.position())

    val changeAt = 4
    doubleBufB.put(changeAt, 'z'.toDouble)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      doubleBufA.mismatch(doubleBufB)
    )

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      doubleBufA.mismatch(doubleBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      doubleBufA.mismatch(doubleBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      doubleBufC.mismatch(doubleBufA)
    )
  }


  @Test def FloatBuffersMismatchNativeOrder(): Unit = {

    val floatBufA = FloatBuffer.allocate(matchData.size())
    matchData.forEach(e => floatBufA.put(e.toFloat))
    floatBufA.flip()

    val floatBufB = FloatBuffer.allocate(matchData.size())
    matchData.forEach(e => floatBufB.put(e.toFloat))
    floatBufB.flip()

    val floatBufC = FloatBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => floatBufC.put(e.toFloat))
    floatBufC.put('M'.toFloat)
    floatBufC.flip()

    assertEquals("bufA should match bufB",
                 -1,
                 floatBufA.mismatch(floatBufB))

    assertEquals("floatBufA position should not move",
                 0,
                 floatBufA.position())
    assertEquals("floatBufB position should not move",
                 0,
                 floatBufB.position())

    val changeAt = 4
    floatBufB.put(changeAt, 'z'.toFloat)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      floatBufA.mismatch(floatBufB)
    )

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      floatBufA.mismatch(floatBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      floatBufA.mismatch(floatBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      floatBufC.mismatch(floatBufA)
    )
  }


  @Test def IntBuffersMismatchNativeOrder(): Unit = {

    val integerBufA = IntBuffer.allocate(matchData.size())
    matchData.forEach(e => integerBufA.put(e.toInt))
    integerBufA.flip()

    val integerBufB = IntBuffer.allocate(matchData.size())
    matchData.forEach(e => integerBufB.put(e.toInt))
    integerBufB.flip()

    val integerBufC = IntBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => integerBufC.put(e.toInt))
    integerBufC.put('M'.toInt)
    integerBufC.flip()

    assertEquals("bufA should match bufB",
                 -1,
                 integerBufA.mismatch(integerBufB))

    assertEquals("integerBufA position should not move",
                 0,
                 integerBufA.position())
    assertEquals("integerBufB position should not move",
                 0,
                 integerBufB.position())

    val changeAt = 4
    integerBufB.put(changeAt, 'z'.toInt)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      integerBufA.mismatch(integerBufB)
    )

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      integerBufA.mismatch(integerBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      integerBufA.mismatch(integerBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      integerBufC.mismatch(integerBufA)
    )
  }


  @Test def LongBuffersMismatchNativeOrder(): Unit = {

    val longBufA = LongBuffer.allocate(matchData.size())
    matchData.forEach(e => longBufA.put(e.toLong))
    longBufA.flip()

    val longBufB = LongBuffer.allocate(matchData.size())
    matchData.forEach(e => longBufB.put(e.toLong))
    longBufB.flip()

    val longBufC = LongBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => longBufC.put(e.toLong))
    longBufC.put('M'.toLong)
    longBufC.flip()

    assertEquals("bufA should match bufB",
                 -1,
                 longBufA.mismatch(longBufB))

    assertEquals("longBufA position should not move",
                 0,
                 longBufA.position())
    assertEquals("longBufB position should not move",
                 0,
                 longBufB.position())

    val changeAt = 4
    longBufB.put(changeAt, 'z'.toLong)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      longBufA.mismatch(longBufB)
    )

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      longBufA.mismatch(longBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      longBufA.mismatch(longBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      longBufC.mismatch(longBufA)
    )
  }


  @Test def ShortBuffersMismatchNativeOrder(): Unit = {

    val shortBufA = ShortBuffer.allocate(matchData.size())
    matchData.forEach(e => shortBufA.put(e.toShort))
    shortBufA.flip()

    val shortBufB = ShortBuffer.allocate(matchData.size())
    matchData.forEach(e => shortBufB.put(e.toShort))
    shortBufB.flip()

    val shortBufC = ShortBuffer.allocate(matchData.size() + 1)
    matchData.forEach(e => shortBufC.put(e.toShort))
    shortBufC.put('M'.toShort)
    shortBufC.flip()

    assertEquals("bufA should match bufB",
                 -1,
                 shortBufA.mismatch(shortBufB))

    assertEquals("shortBufA position should not move",
                 0,
                 shortBufA.position())
    assertEquals("shortBufB position should not move",
                 0,
                 shortBufB.position())

    val changeAt = 4
    shortBufB.put(changeAt, 'z'.toShort)

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      shortBufA.mismatch(shortBufB)
    )

    assertEquals(
      "bufA should mismatch bufB",
      changeAt,
      shortBufA.mismatch(shortBufB)
    )

    assertEquals(
      "bufA < bufC",
      matchData.size(),
      shortBufA.mismatch(shortBufC)
    )

    assertEquals(
      "bufC < bufB",
      matchData.size(),
      shortBufC.mismatch(shortBufA)
    )
  }


}
