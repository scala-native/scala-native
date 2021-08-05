// Ported, with thanks & gratitude, from Scala.js
// 2020-09-20
// Scala.js Repository Info
//   commit: 9dc4d5b36ff2b2a3dfe2e91d5c6b1ef6d10d3e51
//   commit date: 2018-10-11
//
// Slightly modified for Scala Native.

package javalib.lang

import java.lang._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.Platform.{
  executingInJVM,
  executingInScalaNative
}

class ThreadTest {

  @Test def getNameAndSetName(): Unit = {
    if (!executingInJVM) {
      val t = Thread.currentThread()
      assertEquals("main", t.getName) // default name of the main thread
      t.setName("foo")
      try {
        assertEquals("foo", t.getName)
      } finally {
        t.setName("main") // don't pollute the rest of the world with this test
      }
      assertEquals("main", t.getName)
    }
  }

  @Test def currentThreadGetStackTrace(): Unit = {
    val trace = Thread.currentThread().getStackTrace()
    if (executingInScalaNative) {
      assertEquals(trace.length, 0)
    }
  }

  @Test def getId(): Unit = {
    assertTrue(Thread.currentThread().getId > 0)
  }

  @Test def interruptExistAndTheStatusIsProperlyReflected(): Unit = {
    val t = Thread.currentThread()
    assertFalse(t.isInterrupted())
    assertFalse(Thread.interrupted())
    assertFalse(t.isInterrupted())
    t.interrupt()
    assertTrue(t.isInterrupted())
    assertTrue(Thread.interrupted())
    assertFalse(t.isInterrupted())
    assertFalse(Thread.interrupted())
  }
}
