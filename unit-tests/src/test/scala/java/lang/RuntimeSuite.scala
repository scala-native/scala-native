package java.lang

object RuntimeSuite extends tests.Suite {
  test("availableProcessors") {
    assert(Runtime.getRuntime.availableProcessors() > 0)
  }
}
