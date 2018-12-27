package java.util

// Ported from Scala.js

import java.{util => ju}
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

}

class IdentityMapSuiteFactory extends AbstractMapSuiteFactory {
  override def implementationName: String =
    "java.util.IdentityHashMap"

  override def empty[K: ClassTag, V: ClassTag]: ju.IdentityHashMap[K, V] =
    new ju.IdentityHashMap[K, V]

  def allowsNullKeys: Boolean                   = true
  def allowsNullValues: Boolean                 = true
  override def allowsIdentityBasedKeys: Boolean = true

}
