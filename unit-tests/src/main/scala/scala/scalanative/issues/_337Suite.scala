package scala.scalanative.issues

object _337Suite extends tests.Suite {
  test("#337") {
    case class TestObj(value: Int)
    val obj = TestObj(10)
    assert(obj.value == 10)
  }
}
