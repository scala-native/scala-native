package java.time

object DurationSuite extends tests.Suite {

  val v1 = Duration.ofSeconds(350070L, 112233L)
  val v2 = Duration.ofSeconds(350070L, 112233L)
  val v3 = Duration.ofSeconds(350070L, 0L)

  test("equals") {
    assert(v1.equals(v1))
    assert(v1.equals(v2))
    assert(v2.equals(v1))
    assert(v1 != null)
    assertNot(v1.equals(new Object()))
  }

  test("compareTo") {
    assert(v1.compareTo(v1) == 0)
    assert(v1.compareTo(v2) == 0)
    assert(v2.compareTo(v1) == 0)
    assert(v2.compareTo(v3) > 0)
    assert(v3.compareTo(v1) < 0)
  }

}
