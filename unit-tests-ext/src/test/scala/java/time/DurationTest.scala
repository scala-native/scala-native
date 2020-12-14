package java.time

import org.junit.Test
import org.junit.Assert._

class DurationTest {

  val v1 = Duration.ofSeconds(350070L, 112233L)
  val v2 = Duration.ofSeconds(350070L, 112233L)
  val v3 = Duration.ofSeconds(350070L, 0L)

  @Test def testEquals(): Unit = {
    assertTrue(v1.equals(v1))
    assertTrue(v1.equals(v2))
    assertTrue(v2.equals(v1))
    assertTrue(v1 != null)
    assertFalse(v1.equals(new Object()))
  }

  @Test def testCompareTo(): Unit = {
    assertTrue(v1.compareTo(v1) == 0)
    assertTrue(v1.compareTo(v2) == 0)
    assertTrue(v2.compareTo(v1) == 0)
    assertTrue(v2.compareTo(v3) > 0)
    assertTrue(v3.compareTo(v1) < 0)
  }

}
