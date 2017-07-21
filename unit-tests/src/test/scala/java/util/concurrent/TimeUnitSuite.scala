package java.util
package concurrent

// Ported from Scala.js

object TimeUnitSuite extends tests.Suite {

  test("toNanos()") {
    assert(42L == TimeUnit.NANOSECONDS.toNanos(42L))
    assert(42000L == TimeUnit.MICROSECONDS.toNanos(42L))
    assert(42000000L == TimeUnit.MILLISECONDS.toNanos(42L))
    assert(42000000000L == TimeUnit.SECONDS.toNanos(42L))
    assert(2520000000000L == TimeUnit.MINUTES.toNanos(42L))
    assert(151200000000000L == TimeUnit.HOURS.toNanos(42L))
    assert(3628800000000000L == TimeUnit.DAYS.toNanos(42L))
  }

  test("toMicros()") {
    assert(0L == TimeUnit.NANOSECONDS.toMicros(42L))
    assert(42L == TimeUnit.NANOSECONDS.toMicros(42123L))
    assert(42L == TimeUnit.MICROSECONDS.toMicros(42L))
    assert(42000L == TimeUnit.MILLISECONDS.toMicros(42L))
    assert(42000000L == TimeUnit.SECONDS.toMicros(42L))
    assert(2520000000L == TimeUnit.MINUTES.toMicros(42L))
    assert(151200000000L == TimeUnit.HOURS.toMicros(42L))
    assert(3628800000000L == TimeUnit.DAYS.toMicros(42L))
  }

  test("toMillis()") {
    assert(0L == TimeUnit.NANOSECONDS.toMillis(42L))
    assert(42L == TimeUnit.NANOSECONDS.toMillis(42000123L))
    assert(0L == TimeUnit.MICROSECONDS.toMillis(42L))
    assert(42L == TimeUnit.MICROSECONDS.toMillis(42123L))
    assert(42L == TimeUnit.MILLISECONDS.toMillis(42L))
    assert(42000L == TimeUnit.SECONDS.toMillis(42L))
    assert(2520000L == TimeUnit.MINUTES.toMillis(42L))
    assert(151200000L == TimeUnit.HOURS.toMillis(42L))
    assert(3628800000L == TimeUnit.DAYS.toMillis(42L))
  }

  test("toSeconds()") {
    assert(0L == TimeUnit.NANOSECONDS.toSeconds(42L))
    assert(42L == TimeUnit.NANOSECONDS.toSeconds(42000000123L))
    assert(0L == TimeUnit.MICROSECONDS.toSeconds(42L))
    assert(42L == TimeUnit.MICROSECONDS.toSeconds(42000123L))
    assert(0L == TimeUnit.MILLISECONDS.toSeconds(42L))
    assert(42L == TimeUnit.MILLISECONDS.toSeconds(42123L))
    assert(42L == TimeUnit.SECONDS.toSeconds(42L))
    assert(2520L == TimeUnit.MINUTES.toSeconds(42L))
    assert(151200L == TimeUnit.HOURS.toSeconds(42L))
    assert(3628800L == TimeUnit.DAYS.toSeconds(42L))
  }

  test("toMinutes()") {
    assert(0L == TimeUnit.NANOSECONDS.toMinutes(42L))
    assert(42L == TimeUnit.NANOSECONDS.toMinutes(2520000007380L))
    assert(0L == TimeUnit.MICROSECONDS.toMinutes(42L))
    assert(42L == TimeUnit.MICROSECONDS.toMinutes(2520007380L))
    assert(0L == TimeUnit.MILLISECONDS.toMinutes(42L))
    assert(42L == TimeUnit.MILLISECONDS.toMinutes(2520738L))
    assert(0L == TimeUnit.SECONDS.toMinutes(42L))
    assert(42L == TimeUnit.SECONDS.toMinutes(2520L))
    assert(42L == TimeUnit.MINUTES.toMinutes(42L))
    assert(2520L == TimeUnit.HOURS.toMinutes(42L))
    assert(60480L == TimeUnit.DAYS.toMinutes(42L))
  }

  test("toHours()") {
    assert(0L == TimeUnit.NANOSECONDS.toHours(42L))
    assert(42L == TimeUnit.NANOSECONDS.toHours(151200000442800L))
    assert(0L == TimeUnit.MICROSECONDS.toHours(42L))
    assert(42L == TimeUnit.MICROSECONDS.toHours(151200442800L))
    assert(0L == TimeUnit.MILLISECONDS.toHours(42L))
    assert(42L == TimeUnit.MILLISECONDS.toHours(151244280L))
    assert(0L == TimeUnit.SECONDS.toHours(42L))
    assert(42L == TimeUnit.SECONDS.toHours(151200L))
    assert(0L == TimeUnit.MINUTES.toHours(42L))
    assert(42L == TimeUnit.MINUTES.toHours(2520L))
    assert(42L == TimeUnit.HOURS.toHours(42L))
    assert(1008L == TimeUnit.DAYS.toHours(42L))
  }

  test("toDays()") {
    assert(0L == TimeUnit.NANOSECONDS.toDays(42L))
    assert(42L == TimeUnit.NANOSECONDS.toDays(3628800010627200L))
    assert(0L == TimeUnit.MICROSECONDS.toDays(42L))
    assert(42L == TimeUnit.MICROSECONDS.toDays(3628810627200L))
    assert(0L == TimeUnit.MILLISECONDS.toDays(42L))
    assert(42L == TimeUnit.MILLISECONDS.toDays(3629862720L))
    assert(0L == TimeUnit.SECONDS.toDays(42L))
    assert(42L == TimeUnit.SECONDS.toDays(3628800L))
    assert(0L == TimeUnit.MINUTES.toDays(42L))
    assert(42L == TimeUnit.MINUTES.toDays(60480L))
    assert(1L == TimeUnit.HOURS.toDays(42L))
    assert(42L == TimeUnit.HOURS.toDays(1008L))
    assert(42L == TimeUnit.DAYS.toDays(42L))
  }

  test("values()") {
    val values = TimeUnit.values()

    assert(7 == values.length)
    assert(TimeUnit.NANOSECONDS == values(0))
    assert(TimeUnit.MICROSECONDS == values(1))
    assert(TimeUnit.MILLISECONDS == values(2))
    assert(TimeUnit.SECONDS == values(3))
    assert(TimeUnit.MINUTES == values(4))
    assert(TimeUnit.HOURS == values(5))
    assert(TimeUnit.DAYS == values(6))
  }

  test("valueOf()") {
    assert(TimeUnit.NANOSECONDS == TimeUnit.valueOf("NANOSECONDS"))
    assert(TimeUnit.MICROSECONDS == TimeUnit.valueOf("MICROSECONDS"))
    assert(TimeUnit.MILLISECONDS == TimeUnit.valueOf("MILLISECONDS"))
    assert(TimeUnit.SECONDS == TimeUnit.valueOf("SECONDS"))
    assert(TimeUnit.MINUTES == TimeUnit.valueOf("MINUTES"))
    assert(TimeUnit.HOURS == TimeUnit.valueOf("HOURS"))
    assert(TimeUnit.DAYS == TimeUnit.valueOf("DAYS"))
  }
}
