package test

import org.junit.Assert.*
import org.junit.Test

class LibTest {
  @Test
  def square() = {
    assertEquals(9, Lib.square(3))
  }
}
