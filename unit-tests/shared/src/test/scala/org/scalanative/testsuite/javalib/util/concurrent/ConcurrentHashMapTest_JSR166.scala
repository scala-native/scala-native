/* Ported from Oswego revision: 1.66 dated: 2021-01-27 02:55:18
 * URL:
 *   http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166/src/tests/tck/
 * 
 * Modified for Scala Native.
 */

/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.BeforeClass

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import scala.scalanative.buildinfo.ScalaNativeBuildInfo

import java.lang as jl
import java.util as ju
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.Enumeration
import java.util.Iterator
import java.util.Map
import java.util.Random
import java.util.Set

import java.util.concurrent as juc
import java.util.concurrent.ConcurrentHashMap

import java.util.stream.Collectors

import scala.annotation.tailrec

object ConcurrentHashMapTest_JSR166 {
  import JSR166Test.*

  @BeforeClass def disableJvmJava8(): Unit = {
    /* Some version of JDK greater than 8 and less than 17 may
     * work but have not been tested.
     */
    assumeTrue(
      "Fails (deadlocks) running on JVM using Java 8",
      Platform.executingInJVMWithJDKIn(17 to 99) ||
        Platform.executingInScalaNative
    )
  }

  /** Returns a new map from Items 1-5 to Strings "A"-"E".
   */
  private def map5(): ConcurrentHashMap[Integer, String] = {
    val map = new ConcurrentHashMap[Integer, String]()
    assertTrue(map.isEmpty())

    map.put(one, "A")
    map.put(two, "B")
    map.put(three, "C")
    map.put(four, "D")
    map.put(five, "E")

    assertFalse(map.isEmpty())
    assertEquals("size", 5, map.size())

    map
  }

  // classes for testing Comparable fallbacks
  class BI(protected val value: Int) extends Comparable[BI] {
    def compareTo(other: BI): Int =
      Integer.compare(value, other.value)

    override def equals(x: Any): Boolean = x match {
      case other: BI => this.value == other.value
      case _         => false
    }

    override def hashCode(): Int = 42
  }

  class CI(value: Int) extends BI(value) {}

  class DI(value: Int) extends BI(value) {}

  class BS(val value: String) extends Comparable[BS] {

    /* JVM is happy with this definition and several variants of it.
     * SN throws (lightly edited for length)
     *   java.lang.ClassCastException:
     *   ConcurrentHashMapTest_JSR166$BI cannot be cast to
     *     ConcurrentHashMapTest_JSR166$BS
     *
     *  Probably differing mumble type erasure mumble details.
     *  Sort that out someday. For now ConcurrentHashMap has bigger issues.
     */
    def compareTo(other: BS): Int =
      value.compareTo(other.value)

    override def equals(x: Any): Boolean = x match {
      case other: BS => value.equals(other.value)
      case _         => false
    }

    override def hashCode(): Int = 42
  }

  class LexicographicList[E <: Comparable[E]]
      extends ArrayList[E]
      with Comparable[LexicographicList[E]] {

    def this(c: Collection[E]) = {
      this()
      this.addAll(c)
    }

    def this(e: E) = {
      this()
      this.add(e)
    }

    def compareTo(other: LexicographicList[E]): Int = {
      @tailrec
      def loop(depth: Integer, maxDepth: Integer): Integer = {
        if (depth >= maxDepth) 0
        else {
          val balance = this.get(depth).compareTo(other.get(depth))
          if (balance != 0) balance
          else loop(depth + 1, maxDepth)
        }
      }

      val common = Math.min(size(), other.size())

      val cmp = loop(0, common)
      if (cmp != 0) cmp
      else Integer.compare(size(), other.size())
    }
  }

  class CollidingObject(protected val value: String) {

    override def hashCode(): Int =
      this.value.hashCode() & 1

    override def equals(x: Any): Boolean = x match {
      case other: CollidingObject => this.value == other.value
      case _                      => false
    }
  }

  class ComparableCollidingObject(value: String)
      extends CollidingObject(value)
      with Comparable[ComparableCollidingObject] {

    def compareTo(o: ComparableCollidingObject): Int =
      value.compareTo(o.value)
  }

  private def findValue(map: ConcurrentHashMap[Item, Item], key: Item): Item = {
    if (key.value % 5 == 0) key
    else
      map.computeIfAbsent(
        new Item(key.value + 1),
        k => new Item(findValue(map, k))
      )
  }
}

