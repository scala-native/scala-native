package scala.scalanative.libc
import scala.scalanative.unsafe._
import scala.scalanative.libc._
import org.junit.Test
import org.junit.Assert._
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
    assertEquals(
      0,
      fenv.feraiseexcept(fenv.FE_DIVBYZERO | fenv.FE_INEXACT)
    )
    assertEquals(
      fenv.FE_DIVBYZERO | fenv.FE_INEXACT,
      fenv.fetestexcept(fenv.FE_ALL_EXCEPT)
    )
    assertEquals(
      0,
      fenv.feclearexcept(fenv.FE_ALL_EXCEPT)
    )
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
    assertEquals(
      0,
      fenv.fesetround(fenv.FE_DOWNWARD)
    )
    assertEquals(
      fenv.FE_DOWNWARD,
      fenv.fegetround()
    )
  }
}
