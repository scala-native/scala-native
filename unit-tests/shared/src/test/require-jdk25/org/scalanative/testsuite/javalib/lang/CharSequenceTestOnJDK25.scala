package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharSequenceTestOnJDK25 {

  class TestCharSequence(underlying: String) extends CharSequence {
    def charAt(x0: Int): Char = underlying.charAt(x0)
    def length(): Int = underlying.length()
    def subSequence(x0: Int, x1: Int): CharSequence =
      underlying.subSequence(x0, x1)
  }

  @Test def getChars_Exceptions(): Unit = {
    val cs = new TestCharSequence("Exceptions")
    val dst = new Array[Char](cs.length)

    assertThrows(
      s"srcBegin negative",
      classOf[IndexOutOfBoundsException],
      cs.getChars(-1, 3, dst, 1)
    )

    assertThrows(
      "dstBegin is negative",
      classOf[IndexOutOfBoundsException],
      cs.getChars(1, 3, dst, -2)
    )

    locally {
      val srcBegin = 3
      val srcEnd = 2

      assertThrows(
        s"srcBegin: ${srcBegin} greater than srcEnd: ${srcEnd}",
        classOf[IndexOutOfBoundsException],
        cs.getChars(srcBegin, srcEnd, dst, 1)
      )
    }

    locally {
      val srcEnd = 99

      assertThrows(
        s"srcEnd: ${srcEnd} greater than ${cs.length}",
        classOf[IndexOutOfBoundsException],
        cs.getChars(3, srcEnd, dst, 1)
      )
    }

    assertThrows(
      s"dstBegin+srcEnd-SrcBegin is greater than dst length: ${dst.length}",
      classOf[IndexOutOfBoundsException],
      cs.getChars(1, 3, dst, 99)
    )
  }

  @Test def getChars_String(): Unit = {
    val src = "There's a train every day leaving either way"

    val srcBegin = 8
    val srcEnd = 25

    val expected = src.substring(srcBegin, srcEnd) // "a train every day"

    val nChars = expected.length
    val dst = new Array[Char](nChars)

    val cs = new TestCharSequence(src)

    cs.getChars(srcBegin, srcEnd, dst, 0)

    val dstAsString = new String(dst)
    assertTrue(
      s"""\n\texpected: "${expected}"\n\treceived: "${dstAsString}"""",
      dstAsString.compareTo(expected) == 0
    )
  }
}