class ConcurrentHashMapTest_JSR166 extends JSR166Test {
  import JSR166Test.*
  import ConcurrentHashMapTest_JSR166.*

  /** Inserted elements that are subclasses of the same Comparable class are
   *  found.
   */
  @Test def testComparableFamily(): Unit = {
    val size = 500; // makes measured test run time -> 60ms

    val m = new juc.ConcurrentHashMap[BI, Boolean]()

    for (i <- 0 until size)
      assertNull("a1", m.put(new CI(i), true))

    for (i <- 0 until size) {
      assertTrue("a2", m.containsKey(new CI(i)))
      assertTrue("a3", m.containsKey(new DI(i)))
    }
  }

  /** Elements of classes with erased generic type parameters based on
   *  Comparable can be inserted and found.
   */
  @Test def testGenericComparable(): Unit = {
    /* Passes on JVM but  has a "ClassCastException" on SN for conversion of
     *  class BI to BS.
     */
    assumeTrue(
      "Skipped on Scala Native due to poorly understood ClassCastException",
      Platform.executingInJVM
    )

    val size = 120 // makes measured test run time -> 60ms
    val m = new ConcurrentHashMap[Object, Boolean]()

    for (j <- 0 until size) {
      val bi = new BI(j)
      val bs = new BS(j.toString)

      val bis = new LexicographicList[BI](bi)
      val bss = new LexicographicList[BS](bs)

      assertNull("bis", m.putIfAbsent(bis, true))
      assertTrue("key(bis)", m.containsKey(bis))
      if (m.putIfAbsent(bss, true) == null.asInstanceOf[Boolean])
        assertTrue("containsKey(bss)", m.containsKey(bss))
      assertTrue("containsKey(bis)", m.containsKey(bis))
    }

    for (j <- 0 until size) {
      assertTrue(
        "containsKey(singletonList)",
        m.containsKey(Collections.singletonList(new BI(j)))
      )
    }
  }

  /** Elements of non-comparable classes equal to those of classes with erased
   *  generic type parameters based on Comparable can be inserted and found.
   */
  @Test def testGenericComparable2(): Unit = {
    val size = 500 // makes measured test run time -> 60ms

    val m = new ConcurrentHashMap[Object, Boolean]()

    for (j <- 0 until size)
      m.put(Collections.singletonList(new BI(j)), true)

    for (j <- 0 until size) {
      val bis = new LexicographicList[BI](new BI(j))
      assertTrue(s"BI($j)", m.containsKey(bis))
    }
  }

  /** Mixtures of instances of comparable and non-comparable classes can be
   *  inserted and found.
   */
  @Test def testMixedComparable2(): Unit = {
    val size = 1200 // makes measured test run time -> 35ms

    val map = new ConcurrentHashMap[Object, Object]()
    val rng = new Random()

    for (j <- 0 until size) {

      val x = rng.nextInt(4) match {
        case 0 => new Object()

        case 1 => new CollidingObject(Integer.toString(j))

        case _ =>
          new ComparableCollidingObject(Integer.toString(j))
      }

      assertNull("put(x, x)", map.put(x, x))
    }

    var count = 0

    map
      .keySet()
      .forEach(k => {
        assertEquals("$k", map.get(k), k)
        count += 1
      })

    assertEquals("count", count, size)
    assertEquals("map.size", map.size(), size)

    map
      .keySet()
      .forEach(k => {
        assertEquals("$k", map.put(k, k), k)
      })
  }

  /** clear removes all pairs
   */
  @Test def testClear(): Unit = {
    val map = map5()
    map.clear()
    assertEquals("size", 0, map.size())
  }

  /** Maps with same contents are equal
   */
  @Test def testEquals(): Unit = {
    val map1 = map5()
    val map2 = map5()

    assertTrue("a1", map1.equals(map2))
    assertTrue("a2", map2.equals(map1))

    map1.clear()
    assertFalse("a2", map1.equals(map2))
    assertFalse("a3", map2.equals(map1))
  }

