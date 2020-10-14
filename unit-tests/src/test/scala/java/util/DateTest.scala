package java.util

import org.junit.Test
import org.junit.Assert._

class DateTest {
  // now : java.util.Date = Fri Mar 31 14:47:44 EDT 2017
  val nowUt    = 1490986064740L
  val beforeUt = 1490986059300L
  val afterUt  = 1490986090620L
  val now      = new Date(nowUt)
  val before   = new Date(beforeUt)
  val after    = new Date(afterUt)
  val now2     = new Date(nowUt)

  @Test def testAfter(): Unit = {
    assertTrue(after.after(now))
  }

  @Test def testBefore(): Unit = {
    assertTrue(before.before(now))
  }

  @Test def testClone(): Unit = {
    val clone = now.clone().asInstanceOf[Date]
    assertTrue(clone.getTime equals now.getTime)
  }

  @Test def testCompareTo(): Unit = {
    assertTrue(now.compareTo(now2) == 0)
    assertTrue(before.compareTo(now) == -1)
    assertTrue(after.compareTo(now) == 1)
  }

  @Test def testEquals(): Unit = {
    assertTrue(now.equals(now2))
  }

  @Test def testGetTime(): Unit = {
    assertTrue(now.getTime == nowUt)
  }

  @Test def testHashCode(): Unit = {
    assertTrue(now.hashCode == nowUt.hashCode())
  }

  @Test def testSetTime(): Unit = {
    val nowBefore = new Date(nowUt)
    nowBefore.setTime(afterUt)
    assertTrue(nowBefore equals after)
  }

  @Test def testToString(): Unit = {
    assertTrue(now.toString equals "Date(1490986064740)")
  }
}
