package scala.scalanative.issues

object _337 extends tests.Suite {
  test("github.com/scala-native/scala-native/issues/337") {
    case class TestObj(value: Int)
    val obj = TestObj(10)
    assert(obj.value == 10)
  }
}
