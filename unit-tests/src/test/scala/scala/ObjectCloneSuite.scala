package scala

object ObjectCloneSuite extends tests.Suite {

  case class I(var i: Int) {
    def copy(): I = this.clone().asInstanceOf[I]
  }

  test("clone with primitive") {
    val obj   = I(123)
    val clone = obj.copy()

    obj.i = 124

    assert(obj.i == 124)
    assert(clone.i == 123)
  }

  case class Arr(val arr: Array[Int]) {
    def copy(): Arr = this.clone().asInstanceOf[Arr]
  }

  test("clone with ref") {
    val obj   = Arr(Array(123))
    val clone = obj.copy()

    obj.arr(0) = 124

    assert(obj.arr(0) == 124)
    assert(clone.arr(0) == 124)
  }

}
