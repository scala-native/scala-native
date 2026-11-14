package org.scalanative.testsuite.javalib.util

import java.util.LinkedHashMap
import java.{lang => jl, util => ju}

import org.junit.Assert._
import org.junit.Test

/* This file exercises local overrides of SequencedMap default methods.
 *   - It exercises both insertion and access orders.
 *
 *   - removeEldestEntry() is exercised in each order with each of
 *     default always 'false' and a supplied method which can return 'true'.
 */

trait JEP431_LinkedHashMapTestTraitOnJDK21
    extends JEP431_SequencedMapTestTraitOnJDK21 {

  val loadFactor = 0.75f

  val accessOrder: Boolean

  def sequencedMapFactory[K, V](): ju.SequencedMap[K, V] =
    new ju.LinkedHashMap[K, V](populationSize, loadFactor, accessOrder)

  def sequencedMapRemoveEldestFactory[K, V](): ju.SequencedMap[K, V] = {
    // Detect any invisible excess node addition in SN putLast() implementation

    new ju.LinkedHashMap[K, V](populationSize, loadFactor, accessOrder) {
      var nTimesCalled = 0

      override def removeEldestEntry(eldest: ju.Map.Entry[K, V]): Boolean = {
        nTimesCalled += 1
        nTimesCalled > populationSize
      }
    }
  }

  val expectedLastValue = populationSize
  val expectedLastKey = s"${keyPrefix}${expectedLastValue}"

  @Test def linkedHashMap_putFirst_emptyMap(): Unit = {
    locally {
      val seqMap = sequencedMapFactory[String, Int]()

      val replacementValue = 1
      val testKey = s"${keyPrefix}${replacementValue}"

      assertEquals(
        s"${classUnderTest}#putFirst, empty map, should return null",
        null,
        seqMap.putFirst(testKey, replacementValue)
      )

      assertEquals(
        s"${classUnderTest}#putFirst, empty map, size()",
        1,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putFirst, key empty map,",
        testKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putFirst, value in empty map",
        replacementValue,
        fe.getValue()
      )
    }
  }

  @Test def linkedHashMap_putFirst_singletonMap(): Unit = {
    locally {
      val seqMap = sequencedMapFactory[String, Int]()

      val singletonValue = 1
      val singletonKey = s"${keyPrefix}${singletonValue}"
      seqMap.putFirst(singletonKey, singletonValue)

      val expectedValue = 2
      val testKey = s"${keyPrefix}${expectedValue}"

      assertEquals(
        s"${classUnderTest}#putFirst, singleton map, should return null",
        null,
        seqMap.putFirst(testKey, expectedValue)
      )

      assertEquals(
        s"${classUnderTest}#putFirst, singleton map, size()",
        2,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putFirst first key in singleton map",
        testKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putFirst first value in singleton map",
        expectedValue,
        fe.getValue()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putFirst new last key in singleton map",
        singletonKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putFirst new last value in singleton map",
        singletonValue,
        le.getValue()
      )
    }
  }

  @Test def linkedHashMap_putFirst(): Unit = {
    locally {
      val seqMap = sequencedMapFactory[String, Int]()
      populateStringIntMap(seqMap)

      assertEquals(
        s"${classUnderTest}#putFirst, key not-in-map, should return null",
        null,
        seqMap.putFirst(s"${keyPrefix}_${jl.Integer.MAX_VALUE}", -1)
      )
    }

    locally {
      val seqMap = sequencedMapFactory[String, Int]()
      populateStringIntMap(seqMap)

      val expectedExistingValue = 4
      val expectedReplacedValue = 33
      val testKey = s"${keyPrefix}${expectedExistingValue}"

      assertEquals(
        s"${classUnderTest}#putFirst, key in map, should return old value",
        expectedExistingValue,
        seqMap.putFirst(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putFirst, key in map, size()",
        populationSize,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putFirst, key in map, should reorder",
        testKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putFirst, key:value in map should reorder",
        expectedReplacedValue,
        fe.getValue()
      )
    }
  }

  @Test def linkedHashMap_putLast_emptyMap(): Unit = {
    locally {
      val seqMap = sequencedMapFactory[String, Int]()

      val expectedValue = 1
      val testKey = s"${keyPrefix}${expectedValue}"

      assertEquals(
        s"${classUnderTest}#putLast, empty map, should return null",
        null,
        seqMap.putLast(testKey, expectedValue)
      )

      assertEquals(
        s"${classUnderTest}#putLast, empty map, size()",
        1,
        seqMap.size()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putLast, empty map,",
        testKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast, key:value empty map",
        expectedValue,
        le.getValue()
      )
    }
  }

  @Test def linkedHashMap_putLast_singletonMap(): Unit = {
    locally {
      val seqMap = sequencedMapFactory[String, Int]()

      val singletonValue = 1
      val singletonKey = s"${keyPrefix}${singletonValue}"
      seqMap.putLast(singletonKey, singletonValue)

      val expectedValue = 2
      val testKey = s"${keyPrefix}${expectedValue}"

      assertEquals(
        s"${classUnderTest}#putLast, key in singleton map, should return null",
        null,
        seqMap.putLast(testKey, expectedValue)
      )

      assertEquals(
        s"${classUnderTest}#putLast, key in singleton map, size()",
        2,
        seqMap.size()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putLast, key in singleton map",
        testKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast, key:value in singleton map",
        expectedValue,
        le.getValue()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putLast new first key in singleton map",
        singletonKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast new first value in singleton map",
        singletonValue,
        fe.getValue()
      )
    }
  }

  @Test def linkedHashMap_putLast(): Unit = {
    locally {
      val seqMap = sequencedMapFactory[String, Int]()
      populateStringIntMap(seqMap)

      val expectedReplacedValue = jl.Integer.MAX_VALUE
      val testKey = s"${keyPrefix}${expectedReplacedValue}"

      assertEquals(
        s"${classUnderTest}#putLast, key not-in-map, should return null",
        null,
        seqMap.putLast(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putLast, key not-in-map, size()",
        populationSize + 1,
        seqMap.size()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putLast, key not-in-map, should be last key",
        testKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast, key:value in map, should be last key",
        expectedReplacedValue,
        le.getValue()
      )
    }

    locally {
      val seqMap = sequencedMapFactory[String, Int]()
      populateStringIntMap(seqMap)

      val expectedExistingValue = 2
      val expectedReplacedValue = 66
      val testKey = s"${keyPrefix}${expectedExistingValue}"

      assertEquals(
        s"${classUnderTest}#putLast, key in map, should return old value",
        expectedExistingValue,
        seqMap.putLast(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putLast, key in map, size()",
        populationSize,
        seqMap.size()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putLast, key in map, should reorder",
        testKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast, key:value in map should reorder",
        expectedReplacedValue,
        le.getValue()
      )
    }
  }

  @Test def linkedHashMap_putFirst_RemoveEldestEntry(): Unit = {
    locally { // key not in map
      /* Attention:
       *   Java adds the entry, making it the eldest. With the
       *   removeEldestEntry() rule in use this triggers its removal
       *   resulting in an overall no-op.
       */

      val seqMap = sequencedMapRemoveEldestFactory[String, Int]()
      populateStringIntMap(seqMap)

      val populatedFe = seqMap.firstEntry()

      val expectedReplacedValue = jl.Integer.MAX_VALUE
      val testKey = s"${keyPrefix}${expectedReplacedValue}"

      assertEquals(
        s"${classUnderTest}#putFirst, key not-in-map, should return null",
        null,
        seqMap.putFirst(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putFirst, key not-in-map, map size()",
        populationSize,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putFirst, key not-in-map, first key",
        populatedFe.getKey(),
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putFirst, key not-in-map, first value",
        populatedFe.getValue(),
        fe.getValue()
      )
    }

    locally { // key in map node re-ordered, no addition so eldest preserved.
      val seqMap = sequencedMapRemoveEldestFactory[String, Int]()
      populateStringIntMap(seqMap)

      val expectedExistingValue = 2
      val expectedReplacedValue = 67
      val testKey = s"${keyPrefix}${expectedExistingValue}"

      assertEquals(
        s"${classUnderTest}#putFirst, key in map, should return old value",
        expectedExistingValue,
        seqMap.putFirst(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putFirst, key in map, size()",
        populationSize,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putFirst, key in map, should reorder",
        testKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putFirst, key:value in map should reorder",
        expectedReplacedValue,
        fe.getValue()
      )
    }
  }

  @Test def linkedHashMap_putLast_RemoveEldestEntry(): Unit = {
    locally { // key not in map, node added and eldest deleted
      val seqMap = sequencedMapRemoveEldestFactory[String, Int]()
      populateStringIntMap(seqMap)

      val expectedExistingValue = 20
      val expectedReplacedValue = 120
      val testKey = s"${keyPrefix}${expectedExistingValue}"

      assertEquals(
        s"${classUnderTest}#putLast, key not-in-map, should return null",
        null,
        seqMap.putLast(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putLast, key not-in-map, size()",
        populationSize,
        seqMap.size()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putLast, key in map, should reorder",
        testKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast, key:value in map should reorder",
        expectedReplacedValue,
        le.getValue()
      )

      // Was first entry established by populateStringIntMap(seqMap) removed?
      val expectedNewFirstValue = 2
      val newFirstKey = s"${keyPrefix}${expectedNewFirstValue}"

      assertEquals(
        s"${classUnderTest}#putLast remove first should maintain max size",
        populationSize,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putLast should establish new eldest key",
        newFirstKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast should establish new eldest value",
        expectedNewFirstValue,
        fe.getValue()
      )
    }

    locally { // key in map node re-ordered, no addition so eldest preserved.
      val seqMap = sequencedMapRemoveEldestFactory[String, Int]()
      populateStringIntMap(seqMap)

      val expectedExistingValue = 2
      val expectedReplacedValue = 66
      val testKey = s"${keyPrefix}${expectedExistingValue}"

      assertEquals(
        s"${classUnderTest}#putLast, key in map, should return old value",
        expectedExistingValue,
        seqMap.putLast(testKey, expectedReplacedValue)
      )

      assertEquals(
        s"${classUnderTest}#putLast, key in map, size()",
        populationSize,
        seqMap.size()
      )

      val le = seqMap.lastEntry()

      assertEquals(
        s"${classUnderTest}#putLast, key in map, should reorder",
        testKey,
        le.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast, key:value in map, should reorder",
        expectedReplacedValue,
        le.getValue()
      )

      /* first entry established by populateStringIntMap(seqMap) should be
       * reordered and preserved, not deleted then re-added.
       */
      val expectedNewFirstValue = 1
      val newFirstKey = s"${keyPrefix}${expectedNewFirstValue}"

      assertEquals(
        s"${classUnderTest}#putLast removeFirstEntry should maintain max size",
        populationSize,
        seqMap.size()
      )

      val fe = seqMap.firstEntry()

      assertEquals(
        s"${classUnderTest}#putLast should establish new eldest key",
        newFirstKey,
        fe.getKey()
      )

      assertEquals(
        s"${classUnderTest}#putLast should establish new eldest value",
        expectedNewFirstValue,
        fe.getValue()
      )
    }
  }
}

class JEP431_LinkedHashMapTestOnJDK21
    extends JEP431_LinkedHashMapTestTraitOnJDK21 {

  val classUnderTest = "LinkedHashMapInsertionOrder"

  val accessOrder = false
}

class JEP431_LinkedHashMapAccessOrderTestOnJDK21
    extends JEP431_LinkedHashMapTestTraitOnJDK21 {

  val classUnderTest = "LinkedHashMapAccessOrder"

  val accessOrder = true
}
