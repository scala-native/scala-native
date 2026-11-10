package scala

import org.junit.Assert._
import org.junit.Test

class ArrayTransposeTest {

  @Test def transpose(): Unit = {
    val t = Array(Array(0, 2, 4), Array(1, 3, 5)).transpose
    assertEquals(3, t.length)
    assertArrayEquals(Array(0, 1), t(0))
    assertArrayEquals(Array(4, 5), t(2))
    assertEquals(0, Array.empty[Array[Int]].transpose.length)
  }
}
