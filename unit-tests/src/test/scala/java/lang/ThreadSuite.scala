package java.lang

object ThreadSuite extends tests.Suite {

  test("Runtime static variables access and currentThread do not crash") {

    val max  = Thread.MAX_PRIORITY
    val min  = Thread.MIN_PRIORITY
    val norm = Thread.NORM_PRIORITY

    val current = Thread.currentThread()

  }

  test("Get/Set Priority work as it should with currentThread") {

    val current = Thread.currentThread()

    current.setPriority(3)
    assert(current.getPriority == 3)

  }

  def takesAtLeast[R](expectedDelayMs: scala.Long)(f: => R): R = {
    val start  = System.currentTimeMillis()
    val result = f
    val end    = System.currentTimeMillis()

    assert(end - start >= expectedDelayMs)

    result
  }

  def takesAtLeast[R](expectedDelayMs: scala.Long,
                      expectedDelayNanos: scala.Int)(f: => R): R = {
    val expectedDelay = expectedDelayMs * 1000000 + expectedDelayMs
    val start         = System.nanoTime()
    val result        = f
    val end           = System.nanoTime()

    assert(end - start >= expectedDelay)

    result
  }

  test("sleep suspends execution by at least the requested amount") {
    val millisecondTests = Seq(0, 1, 5, 100)
    millisecondTests.foreach { ms =>
      takesAtLeast(ms) {
        Thread.sleep(ms)
      }
    }
    millisecondTests.foreach { ms =>
      takesAtLeast(ms) {
        Thread.sleep(ms, 0)
      }
    }

    val tests = Seq(0 -> 0,
                    0   -> 1,
                    0   -> 999999,
                    1   -> 0,
                    1   -> 1,
                    5   -> 0,
                    100 -> 0,
                    100 -> 50)

    tests.foreach {
      case (ms, nanos) =>
        takesAtLeast(ms, nanos) {
          Thread.sleep(ms, nanos)
        }
    }
  }

}
