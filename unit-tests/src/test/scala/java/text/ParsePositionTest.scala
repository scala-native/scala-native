package java.text

// This file is original content, covered by the Scala Native license.

// The order in which tests are run is not defined, so there is no
// concept of "test a method before using it in another test".
// Because any given test can not assume the existence & passing of
// any other test, there may be checks which would be redundant in a
// more ordered world.

import org.junit.Assert._
import org.junit.Test

class ParsePositionTest {
  @Test def constructorTest(): Unit = {
    val expectedIndex      = 2
    val expectedErrorIndex = -1

    val pp = new ParsePosition(expectedIndex)

    assertEquals("index| ", expectedIndex, pp.getIndex())
    assertEquals("errorIndex| ", expectedErrorIndex, pp.getErrorIndex())
  }

  @Test def equalsTest(): Unit = {
    val idx = 11

    val pp1 = new ParsePosition(idx)
    val pp2 = new ParsePosition(idx)
    val pp3 = new ParsePosition(idx + 1)

    assertFalse("Similar ParsePositions should not be reference equal",
                pp1.eq(pp2))

    assertEquals("Similar ParsePositions should be content equal", pp1, pp2)

    // Detect hidden caching which might mess up content equality setting.
    assertFalse("Different ParsePositions should not be reference equal",
                pp1.eq(pp3))

    assertFalse("Different ParsePositions should not be content equal",
                pp1.equals(pp3))

    val errorIdx = -7
    pp1.setErrorIndex(errorIdx)

    assertFalse("Changed ParsePositions should not be content equal",
                pp1.equals(pp2))

    pp2.setErrorIndex(errorIdx)
    assertEquals("Newly similar ParsePositions should be content equal",
                 pp1,
                 pp2)
  }

  @Test def hashCodeTest(): Unit = {
    val idx = 22

    val pp1 = new ParsePosition(idx)
    val pp2 = new ParsePosition(idx * 2)

    assertFalse("Different ParsePositions should not be reference equal",
                pp1.eq(pp2))

    val firstHash = pp1.hashCode

    // Uses knowledge of underlying implementation having no hash collisions.
    assertFalse("Different ParsePositions should not have same hashCode",
                firstHash == pp2.hashCode)

    pp2.setIndex(idx)

    assertEquals("Similar ParsePositions should have same hashCode",
                 firstHash,
                 pp2.hashCode)

    val errorIdx = -5
    pp1.setErrorIndex(errorIdx)
    pp2.setErrorIndex(errorIdx)

    val secondHash = pp1.hashCode
    assertEquals("Parallel changes should maintain hashCode equality",
                 secondHash,
                 pp2.hashCode)

    assertFalse("Changing ParsePosition should change hashCode",
                secondHash == firstHash)
  }

  @Test def setGetErrorIndexTest(): Unit = {
    val expectedIndex                 = 7
    val expectedConstructorErrorIndex = -1

    val expectedSetErrorIndex = -7

    val pp = new ParsePosition(expectedIndex)

    assertEquals("Constructor errorIndex| ",
                 expectedConstructorErrorIndex,
                 pp.getErrorIndex())

    pp.setErrorIndex(expectedSetErrorIndex)

    assertEquals("Set errorIndex| ", expectedSetErrorIndex, pp.getErrorIndex())

    assertEquals("Set errorIndex should not change index| ",
                 expectedIndex,
                 pp.getIndex())
  }

  @Test def setGetIndexTest(): Unit = {
    val expectedIndex      = 6
    val expectedErrorIndex = -1

    val expectedSetIndex = -8

    val pp = new ParsePosition(expectedIndex)

    assertEquals("Constructor index| ", expectedIndex, pp.getIndex())

    pp.setIndex(expectedSetIndex)

    assertEquals("Set method Index| ", expectedSetIndex, pp.getIndex())

    assertEquals("Set index should not change errorIndex| ",
                 expectedErrorIndex,
                 pp.getErrorIndex())
  }

  @Test def toStringTest(): Unit = {
    val idx      = 99
    val errorIdx = -2

    val pp = new ParsePosition(idx)

    pp.setErrorIndex(errorIdx)

    val expected =
      s"java.text.ParsePosition[index=${idx},errorIndex=${errorIdx}]"

    assertEquals(expected, pp.toString())
  }
}
