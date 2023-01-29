package scala.scalanative.safe

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class SafeZoneTest {
  @Test def `correctly open and close a safe zone`(): Unit = {
    val sz = SafeZone.open()
    assertTrue(sz.isOpen)
    assertFalse(sz.isClosed)
    sz.close()
    assertFalse(sz.isOpen)
    assertTrue(sz.isClosed)
    assertThrows(classOf[IllegalStateException], sz.close())

    SafeZone { sz =>
      assertTrue(sz.isOpen)
      assertFalse(sz.isClosed)
    }
  }

  @Test def `can get the handle of a safe zone`(): Unit = {
    SafeZone { sz =>
      assert(sz.handle != null)
    }
  }
}
