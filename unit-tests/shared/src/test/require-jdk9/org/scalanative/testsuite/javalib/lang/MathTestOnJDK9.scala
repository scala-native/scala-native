package javalib.lang

import org.junit.Test
import org.junit.Assert._

class MathTestOnJDK9 {

  @Test def testFma(): Unit = {
    assertEquals(10.0f, Math.fma(2.0f, 3.0f, 4.0f), 0.0f)
    assertEquals(10.0, Math.fma(2.0, 3.0, 4.0), 0.0)
  }

}
