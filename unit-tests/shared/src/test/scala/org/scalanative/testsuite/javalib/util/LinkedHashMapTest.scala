package org.scalanative.testsuite.javalib.util

import java.util.*

// Ported from Scala.js

import java.{lang as jl, util as ju}
import org.junit.Test
import org.junit.Assert.*
import scala.collection.immutable as im
import scala.reflect.ClassTag
import scala.scalanative.junit.utils.CollectionConverters.*

class LinkedHashMapInsertionOrderTest extends LinkedHashMapTest

class LinkedHashMapInsertionOrderLimitedTest extends LinkedHashMapTest {
  override def factory: LinkedHashMapFactory =
    new LinkedHashMapFactory(accessOrder = false, withSizeLimit = Some(50))
}

class LinkedHashMapAccessOrderTest extends LinkedHashMapTest {
  override def factory: LinkedHashMapFactory =
    new LinkedHashMapFactory(accessOrder = true, withSizeLimit = None)
}

class LinkedHashMapAccessOrderLimitedTest extends LinkedHashMapTest {
  override def factory: LinkedHashMapFactory =
    new LinkedHashMapFactory(accessOrder = true, withSizeLimit = Some(50))
}

abstract class LinkedHashMapTest extends HashMapTest {

  override def factory: LinkedHashMapFactory =
    new LinkedHashMapFactory(accessOrder = false, withSizeLimit = None)

  val accessOrder = factory.accessOrder
  val withSizeLimit = factory.withSizeLimit

  @Test def shouldIterateInInsertionOrderAfterBuilding(): Unit = {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    def expectedKey(index: Int): Int =
      withSizeLimit.getOrElse(0) + index

    def expectedValue(index: Int): String =
      s"elem ${expectedKey(index)}"

    val expectedSize = withSizeLimit.getOrElse(100)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((entry, index) <- lhm.entrySet().toScalaSeq.zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedValue(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().toScalaSeq.zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().toScalaSeq.zipWithIndex)
      assertEquals(expectedValue(index), value)
  }

  @Test def shouldIterateInTheSameOrderAfterRemovalOfElements(): Unit = {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    (0 until 100 by 3).foreach(key => lhm.remove(key))

    val expectedKey =
      ((100 - withSizeLimit.getOrElse(100)) to 100).filter(_ % 3 != 0).toArray

    def expectedValue(index: Int): String =
      s"elem ${expectedKey(index)}"

    val expectedSize = if (withSizeLimit.isDefined) 33 else 66

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((entry, index) <- lhm.entrySet().toScalaSeq.zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedValue(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().toScalaSeq.zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().toScalaSeq.zipWithIndex)
      assertEquals(expectedValue(index), value)
  }

  @Test def shouldIterateInOrderAfterAddingElements(): Unit = {
    val lhm = factory.empty[jl.Integer, String]
    (0 until 100).foreach(key => lhm.put(key, s"elem $key"))

    lhm.put(0, "new 0")
    lhm.put(100, "elem 100")
    lhm.put(42, "new 42")
    lhm.put(52, "new 52")
    lhm.put(1, "new 1")
    lhm.put(98, "new 98")

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

    for ((entry, index) <- lhm.entrySet().toScalaSeq.zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedElem(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().toScalaSeq.zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().toScalaSeq.zipWithIndex)
      assertEquals(expectedElem(index), value)
  }

  @Test def shouldIterateInAfterAccessingElements(): Unit = {
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
    for ((entry, index) <- lhm.entrySet().toScalaSeq.zipWithIndex) {
      assertEquals(expectedKey(index), entry.getKey)
      assertEquals(expectedValue(index), entry.getValue)
    }

    assertEquals(expectedSize, lhm.keySet().size())
    for ((key, index) <- lhm.keySet().toScalaSeq.zipWithIndex)
      assertEquals(expectedKey(index), key)

    assertEquals(expectedSize, lhm.entrySet().size())
    for ((value, index) <- lhm.values().toScalaSeq.zipWithIndex)
      assertEquals(expectedValue(index), value)
  }
}

class LinkedHashMapFactory(
    val accessOrder: Boolean,
    override val withSizeLimit: Option[Int]
) extends HashMapFactory {
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
              eldest: ju.Map.Entry[K, V]
          ): Boolean =
            size() > limit
        }

      case None =>
        new ju.LinkedHashMap[K, V](16, 0.75f, accessOrder)
    }
  }
}
