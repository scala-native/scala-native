package scala.scalanative
package unsafe

import org.junit.Assert._
import org.junit.Test

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
    Zone.acquire { implicit z =>
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

  // Issue #2491
  @Test def objectsReferencedFromZoneMemoryStayAlive(): Unit =
    Zone.acquire { implicit z =>
      val small = alloc[BigInt](64) // in a pool page
      val large = alloc[BigInt](1024) // separate allocation
      ZoneTest.fill(small, 64)
      ZoneTest.fill(large, 1024)
      System.gc()
      ZoneTest.churn()
      for (i <- 0 until 64) assertEquals(BigInt(i) << 100, small(i))
      for (i <- 0 until 1024) assertEquals(BigInt(i) << 100, large(i))
    }
}

object ZoneTest {
  @noinline def fill(ptr: Ptr[BigInt], n: Int): Unit =
    for (i <- 0 until n) ptr(i) = BigInt(i) << 100

  @noinline def churn(): Unit =
    for (i <- 0 until 100000) new Array[Int](8).update(0, i)
}
