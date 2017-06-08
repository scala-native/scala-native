package java.util

object ArraysSuite extends tests.Suite {
  test("asList") {
    val array = Array("a", "c")
    val list  = Arrays.asList(array: _*)
    array.update(1, "b")
    assert(list.size() == 2)
    assert(list.get(0) == "a")
    assert(list.get(1) == "b")

    list.set(0, "1")
    assert(array(0) == "1")
  }
}
