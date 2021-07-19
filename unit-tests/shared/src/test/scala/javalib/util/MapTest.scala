package javalib.util

import java.util._

// Ported from Scala.js

import java.{util => ju}
import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import scala.scalanative.junit.utils.AssertThrows.assertThrows
import scala.collection.{immutable => im}
import scala.collection.{mutable => mu}

import scala.reflect.ClassTag
import scala.scalanative.junit.utils.CollectionConverters._

trait MapTest {
  import MapTest._

  def factory: MapFactory

  def testObj(i: Int): TestObj = TestObj(i)

  // temporary until Platform is introduced
  val executingInJVM = false

  // replace when new IdentityHashMap from Scala.js
  // private def assumeNotIdentityHashMapOnJVM(): Unit =
  //   assumeFalse("JVM vs Native cache differences",
  //               executingInJVM && factory.isIdentityBased)

  // temp implementation
  private def assumeNotIdentityHashMapOnJVM(): Unit =
    assumeFalse("JVM vs Native cache differences", factory.isIdentityBased)

  @Test def shouldStoreStrings(): Unit = {
    val mp = factory.empty[String, String]

    assertEquals(0, mp.size())
    mp.put("ONE", "one")
    assertEquals(1, mp.size())
    assertEquals("one", mp.get("ONE"))
    mp.put("TWO", "two")
    assertEquals(2, mp.size())
    assertEquals("two", mp.get("TWO"))
  }

  @Test def shouldStoreIntegers(): Unit = {
    assumeNotIdentityHashMapOnJVM()
    val mp = factory.empty[Int, Int]

    mp.put(100, 12345)
    assertEquals(1, mp.size())
    val one = mp.get(100)
    assertEquals(12345, one)
  }

