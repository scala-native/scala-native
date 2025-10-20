// Ported from Scala.js, commit: 54648372, dated: 2020-09-24
package org.scalanative.testsuite.javalib.util

import java.util.*

import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

/** Additional tests for `java.util.Date` that require javalib extension
 *  dummies.
 */
class DateTestExt {
  @Test def testToFromInstant(): Unit = {
    def test(
        expectedEpochSecond: Long,
        expectedNano: Int,
        epochMilli: Long
    ): Unit = {
      val instant = Instant.ofEpochSecond(expectedEpochSecond, expectedNano)
      val date = new Date(epochMilli)
      assertEquals(instant, date.toInstant())
      assertEquals(date, Date.from(instant))
    }

    test(123L, 456000000, 123456L)
    test(8640000000000L, 1000000, 8640000000000001L)
    test(-8640000000001L, 999000000, -8640000000000001L)
    test(-9223372036854776L, 192000000, Long.MinValue)
    test(9223372036854775L, 807000000, Long.MaxValue)
  }
}
