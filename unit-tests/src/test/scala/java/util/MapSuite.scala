package java.util

// Ported from Scala.js
///test-suite/js/src/test/scala/org/scalajs/testsuite/jsinterop/MapTest.scala
// Approximate commit SHA could not be found because of re-organization
// of Scala.js repository or lack of skill. Original port to SN happened
// approximately 2017-11-23.
//
// With original additions for Scala Native.
// There are enough Scala Native changes that this file has evolved
// quite a bit from the Scala.js original.
//
// putIfAbsent test comes from Scala.js ConcurrentHashMapTest.scala

// Implementation Note:
//   The ugly explicit implementations of Function, BiFunction, BiConsumer
//   arguments required for Scala 2.11. They can be simplified & beautified
//   for Scala 2.12 and above.

import scala.reflect.ClassTag

import scala.collection.JavaConversions._
import scala.collection.{immutable => im}
import scala.collection.{mutable => mu}

import java.{util => ju}

trait MapSuite extends tests.Suite {
  def factory: MapFactory

  test("should store strings") {
    val mp = factory.empty[String, String]

    assertEquals(0, mp.size())
    mp.put("ONE", "one")
    assertEquals(1, mp.size())
    assertEquals("one", mp.get("ONE"))
    mp.put("TWO", "two")
    assertEquals(2, mp.size())
    assertEquals("two", mp.get("TWO"))
  }

  test("should store integers",
       cond = !factory.isInstanceOf[IdentityMapSuiteFactory]) {
    val mp = factory.empty[Int, Int]

    mp.put(100, 12345)
    assertEquals(1, mp.size())
    val one = mp.get(100)
    assertEquals(12345, one)
  }

  test("should store doubles also in corner cases",
       cond = !factory.isInstanceOf[IdentityMapSuiteFactory]) {
    val mp = factory.empty[Double, Double]

    mp.put(1.2345, 11111.0)
    assertEquals(1, mp.size())
    val one = mp.get(1.2345)
    assertEquals(11111.0, one, 0.0)

    mp.put(Double.NaN, 22222.0)
    assertEquals(2, mp.size())
    val two = mp.get(Double.NaN)
    assertEquals(22222.0, two, 0.0)

    mp.put(+0.0, 33333.0)
    assertEquals(3, mp.size())
    val three = mp.get(+0.0)
    assertEquals(33333.0, three, 0.0)

    mp.put(-0.0, 44444.0)
    assertEquals(4, mp.size())
    val four = mp.get(-0.0)
    assertEquals(44444.0, four, 0.0)
  }

  test("should store custom objects") {
    case class TestObj(num: Int)
    val mp = factory.empty[TestObj, TestObj]

    val testKey = TestObj(100)
    mp.put(testKey, TestObj(12345))
    assertEquals(1, mp.size())
    if (factory.allowsIdentityBasedKeys) {
      val one = mp.get(testKey)
      assertEquals(12345, one.num)
    } else {
      val one = mp.get(TestObj(100))
      assertEquals(12345, one.num)
    }
  }

