package scala.scalanative.safe

import org.junit.Test
import org.junit.Assert._

class SafeZoneTest {
  @Test def `correctly open and close a safe zone`(): Unit = {
    val sz = SafeZone.open()
    assertTrue(sz.isOpen)
    assertFalse(sz.isClosed)
    sz.close()
    assertFalse(sz.isOpen)
    assertTrue(sz.isClosed)

    SafeZone { sz =>
      assertTrue(sz.isOpen)
      assertFalse(sz.isClosed)
    }
  }

  @Test def `can allocate primitive value in a safe zone`(): Unit = {
    SafeZone { sz =>
      val x = sz.alloc[Int]()
      assertEquals(0, x)
    }
  }
}
