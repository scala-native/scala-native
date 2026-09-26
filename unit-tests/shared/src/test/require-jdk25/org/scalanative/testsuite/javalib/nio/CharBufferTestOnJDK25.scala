package org.scalanative.testsuite.javalib.nio

import java.nio._
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharBufferTestOnJDK25 {

  @Test def getChars_Exceptions(): Unit = {
    val cb = CharBuffer.wrap("Exceptions")
    val dst = new Array[Char](20)

    assertThrows(
      s"srcBegin negative",
      classOf[IndexOutOfBoundsException],
      cb.getChars(-1, 3, dst, 1)
    )

    locally {
      val srcBegin = cb.limit() + 1
      val lMinusP = cb.limit() - cb.position()

      assertThrows(
        s"srcBegin: ${srcBegin} greater than limit() - position(): ${lMinusP}",
        classOf[IndexOutOfBoundsException],
        cb.getChars(cb.limit() + 1, 3, dst, 1)
      )
    }

    locally {
      val srcBegin = 3
      val srcEnd = 2

      assertThrows(
        s"srcEnd: ${srcEnd} less than srcBegin: ${srcBegin}",
        classOf[IndexOutOfBoundsException],
        cb.getChars(srcBegin, srcEnd, dst, 1)
      )
    }

    locally {
      val srcEnd = cb.limit() + 1
      val lMinusP = cb.limit() - cb.position()

      assertThrows(
        s"srcEnd: ${srcEnd} greater than limit() - position(): ${lMinusP} ",
        classOf[IndexOutOfBoundsException],
        cb.getChars(3, srcEnd, dst, 1)
      )
    }

    locally {
      assertThrows(
        s"dstBegin negative",
        classOf[IndexOutOfBoundsException],
        cb.getChars(1, 3, dst, -1)
      )

      val dstBegin = dst.length + 1

      assertThrows(
        s"dstBegin: ${dstBegin} greater than dst.length: ${dst.length}",
        classOf[IndexOutOfBoundsException],
        cb.getChars(2, 4, dst, dstBegin)
      )
    }
  }

  @Test def getChars(): Unit = {
    val wellspring = // R. P. Feynman
      "The highest forms of understanding we can achieve are laughter" +
        " and human compassion."

    val cb = CharBuffer.wrap(wellspring)

    val origin = 54
    val expected =
      s"RF: ${wellspring.substring(origin, (wellspring.length - 1))}"

    val nChars = expected.length - 3 // remove "RF:" prefix

    val srcBegin = 3
    val srcBeginOffset = origin - srcBegin - 1
    val srcEnd = srcBegin + nChars

    val dstBegin = 3
    val dst = new Array[Char](expected.length)
    ju.Arrays.fill(dst, 'Z')
    dst(0) = 'R'
    dst(1) = 'F'
    dst(2) = ':'

    cb.position(srcBeginOffset) // complicate things a bit, cb position not 0

    cb.getChars(srcBegin, srcEnd, dst, dstBegin)

    val dstAsString = new String(dst)
    assertTrue(
      s"""\n\texpected: "${expected}"\n\treceived: "${dstAsString}"""",
      dstAsString.compareTo(expected) == 0
    )
  }
}
