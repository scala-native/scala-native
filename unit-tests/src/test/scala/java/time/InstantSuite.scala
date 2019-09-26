package java.time

object InstantSuite extends tests.Suite {

  // Many tests derived, with gratitued, from scala-js. URL:
  //	 https://github.com/scala-js/scala-js-java-time/tree/master/testSuite/\
  //	 shared/src/test/scala/org/scalajs/testsuite/javalib/time

  val somePositiveInstant = Instant.ofEpochMilli(928392983942L)
  val someNegativeInstant = Instant.ofEpochSecond(-83827873287L, 88936253)

  val samples = Seq(Instant.EPOCH,
                    Instant.MIN,
                    Instant.MAX,
                    somePositiveInstant,
                    someNegativeInstant)

  test("compareTo") {
    for (i <- samples)
      assertEquals(0, i.compareTo(i))

    assert(Instant.MIN.compareTo(Instant.MAX) < 0)
    assert(Instant.MIN.compareTo(Instant.EPOCH) < 0)
    assert(Instant.MAX.compareTo(Instant.MIN) > 0)
    assert(Instant.MAX.compareTo(Instant.EPOCH) > 0)
  }

  test("equal") {
    assert(Instant.MIN.equals(Instant.MIN),
           s"Instant.MIN does not equal itself")

    val expected = 99
    assert(!expected.equals(Instant.MIN), s"${expected} equals Instant.MIN")
  }

  test("getEpochSecond") {
    assertEquals(0L, Instant.EPOCH.getEpochSecond)
    assertEquals(-31557014167219200L, Instant.MIN.getEpochSecond)
    assertEquals(31556889864403199L, Instant.MAX.getEpochSecond)
    assertEquals(928392983L, somePositiveInstant.getEpochSecond)
    assertEquals(-83827873287L, someNegativeInstant.getEpochSecond)
  }

  test("getNano") {
    assertEquals(0, Instant.EPOCH.getNano)
    assertEquals(0, Instant.MIN.getNano)
    assertEquals(999999999, Instant.MAX.getNano)
    assertEquals(942000000, somePositiveInstant.getNano)
    assertEquals(88936253, someNegativeInstant.getNano)
  }

  test("isAfter") {
    assertFalse(Instant.MIN.isAfter(Instant.MIN))
    assertFalse(Instant.MIN.isAfter(Instant.MAX))
    assertFalse(Instant.MIN.isAfter(Instant.EPOCH))
    assertTrue(Instant.MAX.isAfter(Instant.MIN))
    assertFalse(Instant.MAX.isAfter(Instant.MAX))
    assertTrue(Instant.MAX.isAfter(Instant.EPOCH))
  }

  test("isBefore") {
    assertFalse(Instant.MIN.isBefore(Instant.MIN))
    assertTrue(Instant.MIN.isBefore(Instant.MAX))
    assertTrue(Instant.MIN.isBefore(Instant.EPOCH))
    assertFalse(Instant.MAX.isBefore(Instant.MIN))
    assertFalse(Instant.MAX.isBefore(Instant.MAX))
    assertFalse(Instant.MAX.isBefore(Instant.EPOCH))
  }

  // Test before using to test now below.
  test("toEpochMillis - test before using in later tests") {
    assertEquals(0L, Instant.EPOCH.toEpochMilli)
    assertEquals(928392983942L, somePositiveInstant.toEpochMilli)
    assertEquals(-83827873286912L, someNegativeInstant.toEpochMilli)
  }

  test("now - milliseconds since Epoch within tolerance") {
    // This is a course sanity check.
    // One could test for matches at a finer granularity, say
    // microsecods or 100 nanosecond but that might generate too
    // many false negatives/failures on slow systems.
    // An exercise for the Gentle Reader.
    val sysNowMillis = System.currentTimeMillis
    val iNowMillis   = Instant.now.toEpochMilli

    val difference = Math.abs(sysNowMillis - iNowMillis)
    val tolerance  = 10 // an arbitrary small value

    assert(difference <= tolerance,
           s"System ms: ${sysNowMillis} Instant ms: ${iNowMillis} are not" +
             s" within tolerance ${tolerance}")
  }

  test("ofEpochSecond") {
    assertEquals(Instant.EPOCH, Instant.ofEpochSecond(0))
    assertEquals(Instant.EPOCH, Instant.ofEpochSecond(0, 0))

    assertEquals(Instant.MIN, Instant.ofEpochSecond(-31557014167219200L))
    assertEquals(Instant.MIN, Instant.ofEpochSecond(-31557014167219200L, 0))
    assertEquals(Instant.MAX,
                 Instant.ofEpochSecond(31556889864403199L, 999999999))

    expectThrows(classOf[DateTimeException],
                 Instant.ofEpochSecond(-31557014167219200L, Long.MinValue))

    val limits       = Seq(-31557014167219200L, 31557014167219200L)
    val invalidNanos = Seq(Long.MinValue, -1L, 1000000000L, Long.MaxValue)

    val invalidPairs =
      limits.flatMap(l => invalidNanos.map(n => (l, n))).filter {
        case (a, b) => ((a < 0) && (b < 0)) || ((a > 0) && (b > 0))
      }
    for ((s, n) <- invalidPairs)
      expectThrows(classOf[DateTimeException], Instant.ofEpochSecond(s, n))
  }

  test("ofEpochMilli") {
    assertEquals(Instant.EPOCH, Instant.ofEpochMilli(0))
  }

  test("toString") {

    val seconds  = 1559843112
    val nanos    = 2001
    val expected = s"${seconds}.${nanos}"

    val instant = Instant.ofEpochSecond(seconds, nanos)
    val result  = instant.toString

    assert(result == expected, s"result: ${result} != expected: ${expected}")

  }

}
