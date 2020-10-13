package java.util.function

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

class BiConsumerTest {
  var result = 0

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

  @Ignore("andThen doesn't work currently")
  @Test def biConsumerAndThen(): Unit = {
    // result = 0
    // assertTrue(result == 0)
    // addT.andThen(timesT).accept(2, 3)

    // // addT will add 2 + 2 = 4 to result which is zero = 4
    // // then 2*3 = 6 will be added to result which is 4, =  11
    // assertTrue(result == 11)
  }
}
