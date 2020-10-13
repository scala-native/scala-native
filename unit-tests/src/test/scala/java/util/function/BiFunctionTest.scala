package java.util.function

import org.junit.Test
import org.junit.Assert._

class BiFunctionTest {
  val f = new BiFunction[Integer, Integer, Integer] {
    override def apply(x: Integer, y: Integer): Integer = {
      x + y
    }
  }

  @Test def biFunctionApplyIntegerInteger(): Unit = {
    assertEquals(f(1, 2), 3)
  }

  val ff = new Function[Integer, Integer] {
    override def apply(x: Integer): Integer = x + 1
  }

  @Test def biFunctionAndThen(): Unit = {
    assertEquals(f.andThen(ff)(1, 2), 4)
  }
}
