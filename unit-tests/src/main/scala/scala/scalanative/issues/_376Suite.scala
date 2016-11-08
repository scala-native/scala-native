package scala.scalanative.issues

object _376Suite extends tests.Suite {
  test("#376") {
    val m     = scala.collection.mutable.Map.empty[String, String]
    val hello = "hello"
    val world = "world"
    m(hello) = world
    val h = m.getOrElse(hello, "Failed !")
    assert(h equals world)
  }
}
