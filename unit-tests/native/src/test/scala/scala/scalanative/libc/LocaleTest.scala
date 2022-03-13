package scala.scalanative.libc

import org.junit.Test
import org.junit.Assert._

import scalanative.unsafe._
import scalanative.libc.locale._

class LocaleTest {
  @Test def categoryConstantsReturnCorrectValue(): Unit = {
    // check LC_* constants are unique
    assertEquals(
      6,
      Set(
        locale.LC_ALL,
        locale.LC_COLLATE,
        locale.LC_CTYPE,
        locale.LC_MONETARY,
        locale.LC_NUMERIC,
        locale.LC_TIME
      ).size
    )
  }
  @Test def lconvInCLocale(): Unit = {
    Zone { implicit z =>
      locale.setlocale(locale.LC_ALL, toCString("C"))
    }
    import LConv.LConvOps
    val clocale = locale.localeconv()
    Zone { implicit z =>
      assertEquals(
        !toCString("."),
        !clocale.decimal_point
      )
      assertEquals(
        !toCString(""),
        !clocale.thousands_sep
      )
      assertEquals(
        !toCString(""),
        !clocale.grouping
      )
      assertEquals(
        !toCString(""),
        !clocale.int_curr_symbol
      )
      assertEquals(
        !toCString(""),
        !clocale.mon_decimal_point
      )
      assertEquals(
        !toCString(""),
        !clocale.mon_thousands_sep
      )
      assertEquals(
        !toCString(""),
        !clocale.mon_grouping
      )
      assertEquals(
        !toCString(""),
        !clocale.positive_sign
      )
      assertEquals(
        !toCString(""),
        !clocale.negative_sign
      )
      // CHAR_MAX
      assertEquals(
        127,
        clocale.int_frac_digits
      )
      def assertEq(exp: Any, act: Any) = assertTrue(
        s"Failure. Expected: ${exp}. Actual: ${act}",
        exp == act
      )
      def assertIn(act:Any,exp:Any*) :Unit = assertTrue(
      s"Failure. Expected values:${exp.toSeq.mkString("(",",",")")} do not contain $act.",
      exp.toSeq.contains(act)
      )

      assertEq(clocale.int_frac_digits, clocale.frac_digits)
      assertEq(clocale.int_frac_digits, clocale.p_cs_precedes)
      assertEq(clocale.int_frac_digits, clocale.p_sep_by_space)
      assertEq(clocale.int_frac_digits, clocale.n_cs_precedes)
      assertEq(clocale.int_frac_digits, clocale.n_sep_by_space)
      assertEq(clocale.int_frac_digits, clocale.p_sign_posn)
      assertEq(clocale.int_frac_digits, clocale.n_sign_posn)
    }

  }
}
