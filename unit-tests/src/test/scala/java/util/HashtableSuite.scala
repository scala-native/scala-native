package java.util

object HashtableSuite extends tests.Suite {

  test("put on null key or value") {
    val t = new Hashtable[AnyRef, AnyRef]()
    assertThrows[NullPointerException](t.put(null, "value"))
    assertThrows[NullPointerException](t.put("key", null))
  }
}
