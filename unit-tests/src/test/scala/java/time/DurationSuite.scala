package java.time

object DurationSuite extends tests.Suite {

  val one   = Duration.ofSeconds(350070L, 112233L)
  val two   = Duration.ofSeconds(350070L, 112233L)
  val three = Duration.ofSeconds(350070L, 0L)

  test("equals") {
    assert(one.equals(one))
    assert(one.equals(two))
    assert(two.equals(one))
    assert(one != null)
    assertNot(one.equals(new Object()))
  }

  test("compareTo") {
    assert(one.compareTo(one) == 0)
    assert(one.compareTo(two) == 0)
    assert(two.compareTo(three) > 0)
    assert(three.compareTo(one) < 0)
  }

}
