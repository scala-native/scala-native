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
    assert(
      System
        .getenv()
        .get("SCALA_NATIVE_ENV_WITH_UNICODE") == 0x2192.toChar.toString)
  }

  test("System.getenv(key) should read known env variables") {
    assert(System.getenv("USER") == "scala-native")
    assert(System.getenv("SCALA_NATIVE_ENV_WITH_EQUALS") == "1+1=2")
    assert(System.getenv("SCALA_NATIVE_ENV_WITHOUT_VALUE") == "")
    assert(System.getenv("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST") == null)
  }

  test("System.currentTimeMillis seconds should approximately == C time()") {
    // This is a coarse-grain sanity check, primarily to ensure that 64 bit
    // math is being done on 32 bit systems. Only seconds are considered.

    // Taking the two time samples can not be done atomically, so the two
    // times can validly differ by one second. On a mighty slow system the
    // two times might differ by two.
    // Spurious false failures are expensive and annoying to track down,
    // be defensive and add _two_ extra seconds tolerance to expected
    // difference.

    import scalanative.posix.time.time

    val tolerance = 3

    val ctmMillis = System.currentTimeMillis()
    val cSeconds  = time(null)

    // Truncate down to keep math simple & reduce number of bits in play.
    val ctmSeconds = ctmMillis / 1000

    val delta = Math.abs(ctmSeconds - cSeconds)

    assert(delta <= tolerance)
  }

  test("Property user.home should be set") {
    assertEquals(System.getProperty("user.home"), System.getenv("HOME"))
  }

  test("Property user.dir should be set") {
    assertEquals(System.getProperty("user.dir"),
                 System.getenv("SCALA_NATIVE_USER_DIR"))
  }

}
