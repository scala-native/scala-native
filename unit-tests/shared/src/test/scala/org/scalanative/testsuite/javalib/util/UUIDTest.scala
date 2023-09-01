// Ported from Scala.js commit: e20d6d6 dated: 2023-07-19

package org.scalanative.testsuite.javalib.util

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import java.util.UUID

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform._

class UUIDTest {

  @Test def constructor(): Unit = {
    val uuid = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L)
    assertEquals(0xf81d4fae7dec11d0L, uuid.getMostSignificantBits())
    assertEquals(0xa76500a0c91e6bf6L, uuid.getLeastSignificantBits())
    assertEquals(2, uuid.variant())
    assertEquals(1, uuid.version())
    assertEquals(0x1d07decf81d4faeL, uuid.timestamp())
    assertEquals(0x2765, uuid.clockSequence())
    assertEquals(0xa0c91e6bf6L, uuid.node())
  }

  @Test def getLeastSignificantBits(): Unit = {
    assertEquals(0L, new UUID(0L, 0L).getLeastSignificantBits())
    assertEquals(
      Long.MinValue,
      new UUID(0L, Long.MinValue).getLeastSignificantBits()
    )
    assertEquals(
      Long.MaxValue,
      new UUID(0L, Long.MaxValue).getLeastSignificantBits()
    )
  }

  @Test def getMostSignificantBits(): Unit = {
    assertEquals(0L, new UUID(0L, 0L).getMostSignificantBits())
    assertEquals(
      Long.MinValue,
      new UUID(Long.MinValue, 0L).getMostSignificantBits()
    )
    assertEquals(
      Long.MaxValue,
      new UUID(Long.MaxValue, 0L).getMostSignificantBits()
    )
  }

  @Test def version(): Unit = {
    assertEquals(0, new UUID(0L, 0L).version())
    assertEquals(1, new UUID(0x0000000000001000L, 0L).version())
    assertEquals(2, new UUID(0x00000000000f2f00L, 0L).version())
  }

  @Test def variant(): Unit = {
    assertEquals(0, new UUID(0L, 0L).variant())
    assertEquals(0, new UUID(0L, 0x7000000000000000L).variant())
    assertEquals(0, new UUID(0L, 0x3ff0000000000000L).variant())
    assertEquals(0, new UUID(0L, 0x1ff0000000000000L).variant())

    assertEquals(2, new UUID(0L, 0x8000000000000000L).variant())
    assertEquals(2, new UUID(0L, 0xb000000000000000L).variant())
    assertEquals(2, new UUID(0L, 0xaff0000000000000L).variant())
    assertEquals(2, new UUID(0L, 0x9ff0000000000000L).variant())

    assertEquals(6, new UUID(0L, 0xc000000000000000L).variant())
    assertEquals(6, new UUID(0L, 0xdf00000000000000L).variant())
  }

  @Test def timestamp(): Unit = {
    assertEquals(
      0L,
      new UUID(0x0000000000001000L, 0x8000000000000000L).timestamp()
    )
    assertEquals(
      0x333555577777777L,
      new UUID(0x7777777755551333L, 0x8000000000000000L).timestamp()
    )

    assertThrows(
      classOf[Exception],
      new UUID(0x0000000000000000L, 0x8000000000000000L).timestamp()
    )
    assertThrows(
      classOf[Exception],
      new UUID(0x0000000000002000L, 0x8000000000000000L).timestamp()
    )
  }

  @Test def clockSequence(): Unit = {
    assertEquals(
      0,
      new UUID(0x0000000000001000L, 0x8000000000000000L).clockSequence()
    )
    assertEquals(
      0x0fff,
      new UUID(0x0000000000001000L, 0x8fff000000000000L).clockSequence()
    )
    assertEquals(
      0x3fff,
      new UUID(0x0000000000001000L, 0xbfff000000000000L).clockSequence()
    )

    assertThrows(
      classOf[Exception],
      new UUID(0x0000000000000000L, 0x8000000000000000L).clockSequence()
    )
    assertThrows(
      classOf[Exception],
      new UUID(0x0000000000002000L, 0x8000000000000000L).clockSequence()
    )
  }

  @Test def node(): Unit = {
    assertEquals(0L, new UUID(0x0000000000001000L, 0x8000000000000000L).node())
    assertEquals(
      0xffffffffffffL,
      new UUID(0x0000000000001000L, 0x8000ffffffffffffL).node()
    )

    assertThrows(
      classOf[Exception],
      new UUID(0x0000000000000000L, 0x8000000000000000L).node()
    )
    assertThrows(
      classOf[Exception],
      new UUID(0x0000000000002000L, 0x8000000000000000L).node()
    )
  }

  @Test def compareTo(): Unit = {
    /* #4882 `UUID.compareTo()` is known not to match the specification in RFC
     * 4122. However, the exact algorithm used by the JVM is not publicly
     * available with a license that we can use. The best we have is the
     * JavaDoc that says
     *
     * > The first of two UUIDs is greater than the second if the most
     * > significant field in which the UUIDs differ is greater for the first
     * > UUID.
     *
     * We do not know what a "field" is; it does not match what the JavaDoc of
     * the class calls "fields" of a variant 2 UUID, and there is no other
     * mention of "field" elsewhere. We can guess that it is either the pair
     * `get{Least,Most}SignificantBits()`, or the dash-separated segments of
     * the string representation of a UUID. The latter is of the form
     *
     *   xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     *      8       4    4    4       12
     *
     * Note that the first 3 segments make up the result of
     * `getMostSignificantBits()`, while the last 2 segments make up
     * `getLeastSignificantBits()`.
     *
     * In order to infer the algorithm used by the JVM, we generated UUIDs for
     * all corner-case values of these 5 segments: the minimum and maximum
     * values of the signed and unsigned representations of the fields. That
     * makes 4^5 = 1024 different UUIDs. By construction, this also generates
     * all corner-case values of `get{Most,Least}SignificantBits()`.
     *
     * We then tried implementations of `referenceLessThan` until it matched
     * the result of the JVM's `compareTo` for all pairs of our UUIDs. There
     * are 1024^2 ~= 1M such pairs.
     *
     * This test generates the 1024 UUIDs mentioned above, sorts them according
     * to `referenceLessThan`, then verifies that `compareTo` agrees with the
     * resulting order. This way, we only test 2*1024 pairs instead of the full
     * 1 million.
     */

    // Reference comparison obtained by trial-and-error against the JVM.
    def referenceLessThan(x: UUID, y: UUID): Boolean = {
      if (x.getMostSignificantBits() != y.getMostSignificantBits())
        x.getMostSignificantBits() < y.getMostSignificantBits()
      else
        x.getLeastSignificantBits() < y.getLeastSignificantBits()
    }

    def cornerCases(hexDigitCount: Int): List[Long] = {
      val bits = hexDigitCount * 4
      List(
        0L, // unsigned min value
        (1L << bits) - 1L, // unsigned max value
        1L << (bits - 1), // signed min value
        (1L << (bits - 1)) - 1L // signed max value
      )
    }

    val uuids = for {
      f1 <- cornerCases(8)
      f2 <- cornerCases(4)
      f3 <- cornerCases(4)
      f4 <- cornerCases(4)
      f5 <- cornerCases(12)
    } yield {
      new UUID((f1 << 32) | (f2 << 16) | f3, (f4 << 48) | f5)
    }

    val sortedUUIDs = uuids.sortWith(referenceLessThan(_, _))

    /* For reference: full loop to run on the JVM to test all 1M pairs
     * for (u1 <- sortedUUIDs; u2 <- sortedUUIDs) {
     *   if (referenceLessThan(u1, u2) != (u1.compareTo(u2) < 0))
     *     println(s"$u1 $u2")
     * }
     */

    // For our unit tests, only test consecutive UUIDs, and assume that transitivity holds
    for ((smaller, larger) <- sortedUUIDs.zip(sortedUUIDs.tail)) {
      assertEquals(s"$smaller == $smaller", 0, smaller.compareTo(smaller))
      assertEquals(s"$smaller < $larger", -1, smaller.compareTo(larger))
      assertEquals(s"$larger > $smaller", 1, larger.compareTo(smaller))
    }
  }

  @Test def hashCodeTest(): Unit = {
    assertEquals(0, new UUID(0L, 0L).hashCode())
    assertEquals(
      new UUID(123L, 123L).hashCode(),
      new UUID(123L, 123L).hashCode()
    )
  }

  @Test def equalsTest(): Unit = {
    val uuid1 = new UUID(0L, 0L)
    assertTrue(uuid1.equals(uuid1))
    assertFalse(uuid1.equals(null))
    assertFalse(uuid1.equals("something else"))

    val uuid2 = new UUID(0L, 0L)
    assertTrue(uuid1.equals(uuid2))

    val uuid3 = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L)
    val uuid4 = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L)
    assertTrue(uuid3.equals(uuid4))
    assertFalse(uuid3.equals(uuid1))

    assertFalse(
      uuid3.equals(new UUID(0x781d4fae7dec11d0L, 0xa76500a0c91e6bf6L))
    )
    assertFalse(
      uuid3.equals(new UUID(0xf81d4fae7dec11d1L, 0xa76500a0c91e6bf6L))
    )
    assertFalse(
      uuid3.equals(new UUID(0xf81d4fae7dec11d0L, 0xa76530a0c91e6bf6L))
    )
    assertFalse(
      uuid3.equals(new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6cf6L))
    )
  }

  @Test def toStringTest(): Unit = {
    assertEquals(
      "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
      new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L).toString
    )
    assertEquals(
      "00000000-0000-1000-8000-000000000000",
      new UUID(0x0000000000001000L, 0x8000000000000000L).toString
    )
  }

  @Test def fromString(): Unit = {
    val uuid1 = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    assertTrue(uuid1.equals(new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L)))
    assertEquals(0xf81d4fae7dec11d0L, uuid1.getMostSignificantBits())
    assertEquals(0xa76500a0c91e6bf6L, uuid1.getLeastSignificantBits())
    assertEquals(2, uuid1.variant())
    assertEquals(1, uuid1.version())
    assertEquals(130742845922168750L, uuid1.timestamp())
    assertEquals(10085, uuid1.clockSequence())
    assertEquals(690568981494L, uuid1.node())

    val uuid2 = UUID.fromString("00000000-0000-1000-8000-000000000000")
    assertEquals(uuid2, new UUID(0x0000000000001000L, 0x8000000000000000L))
    assertEquals(0x0000000000001000L, uuid2.getMostSignificantBits())
    assertEquals(0x8000000000000000L, uuid2.getLeastSignificantBits())
    assertEquals(2, uuid2.variant())
    assertEquals(1, uuid2.version())
    assertEquals(0L, uuid2.timestamp())
    assertEquals(0, uuid2.clockSequence())
    assertEquals(0L, uuid2.node())

    assertThrows(classOf[IllegalArgumentException], UUID.fromString(""))
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae_7dec-11d0-a765-00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec_11d0-a765-00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec-11d0_a765-00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec-11d0-a765_00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("-7dec-11d0-a765-00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae--11d0-a765-00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec--a765-00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec-11d0--00a0c91e6bf6")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec-11d0-a765-")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dec-11d0-a765")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      UUID.fromString("f81d4fae-7dZc-11d0-a765-00a0c91e6bf6")
    )
  }

}
