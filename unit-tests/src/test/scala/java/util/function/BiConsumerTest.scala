package java.util.function

import org.junit.Ignore
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class BiConsumerTest {
  var result = 0

  @Before
  def setUp(): Unit = {
    result = 0
  }

  val addT = new BiConsumer[Int, Int] {
    override def accept(t: Int, u: Int): Unit = {
      result = result + (t + u)
    }
  }

  val timesT = new BiConsumer[Int, Int] {
    override def accept(t: Int, u: Int): Unit = {
      result = result + (t * u)
    }
  }

  @Test def biConsumerApply(): Unit = {
    assertTrue(result == 0)
    addT.accept(2, 2)
    assertTrue(result == 4)
  }

  @Ignore("#1229 - 2.11 does not have default method support")
  @Test def biConsumerAndThen(): Unit = {
    // assertTrue(result == 0)
    // addT.andThen(timesT).accept(2, 3)
    // assertTrue(result == 11)
  }
}
