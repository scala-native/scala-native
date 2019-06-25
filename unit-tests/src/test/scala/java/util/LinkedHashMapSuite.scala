package java.util

// Ported from Scala.js

import java.{util => ju, lang => jl}
import scala.collection.JavaConversions._
import scala.collection.{immutable => im}
import scala.reflect.ClassTag

object LinkedHashMapInsertionOrderSuite extends LinkedHashMapSuite

object LinkedHashMapInsertionOrderLimitedSuite extends LinkedHashMapSuite {
  override def factory: LinkedHashMapSuiteFactory =
    new LinkedHashMapSuiteFactory(accessOrder = false, withSizeLimit = Some(50))
}

object LinkedHashMapAccessOrderSuite extends LinkedHashMapSuite {
  override def factory: LinkedHashMapSuiteFactory =
    new LinkedHashMapSuiteFactory(accessOrder = true, withSizeLimit = None)
}

object LinkedHashMapAccessOrderLimitedSuite extends LinkedHashMapSuite {
  override def factory: LinkedHashMapSuiteFactory =
    new LinkedHashMapSuiteFactory(accessOrder = true, withSizeLimit = Some(50))
}

abstract class LinkedHashMapSuite extends MapSuite {
  override def factory: LinkedHashMapSuiteFactory =
    new LinkedHashMapSuiteFactory(accessOrder = false, withSizeLimit = None)

  val accessOrder   = factory.accessOrder
  val withSizeLimit = factory.withSizeLimit

  test("should iterate in insertion order after building") {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    def expectedKey(index: Int): Int =
      withSizeLimit.getOrElse(0) + index

    def expectedValue(index: Int): String =
      s"elem ${expectedKey(index)}"

    val expectedSize = withSizeLimit.getOrElse(100)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((entry, index) <- lhm.entrySet().zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedValue(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().zipWithIndex)
      assertEquals(expectedValue(index), value)
  }

  test("should iterate in the same order after removal of elements") {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    (0 until 100 by 3).foreach(key => lhm.remove(key))

    val expectedKey =
      ((100 - withSizeLimit.getOrElse(100)) to 100).filter(_ % 3 != 0).toArray

    def expectedValue(index: Int): String =
      s"elem ${expectedKey(index)}"

    val expectedSize = if (withSizeLimit.isDefined) 33 else 66

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((entry, index) <- lhm.entrySet().zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedValue(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().zipWithIndex)
      assertEquals(expectedValue(index), value)
  }

  test("should iterate in order after adding elements") {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    lhm(0) = "new 0"
    lhm(100) = "elem 100"
    lhm(42) = "new 42"
    lhm(52) = "new 52"
    lhm(1) = "new 1"
    lhm(98) = "new 98"

    val expectedKey = {
      if (factory.accessOrder) {
        val keys = (2 until 42) ++ (43 until 52) ++ (53 until 98) ++
          im.List(99, 0, 100, 42, 52, 1, 98)
        keys.takeRight(withSizeLimit.getOrElse(keys.length))
      } else {
        if (withSizeLimit.isDefined)
          (55 until 100) ++ im.List(0, 100, 42, 52, 1)
        else 0 to 100
      }
    }.toArray

    def expectedElem(index: Int): String = {
      val key = expectedKey(index)
      if (key == 0 || key == 1 || key == 42 || key == 52 || key == 98)
        s"new $key"
      else
        s"elem $key"
    }

    val expectedSize = withSizeLimit.getOrElse(101)

    assertEquals(expectedSize, lhm.entrySet().size())

    for ((entry, index) <- lhm.entrySet().zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedElem(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().zipWithIndex)
      assertEquals(expectedElem(index), value)
  }

  test("should iterate in after accessing elements") {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    lhm.get(42)
    lhm.get(52)
    lhm.get(5)

    def expectedKey(index: Int): Int = {
      if (accessOrder) {
        // elements ordered by insertion order except for those accessed
        if (withSizeLimit.isEmpty) {
          if (index < 5) index // no elements removed in this range
          else if (index + 1 < 42) index + 1 // shifted by 1 removed element
          else if (index + 2 < 52) index + 2 // shifted by 2 removed element
          else if (index < 97) index + 3 // shifted by 3 removed element
          // elements reordered by accesses
          else if (index == 97) 42
          else if (index == 98) 52
          else 5
        } else {
          // note that 5 and 42 are not accessed because they where dropped
          // due to the size limit
          if (index < 2) index + 50 // no elements removed in this range
          else if (index < 49) index + 51 // shifted by 1 removed element
          // element reordered by accesses
          else 52
        }
      } else {
        // accesses shouldn't modify the order
        withSizeLimit.getOrElse(0) + index
      }
    }

    def expectedValue(index: Int): String =
      s"elem ${expectedKey(index)}"

    val expectedSize = withSizeLimit.getOrElse(100)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((entry, index) <- lhm.entrySet().zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedValue(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().zipWithIndex)
      assertEquals(expectedValue(index), value)
  }
}

object LinkedHashMapSuiteFactory {
  def allFactories: scala.Iterator[MapFactory] = {
    scala.Iterator(
      new LinkedHashMapSuiteFactory(true, Some(50)),
      new LinkedHashMapSuiteFactory(true, None),
      new LinkedHashMapSuiteFactory(false, Some(50)),
      new LinkedHashMapSuiteFactory(false, None)
    )
  }
}

class LinkedHashMapSuiteFactory(val accessOrder: Boolean,
                                val withSizeLimit: Option[Int])
    extends HashMapSuiteFactory {
  def orderName: String =
    if (accessOrder) "access-order"
    else "insertion-order"

  override def implementationName: String = {
    val sizeLimitSting = withSizeLimit.fold("")(", maxSize=" + _)
    s"java.util.LinkedHashMap{$orderName$sizeLimitSting}"
  }

  override def empty[K: ClassTag, V: ClassTag]: ju.LinkedHashMap[K, V] = {
    withSizeLimit match {
      case Some(limit) =>
        new ju.LinkedHashMap[K, V](16, 0.75f, accessOrder) {
          override protected def removeEldestEntry(
              eldest: ju.Map.Entry[K, V]): Boolean =
            size() > limit
        }

      case None =>
        new ju.LinkedHashMap[K, V](16, 0.75f, accessOrder)
    }
  }
}
