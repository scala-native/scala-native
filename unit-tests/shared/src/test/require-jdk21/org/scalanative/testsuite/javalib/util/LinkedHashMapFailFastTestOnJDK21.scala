package org.scalanative.testsuite.javalib.util

import java.util._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class LinkedHashMapFailFastTestOnJDK21 {

  private type CME = ConcurrentModificationException

  private def int(i: Int): Integer = Integer.valueOf(i)

  private def fill(accessOrder: Boolean): LinkedHashMap[Integer, Integer] = {
    val m = new LinkedHashMap[Integer, Integer](16, 0.75f, accessOrder)
    for (i <- 0 until 4) m.put(int(i), int(i))
    m
  }

  private def check(
      name: String,
      expectCME: Boolean,
      op: LinkedHashMap[Integer, Integer] => Unit
  ): Unit = {
    for (accessOrder <- Seq(false, true)) {
      val m = fill(accessOrder)
      val it = m.keySet().iterator()
      it.next()
      op(m)
      val label = s"$name (accessOrder=$accessOrder)"
      if (expectCME) assertThrows(label, classOf[CME], it.next())
      else it.next()
    }
  }

  @Test def putFirstMovingAKeyIsStructural(): Unit = {
    check("putFirst(last key)", true, _.putFirst(int(3), int(9)))
    check("putFirst(new key)", true, _.putFirst(int(7), int(9)))
    check("putFirst(first key)", false, _.putFirst(int(0), int(9)))
  }

  @Test def putLastMovingAKeyIsStructural(): Unit = {
    check("putLast(first key)", true, _.putLast(int(0), int(9)))
    check("putLast(new key)", true, _.putLast(int(7), int(9)))
    check("putLast(last key)", false, _.putLast(int(3), int(9)))
  }

  @Test def putFirstAndPutLastKeepOrderWhenNotMoving(): Unit = {
    val m = fill(accessOrder = false)
    assertEquals(int(0), m.putFirst(int(0), int(10)))
    assertEquals(int(3), m.putLast(int(3), int(13)))
    assertEquals(
      Arrays.asList[Integer](int(0), int(1), int(2), int(3)),
      new ArrayList[Integer](m.keySet())
    )
    assertEquals(int(10), m.get(int(0)))
    assertEquals(int(13), m.get(int(3)))
  }

  @Test def pollFirstAndLastEntryAreStructural(): Unit = {
    check("pollFirstEntry", true, _.pollFirstEntry())
    check("pollLastEntry", true, _.pollLastEntry())
    check("firstEntry", false, _.firstEntry())
    check("lastEntry", false, _.lastEntry())
  }
}
