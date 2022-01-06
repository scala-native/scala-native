package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert._

import scalanative.runtime.time.scalanative_time_zone_offset

class TimeTest {
  @Test def testTimeZoneOffset(): Unit = {
    val offset = scalanative_time_zone_offset()
    // max 12 hours +- in seconds
    println(s"Time zone offset: ${offset}s")
    assertTrue("time_zone_offset >= -43200", offset >= -43200)
    assertTrue("time_zone_offset <= 43200", offset <= 43200)
  }
}