  /** hashCode() equals sum of each key.hashCode ^ value.hashCode
   */
  @Test def testHashCode(): Unit = {
    val map = map5()
    var sum = 0

    map
      .entrySet()
      // '+=' does not compile, so add the long way in the interest of "done"
      .forEach(e =>
        sum = sum +
          (e.getKey().hashCode() ^ e.getValue().hashCode())
      )

    assertEquals("sum", sum, map.hashCode())
  }

  /** contains returns true for contained value
   */
  @Test def testContains(): Unit = {
    val map = map5()
    assertTrue("a1", map.contains("A"))
    assertFalse("a2", map.contains("Z"))
  }

  /** containsKey returns true for contained key
   */
  @Test def testContainsKey(): Unit = {
    val map = map5()

    assertTrue("a1", map.containsKey(one))
    assertFalse("a2", map.containsKey(zero))
  }

  /** containsValue returns true for held values
   */
  @Test def testContainsValue(): Unit = {
    val map = map5()
    assertTrue("a1", map.containsValue("A"))
    assertFalse("a2", map.containsValue("Z"))
  }

  /** enumeration returns an enumeration containing the correct elements
   */
  @Test def testEnumeration(): Unit = {
    val map = map5()
    val e = map.elements()
    var count = 0

    while (e.hasMoreElements()) {
      count += 1
      e.nextElement()
    }

    assertEquals("count", 5, count)
  }

  /** get returns the correct element at the given key, or null if not present
   */
  @Test def testGet(): Unit = {
    val map: ConcurrentHashMap[Integer, String] = map5()

    assertEquals("A", "A", map.get(one))

    val empty = new ConcurrentHashMap[Integer, String]()
    assertNull(map.get("anything"))
    assertNull(empty.get("anything"))
  }

  /** isEmpty is true of empty map and false for non-empty
   */
  @Test def testIsEmpty(): Unit = {
    val empty = new ConcurrentHashMap[Integer, String]()
    val map = map5()

    assertTrue(empty.isEmpty())
    assertFalse(map.isEmpty())
  }

  /** keys returns an enumeration containing all the keys from the map
   */
  @Test def testKeys(): Unit = {
    val map = map5()
    val e = map.keys()
    var count = 0

    while (e.hasMoreElements()) {
      count += 1
      e.nextElement()
    }

    assertEquals("count", 5, count)
  }

  /** keySet returns a Set containing all the keys
   */
  @Test def testKeySet(): Unit = {
    val map = map5()
    val s = map.keySet()

    assertEquals("size", 5, s.size())

    assertTrue("one", s.contains(one))

    assertTrue("two", s.contains(two))
    assertTrue("three", s.contains(three))
    assertTrue("four", s.contains(four))
    assertTrue("five", s.contains(five))
  }

  /** Test keySet().removeAll on empty map
   */
  @Test def testKeySet_empty_removeAll(): Unit = {
    val map = new ConcurrentHashMap[Integer, String]()
    val set = map.keySet()

    set.removeAll(Collections.emptyList())
    assertTrue(map.isEmpty())
    assertTrue(set.isEmpty())
    // following is test for JDK-8163353
    set.removeAll(Collections.emptySet())
    assertTrue(map.isEmpty())
    assertTrue(set.isEmpty())
  }

  /** keySet.toArray returns contains all keys
   */
  @Test def testKeySetToArray(): Unit = {
    val minusTen = Integer.valueOf(-10)
    val map = map5()
    val s = map.keySet()

    //   val ar = s.toArray(new Array[Integer](s.size))
    val ar = s.toArray()
    assertEquals("ar.length", 5, ar.length)

    // Expensive but avoids differing Scala 2 vs 3 syntax for expanding arrays.
    val al_1 = Arrays.stream(ar).collect(Collectors.toList())

    assertTrue("containsAll true", s.containsAll(al_1))

    ar(0) = minusTen

    // Ensure false for right reason, not because of incorrect conversions.
    val al_2 = Arrays.stream(ar).collect(Collectors.toList())
    assertFalse("containsAll false", s.containsAll(al_2))
  }

  /** Values.toArray contains all values
   */
  @Test def testValuesToArray(): Unit = {
    val map = map5()
    val v = map.values()

    val ar = v.toArray(new Array[String](0))
    assertEquals("ar.length", 5, ar.length)

    // Expensive but avoids differing Scala 2 vs 3 syntax for expanding arrays.
    val s = Arrays.stream(ar).collect(Collectors.toList())

    assertTrue("A", s.contains("A"))
    assertTrue("B", s.contains("B"))
    assertTrue("C", s.contains("C"))
    assertTrue("D", s.contains("D"))
    assertTrue("E", s.contains("E"))
  }