  test("should remove stored elements") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    assertEquals(1, mp.size())
    assertEquals("one", mp.remove("ONE"))
    val newOne = mp.get("ONE")
    assertNull(mp.get("ONE"))
  }

  test("should remove stored elements on double corner cases",
       cond = !factory.isInstanceOf[IdentityMapSuiteFactory]) {
    val mp = factory.empty[Double, String]

    mp.put(1.2345, "11111.0")
    mp.put(Double.NaN, "22222.0")
    mp.put(+0.0, "33333.0")
    mp.put(-0.0, "44444.0")

    assertEquals("11111.0", mp.get(1.2345))
    assertEquals("22222.0", mp.get(Double.NaN))
    assertEquals("33333.0", mp.get(+0.0))
    assertEquals("44444.0", mp.get(-0.0))

    assertEquals("44444.0", mp.remove(-0.0))
    assertNull(mp.get(-0.0))

    mp.put(-0.0, "55555.0")

    assertEquals("33333.0", mp.remove(+0.0))
    assertNull(mp.get(+0.0))

    mp.put(+0.0, "66666.0")

    assertEquals("22222.0", mp.remove(Double.NaN))
    assertNull(mp.get(Double.NaN))

    mp.put(Double.NaN, "77777.0")

    mp.clear()

    assertTrue(mp.isEmpty)
  }

  test("should put or fail on null keys") {
    if (factory.allowsNullKeys) {
      val mp = factory.empty[String, String]
      mp.put(null, "one")
      assertEquals("one", mp.get(null))
    } else {
      val mp = factory.empty[String, String]
      expectThrows(classOf[NullPointerException], mp.put(null, "one"))
    }
  }

  test("should put or fail on null values") {
    if (factory.allowsNullValues) {
      val mp = factory.empty[String, String]
      mp.put("one", null)
      assertNull(mp.get("one"))
    } else {
      val mp = factory.empty[String, String]
      expectThrows(classOf[NullPointerException], mp.put("one", null))
    }
  }

  test("should be cleared with one operation") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")
    assertEquals(2, mp.size())
    mp.clear()
    assertEquals(0, mp.size())
  }

  test("should check contained key presence") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    assertTrue(mp.containsKey("ONE"))
    assertFalse(mp.containsKey("TWO"))
    if (factory.allowsNullKeysQueries)
      assertFalse(mp.containsKey(null))
    else
      expectThrows(classOf[Throwable], mp.containsKey(null))
  }

  test("should check contained value presence") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    assertTrue(mp.containsValue("one"))
    assertFalse(mp.containsValue("two"))
    if (factory.allowsNullValuesQueries)
      assertFalse(mp.containsValue(null))
    else
      expectThrows(classOf[Throwable], mp.containsValue(null))
  }

  test("should give proper Collection over values") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    val values = mp.values()
    assertEquals(1, values.size())
    val iter = values.iterator
    assertTrue(iter.hasNext)
    assertEquals("one", iter.next)
    assertFalse(iter.hasNext)
  }

  test("should give proper EntrySet over key value pairs") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    val entrySet = mp.entrySet

    assertEquals(1, entrySet.size())

    val iter = entrySet.iterator
    assertTrue(iter.hasNext)
    val next = iter.next
    assertFalse(iter.hasNext)
    assertEquals("ONE", next.getKey)
    assertEquals("one", next.getValue)
  }

  test("should give proper KeySet over keys") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    val keySet = mp.keySet()

    assertEquals(1, keySet.size())

    val iter = keySet.iterator
    assertTrue(iter.hasNext)
    assertEquals("ONE", iter.next)
    assertFalse(iter.hasNext)
  }

  test("should put a whole map into") {
    val mp = factory.empty[String, String]

    val m = mu.Map[String, String]("X" -> "y")
    mp.putAll(mutableMapAsJavaMap(m))
    assertEquals(1, mp.size())
    assertEquals("y", mp.get("X"))

    val nullMap = mu.Map[String, String]((null: String) -> "y", "X" -> "y")

    if (factory.allowsNullKeys) {
      mp.putAll(mutableMapAsJavaMap(nullMap))
      assertEquals("y", mp.get(null))
      assertEquals("y", mp.get("X"))
    } else {
      expectThrows(classOf[NullPointerException],
                   mp.putAll(mutableMapAsJavaMap(nullMap)))
    }
  }

  class SimpleQueryableMap[K, V](inner: mu.HashMap[K, V])
      extends ju.AbstractMap[K, V] {
    def entrySet(): java.util.Set[java.util.Map.Entry[K, V]] = {
      setAsJavaSet(inner.map {
        case (k, v) => new ju.AbstractMap.SimpleImmutableEntry(k, v)
      }.toSet)
    }
  }

  test("values should mirror the related map size") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    val values = mp.values()
    assertEquals(2, values.size())

    mp.put("THREE", "three")

    assertEquals(3, values.size())

    mp.remove("ONE")

    assertEquals(2, values.size())

    assertFalse(values.isEmpty)

    mp.clear()

    assertEquals(0, values.size())

    assertTrue(values.isEmpty)

    val hm1 = mu.HashMap("ONE"          -> "one", "TWO" -> "two")
    val hm2 = mu.HashMap("ONE"          -> null, "TWO"  -> "two")
    val hm3 = mu.HashMap((null: String) -> "one", "TWO" -> "two")
    val hm4 = mu.HashMap((null: String) -> null, "TWO"  -> "two")

    assertEquals(2, new SimpleQueryableMap(hm1).values().size())
    assertEquals(2, new SimpleQueryableMap(hm2).values().size())
    assertEquals(2, new SimpleQueryableMap(hm3).values().size())
    assertEquals(2, new SimpleQueryableMap(hm4).values().size())
  }

  test("values should check single and multiple objects presence") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    val values = mp.values()
    assertTrue(values.contains("one"))
    assertTrue(values.contains("two"))
    assertFalse(values.contains("three"))
    if (factory.allowsNullValuesQueries)
      assertFalse(values.contains(null))
    else
      expectThrows(classOf[Throwable], mp.contains(null))

    mp.put("THREE", "three")

    assertTrue(values.contains("three"))

    val coll1 = asJavaCollection(im.Set("one", "two", "three"))
    assertTrue(values.containsAll(coll1))

    val coll2 = asJavaCollection(im.Set("one", "two", "three", "four"))
    assertFalse(values.containsAll(coll2))

    val coll3 = asJavaCollection(im.Set("one", "two", "three", null))
    assertFalse(values.containsAll(coll2))

    val nummp = factory.empty[Double, Double]

    val numValues = nummp.values()
    nummp.put(1, +0.0)
    assertTrue(numValues.contains(+0.0))
    assertFalse(numValues.contains(-0.0))
    assertFalse(numValues.contains(Double.NaN))

    nummp.put(2, -0.0)
    assertTrue(numValues.contains(+0.0))
    assertTrue(numValues.contains(-0.0))
    assertFalse(numValues.contains(Double.NaN))

    nummp.put(3, Double.NaN)
    assertTrue(numValues.contains(+0.0))
    assertTrue(numValues.contains(-0.0))
    assertTrue(numValues.contains(Double.NaN))

    val hm1 = mu.HashMap(1.0         -> null, 2.0 -> 2.0)
    val hm2 = mu.HashMap((null: Any) -> 1.0, 2.0  -> 2.0)
    val hm3 = mu.HashMap((null: Any) -> null, 2.0 -> 2.0)

    assertFalse(new SimpleQueryableMap(hm1).values().contains(1.0))
    assertTrue(new SimpleQueryableMap(hm2).values().contains(1.0))
    assertFalse(new SimpleQueryableMap(hm3).values().contains(1.0))

    assertTrue(new SimpleQueryableMap(hm1).values().contains(null))
    assertFalse(new SimpleQueryableMap(hm2).values().contains(null))
    assertTrue(new SimpleQueryableMap(hm3).values().contains(null))
  }

  test("values should side effect clear remove retain on the related map") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    val values = mp.values()
    assertFalse(values.isEmpty)
    assertFalse(mp.isEmpty)

    values.clear()

    assertTrue(values.isEmpty)
    assertTrue(mp.isEmpty)

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    assertTrue(mp.containsKey("ONE"))

    values.remove("one")

    assertFalse(mp.containsKey("ONE"))

    mp.put("ONE", "one")
    mp.put("THREE", "three")

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    values.removeAll(asJavaCollection(im.List("one", "two")))

    assertFalse(mp.containsKey("ONE"))
    assertFalse(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    mp.put("ONE", "one")
    mp.put("TWO", "two")
    mp.put("THREE", "three")

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    values.retainAll(asJavaCollection(im.List("one", "two")))

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertFalse(mp.containsKey("THREE"))
  }

  test("keySet should mirror the related map size") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    val keySet = mp.keySet()
    assertEquals(2, keySet.size())

    mp.put("THREE", "three")

    assertEquals(3, keySet.size())

    mp.remove("ONE")

    assertEquals(2, keySet.size())

    assertFalse(keySet.isEmpty)

    mp.clear()

    assertEquals(0, keySet.size())

    assertTrue(keySet.isEmpty)

    val hm1 = mu.HashMap("ONE"          -> "one", "TWO" -> "two")
    val hm2 = mu.HashMap("ONE"          -> null, "TWO"  -> "two")
    val hm3 = mu.HashMap((null: String) -> "one", "TWO" -> "two")
    val hm4 = mu.HashMap((null: String) -> null, "TWO"  -> "two")

    assertEquals(2, new SimpleQueryableMap(hm1).keySet().size())
    assertEquals(2, new SimpleQueryableMap(hm2).keySet().size())
    assertEquals(2, new SimpleQueryableMap(hm3).keySet().size())
    assertEquals(2, new SimpleQueryableMap(hm4).keySet().size())
  }

  test("keySet should check single and multiple objects presence") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    val keySet = mp.keySet()
    assertTrue(keySet.contains("ONE"))
    assertTrue(keySet.contains("TWO"))
    assertFalse(keySet.contains("THREE"))
    if (factory.allowsNullKeysQueries)
      assertFalse(keySet.contains(null))
    else
      expectThrows(classOf[Throwable], mp.contains(null))

    mp.put("THREE", "three")

    assertTrue(keySet.contains("THREE"))

    val coll1 =
      asJavaCollection(im.Set("ONE", "TWO", "THREE"))
    assertTrue(keySet.containsAll(coll1))

    val coll2 =
      asJavaCollection(im.Set("ONE", "TWO", "THREE", "FOUR"))
    assertFalse(keySet.containsAll(coll2))

    val coll3 =
      asJavaCollection(im.Set("ONE", "TWO", "THREE", null))
    assertFalse(keySet.containsAll(coll2))

    val nummp = factory.empty[Double, Double]

    val numkeySet = nummp.keySet()
    nummp.put(+0.0, 1)
    assertTrue(numkeySet.contains(+0.0))
    assertFalse(numkeySet.contains(-0.0))
    assertFalse(numkeySet.contains(Double.NaN))

    nummp.put(-0.0, 2)
    assertTrue(numkeySet.contains(+0.0))
    assertTrue(numkeySet.contains(-0.0))
    assertFalse(numkeySet.contains(Double.NaN))

    nummp.put(Double.NaN, 3)
    assertTrue(numkeySet.contains(+0.0))
    assertTrue(numkeySet.contains(-0.0))
    assertTrue(numkeySet.contains(Double.NaN))

    val hm1 = mu.HashMap(1.0         -> null, 2.0 -> 2.0)
    val hm2 = mu.HashMap((null: Any) -> 1.0, 2.0  -> 2.0)
    val hm3 = mu.HashMap((null: Any) -> null, 2.0 -> 2.0)

    assertTrue(new SimpleQueryableMap(hm1).keySet().contains(1.0))
    assertFalse(new SimpleQueryableMap(hm2).keySet().contains(1.0))
    assertFalse(new SimpleQueryableMap(hm3).keySet().contains(1.0))

    assertFalse(new SimpleQueryableMap(hm1).keySet().contains(null))
    assertTrue(new SimpleQueryableMap(hm2).keySet().contains(null))
    assertTrue(new SimpleQueryableMap(hm3).keySet().contains(null))
  }

  test("keySet should side effect clear remove retain on the related map") {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    val keySet = mp.keySet()
    assertFalse(keySet.isEmpty)
    assertFalse(mp.isEmpty)

    keySet.clear()

    assertTrue(keySet.isEmpty)

    assertTrue(mp.isEmpty)

    mp.put("ONE", "one")
    mp.put("TWO", "two")

    assertTrue(mp.containsKey("ONE"))

    keySet.remove("ONE")

    assertFalse(mp.containsKey("ONE"))

    mp.put("ONE", "one")
    mp.put("THREE", "three")

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    keySet.removeAll(asJavaCollection(im.List("ONE", "TWO")))

    assertFalse(mp.containsKey("ONE"))
    assertFalse(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    mp.put("ONE", "one")
    mp.put("TWO", "two")
    mp.put("THREE", "three")

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    keySet.retainAll(asJavaCollection(im.List("ONE", "TWO")))

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertFalse(mp.containsKey("THREE"))
  }

  // Tests of concrete default methods in Map.scala.
  // These are selected as being default methods, then listed
  // according to the order of their full, not summary,
  // description in the Java 8 documentation. That is a subset of the
  // Scala.js practice.

  test("getOrDefault(key, d) should return value when key is present") {
    val mp = factory.empty[Int, Int]

    val presentKey   = 400
    val presentValue = 876234
    val defaultValue = 9999

    mp.put(100, 12345)
    mp.put(300, 98765)
    mp.put(presentKey, presentValue)
    assertEquals(3, mp.size())

    assertEquals(presentValue, mp.getOrDefault(presentKey, defaultValue))
  }

  test("getOrDefault(key, d) should return default when key is absent") {
    type K = Int
    type V = Int
    val mp = factory.empty[K, V]

    val presentKey   = 400
    val presentValue = 876234
    val missingKey   = 200
    val defaultValue = 9999

    mp.put(100, 12345)
    mp.put(300, 98765)
    mp.put(presentKey, presentValue)
    assertEquals(3, mp.size())

    assertEquals(defaultValue, mp.getOrDefault(missingKey, defaultValue))
  }

  test("forEach(action) should invoke action on every element") {
    type K = Int
    type V = Double
    val mp = factory.empty[K, V]

    val startIdx = -2
    val endIdx   = 5
    val total    = 12
    var sum      = 0

    (startIdx to endIdx).foreach(i => mp.put(i, (i * 5.0)))

    mp.forEach(new function.BiConsumer[K, V]() {
      def accept(key: K, value: V) = (sum += key)
    })

    assertEquals(total, sum)
  }

  test("replaceAll(f) should change every element value to function result") {
    type K = Int
    type V = Double
    val mp = factory.empty[K, V]

    val startIdx = 1
    val endIdx   = 10
    val factor   = 2

    (startIdx to endIdx).foreach(i => mp.put(i, i))

    val replaceFunc = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V = {
        value * factor
      }
    }

    mp.replaceAll(replaceFunc)

    (startIdx to endIdx).foreach(i => assertEquals(i * factor, mp.get(i)))
  }

  test("putIfAbsent(k, v) should conditionally insert & return expected") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    assertNull(mp.putIfAbsent("abc", "def"))
    assertEquals("def", mp.get("abc"))

    assertNull(mp.putIfAbsent("123", "456"))
    assertEquals("456", mp.get("123"))

    assertEquals("def", mp.putIfAbsent("abc", "def"))
    assertEquals("def", mp.putIfAbsent("abc", "ghi"))
    assertEquals("456", mp.putIfAbsent("123", "789"))
    assertEquals("def", mp.putIfAbsent("abc", "jkl"))
  }

  test("remove(k, v) should remove entry only if mapped to specified value") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val testWord        = "tres"
    val removeWhenValue = "si"
    val keepWhenValue   = "no"
    val data            = Array("uno", "dos", "tres", "cuatro", "cinqo", "seis")
    var expectedSize    = data.size

    data.foreach(key =>
      mp.put(key,
             if (key == testWord) removeWhenValue
             else keepWhenValue))

    assertFalse(mp.remove(testWord, keepWhenValue))
    assertEquals(removeWhenValue, mp.get(testWord))
    assertEquals(expectedSize, mp.size())

    assertTrue(mp.remove(testWord, removeWhenValue))
    assertNull(mp.get(testWord))
    expectedSize -= 1

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach { case (key: K, value: V) => assertEquals(keepWhenValue, value) }
  }

  test("replace(k, o, n) should act only when key mapped to old value") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue     = "no"
    val notInitialValue  = "tal vez" // "maybe"
    val replaceWithValue = "si"
    val missingKey       = "missing"
    val replaceKey       = "cuatro"
    val data             = Array("dos", "tres", "cuatro", "cinqo", "seis")
    var expectedSize     = data.size

    data.foreach(key => mp.put(key, initialValue))

    assertFalse(mp.replace(missingKey, initialValue, replaceWithValue))
    assertFalse(mp.replace(replaceKey, notInitialValue, replaceWithValue))

    assertTrue(mp.replace(replaceKey, initialValue, replaceWithValue))

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key, value) =>
        val expected =
          if (key == replaceKey) replaceWithValue
          else initialValue
        assertEquals(expected, value)
    }
  }

  test("replace(k, v) should act only if the key is present") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val replaceKey       = "dos"
    val replaceWithValue = "si"
    val initialValue     = "no"
    val missingKey       = "uno"
    val data             = Array(replaceKey, "tres", "cuatro", "cinqo", "seis")
    var expectedSize     = data.size

    data.foreach(key => mp.put(key, initialValue))

    assertNull(mp.replace(missingKey, replaceWithValue))
    assertEquals(initialValue, mp.replace(replaceKey, replaceWithValue))

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key, value) => {
        val expected =
          if (key == replaceKey) replaceWithValue
          else initialValue
        assertEquals(expected, value)
      }
    }
  }

  test("computeIfAbsent(k, r) should change as documented") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue     = "Covid"
    val replacementValue = "HPV"
    val changeKey        = "sigma"
    val noChangeKey      = "zeta"
    val skipKey          = "upsilon"
    val data             = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize     = data.size

    data.foreach(key => mp.put(key, initialValue))

    val computeFuncChangeKey = new function.Function[K, V]() {
      def apply(key: K): V =
        if (key == changeKey) replacementValue
        else null.asInstanceOf[V]
    }

    val computeFuncAlwaysReturnsNull = new function.Function[K, V]() {
      def apply(key: K): V = null.asInstanceOf[V]
    }

    assertEquals(replacementValue,
                 mp.computeIfAbsent(changeKey, computeFuncChangeKey))
    assertEquals(replacementValue, mp.get(changeKey))
    expectedSize += 1
    assertEquals(expectedSize, mp.size())

    assertEquals(initialValue,
                 mp.computeIfAbsent(noChangeKey, computeFuncChangeKey))
    assertEquals(initialValue, mp.get(noChangeKey))
    assertEquals(expectedSize, mp.size())

    assertNull(mp.computeIfAbsent(skipKey, computeFuncAlwaysReturnsNull))
    assertNull(mp.get(skipKey))

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key: K, value: V) => {
        val expected =
          if (key == changeKey) replacementValue
          else initialValue
        assertEquals(expected, value)
      }
    }
  }

  test("computeIfAbsent(k, r) exceptions in r should not change map.") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue = "Covid"
    val absentKey    = "sigma"
    val data         = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize = data.size

    data.foreach(key => mp.put(key, initialValue))

    type ExpectedException = IllegalArgumentException

    val computeFuncAlwaysThrows = new function.Function[K, V]() {
      def apply(key: K): V =
        throw new ExpectedException("Exceptions should not cause a change")
    }

    assertThrows[ExpectedException] {
      mp.computeIfAbsent(absentKey, computeFuncAlwaysThrows)
    }

    // Verify nothing changed.
    assertEquals(expectedSize, mp.size())
    mp.foreach { case (key: K, value: V) => assertEquals(initialValue, value) }
  }

  test("computeIfPresent(k, r) should change as documented") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue     = "Incumbent"
    val replacementValue = "Challenger" // test below assume non-null
    val changeKey        = "iota"
    val noChangeKey      = "xi"
    val removeKey        = "lambda"
    val data             = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize     = data.size

    data.foreach(key => mp.put(key, initialValue))

    val computeFuncChangeKey = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V =
        if (key == changeKey) replacementValue else value
    }

    val computeFuncAlwaysReturnsNull = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V = null.asInstanceOf[V]
    }

    assertNull(mp.computeIfPresent(noChangeKey, computeFuncChangeKey))
    assertNull(mp.get(noChangeKey))
    assertEquals(expectedSize, mp.size())

    assertEquals(replacementValue,
                 mp.computeIfPresent(changeKey, computeFuncChangeKey))
    assertEquals(replacementValue, mp.get(changeKey))
    assertEquals(expectedSize, mp.size())

    assertNull(mp.computeIfPresent(removeKey, computeFuncAlwaysReturnsNull))
    assertNull(mp.get(removeKey))
    expectedSize -= 1

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key: K, value: V) => {
        val expected =
          if (key == changeKey) replacementValue
          else initialValue
        assertEquals(expected, value)
      }
    }
  }

  test("computeIfPresent(k, r) exceptions in r should not change map.") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue = "Covid"
    val presentKey   = "zeta"
    val data         = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize = data.size

    type ExpectedException = IllegalArgumentException

    data.foreach(key => mp.put(key, initialValue))

    val computeBiFuncAlwaysThrows = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V =
        throw new ExpectedException("Exceptions should not cause a change")
    }

    assertThrows[ExpectedException] {
      mp.computeIfPresent(presentKey, computeBiFuncAlwaysThrows)
    }

    // Verify nothing changed.
    assertEquals(expectedSize, mp.size())
    mp.foreach { case (key: K, value: V) => assertEquals(initialValue, value) }
  }

  test("compute(k, r) key not found, should pass null value to BiFunction") {
    type K = String
    type V = String
    // Ints complicate null handling. null.asInstanceOf[Int] is 0,
    // meaning the two can not be distinguished.
    val mp = factory.empty[K, V]

    val missingKey       = "kappa"
    val changeKey        = "iota"
    val initialValue     = "Incumbent"
    val replacementValue = "Challenger" // test below assume non-null
    val nullValue        = "None of the above"
    val data             = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize     = data.size

    data.foreach(key => if (key != missingKey) mp.put(key, initialValue))

    val computeFuncExpectNullValue = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V = {
        if (value != null.asInstanceOf[V]) "FAIL!"
        else replacementValue
      }
    }

    // Eliminate  "null as missing key" before testing for
    // "null as missing replacement function".
    assertNull(mp.get(missingKey))

    assertThrows[NullPointerException] {
      mp.compute(missingKey, null.asInstanceOf[function.BiFunction[K, V, V]])
    }

    assertNull(mp.compute(missingKey, computeFuncExpectNullValue))

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key, value) => {
        val expected =
          if (key == missingKey) replacementValue
          else initialValue
        assertEquals(expected, value)
      }
    }
  }

  test("compute(k, r) key found, should pass (key, value) to BiFunction") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val changeKey        = "eta"
    val noChangeKey      = "zeta"
    val initialValue     = "Incumbent"
    val replacementValue = "Challenger"
    val data             = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize     = data.size

    data.foreach(key => mp.put(key, initialValue))

    val computeFuncChangeKey = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V = {
        if (key == changeKey) replacementValue
        else value
      }
    }

    mp.compute(changeKey, computeFuncChangeKey)
    assertEquals(replacementValue, mp.get(changeKey))

    mp.compute(noChangeKey, computeFuncChangeKey)
    assertEquals(initialValue, mp.get(noChangeKey))

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key, value) => {
        val expected =
          if (key == changeKey) replacementValue
          else initialValue
        assertEquals(expected, value)
      }
    }
  }

  test("compute(k, r) key found, BiFunction result null, should remove key") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue = "ab ovo"
    val removeKey    = "lambda"
    val data         = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize = data.size - 1

    data.foreach(key => mp.put(key, initialValue))

    val computeFuncAlwaysReturnsNull = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V = null.asInstanceOf[V]
    }

    assertEquals(initialValue,
                 mp.compute(removeKey, computeFuncAlwaysReturnsNull))

    assertNull(mp.get(removeKey)) // key is gone! and other keys are not

    // Intended change happened and nobody else was disturbed.
    assertEquals(expectedSize, mp.size)
    mp.foreach { case (key, value) => assertEquals(initialValue, value) }
  }

  test("compute(k, r) exceptions in r should not change map.") {
    type K = String
    type V = String
    val mp = factory.empty[K, V]

    val initialValue = "Covid"
    val presentKey   = "zeta"
    val data         = Array("zeta", "eta", "theta", "iota", "kappa", "lambda")
    var expectedSize = data.size

    type ExpectedException = IllegalArgumentException

    data.foreach(key => mp.put(key, initialValue))

    val computeBiFuncAlwaysThrows = new function.BiFunction[K, V, V]() {
      def apply(key: K, value: V): V =
        throw new ExpectedException("Exceptions should not cause a change")
    }

    assertThrows[ExpectedException] {
      mp.compute(presentKey, computeBiFuncAlwaysThrows)
    }

    // Verify nothing changed.
    assertEquals(expectedSize, mp.size())
    mp.foreach { case (key: K, value: V) => assertEquals(initialValue, value) }
  }

  test("merge(k, v, r) should replace existing elements as directed") {
    type K = String
    type V = Int
    val mp = factory.empty[K, V]

    // Central idea: count how many times a word has been seen.

    val unseenWord                  = "upsilon"
    val changeWord                  = "tau"
    val realWorldStartOfCount       = 1
    val computerScienceStartOfCount = 0
    val expectedUnseenWordCount     = 0
    val expectedUnchangedCount      = 1
    val expectedChangedCount        = 2
    val data                        = Array("tau", "upsilon", "phi", "chi", "psi", "omega")
    var expectedSize                = data.size

    data.foreach(key =>
      if (key != unseenWord)
        mp.put(key, realWorldStartOfCount))

    val mergeFunc = new function.BiFunction[V, V, V]() {
      def apply(currentValue: V, mergee: V): V = {
        currentValue + mergee
      }
    }

    assertNull(mp.get(unseenWord))

    mp.merge(unseenWord, computerScienceStartOfCount, mergeFunc)
    mp.merge(changeWord, 1, mergeFunc)

    // New word has not been remapped and selected previously seen word has.
    // Values for unselected words have have not changed.
    assertEquals(expectedSize, mp.size())
    mp.foreach {
      case (key, value) =>
        val expected =
          if (key == unseenWord)
            expectedUnseenWordCount
          else if (key == changeWord)
            expectedChangedCount
          else
            expectedUnchangedCount
        assertEquals(expected, value)
    }
  }
}

object MapFactory {
  def allFactories: Iterator[MapFactory] =
    HashMapSuiteFactory.allFactories
}

trait MapFactory {
  def implementationName: String

  def empty[K: ClassTag, V: ClassTag]: ju.Map[K, V]

  def allowsNullKeys: Boolean

  def allowsNullValues: Boolean

  def allowsNullKeysQueries: Boolean = true

  def allowsNullValuesQueries: Boolean = true

  def allowsIdentityBasedKeys: Boolean = false
}
