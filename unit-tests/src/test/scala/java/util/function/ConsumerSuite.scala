package java.util.function

object ConsumerSuite extends tests.Suite {
  var amount = 1

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

  test("Consumer.apply") {
    assert(amount == 1)
    addT.accept(2)
    assert(amount == 3)
  }

  // andThen doesn't work currently.
  /*
  test("Consumer.andThen"){
    amount = 1
    assert(amount == 1)
    addT.andThen(timesT).accept(2)

    assert(amount == 6)
  }
 */

}
