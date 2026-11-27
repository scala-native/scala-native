package org.scalanative.testsuite.javalib.util

import java.util.{EmptyStackException, Stack}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StackTest {
  @Test def emptyStack(): Unit = {
    val s = new Stack[String]
    assertTrue(s.empty())
    assertThrows(classOf[EmptyStackException], s.peek())
    assertThrows(classOf[EmptyStackException], s.pop())
  }

  @Test def pushPeekPop(): Unit = {
    val s = new Stack[String]
    assertEquals("a", s.push("a"))
    s.push("b")
    assertNull(s.push(null))
    assertFalse(s.empty())
    assertEquals(3, s.size())
    assertNull(s.peek())
    assertNull(s.pop())
    assertEquals("b", s.pop())
    assertEquals("a", s.peek())
    assertEquals(1, s.size())
  }

  @Test def search(): Unit = {
    val s = new Stack[String]
    s.push("a")
    s.push("b")
    s.push("a")
    s.push(null)
    assertEquals(1, s.search(null))
    assertEquals(2, s.search("a"))
    assertEquals(3, s.search("b"))
    assertEquals(-1, s.search("z"))
  }

  @Test def isAVector(): Unit = {
    val s = new Stack[Integer]
    s.push(Integer.valueOf(1))
    s.push(Integer.valueOf(2))
    assertEquals("[1, 2]", s.toString())
    assertEquals(Integer.valueOf(1), s.get(0))
  }
}
