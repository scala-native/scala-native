package scala.scalanative.nir

import org.junit.Test
import org.junit.Assert._

class VersionsSuite {
  @Test def crossBinaryVersion(): Unit = {
    def test(full: String, cross: String): Unit =
      assertEquals(full, cross, Versions.binaryVersion(full))

    test("0.5.0-SNAPSHOT", "0.5.0-SNAPSHOT")
    test("0.5.0-M1", "0.5.0-M1")
    test("0.5.0", "0.5")
    test("0.5.1-SNAPSHOT", "0.5")
    test("0.5.1", "0.5")
    test("1.0.0", "1")
    test("1.0.2", "1")
    test("1.0.2-M1", "1")
    test("1.0.0-SNAPSHOT", "1.0-SNAPSHOT")
    test("1.0.0-M1", "1.0-M1")
    test("1.2.0-SNAPSHOT", "1")
    test("1.2.0-M1", "1")
    test("1.3.0-M1", "1")
  }
}
