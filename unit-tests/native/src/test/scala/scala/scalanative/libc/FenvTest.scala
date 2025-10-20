package scala.scalanative.libc
import scala.scalanative.unsafe.*
import scala.scalanative.libc.*
import scala.scalanative.meta.LinktimeInfo
import org.junit.Test
import org.junit.Assert.*
class FEnvTest {

  @Test def exceptionFlagsAreUnique(): Unit = {
    assertEquals(
      6,
      Set(
        fenv.FE_DIVBYZERO,
        fenv.FE_INEXACT,
        fenv.FE_INVALID,
        fenv.FE_OVERFLOW,
        fenv.FE_UNDERFLOW,
        fenv.FE_ALL_EXCEPT
      ).size
    )
  }

  @Test def raiseCheckClearException(): Unit = {
    if (LinktimeInfo.isWindows) {
      // Avoid link time error --`fe_raiseexcept` does not exist -- because it is not supported on Windows.
    } else {

      def assertContainsFlags(expected: CInt, actual: CInt): Unit = {
        assertTrue(
          s"""expected: $expected
        |actual: $actual
        |FE_DIVBYZERO: ${fenv.FE_DIVBYZERO}
        |FE_INEXACT: ${fenv.FE_INEXACT}
        |FE_INVALID: ${fenv.FE_INVALID}
        |FE_OVERFLOW: ${fenv.FE_OVERFLOW}
        |FE_UNDERFLOW: ${fenv.FE_UNDERFLOW}
        |""".stripMargin,
          (actual & expected) == expected
        )
      }
      val exs = List(
        fenv.FE_DIVBYZERO,
        fenv.FE_INEXACT,
        fenv.FE_INVALID,
        fenv.FE_OVERFLOW,
        fenv.FE_UNDERFLOW
      )
      for {
        n <- 1 to exs.length
        comb <- exs.combinations(n)
      } yield {
        val flag = comb.foldLeft(0) { case (acc, f) => acc | f }
        assertEquals(
          0,
          fenv.feclearexcept(fenv.FE_ALL_EXCEPT)
        )
        assertEquals(
          0,
          fenv.feraiseexcept(flag)
        )
        assertContainsFlags(
          flag,
          fenv.fetestexcept(fenv.FE_ALL_EXCEPT)
        )
        assertEquals(
          0,
          fenv.feclearexcept(fenv.FE_ALL_EXCEPT)
        )
      }
    }
  }

  @Test def roundingFlagsAreUnique(): Unit = {
    assertEquals(
      4,
      Set(
        fenv.FE_DOWNWARD,
        fenv.FE_TONEAREST,
        fenv.FE_TOWARDZERO,
        fenv.FE_UPWARD
      ).size
    )
  }
  @Test def setAndGetRoundingFlag(): Unit = {
    val original = fenv.fegetround()
    for {
      flag <- List(
        fenv.FE_DOWNWARD,
        fenv.FE_TONEAREST,
        fenv.FE_TOWARDZERO,
        fenv.FE_UPWARD
      )
    } yield {
      assertEquals(
        0,
        fenv.fesetround(flag)
      )
      assertEquals(
        flag,
        fenv.fegetround()
      )
    }
    // restore fenv
    fenv.fesetround(original)
  }
}
