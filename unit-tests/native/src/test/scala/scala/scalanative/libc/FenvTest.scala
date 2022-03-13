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
      assertEquals(
        flag,
        fenv.fetestexcept(fenv.FE_ALL_EXCEPT)
      )
      assertEquals(
        0,
        fenv.feclearexcept(fenv.FE_ALL_EXCEPT)
      )
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
  }
}
