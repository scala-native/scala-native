package scala.scalanative.safe

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.language.experimental.captureChecking
import scala.scalanative.safe.SafeZoneCompat.withSafeZone

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

  /** The followings are instances allocation tests. The syntax of allocating an
   *  instance in the safe zone `sz` is `new {sz} T(...)`, and it's translated
   *  to internal function call `withSafeZone(sz, new T(...))` by dotty in typer
   *  phase. Instead of testing the syntax directly, here we test the internal
   *  function equaliventlly.
   */

  @Test def `allocate instances in nested safe zones`(): Unit = {
    case class A(v: Int) {}

    SafeZone { sz0 =>
      val a = SafeZone { sz1 =>
        val a0 = withSafeZone(sz0, new A(0))
        val a1 = withSafeZone(sz1, new A(1))
        a0
      }
      assertEquals(a.v, 0)
    }

    // TODO: Make it a compilation test.
    // assertThrows(
    //   classOf[CompilationFailedException],
    //   SafeZone { sz0 =>
    //     val a = SafeZone { sz1 =>
    //       val a0 = withSafeZone(sz0, new A(0))
    //       val a1 = withSafeZone(sz1, new A(1))
    //       a1
    //     }
    //   }
    // )
  }

  @Test def `allocate instances with members in different memory areas`(): Unit = {
    case class A(area: String) {}
    case class B(a: {*} A) {}
    SafeZone { sz0 =>
      SafeZone { sz1 => 
        val aInSz0 = withSafeZone(sz0, new A("sz0"))
        val aInHeap = new A("heap")
        val b0: {sz1, sz0} B = withSafeZone(sz1, new B(aInSz0))
        val b1: {sz1} B = withSafeZone(sz1, new B(aInHeap))
        val b2: {sz0} B = new B(aInSz0)
        val b3: B = new B(aInHeap)
      }
    }
  }
}
