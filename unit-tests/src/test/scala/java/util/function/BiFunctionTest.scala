package java.util.function

import org.junit.Ignore
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

  @Ignore("#1229 - 2.11 does not have default method support")
  @Test def biFunctionAndThen(): Unit = {
    // assertEquals(f.andThen(ff)(1, 2), 4)
  }
}
