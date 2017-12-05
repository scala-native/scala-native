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

  // don't override possible known env vars
  val k = Array[String]("USERZ",
                        "HOMEZ",
                        "SCALA_NATIVE_ENV_WITH_EQUALS",
                        "SCALA_NATIVE_ENV_WITHOUT_VALUE",
                        "SCALA_NATIVE_ENV_WITH_UNICODE",
                        "SCALA_NATIVE_ENV_THAT_DOESNT_EXIST")
  val v = Array[String]("scala-native",
                        "/home/scala-native",
                        "1+1=2",
                        "",
                        0x2192.toChar.toString,
                        null)

  test("scalanative.native.system.setenv") {
    import scalanative.native._
    assert(system.setenv(k(0), v(0)))
    assert(system.setenv(k(1), v(1)))
    assert(system.setenv(k(2), v(2)))
    assert(system.setenv(k(3), v(3)))
    assert(system.setenv(k(4), v(4)))
  }

  test("System.getenv should contain known env variables") {
    val env = System.getenv()
    assert(env.get(k(0)) == v(0))
    assert(env.containsKey(k(1)))
    assert(env.get(k(2)) == v(2))
    assert(env.get(k(3)) == v(3))
    assert(env.get(k(4)) == v(4))
    assert(env.get(k(5)) == v(5))
  }

  test("System.getenv(key) should read known env variables") {
    assert(System.getenv(k(0)) == v(0))
    assert(System.getenv(k(1)) == v(1))
    assert(System.getenv(k(2)) == v(2))
    assert(System.getenv(k(3)) == v(3))
    assert(System.getenv(k(4)) == v(4))
    assert(System.getenv(k(5)) == v(5))
  }

  test("Property user.home should be set") {
    assertEquals(System.getProperty("user.home"), System.getenv("HOME"))
  }
}