  /** entrySet.toArray contains all entries
   */
  @Test def testEntrySetToArray(): Unit = {
    val map = map5()
    val s = map.entrySet()

    val ar = s.toArray()
    assertEquals("ar.length", 5, ar.length)

    for (j <- 0 until map.size) {
      val element = ar(j).asInstanceOf[Map.Entry[Integer, String]]
      assertTrue(map.containsKey(element.getKey()))
      assertTrue(map.containsValue(element.getValue()))
    }
  }

  /** values collection contains all values
   */
  @Test def testValues(): Unit = {
    val map = map5()
    val s = map.values()

    assertEquals("s.size", 5, s.size)

    assertTrue("A", s.contains("A"))
    assertTrue("B", s.contains("B"))
    assertTrue("C", s.contains("C"))
    assertTrue("D", s.contains("D"))
    assertTrue("E", s.contains("E"))
  }

  /** entrySet contains all pairs
   */
  @Test def testEntrySet(): Unit = {
    val map = map5()
    val s = map.entrySet()

    assertEquals("s.size", 5, s.size)

    val it = s.iterator()

    while (it.hasNext()) {
      val e = it.next()
      assertTrue(
        (e.getKey().equals(one) && e.getValue().equals("A")) ||
        (e.getKey().equals(two) && e.getValue().equals("B")) ||
        (e.getKey().equals(three) && e.getValue().equals("C")) ||
        (e.getKey().equals(four) && e.getValue().equals("D")) ||
        (e.getKey().equals(five) && e.getValue().equals("E"))
      )
    }
  }

  /** putAll adds all key-value pairs from the given map
   */
  @Test def testPutAll(): Unit = {
    val p = new ConcurrentHashMap[Integer, String]()
    val map = map5()

    p.putAll(map)

    assertEquals("p.size", 5, p.size)

    assertTrue("one", p.containsKey(one))
    assertTrue("two", p.containsKey(two))
    assertTrue("three", p.containsKey(three))
    assertTrue("four", p.containsKey(four))
    assertTrue("five", p.containsKey(five))
  }

  /** putIfAbsent works when the given key is not present
   */
  @Test def testPutIfAbsent(): Unit = {
    val map = map5()

    map.putIfAbsent(six, "Z")
    assertTrue("contains six", map.containsKey(six))
  }

  /** putIfAbsent does not add the pair if the key is already present
   */
  @Test def testPutIfAbsent2(): Unit = {
    val map = map5()

    assertEquals("A", "A", map.putIfAbsent(one, "Z"))
  }

  /** replace fails when the given key is not present
   */
  @Test def testReplace(): Unit = {
    val map = map5()

    assertNull("replace six", map.replace(six, "Z"))
    assertFalse("contains siz", map.containsKey(six))
  }

  /** replace succeeds if the key is already present
   */
  @Test def testReplace2(): Unit = {
    val map = map5()

    assertNotNull("replace one", map.replace(one, "Z"))
    assertEquals("Z", "Z", map.get(one))
  }

  /** replace value fails when the given key not mapped to expected value
   */
  @Test def testReplaceValue(): Unit = {
    val map = map5()

    assertEquals("A", "A", map.get(one))
    assertFalse("Z", map.replace(one, "Z", "Z"))
    assertEquals("A", "A", map.get(one))
  }

  /** replace value succeeds when the given key mapped to expected value
   */
  @Test def testReplaceValue2(): Unit = {
    val map = map5()

    assertEquals("A", "A", map.get(one))
    assertTrue("Z", map.replace(one, "A", "Z"))
    assertEquals("Z", "Z", map.get(one))
  }

  /** remove removes the correct key-value pair from the map
   */
  @Test def testRemove(): Unit = {
    val map = map5()

    map.remove(five)

    assertEquals("map.size", 4, map.size())
    assertFalse("contains", map.containsKey(five))
  }

  /** remove(key,value) removes only if pair present
   */
  @Test def testRemove2(): Unit = {
    val map = map5()
    map.remove(five, "E")

    assertEquals("map.size", 4, map.size())
    assertFalse("five", map.containsKey(five))

    map.remove(four, "A")
    assertEquals("map.size", 4, map.size())
    assertTrue("four", map.containsKey(four))
  }

