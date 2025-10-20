package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert.*
import scala.scalanative.unsafe.*

@extern object TimeTestFFI {
  def scalanative_time_zone_offset(): CLongLong = extern
}

class TimeTest {
  @Test def testTimeZoneOffset(): Unit = {
    val offset = TimeTestFFI.scalanative_time_zone_offset()
    // Between -12 and +14 hrs in seconds. Offset is 0s (UTC) in CI
    // println(s"Time zone offset: ${offset}s")
    assertTrue("time_zone_offset >= -43200", offset >= -43200)
    assertTrue("time_zone_offset <= 50400", offset <= 50400)
  }
}
