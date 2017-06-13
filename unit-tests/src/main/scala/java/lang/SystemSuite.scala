package java.lang

object SystemSuite extends tests.Suite {
  test("System.nanoTime is monotonically increasing") {
    var t0 = 0L
    var t1 = 0L

    // a monotonic function (or monotone function) is a function between ordered sets
    // that preserves or reverses the given order.
    // It shoud never be less, but could be equal,
    // so we want to test if time is correctly increasing during the test as well
    val startTime = System.nanoTime()
    for (_ <- 1 to 100000) {
      t1 = System.nanoTime()
      assert(t0 - t1 <= 0L)
      t0 = t1
    }
    val endTime = System.nanoTime()
    assert(startTime - endTime < 0L)
  }

  test("System.getenv should contain known env variables") {
    assert(System.getenv().containsKey("HOME"))
    assert(System.getenv().get("USER") == "scala-native")
    assert(System.getenv().get("SCALA_NATIVE_ENV_WITH_EQUALS") == "1+1=2")
    assert(System.getenv().get("SCALA_NATIVE_ENV_WITHOUT_VALUE") == "")
    assert(System.getenv().get("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST") == null)
  }

  test("System.getenv(key) should read known env variables") {
    assert(System.getenv("USER") == "scala-native")
    assert(System.getenv("SCALA_NATIVE_ENV_WITH_EQUALS") == "1+1=2")
    assert(System.getenv("SCALA_NATIVE_ENV_WITHOUT_VALUE") == "")
    assert(System.getenv("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST") == null)
  }
}
