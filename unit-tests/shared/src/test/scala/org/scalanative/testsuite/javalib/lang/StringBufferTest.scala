package org.scalanative.testsuite.javalib.lang

import java.lang._

// Ported from Scala.js

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringBufferTest {

  def newBuf: java.lang.StringBuffer =
    new java.lang.StringBuffer

  def initBuf(str: String): java.lang.StringBuffer =
    new java.lang.StringBuffer(str)

  @Test def append(): Unit = {
    assertEquals("asdf", newBuf.append("asdf").toString)
    assertEquals("null", newBuf.append(null: AnyRef).toString)
    assertEquals("null", newBuf.append(null: String).toString)
    assertEquals("nu", newBuf.append(null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuf.append(true).toString)
    assertEquals("a", newBuf.append('a').toString)
    assertEquals("abcd", newBuf.append(Array('a', 'b', 'c', 'd')).toString)
    assertEquals("bc", newBuf.append(Array('a', 'b', 'c', 'd'), 1, 2).toString)
    assertEquals("4", newBuf.append(4.toByte).toString)
    assertEquals("304", newBuf.append(304.toShort).toString)
    assertEquals("100000", newBuf.append(100000).toString)
  }

  @Test def appendFloats(): Unit = {
    assertEquals("2.5", newBuf.append(2.5f).toString)
    assertEquals(
      "2.5 3.5",
      newBuf.append(2.5f).append(' ').append(3.5f).toString
    )
  }

  @Test def appendDoubles(): Unit = {
    assertEquals("3.5", newBuf.append(3.5).toString)
    assertEquals(
      "2.5 3.5",
      newBuf.append(2.5).append(' ').append(3.5).toString
    )
  }

  @Test def insert(): Unit = {
    assertEquals("asdf", newBuf.insert(0, "asdf").toString)
    assertEquals("null", newBuf.insert(0, null: AnyRef).toString)
    assertEquals("null", newBuf.insert(0, null: String).toString)
    assertEquals("nu", newBuf.insert(0, null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuf.insert(0, true).toString)
    assertEquals("a", newBuf.insert(0, 'a').toString)
    assertEquals("abcd", newBuf.insert(0, Array('a', 'b', 'c', 'd')).toString)
    assertEquals(
      "bc",
      newBuf.insert(0, Array('a', 'b', 'c', 'd'), 1, 2).toString
    )
    assertEquals("4", newBuf.insert(0, 4.toByte).toString)
    assertEquals("304", newBuf.insert(0, 304.toShort).toString)
    assertEquals("100000", newBuf.insert(0, 100000).toString)

    assertEquals("abcdef", initBuf("adef").insert(1, "bc").toString)
    assertEquals("abcdef", initBuf("abcd").insert(4, "ef").toString)
    assertEquals("abcdef", initBuf("adef").insert(1, Array('b', 'c')).toString)
    assertEquals("abcdef", initBuf("adef").insert(1, initBuf("bc")).toString)
    assertEquals(
      "abcdef",
      initBuf("abef").insert(2, Array('a', 'b', 'c', 'd', 'e'), 2, 2).toString
    )

    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuf("abcd").insert(-1, "whatever")
    )
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuf("abcd").insert(5, "whatever")
    )
  }

  @Test def insertFloatOrDouble(): Unit = {
    assertEquals("2.5", newBuf.insert(0, 2.5f).toString)
    assertEquals("3.5", newBuf.insert(0, 3.5).toString)
  }

  // TODO: segfaults with EXC_BAD_ACCESS (code=1, address=0x0)
  @Test def insertStringBuffer(): Unit = {
    assertEquals(
      "abcdef",
      initBuf("abef").insert(2, initBuf("abcde"), 2, 4).toString
    )
  }

  @Test def deleteCharAt(): Unit = {
    assertEquals("023", initBuf("0123").deleteCharAt(1).toString)
    assertEquals("123", initBuf("0123").deleteCharAt(0).toString)
    assertEquals("012", initBuf("0123").deleteCharAt(3).toString)
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuf("0123").deleteCharAt(-1)
    )
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuf("0123").deleteCharAt(4)
    )
  }

  @Test def replace(): Unit = {
    assertEquals("0bc3", initBuf("0123").replace(1, 3, "bc").toString)
    assertEquals("abcd", initBuf("0123").replace(0, 4, "abcd").toString)
    assertEquals("abcd", initBuf("0123").replace(0, 10, "abcd").toString)
    assertEquals("012defg", initBuf("0123").replace(3, 10, "defg").toString)
    assertEquals("xxxx123", initBuf("0123").replace(0, 1, "xxxx").toString)
    assertEquals("0xxxx123", initBuf("0123").replace(1, 1, "xxxx").toString)
    assertEquals("0123x", initBuf("0123").replace(4, 5, "x").toString)

    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuf("0123").replace(-1, 3, "x")
    )
  }

  @Test def setCharAt(): Unit = {
    val buf = newBuf
    buf.append("foobar")

    buf.setCharAt(2, 'x')
    assertEquals("foxbar", buf.toString)

    buf.setCharAt(5, 'h')
    assertEquals("foxbah", buf.toString)

    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      buf.setCharAt(-1, 'h')
    )
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      buf.setCharAt(6, 'h')
    )
  }

  @Test def ensureCapacity(): Unit = {
    // test that ensureCapacity is linking
    newBuf.ensureCapacity(10)
  }

  @Test def shouldProperlySetLength(): Unit = {
    val buf = newBuf
    buf.append("foobar")

    assertThrows(classOf[StringIndexOutOfBoundsException], buf.setLength(-3))

    assertEquals("foo", { buf.setLength(3); buf.toString })
    assertEquals("foo\u0000\u0000\u0000", { buf.setLength(6); buf.toString })
  }

  @Test def appendCodePoint(): Unit = {
    val buf = newBuf
    buf.appendCodePoint(0x61)
    assertEquals("a", buf.toString)
    buf.appendCodePoint(0x10000)
    assertEquals("a\uD800\uDC00", buf.toString)
    buf.append("fixture")
    buf.appendCodePoint(0x00010ffff)
    assertEquals("a\uD800\uDC00fixture\uDBFF\uDFFF", buf.toString)
  }

  /** Checks that modifying a StringBuffer, converted to a String using a
   *  `.toString` call, is not breaking String immutability. See:
   *  https://github.com/scala-native/scala-native/issues/2925
   */
  @Test def toStringThenModifyStringBuffer(): Unit = {
    val buf = new StringBuffer()
    buf.append("foobar")

    val s = buf.toString
    buf.setCharAt(0, 'm')

    assertTrue(
      s"foobar should start with 'f' instead of '${s.charAt(0)}'",
      'f' == s.charAt(0)
    )
  }
}
