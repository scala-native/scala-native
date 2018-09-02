package java.util.function

object BiConsumerSuite extends tests.Suite {
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

  test("BiConsumer.apply") {
    assert(result == 0)
    addT.accept(2, 2)
    assert(result == 4)
  }

  // andThen doesn't work currently.
  /*
  test("BiConsumer.andThen"){
    result = 0
    assert(result == 0)
    addT.andThen(timesT).accept(2, 3)

    // addT will add 2 + 2 = 4 to result which is zero = 4
    // then 2*3 = 6 will be added to result which is 4, =  11
    assert(result == 11)
  }
 */

}
