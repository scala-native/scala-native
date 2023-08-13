package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scalanative.unsigned._

class ZoneTest {
  private def assertAccessible(bptr: Ptr[_], n: Int): Unit = {
    val ptr = bptr.asInstanceOf[Ptr[Int]]
    var i = 0
    while (i < n) {
      ptr(i) = i
      i += 1
    }

    i = 0
    var sum = 0
    while (i < n) {
      sum += ptr(i)
      i += 1
    }

    assertTrue(sum == (0 until n).sum)
  }

  @Test def zoneAllocatorAllocWithApply(): Unit = {
    Zone { implicit z =>
      val ptr = z.alloc(64.toUSize * sizeof[Int])

      assertAccessible(ptr, 64)

      val ptr2: Ptr[Int] = alloc[Int](128)

      assertAccessible(ptr2, 128)
    }
  }

  @Test def zoneAllocatorAllocWithOpen(): Unit = {
    implicit val zone: Zone = Zone.open()
    assertTrue(zone.isOpen)
    assertFalse(zone.isClosed)

    val ptr = zone.alloc(64.toUSize * sizeof[Int])

    assertAccessible(ptr, 64)

    val ptr2: Ptr[Int] = alloc[Int](128)

    assertAccessible(ptr2, 128)

    zone.close()
    assertFalse(zone.isOpen)
    assertTrue(zone.isClosed)
  }

  @Test def allocThrowsExceptionIfZoneAllocatorIsClosed(): Unit = {
    implicit val zone: Zone = Zone.open()

    zone.alloc(64.toUSize * sizeof[Int])

    zone.close()

    assertThrows(
      classOf[IllegalStateException],
      zone.alloc(64.toUSize * sizeof[Int])
    )
    assertThrows(classOf[IllegalStateException], zone.close())
  }
}
