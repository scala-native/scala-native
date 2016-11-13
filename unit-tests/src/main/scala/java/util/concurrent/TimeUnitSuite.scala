package java.util.concurrent

object TimeUnitSuite extends tests.Suite {
  import TimeUnit._

  test("1 day is 24 hours") {
    assert(24L == HOURS.convert(1L, DAYS))
    assert(1L == DAYS.convert(24L, HOURS))
  }

  test("1 hours is 60 minutes") {
    assert(60L == MINUTES.convert(1L, HOURS))
    assert(1L == HOURS.convert(60L, MINUTES))
  }

  test("1 minute is 60 seconds") {
    assert(60L == SECONDS.convert(1L, MINUTES))
    assert(1L == MINUTES.convert(60L, SECONDS))
  }

  test("1 second is 1000 milliseconds") {
    assert(1000L == MILLISECONDS.convert(1L, SECONDS))
    assert(1L == SECONDS.convert(1000L, MILLISECONDS))
  }

  test("1 milliseconds is 1000 microseconds") {
    assert(1000L == MICROSECONDS.convert(1L, MILLISECONDS))
    assert(1L == MILLISECONDS.convert(1000L, MICROSECONDS))
  }

  test("1 microseconds is 1000 nanoseconds") {
    assert(1000L == NANOSECONDS.convert(1L, MICROSECONDS))
    assert(1L == MICROSECONDS.convert(1000L, NANOSECONDS))
  }

  test("1 day is 1440 (= 24 * 60) minutes") {
    assert(1L == DAYS.convert(1440L, MINUTES))
    assert(1440L == MINUTES.convert(1L, DAYS))
  }

  test("Positive overflow returns Long.MAX_VALUE") {
    assert(java.lang.Long.MAX_VALUE == NANOSECONDS.convert(1000000L, DAYS))
  }

  test("Negative overflow returns Long.MIN_VALUE") {
    assert(java.lang.Long.MIN_VALUE == NANOSECONDS.convert(-1000000L, DAYS))
  }

}
