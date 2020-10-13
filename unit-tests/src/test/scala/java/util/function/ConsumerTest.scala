package java.util.function

import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class ConsumerTest {
  var amount = 1

  @Before
  protected def setUp(): Unit = {
    var amount = 1
  }

  val addT = new Consumer[Int] {
    override def accept(t: Int): Unit = {
      amount = amount + t
    }
  }

  val timesT = new Consumer[Int] {
    override def accept(t: Int): Unit = {
      amount = amount * t
    }
  }

  @Test def consumerApply(): Unit = {
    assertTrue(amount == 1)
    addT.accept(2)
    assertTrue(amount == 3)
  }

  @Test def consumerAndThen(): Unit = {
    assertTrue(amount == 1)
    addT.andThen(timesT).accept(2)

    assertTrue(amount == 6)
  }
}
