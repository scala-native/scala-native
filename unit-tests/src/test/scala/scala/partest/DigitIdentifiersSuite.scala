package scala.partest

object DigitIdentifiersSuite extends tests.Suite {

  object `1Test` {
    val `1S`  = "One"
    val `-1L` = -1L

    sealed abstract class `0` {
      val `1` = 1

      def `2` = 2

      var `3` = `1` + `2`
    }

    sealed trait `-1` {
      val `-1` = -1
    }

    case class `1Foo`(`2Δ`: Double, `-2Δ`: Float) extends `0` with `-1`

  }

  test("Handles identifiers starting with digit") {

    import `1Test`._

    val x = new `1Foo`(2.0, -2.0f)
    x.`3` = 0

    assert(`1S` == "One")
    assert(`-1L` == -1L)
    assertEquals(2.0, x.`2Δ`, 0.00000001)
    assertEquals(-2.0, x.`-2Δ`, 0.00000001)
    assert(x.`-1` == -1)
    assert(x.`1` == 1)
    assert(x.`2` == 2)
    assert(x.`3` == 0)
  }
}