  /** size returns the correct values
   */
  @Test def testSize(): Unit = {
    val map = map5()
    val empty = new ConcurrentHashMap[Integer, String]()

    assertEquals("empty.size", 0, empty.size())
    assertEquals("map.size", 5, map.size())
  }

  /** toString contains toString of elements
   */
  @Test def testToString(): Unit = {
    val map = map5()
    val s = map.toString()

    for (j <- 1 to 5)
      assertTrue(s"j: $j", s.contains(String.valueOf(j)))
  }

// Exception tests

  /** Cannot create with only negative capacity
   */
  @Test def testConstructor1(): Unit = {
    assertThrows(
      "constructor_1 with (-1)",
      classOf[IllegalArgumentException],
      new ConcurrentHashMap[Integer, String](-1)
    )
  }

  /** Constructor (initialCapacity, loadFactor) throws IllegalArgumentException
   *  if either argument is negative
   */
  @Test def testConstructor2(): Unit = {

    assertThrows(
      "constructor_2 with (-1, 0.75f)",
      classOf[IllegalArgumentException],
      new ConcurrentHashMap[Integer, String](-1, 0.75f)
    )

    assertThrows(
      "constructor_2 with (16, -1.0f)",
      classOf[IllegalArgumentException],
      new ConcurrentHashMap[Integer, String](16, -1.0f)
    )
  }

  /** Constructor (initialCapacity, loadFactor, concurrencyLevel) throws
   *  IllegalArgumentException if any argument is negative
   */
  @Test def testConstructor3(): Unit = {

    assertThrows(
      "constructor_3 with (-1, 0.75f, 1)",
      classOf[IllegalArgumentException],
      new ConcurrentHashMap[Integer, String](-1, 0.75f, 1)
    )

    assertThrows(
      "constructor_3 with (16, -1.0f, 1)",
      classOf[IllegalArgumentException],
      new ConcurrentHashMap[Integer, String](16, -1.00f, 1)
    )

    assertThrows(
      "constructor_3 with (16, 0.75f, -1)",
      classOf[IllegalArgumentException],
      new ConcurrentHashMap[Integer, String](16, 0.75f, -1)
    )
  }

  /** ConcurrentHashMap(map) throws NullPointerException if the given map is
   *  null
   */
  @Test def testConstructor4(): Unit = {
    assertThrows(
      "constructor_4 with (null)",
      classOf[NullPointerException],
      new ConcurrentHashMap[Integer, String](null)
    )
  }

  /** ConcurrentHashMap(map) creates a new map with the same mappings as the
   *  given map
   */
  @Test def testConstructor5(): Unit = {
    val map1 = map5()
    val map2 = new ConcurrentHashMap[Integer, String](map1)

    assertTrue("m2 == m1", map2.equals(map1))
    map2.put(one, "F")
    assertFalse(map2.equals(map1))
    assertFalse("m2 != m1", map2.equals(map1))
  }

