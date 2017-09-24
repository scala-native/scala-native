package scala

object EqualitySuite extends tests.Suite {
  case class O(m: Int)

  test("case class equality") {
    assert(O(5) == O(5))
  }

  test("null equals null") {
    assert((null: Object) == (null: Object))
  }

  test("null does not equal object") {
    val obj = new Object
    assert((null: Object) != obj)
  }

  test("object does not equal null") {
    val obj = new Object
    assert(obj != (null: Object))
  }

  test("== null doesn't call equals") {
    var equalsCalled = false
    val obj = new Object {
      override def equals(other: Any) = {
        equalsCalled = true
        other.asInstanceOf[AnyRef] eq this
      }
    }
    assert(obj != null)
    assert(!equalsCalled)
    val iamnull: Any = null
    assert(obj != iamnull)
    assert(equalsCalled)
  }
}
