package scala.scalanative.issues

object _253Suite extends tests.Suite {
  test("#253") {
    class Cell(val value: Int)

    val arr     = Array(new Cell(1), new Cell(2))
    val reverse = arr.reverse

    assert(reverse(0).value == 2)
    assert(reverse(1).value == 1)
  }
}