  /** get(null) throws NPE
   */
  @Test def testGet_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.get(null)",
      classOf[NullPointerException],
      c.get(null)
    )
  }

  /** containsKey(null) throws NPE
   */
  @Test def testContainsKey_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.containsKey(null)",
      classOf[NullPointerException],
      c.containsKey(null)
    )
  }

  /** containsValue(null) throws NPE
   */
  @Test def testContainsValue_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.containsValue(null)",
      classOf[NullPointerException],
      c.containsValue(null)
    )
  }

  /** contains(null) throws NPE
   */
  @Test def testContains_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.contains(null)",
      classOf[NullPointerException],
      c.contains(null)
    )
  }

  /** put(null,x) throws NPE
   */
  @Test def testPut1_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.put()",
      classOf[NullPointerException],
      c.put(null, "whatever")
    )
  }

  /** put(x, null) throws NPE
   */
  @Test def testPut2_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.put(zero, null)",
      classOf[NullPointerException],
      c.put(zero, null)
    )
  }

  /** putIfAbsent(null, x) throws NPE
   */
  @Test def testPutIfAbsent1_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.putIfAbsent(null)",
      classOf[NullPointerException],
      c.putIfAbsent(null, "whatever")
    )
  }

  /** replace(null, x) throws NPE
   */
  @Test def testReplace_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.replace(null)",
      classOf[NullPointerException],
      c.replace(null, "whatever")
    )
  }

  /** replace(null, x, y) throws NPE
   */
  @Test def testReplaceValue_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.replace(null, A, B)",
      classOf[NullPointerException],
      c.replace(null, "A", "B")
    )
  }

  /** putIfAbsent(x, null) throws NPE
   */
  @Test def testPutIfAbsent2_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.putIfAbsent(zero, null)",
      classOf[NullPointerException],
      c.putIfAbsent(zero, null)
    )
  }

  /** replace(x, null) throws NPE
   */
  @Test def testReplace2_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.replace(one, null)",
      classOf[NullPointerException],
      c.replace(one, null)
    )
  }

  /** replace(x, null, y) throws NPE
   */
  @Test def testReplaceValue2_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.replace(one, null, A)",
      classOf[NullPointerException],
      c.replace(one, null, "A")
    )
  }

  /** replace(x, y, null) throws NPE
   */
  @Test def testReplaceValue3_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    assertThrows(
      "c.replace(zero, A, null)",
      classOf[NullPointerException],
      c.replace(zero, "A", null)
    )
  }

  /** remove(null) throws NPE
   */
  @Test def testRemove1_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    c.put(one, "asdads")

    assertThrows(
      "c.remove(null)",
      classOf[NullPointerException],
      c.remove(null)
    )
  }

  /** remove(null, x) throws NPE
   */
  @Test def testRemove2_NullPointerException(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    c.put(one, "asdads")

    assertThrows(
      "c.remove(null, whatever)",
      classOf[NullPointerException],
      c.remove(null, "whatever")
    )
  }

  /** remove(x, null) returns false
   */
  @Test def testRemove3(): Unit = {
    val c = new ConcurrentHashMap[Integer, String](5)

    c.put(one, "asdads")
    assertFalse(c.remove(one, null))
  }

  /** SetValue of an EntrySet entry sets value in the map.
   */
  @Test def testSetValueWriteThrough(): Unit = {
    // JSR-166: Adapted from a bug report by Eric Zoerner

    val map = new ConcurrentHashMap[Object, Object](2, 5.0f, 1)

    assertTrue("empty", map.isEmpty())

    for (j <- 0 until 20)
      map.put(itemFor(j), itemFor(j))

    assertFalse("!empty", map.isEmpty())

    val key = itemFor(16)
    val entry1 = map.entrySet().iterator().next()

    // Unless it happens to be first (in which case remainder of
    // test is skipped), remove a possibly-colliding key from map
    // which, under some implementations, may cause entry1 to be
    // cloned in map
    if (!entry1.getKey().equals(key)) {
      map.remove(key)
      val value = "XYZ"
      entry1.setValue(value)
      assertTrue(
        s"map does not contain value '$value'",
        map.containsValue(value)
      ) // fails if write-through broken
    }
  }

  /** Tests performance of removeAll when the other collection is much smaller.
   */
  @Test def testRemoveAll_performance(): Unit = {
    val minusTwo = Integer.valueOf(-2)

    // val expensiveTests = false // for CI
    val expensiveTests = true // for manual testing & development

    val mapSize =
      if (!expensiveTests) 100
      else 1000 * 1000 // no underbars in Scala 2.12

    val iterations =
      if (!expensiveTests) 2
      else 500

    val map = new ConcurrentHashMap[Item, Item]()

    for (j <- 0 until mapSize) {
      val item_j = itemFor(j)
      map.put(item_j, item_j)
    }

    val keySet = map.keySet()

    val removeMe = Arrays.asList(minusOne, minusTwo)
    for (j <- 0 until iterations) {
      assertFalse("iteration: ${j}", keySet.removeAll(removeMe))
      assertEquals("map.size", mapSize, map.size())
    }
  }

  @Test def testReentrantComputeIfAbsent(): Unit = {
    val map = new ConcurrentHashMap[Item, Item](16)

    assertThrows(
      "recursive()",
      classOf[IllegalStateException],
      for (j <- 0 until 100) // force a resize
        map.computeIfAbsent(new Item(j), key => new Item(findValue(map, key)))
    )
  }
}