  @Test def shouldStoreDoublesAlsoInCornerCases() {
    assumeNotIdentityHashMapOnJVM()

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

  @Test def shouldStoreCustomObjects(): Unit = {
    case class TestObj(num: Int)
    val mp = factory.empty[TestObj, TestObj]

    val testKey = TestObj(100)
    mp.put(testKey, TestObj(12345))
    assertEquals(1, mp.size())
    if (factory.isIdentityBased) {
      val one = mp.get(testKey)
      assertEquals(12345, one.num)
    } else {
      val one = mp.get(TestObj(100))
      assertEquals(12345, one.num)
    }
  }

  @Test def shouldRemoveStoredElements(): Unit = {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    assertEquals(1, mp.size())
    assertEquals("one", mp.remove("ONE"))
    val newOne = mp.get("ONE")
    assertNull(mp.get("ONE"))
  }

  @Test def shouldRemoveStoredElementsInDoubleCornerCases() {
    assumeNotIdentityHashMapOnJVM()

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

  @Test def shouldPutOrFailOnNullKeys(): Unit = {
    if (factory.allowsNullKeys) {
      val mp = factory.empty[String, String]
      mp.put(null, "one")
      assertEquals("one", mp.get(null))
    } else {
      val mp = factory.empty[String, String]
      assertThrows(classOf[NullPointerException], mp.put(null, "one"))
    }
  }

  @Test def shouldPutOrFailOnNullValues(): Unit = {
    if (factory.allowsNullValues) {
      val mp = factory.empty[String, String]
      mp.put("one", null)
      assertNull(mp.get("one"))
    } else {
      val mp = factory.empty[String, String]
      assertThrows(classOf[NullPointerException], mp.put("one", null))
    }
  }

  @Test def shouldBeClearedWithOneOperation(): Unit = {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    mp.put("TWO", "two")
    assertEquals(2, mp.size())
    mp.clear()
    assertEquals(0, mp.size())
  }

  @Test def shouldCheckContainedKeyPresence(): Unit = {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    assertTrue(mp.containsKey("ONE"))
    assertFalse(mp.containsKey("TWO"))
    if (factory.allowsNullKeysQueries)
      assertFalse(mp.containsKey(null))
    else
      assertThrows(classOf[Throwable], mp.containsKey(null))
  }

  @Test def shouldCheckContainedValuePresence(): Unit = {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    assertTrue(mp.containsValue("one"))
    assertFalse(mp.containsValue("two"))
    if (factory.allowsNullValuesQueries)
      assertFalse(mp.containsValue(null))
    else
      assertThrows(classOf[Throwable], mp.containsValue(null))
  }

  @Test def shouldGiveProperCollectionOverValues(): Unit = {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    val values = mp.values()
    assertEquals(1, values.size())
    val iter = values.iterator
    assertTrue(iter.hasNext)
    assertEquals("one", iter.next)
    assertFalse(iter.hasNext)
  }

  @Test def shouldGiveProperEntrySetOverKeyValuePairs(): Unit = {
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

  @Test def shouldGiveProperKeySetOverKeys(): Unit = {
    val mp = factory.empty[String, String]

    mp.put("ONE", "one")
    val keySet = mp.keySet()

    assertEquals(1, keySet.size())

    val iter = keySet.iterator
    assertTrue(iter.hasNext)
    assertEquals("ONE", iter.next)
    assertFalse(iter.hasNext)
  }

  @Test def shouldPutAWholeMapInto(): Unit = {
    val mp = factory.empty[String, String]

    val m = mu.Map[String, String]("X" -> "y")
    mp.putAll(m.toJavaMap[String, String])
    assertEquals(1, mp.size())
    assertEquals("y", mp.get("X"))

    val nullMap = mu.Map[String, String]((null: String) -> "y", "X" -> "y")

    if (factory.allowsNullKeys) {
      mp.putAll(nullMap.toJavaMap[String, String])
      assertEquals("y", mp.get(null))
      assertEquals("y", mp.get("X"))
    } else {
      assertThrows(
        classOf[NullPointerException],
        mp.putAll(nullMap.toJavaMap[String, String])
      )
    }
  }

  class SimpleQueryableMap[K, V](inner: mu.HashMap[K, V])
      extends ju.AbstractMap[K, V] {
    def entrySet(): java.util.Set[java.util.Map.Entry[K, V]] = {
      inner
        .map {
          case (k, v) =>
            val entry = new ju.AbstractMap.SimpleImmutableEntry(k, v)
            entry: java.util.Map.Entry[K, V]
        }
        .toSet
        .toJavaSet
    }
  }

  @Test def valuesShouldMirrorTheRelatedMapSize(): Unit = {
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

    val hm1 = mu.HashMap("ONE" -> "one", "TWO" -> "two")
    val hm2 = mu.HashMap("ONE" -> null, "TWO" -> "two")
    val hm3 = mu.HashMap((null: String) -> "one", "TWO" -> "two")
    val hm4 = mu.HashMap((null: String) -> null, "TWO" -> "two")

    assertEquals(2, new SimpleQueryableMap(hm1).values().size())
    assertEquals(2, new SimpleQueryableMap(hm2).values().size())
    assertEquals(2, new SimpleQueryableMap(hm3).values().size())
    assertEquals(2, new SimpleQueryableMap(hm4).values().size())
  }

  @Test def valuesShouldCheckSingleAndMultipleObjectsPresence(): Unit = {
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
      assertThrows(classOf[Throwable], mp.values().contains(null))

    mp.put("THREE", "three")

    assertTrue(values.contains("three"))

    val coll1 = im.Set("one", "two", "three").toJavaSet
    assertTrue(values.containsAll(coll1))

    val coll2 = im.Set("one", "two", "three", "four").toJavaSet
    assertFalse(values.containsAll(coll2))

    val coll3 = im.Set("one", "two", "three", null).toJavaSet
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

    val hm1 = mu.HashMap(1.0 -> null, 2.0 -> 2.0)
    val hm2 = mu.HashMap((null: Any) -> 1.0, 2.0 -> 2.0)
    val hm3 = mu.HashMap((null: Any) -> null, 2.0 -> 2.0)

    assertFalse(new SimpleQueryableMap(hm1).values().contains(1.0))
    assertTrue(new SimpleQueryableMap(hm2).values().contains(1.0))
    assertFalse(new SimpleQueryableMap(hm3).values().contains(1.0))

    assertTrue(new SimpleQueryableMap(hm1).values().contains(null))
    assertFalse(new SimpleQueryableMap(hm2).values().contains(null))
    assertTrue(new SimpleQueryableMap(hm3).values().contains(null))
  }

  @Test def valuesShouldSideEffectClearRemoveRetainOnTheRelatedMap(): Unit = {
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

    values.removeAll(im.List("one", "two").toJavaList)

    assertFalse(mp.containsKey("ONE"))
    assertFalse(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    mp.put("ONE", "one")
    mp.put("TWO", "two")
    mp.put("THREE", "three")

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    values.retainAll(im.List("one", "two").toJavaList)

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertFalse(mp.containsKey("THREE"))
  }

  @Test def keySetShouldMirrorTheRelatedMapSize(): Unit = {
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

    val hm1 = mu.HashMap("ONE" -> "one", "TWO" -> "two")
    val hm2 = mu.HashMap("ONE" -> null, "TWO" -> "two")
    val hm3 = mu.HashMap((null: String) -> "one", "TWO" -> "two")
    val hm4 = mu.HashMap((null: String) -> null, "TWO" -> "two")

    assertEquals(2, new SimpleQueryableMap(hm1).keySet().size())
    assertEquals(2, new SimpleQueryableMap(hm2).keySet().size())
    assertEquals(2, new SimpleQueryableMap(hm3).keySet().size())
    assertEquals(2, new SimpleQueryableMap(hm4).keySet().size())
  }

  @Test def keySetShouldCheckSingleAndMultipleObjectsPresence(): Unit = {
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
      assertThrows(classOf[Throwable], mp.keySet().contains(null))

    mp.put("THREE", "three")

    assertTrue(keySet.contains("THREE"))

    val coll1 = im.Set("ONE", "TWO", "THREE").toJavaSet
    assertTrue(keySet.containsAll(coll1))

    val coll2 = im.Set("ONE", "TWO", "THREE", "FOUR").toJavaSet
    assertFalse(keySet.containsAll(coll2))

    val coll3 = im.Set("ONE", "TWO", "THREE", null).toJavaSet
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

    val hm1 = mu.HashMap(1.0 -> null, 2.0 -> 2.0)
    val hm2 = mu.HashMap((null: Any) -> 1.0, 2.0 -> 2.0)
    val hm3 = mu.HashMap((null: Any) -> null, 2.0 -> 2.0)

    assertTrue(new SimpleQueryableMap(hm1).keySet().contains(1.0))
    assertFalse(new SimpleQueryableMap(hm2).keySet().contains(1.0))
    assertFalse(new SimpleQueryableMap(hm3).keySet().contains(1.0))

    assertFalse(new SimpleQueryableMap(hm1).keySet().contains(null))
    assertTrue(new SimpleQueryableMap(hm2).keySet().contains(null))
    assertTrue(new SimpleQueryableMap(hm3).keySet().contains(null))
  }

  @Test def keySetShouldSideEffectClearRemoveRetainOnTheRelatedMap(): Unit = {
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

    keySet.removeAll(im.List("ONE", "TWO").toJavaList)

    assertFalse(mp.containsKey("ONE"))
    assertFalse(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    mp.put("ONE", "one")
    mp.put("TWO", "two")
    mp.put("THREE", "three")

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertTrue(mp.containsKey("THREE"))

    keySet.retainAll(im.List("ONE", "TWO").toJavaList)

    assertTrue(mp.containsKey("ONE"))
    assertTrue(mp.containsKey("TWO"))
    assertFalse(mp.containsKey("THREE"))
  }
}

object MapTest {
  final case class TestObj(num: Int)
}

trait MapFactory {
  def implementationName: String

  def empty[K: ClassTag, V: ClassTag]: ju.Map[K, V]

  def allowsNullKeys: Boolean

  def allowsNullValues: Boolean

  def allowsNullKeysQueries: Boolean = true

  def allowsNullValuesQueries: Boolean = true

  def withSizeLimit: Option[Int] = None

  def isIdentityBased: Boolean = false
}
