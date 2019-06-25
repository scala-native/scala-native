package scala

object HashCodeSuite extends tests.Suite {
  case class MyData(string: String, num: Int)

  test("Hash code of string matches Scala JVM") {
    assert("hello".hashCode == 99162322)
  }

  test("Hash code of case class matches Scala JVM") {
    assert(MyData("hello", 12345).hashCode == -1824015247)
  }
}
