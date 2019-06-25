package java.util

// Ported from Scala.js

import scala.reflect.ClassTag

object IdentityHashMapSuite extends MapSuite {
  override def factory: IdentityMapSuiteFactory = new IdentityMapSuiteFactory

  // tests from Harmony
  // tests with null keys and values
  val map                  = factory.empty[AnyRef, AnyRef]
  var result: AnyRef       = _
  var value: AnyRef        = _
  var anothervalue: AnyRef = _

  test("null key and value") { //
    result = map.put(null, null)
    assertTrue(map.containsKey(null))
    assertTrue(map.containsValue(null))
    assertNull(map.get(null))
    assertNull(result)
  }

  test("null key replaces") {
    value = "a value"
    result = map.put(null, value)
    assertTrue(map.containsKey(null))
    assertTrue(map.containsValue(value))
    assertTrue(map.get(null) eq value)
    assertNull(result)
  }

  test("null value") {
    val key = "a key"
    result = map.put(key, null)
    assertTrue(map.containsKey(key))
    assertTrue(map.containsValue(null))
    assertNull(map.get(key))
    assertNull(result)
  }

  test("another null key replaces old val") {
    anothervalue = "another value"
    result = map.put(null, anothervalue)
    assertTrue(map.containsKey(null))
    assertTrue(map.containsValue(anothervalue))
    assertTrue(map.get(null) eq anothervalue)
    assertTrue(result eq value)
  }

  test("remove a null key") {
    result = map.remove(null)
    assertTrue(result eq anothervalue)
    assertTrue(!map.containsKey(null))
    assertTrue(!map.containsValue(anothervalue))
    assertNull(map.get(null))
  }

  test("check put, null key, null value") {
    val map   = factory.empty[AnyRef, AnyRef]
    val value = "Some value"
    map.put(null, value)
    assertEquals(value, map.get(null))
    val key = "Some key"
    map.put(key, null)
    assertNull(map.get(key))
  }

  test("check remove") {
    val map = factory.empty[AnyRef, AnyRef]
    map.put(null, null)
    map.put("key1", "value1")
    map.put("key2", "value2")
    map.remove("key1")
    assertTrue(!map.containsKey("key1"))                                 // Did not remove key1
    assertTrue(!map.containsValue("value1"))                             // Did not remove the value for key
    assertTrue(map.get("key2") != null && (map.get("key2") eq "value2")) // Modified key2
    assertNull(map.get(null))                                            // Modified null entry
  }

  test("Regression for HARMONY-37") {
    // cannot link: @java.util.IdentityHashMap::field.size
    // requires size()
    val map = factory.empty[String, String]
    map.remove("absent")
    assertEquals(0, map.size()) // Size is incorrect
    map.put("key", "value")
    map.remove("key")
    assertEquals(0, map.size()) // After removing non-null element size is incorrect
    map.put(null, null)
    assertEquals(1, map.size()) // adding literal null failed
    map.remove(null)
    assertEquals(0, map.size()) // After removing null element size is incorrect
  }
  // other Harmony tests available
}

class IdentityMapSuiteFactory extends AbstractMapSuiteFactory {
  override def implementationName: String =
    "java.util.IdentityHashMap"

  override def empty[K: ClassTag, V: ClassTag]: IdentityHashMap[K, V] =
    new IdentityHashMap[K, V]

  def allowsNullKeys: Boolean                   = true
  def allowsNullValues: Boolean                 = true
  override def allowsIdentityBasedKeys: Boolean = true

}
