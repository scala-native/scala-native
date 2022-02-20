package scala.scalanative.unsafe

import org.junit.{Test, BeforeClass}
import org.junit.Assert._
import org.junit.Assume._

import scalanative.libc._
import scalanative.unsigned._

@extern
object RustLib {
  // from standalone compilation
  @name("check_test_string")
  def checkTestString(string: CString): Boolean = extern

  //from cargo build
  def isEven(v: CInt): Boolean = extern
}

object RustIntegrationTest {
  @BeforeClass
  def assumeSupportsRust() = {
    // LTO w/ rust requires a newer LLVM than the minimum LLVM version for SN
    assumeFalse(
      "RustIntegrationTest not available in the current build",
      sys.env.get("SCALANATIVE_LTO").exists(_ != "none")
    )
  }
}

class RustIntegrationTest {
  import RustLib._
  @Test
  def canUseBindingsFromStandaloneCompilation(): Unit = {
    assertTrue(isEven(32))
    assertFalse(isEven(33))
  }

  @Test
  def canUseBindingsFromCargoBuild(): Unit = {
    assertTrue("constant", checkTestString(c"Hello Rust"))
    assertFalse("fail constant", checkTestString(c"Not correct string"))

    Zone { implicit z: Zone =>
      assertTrue("allocated", checkTestString(toCString("Hello Rust")))
      assertFalse(
        "fail allocated",
        checkTestString(toCString("Oter not correct"))
      )
    }
  }
}
