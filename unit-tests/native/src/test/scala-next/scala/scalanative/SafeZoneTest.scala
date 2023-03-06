package scala.scalanative

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.util.{Try,Success,Failure}
import scala.language.experimental.captureChecking
import scala.scalanative.runtime.SafeZoneAllocator.allocate
import scala.scalanative.SafeZone
import scala.scalanative.SafeZone._

/* Test safe zone operations which are private to package `scala.scalanative`. */
class SafeZoneTest {

  @Test def `can get the handle of a safe zone`(): Unit = {
    SafeZone { sz =>
      assert(sz.handle != null)
    }
  }

  @Test def `report error when trying to allocate an instances in a closed safe zone`(): Unit = {
    class A {}
    assertThrows(classOf[IllegalStateException], 
      SafeZone { sz => 
        sz.close()
        Try[{sz} A].apply(allocate(sz, new A())) match {
          case Success(_) => fail("Should not allocate instances in a closed safe zone.")
          case Failure(e: IllegalStateException) => ()
          case Failure(_) => fail("Unexpected error.")
        }
      }
    )
  }
}
