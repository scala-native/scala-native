/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.scalanative.testsuite.javalib.util

import java.util.{Vector, Collection, LinkedList, List, HashSet, Arrays}
import java.util.NoSuchElementException

import org.junit._
import org.junit.Assert._
import scala.scalanative.junit.utils.AssumesHelper

class VectorTest {
  private var tVector: Vector[AnyRef] = _
  private var objArray: Array[AnyRef] = _
  private val vString =
    "[Test 0, Test 1, Test 2, Test 3, Test 4, Test 5, Test 6, Test 7, Test 8, Test 9, Test 10, Test 11, Test 12, Test 13, Test 14, Test 15, Test 16, Test 17, Test 18, Test 19, Test 20, Test 21, Test 22, Test 23, Test 24, Test 25, Test 26, Test 27, Test 28, Test 29, Test 30, Test 31, Test 32, Test 33, Test 34, Test 35, Test 36, Test 37, Test 38, Test 39, Test 40, Test 41, Test 42, Test 43, Test 44, Test 45, Test 46, Test 47, Test 48, Test 49, Test 50, Test 51, Test 52, Test 53, Test 54, Test 55, Test 56, Test 57, Test 58, Test 59, Test 60, Test 61, Test 62, Test 63, Test 64, Test 65, Test 66, Test 67, Test 68, Test 69, Test 70, Test 71, Test 72, Test 73, Test 74, Test 75, Test 76, Test 77, Test 78, Test 79, Test 80, Test 81, Test 82, Test 83, Test 84, Test 85, Test 86, Test 87, Test 88, Test 89, Test 90, Test 91, Test 92, Test 93, Test 94, Test 95, Test 96, Test 97, Test 98, Test 99]"

  @Before def setUp() = {
    tVector = new Vector[AnyRef]()
    for (i <- 0 until 100) tVector.addElement("Test " + i)
    objArray = Array.tabulate[AnyRef](100)("Test " + _)
  }

  /** @tests
   *    java.Vector#Vector()
   */
  @Test def test_Constructor(): Unit = {
    val v = new Vector[AnyRef]()
    assertEquals("Vector creation failed", 0, v.size)
    assertEquals("Wrong capacity", 10, v.capacity)
  }

  /** @tests
   *    java.Vector#Vector(int)
   */
  @Test def test_ConstructorI(): Unit = {
    // Test for method java.Vector(int)
    val v = new Vector[AnyRef](100)
    assertEquals("Vector creation failed", 0, v.size)
    assertEquals("Wrong capacity", 100, v.capacity)
  }

  /** @tests
   *    java.Vector#Vector(int, int)
   */
  @Test def test_ConstructorII(): Unit = {
    // Test for method java.Vector(int, int)
    val v = new Vector[AnyRef](2, 10)
    v.addElement(new AnyRef)
    v.addElement(new AnyRef)
    v.addElement(new AnyRef)
    assertEquals("Failed to inc capacity by proper amount", 12, v.capacity)
    val grow = new Vector[AnyRef](3, -1)
    grow.addElement("one")
    grow.addElement("two")
    grow.addElement("three")
    grow.addElement("four")
    assertEquals("Wrong size", 4, grow.size)
    assertEquals("Wrong capacity", 6, grow.capacity)
    val emptyVector = new Vector[AnyRef](0, 0)
    emptyVector.addElement("one")
    assertEquals("Wrong size", 1, emptyVector.size)
    emptyVector.addElement("two")
    emptyVector.addElement("three")
    assertEquals("Wrong size", 3, emptyVector.size)
    try {
      val negativeVector = new Vector[AnyRef](-1, 0)
      fail("Should throw IllegalArgumentException")
    } catch {
      case e: IllegalArgumentException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#Vector(java.Collection)
   */
  @Test def test_ConstructorLjava_util_Collection(): Unit = {
    // Test for method java.Vector(java.Collection)
    val l = new LinkedList[AnyRef]
    for (i <- 0 until 100) {
      l.add("Test " + i)
    }
    val myVector = new Vector[AnyRef](l)
    assertTrue("Vector is not correct size", myVector.size == objArray.length)
    for (counter <- 0 until objArray.length) {
      assertTrue(
        "Vector does not contain correct elements",
        myVector.contains(l.asInstanceOf[List[AnyRef]].get(counter))
      )
    }
  }

  /** @tests
   *    java.Vector#add(int, java.lang.Object)
   */
  @Test def test_addILjava_lang_Object(): Unit = {
    // Test for method void java.Vector.add(int, java.lang.Object)
    val o = new AnyRef
    var prev = tVector.get(45)
    tVector.add(45, o)
    assertTrue("Failed to add Object", tVector.get(45) eq o)
    assertTrue("Failed to fix-up existing indices", tVector.get(46) eq prev)
    assertEquals("Wrong size after add", 101, tVector.size)
    prev = tVector.get(50)
    tVector.add(50, null)
    assertNull("Failed to add null", tVector.get(50))
    assertTrue(
      "Failed to fix-up existing indices after adding null",
      tVector.get(51) eq prev
    )
    assertEquals("Wrong size after add", 102, tVector.size)
  }

  /** @tests
   *    java.Vector#add(java.lang.Object)
   */
  @Test def test_addLjava_lang_Object(): Unit = {
    // Test for method boolean java.Vector.add(java.lang.Object)
    val o = new AnyRef
    tVector.add(o)
    assertTrue("Failed to add Object", tVector.lastElement eq o)
    assertEquals("Wrong size after add", 101, tVector.size)
    tVector.add(null)
    assertNull("Failed to add null", tVector.lastElement)
    assertEquals("Wrong size after add", 102, tVector.size)
  }

  /** @tests
   *    java.Vector#addAll(int, java.Collection)
   */
  @Test def test_addAllILjava_util_Collection(): Unit = {
    // Test for method boolean java.Vector.addAll(int,
    // java.Collection)
    var l = new LinkedList[AnyRef]
    for (i <- 0 until 100) {
      l.add("Test " + i)
    }
    var v = new Vector[AnyRef]
    tVector.addAll(50, l)
    for (i <- 50 until 100) {
      assertTrue(
        "Failed to add all elements",
        tVector.get(i) eq l.asInstanceOf[List[AnyRef]].get(i - 50)
      )
    }
    v = new Vector[AnyRef]
    v.add("one")
    var r = 0
    try v.addAll(3, Arrays.asList(Array[String]("two", "three")))
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        r = 1
      case e: IndexOutOfBoundsException =>
        r = 2
    }
    assertTrue("Invalid add: " + r, r == 1)
    l = new LinkedList[AnyRef]
    l.add(null)
    l.add("gah")
    l.add(null)
    tVector.addAll(50, l)
    assertNull("Wrong element at position 50--wanted null", tVector.get(50))
    assertEquals(
      "Wrong element at position 51--wanted 'gah'",
      "gah",
      tVector.get(51)
    )
    assertNull("Wrong element at position 52--wanted null", tVector.get(52))
    try {
      v.addAll(0, null)
      fail("Should throw NullPointerException")
    } catch {
      case e: NullPointerException =>

      // Excepted
    }
    try {
      v.addAll(-1, null)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#addAll(java.Collection)
   */
  @Test def test_addAllLjava_util_Collection(): Unit = {
    // Test for method boolean java.Vector.addAll(java.Collection)
    val v = new Vector[AnyRef]
    var l = new LinkedList[AnyRef]
    for (i <- 0 until 100) {
      l.add("Test " + i)
    }
    v.addAll(l)
    assertTrue("Failed to add all elements", tVector == v)
    v.addAll(l)
    val vSize = tVector.size
    for (counter <- vSize - 1 to 0 by -1) {
      assertTrue(
        "Failed to add elements correctly",
        v.get(counter) eq v.get(counter + vSize)
      )
    }
    l = new LinkedList[AnyRef]
    l.add(null)
    l.add("gah")
    l.add(null)
    tVector.addAll(l)
    assertNull(
      "Wrong element at 3rd last position--wanted null",
      tVector.get(vSize)
    )
    assertEquals(
      "Wrong element at 2nd last position--wanted 'gah'",
      "gah",
      tVector.get(vSize + 1)
    )
    assertNull(
      "Wrong element at last position--wanted null",
      tVector.get(vSize + 2)
    )
    try {
      v.addAll(null)
      fail("Should throw NullPointerException")
    } catch {
      case e: NullPointerException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#addElement(java.lang.Object)
   */
  @Test def test_addElementLjava_lang_Object(): Unit = {
    // Test for method void java.Vector.addElement(java.lang.Object)
    val v = vectorClone(tVector)
    v.addElement("Added Element")
    assertTrue("Failed to add element", v.contains("Added Element"))
    assertEquals(
      "Added Element to wrong slot",
      "Added Element",
      v.elementAt(100).asInstanceOf[String]
    )
    v.addElement(null)
    assertTrue("Failed to add null", v.contains(null))
    assertNull("Added null to wrong slot", v.elementAt(101))
  }

  /** @tests
   *    java.Vector#addElement(java.lang.Object)
   */
  @Test def test_addElementLjava_lang_Object_subtest0(): Unit = {
    // Test for method void java.Vector.addElement(java.lang.Object)
    val v = vectorClone(tVector)
    v.addElement("Added Element")
    assertTrue("Failed to add element", v.contains("Added Element"))
    assertEquals(
      "Added Element to wrong slot",
      "Added Element",
      v.elementAt(100).asInstanceOf[String]
    )
    v.addElement(null)
    assertTrue("Failed to add null", v.contains(null))
    assertNull("Added null to wrong slot", v.elementAt(101))
  }

  /** @tests
   *    java.Vector#capacity()
   */
  @Test def test_capacity(): Unit = {
    // Test for method int java.Vector.capacity()
    val v = new Vector[AnyRef](9)
    assertEquals("Incorrect capacity returned", 9, v.capacity)
  }

  /** @tests
   *    java.Vector#clear()
   */
  @Test def test_clear(): Unit = {
    // Test for method void java.Vector.clear()
    val orgVector = vectorClone(tVector)
    tVector.clear()
    assertEquals("a) Cleared Vector has non-zero size", 0, tVector.size)
    var e = orgVector.elements
    while (e.hasMoreElements)
      assertTrue(
        "a) Cleared vector contained elements",
        !tVector.contains(e.nextElement)
      )
    tVector.add(null)
    tVector.clear()
    assertEquals("b) Cleared Vector has non-zero size", 0, tVector.size)
    e = orgVector.elements
    while (e.hasMoreElements)
      assertTrue(
        "b) Cleared vector contained elements",
        !tVector.contains(e.nextElement)
      )
  }

  /** @tests
   *    java.Vector#clone()
   */
  @Test def test_clone(): Unit = {
    // Test for method java.lang.Object java.Vector.clone()
    tVector.add(25, null)
    tVector.add(75, null)
    val v = tVector.clone.asInstanceOf[Vector[AnyRef]]
    val orgNum = tVector.elements
    val cnum = v.elements
    while (orgNum.hasMoreElements) {
      assertTrue("Not enough elements copied", cnum.hasMoreElements)
      assertTrue(
        "Vector cloned improperly, elements do not match",
        orgNum.nextElement eq cnum.nextElement
      )
    }
    assertTrue("Not enough elements copied", !cnum.hasMoreElements)
  }

  /** @tests
   *    java.Vector#contains(java.lang.Object)
   */
  @Test def test_containsLjava_lang_Object(): Unit = {
    // Test for method boolean java.Vector.contains(java.lang.Object)
    assertTrue("Did not find element", tVector.contains("Test 42"))
    assertTrue("Found bogus element", !tVector.contains("Hello"))
    assertTrue(
      "Returned true looking for null in vector without null element",
      !tVector.contains(null)
    )
    tVector.insertElementAt(null, 20)
    assertTrue(
      "Returned false looking for null in vector with null element",
      tVector.contains(null)
    )
  }

  /** @tests
   *    java.Vector#containsAll(java.Collection)
   */
  @Test def test_containsAllLjava_util_Collection(): Unit = {
    // Test for method boolean
    // java.Vector.containsAll(java.Collection)
    var s = new HashSet[AnyRef]
    for (i <- 0 until 100) {
      s.add("Test " + i)
    }
    assertTrue("Returned false for valid collection", tVector.containsAll(s))
    s.add(null)
    assertTrue(
      "Returned true for invlaid collection containing null",
      !tVector.containsAll(s)
    )
    tVector.add(25, null)
    assertTrue(
      "Returned false for valid collection containing null",
      tVector.containsAll(s)
    )
    s = new HashSet[AnyRef]
    s.add(new AnyRef)
    assertTrue("Returned true for invalid collection", !tVector.containsAll(s))
  }

  /** @tests
   *    java.Vector#copyInto(java.lang.Object[])
   */
  @Test def test_copyInto$Ljava_lang_Object(): Unit = {
    // Test for method void java.Vector.copyInto(java.lang.Object [])
    val a = new Array[AnyRef](100)
    tVector.setElementAt(null, 20)
    tVector.copyInto(a)
    for (i <- 0 until 100) {
      assertTrue("copyInto failed", a(i) eq tVector.elementAt(i))
    }
  }

  /** @tests
   *    java.Vector#elementAt(int)
   */
  @Test def test_elementAtI(): Unit = {
    // Test for method java.lang.Object java.Vector.elementAt(int)
    assertEquals(
      "Incorrect element returned",
      "Test 18",
      tVector.elementAt(18).asInstanceOf[String]
    )
    tVector.setElementAt(null, 20)
    assertNull("Incorrect element returned--wanted null", tVector.elementAt(20))
  }

  /** @tests
   *    java.Vector#elements()
   */
  @Test def test_elements(): Unit = {
    // Test for method java.util.Enumeration java.Vector.elements()
    tVector.insertElementAt(null, 20)
    val e = tVector.elements
    var i = 0
    while (e.hasMoreElements) {
      assertTrue(
        "Enumeration returned incorrect element at pos: " + i,
        e.nextElement eq tVector.elementAt(i)
      )
      i += 1
    }
    assertTrue("Invalid enumeration", i == tVector.size)
  }

  /** @tests
   *    java.Vector#ensureCapacity(int)
   */
  @Test def test_ensureCapacityI(): Unit = {
    // Test for method void java.Vector.ensureCapacity(int)
    var v = new Vector[AnyRef](9)
    v.ensureCapacity(20)
    assertEquals(
      "ensureCapacity failed to set correct capacity",
      20,
      v.capacity
    )
    v = new Vector[AnyRef](100)
    assertEquals("ensureCapacity reduced capacity", 100, v.capacity)
    v.ensureCapacity(150)
    assertEquals(
      "ensuieCapacity failed to set to be twice the old capacity",
      200,
      v.capacity
    )
    v = new Vector[AnyRef](9, -1)
    v.ensureCapacity(20)
    assertEquals(
      "ensureCapacity failed to set to be minCapacity",
      20,
      v.capacity
    )
    v.ensureCapacity(15)
    assertEquals("ensureCapacity reduced capacity", 20, v.capacity)
    v.ensureCapacity(35)
    assertEquals(
      "ensuieCapacity failed to set to be twice the old capacity",
      40,
      v.capacity
    )
    v = new Vector[AnyRef](9, 4)
    v.ensureCapacity(11)
    assertEquals(
      "ensureCapacity failed to set correct capacity",
      13,
      v.capacity
    )
    v.ensureCapacity(5)
    assertEquals("ensureCapacity reduced capacity", 13, v.capacity)
    v.ensureCapacity(20)
    assertEquals(
      "ensuieCapacity failed to set to be twice the old capacity",
      20,
      v.capacity
    )
  }

  /** @tests
   *    java.Vector#equals(java.lang.Object)
   */
  @Test def test_equalsLjava_lang_Object(): Unit = {
    // Test for method boolean java.Vector.equals(java.lang.Object)
    val v = new Vector[AnyRef]
    for (i <- 0 until 100) {
      v.addElement("Test " + i)
    }
    assertTrue("a) Equal vectors returned false", tVector == v)
    v.addElement(null)
    assertTrue("b) UnEqual vectors returned true", !(tVector == v))
    tVector.addElement(null)
    assertTrue("c) Equal vectors returned false", tVector == v)
    tVector.removeElementAt(22)
    assertTrue("d) UnEqual vectors returned true", !(tVector == v))
    assertTrue("e) Equal vectors returned false", tVector == tVector)
    assertFalse("f) UnEqual vectors returned true", tVector == new AnyRef)
    assertFalse("g) Unequal vectors returned true", tVector == null)
  }

  /** @tests
   *    java.Vector#firstElement()
   */
  @Test def test_firstElement(): Unit = {
    // Test for method java.lang.Object java.Vector.firstElement()
    assertEquals(
      "Returned incorrect firstElement",
      "Test 0",
      tVector.firstElement
    )
    tVector.insertElementAt(null, 0)
    assertNull(
      "Returned incorrect firstElement--wanted null",
      tVector.firstElement
    )
    val v = new Vector[AnyRef]
    try {
      v.firstElement
      fail("Should throw NoSuchElementException")
    } catch {
      case e: NoSuchElementException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#get(int)
   */
  @Test def test_getI(): Unit = {
    // Test for method java.lang.Object java.Vector.get(int)
    assertEquals("Get returned incorrect object", "Test 80", tVector.get(80))
    tVector.add(25, null)
    assertNull("Returned incorrect element--wanted null", tVector.get(25))
  }

  /** @tests
   *    java.Vector#hashCode()
   */
  @Test def test_hashCode(): Unit = {
    // Test for method int java.Vector.hashCode()
    var hashCode = 1 // one

    tVector.insertElementAt(null, 20)
    for (i <- 0 until tVector.size) {
      val obj = tVector.elementAt(i)
      hashCode = 31 * hashCode + (if (obj == null) 0
                                  else obj.hashCode)
    }
    assertTrue(
      "Incorrect hashCode returned.  Wanted: " + hashCode + " got: " + tVector.hashCode,
      tVector.hashCode == hashCode
    )
  }

  /** @tests
   *    java.Vector#indexOf(java.lang.Object)
   */
  @Test def test_indexOfLjava_lang_Object(): Unit = {
    // Test for method int java.Vector.indexOf(java.lang.Object)
    assertEquals("Incorrect index returned", 10, tVector.indexOf("Test 10"))
    assertEquals(
      "Index returned for invalid Object",
      -1,
      tVector.indexOf("XXXXXXXXXXX")
    )
    tVector.setElementAt(null, 20)
    tVector.setElementAt(null, 40)
    assertTrue(
      "Incorrect indexOf returned for null: " + tVector.indexOf(null),
      tVector.indexOf(null) == 20
    )
  }

  /** @tests
   *    java.Vector#indexOf(java.lang.Object, int)
   */
  @Test def test_indexOfLjava_lang_ObjectI(): Unit = {
    // Test for method int java.Vector.indexOf(java.lang.Object, int)
    assertEquals(
      "Failed to find correct index",
      tVector.indexOf("Test 98", 50),
      98
    )
    assertTrue(
      "Found index of bogus element",
      tVector.indexOf("Test 1001", 50) == -(1)
    )
    tVector.setElementAt(null, 20)
    tVector.setElementAt(null, 40)
    tVector.setElementAt(null, 60)
    assertTrue(
      "a) Incorrect indexOf returned for null: " + tVector.indexOf(null, 25),
      tVector.indexOf(null, 25) == 40
    )
    assertTrue(
      "b) Incorrect indexOf returned for null: " + tVector.indexOf(null, 20),
      tVector.indexOf(null, 20) == 20
    )
    try {
      tVector.indexOf("Test 98", -1)
      fail("should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

    }
    assertEquals(-1, tVector.indexOf("Test 98", 1000))
    assertEquals(-1, tVector.indexOf("Test 98", Integer.MAX_VALUE))
    assertEquals(-1, tVector.indexOf("Test 98", tVector.size))
    assertEquals(98, tVector.indexOf("Test 98", 0))
    try {
      tVector.indexOf("Test 98", Integer.MIN_VALUE)
      fail("should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

    }
  }

  /** @tests
   *    java.Vector#insertElementAt(java.lang.Object, int)
   */
  @Test def test_insertElementAtLjava_lang_ObjectI(): Unit = {
    // Test for method void
    // java.Vector.insertElementAt(java.lang.Object, int)
    val v = vectorClone(tVector)
    val prevElement = v.elementAt(99).asInstanceOf[String]
    v.insertElementAt("Inserted Element", 99)
    assertEquals(
      "Element not inserted",
      "Inserted Element",
      v.elementAt(99).asInstanceOf[String]
    )
    assertTrue(
      "Elements shifted incorrectly",
      v.elementAt(100).asInstanceOf[String] == prevElement
    )
    v.insertElementAt(null, 20)
    assertNull("null not inserted", v.elementAt(20))
    try {
      tVector.insertElementAt("Inserted Element", -1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.insertElementAt(null, -1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.insertElementAt("Inserted Element", tVector.size + 1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.insertElementAt(null, tVector.size + 1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#isEmpty()
   */
  @Test def test_isEmpty(): Unit = {
    // Test for method boolean java.Vector.isEmpty()Vector
    val v = new Vector[AnyRef]
    assertTrue("Empty vector returned false", v.isEmpty)
    v.addElement(new AnyRef)
    assertTrue("non-Empty vector returned true", !v.isEmpty)
  }

  /** @tests
   *    java.Vector#lastElement()
   */
  @Test def test_lastElement(): Unit = {
    // Test for method java.lang.Object java.Vector.lastElement()
    assertEquals(
      "Incorrect last element returned",
      "Test 99",
      tVector.lastElement
    )
    tVector.addElement(null)
    assertNull(
      "Incorrect last element returned--wanted null",
      tVector.lastElement
    )
    val vector = new Vector[AnyRef]
    try {
      vector.lastElement
      fail("Should throw NoSuchElementException")
    } catch {
      case e: NoSuchElementException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#lastIndexOf(java.lang.Object)
   */
  @Test def test_lastIndexOfLjava_lang_Object(): Unit = {
    // Test for method int java.Vector.lastIndexOf(java.lang.Object)
    val v = new Vector[AnyRef](9)
    for (i <- 0 until 9) {
      v.addElement("Test")
    }
    v.addElement("z")
    assertEquals("Failed to return correct index", 8, v.lastIndexOf("Test"))
    tVector.setElementAt(null, 20)
    tVector.setElementAt(null, 40)
    assertTrue(
      "Incorrect lastIndexOf returned for null: " + tVector.lastIndexOf(null),
      tVector.lastIndexOf(null) == 40
    )
  }

  /** @tests
   *    java.Vector#lastIndexOf(java.lang.Object, int)
   */
  @Test def test_lastIndexOfLjava_lang_ObjectI(): Unit = {
    // Test for method int java.Vector.lastIndexOf(java.lang.Object,
    // int)
    assertEquals("Failed to find object", 0, tVector.lastIndexOf("Test 0", 0))
    assertTrue(
      "Found Object outside of index",
      tVector.lastIndexOf("Test 0", 10) > -(1)
    )
    tVector.setElementAt(null, 20)
    tVector.setElementAt(null, 40)
    tVector.setElementAt(null, 60)
    assertTrue(
      "Incorrect lastIndexOf returned for null: " + tVector
        .lastIndexOf(null, 15),
      tVector.lastIndexOf(null, 15) == -1
    )
    assertTrue(
      "Incorrect lastIndexOf returned for null: " + tVector
        .lastIndexOf(null, 45),
      tVector.lastIndexOf(null, 45) == 40
    )
    assertEquals(-1, tVector.lastIndexOf("Test 98", -1))
    assertEquals(-1, tVector.lastIndexOf("Test 98", 0))
    try {
      assertEquals(-1, tVector.lastIndexOf("Test 98", 1000))
      fail("should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException =>

    }
    try {
      assertEquals(-1, tVector.lastIndexOf("Test 98", Integer.MAX_VALUE))
      fail("should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException =>

    }
    try {
      tVector.lastIndexOf("Test 98", tVector.size)
      fail("should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException =>

    }
    try {
      tVector.indexOf("Test 98", Integer.MIN_VALUE)
      fail("should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

    }
  }

  /** @tests
   *    java.Vector#remove(int)
   */
  @Test def test_removeI(): Unit = {
    // Test for method java.lang.Object java.Vector.remove(int)
    var removeElement = tVector.get(36)
    var result = tVector.remove(36)
    assertFalse("Contained element after remove", tVector.contains("Test 36"))
    assertEquals(
      "Should return the element that was removed",
      removeElement,
      result
    )
    assertEquals("Failed to decrement size after remove", 99, tVector.size)
    tVector.add(20, null)
    removeElement = tVector.get(19)
    result = tVector.remove(19)
    assertNull("Didn't move null element over", tVector.get(19))
    assertEquals(
      "Should return the element that was removed",
      removeElement,
      result
    )
    removeElement = tVector.get(19)
    result = tVector.remove(19)
    assertNotNull("Didn't remove null element", tVector.get(19))
    assertEquals(
      "Should return the element that was removed",
      removeElement,
      result
    )
    assertEquals(
      "Failed to decrement size after removing null",
      98,
      tVector.size
    )
    try {
      tVector.remove(-1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.remove(tVector.size)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#remove(java.lang.Object)
   */
  @Test def test_removeLjava_lang_Object(): Unit = {
    // Test for method boolean java.Vector.remove(java.lang.Object)
    tVector.remove("Test 0")
    assertTrue("Contained element after remove", !tVector.contains("Test 0"))
    assertEquals("Failed to decrement size after remove", 99, tVector.size)
    tVector.add(null)
    tVector.remove(null)
    assertTrue("Contained null after remove", !tVector.contains(null))
    assertEquals(
      "Failed to decrement size after removing null",
      99,
      tVector.size
    )
  }

  /** @tests
   *    java.Vector#removeAll(java.Collection)
   */
  @Test def test_removeAllLjava_util_Collection(): Unit = {
    // Test for method boolean
    // java.Vector.removeAll(java.Collection)
    val v = new Vector[AnyRef]
    var l = new LinkedList[AnyRef]
    for (i <- 0 until 5) {
      l.add("Test " + i)
    }
    v.addElement(l)
    val s = new HashSet[AnyRef]
    val o: AnyRef = v.firstElement
    s.add(o)
    v.removeAll(s)
    assertTrue("Failed to remove items in collection", !v.contains(o))
    v.removeAll(l)
    assertTrue("Failed to remove all elements", v.isEmpty)
    v.add(null)
    v.add(null)
    v.add("Boom")
    v.removeAll(s)
    assertEquals("Should not have removed any elements", 3, v.size)
    l = new LinkedList[AnyRef]
    l.add(null)
    v.removeAll(l)
    assertEquals("Should only have one element", 1, v.size)
    assertEquals("Element should be 'Boom'", "Boom", v.firstElement)
  }

  /** @tests
   *    java.Vector#removeAllElements()
   */
  @Test def test_removeAllElements(): Unit = {
    // Test for method void java.Vector.removeAllElements()
    val v = vectorClone(tVector)
    v.removeAllElements()
    assertEquals("Failed to remove all elements", 0, v.size)
  }

  /** @tests
   *    java.Vector#removeElement(java.lang.Object)
   */
  @Test def test_removeElementLjava_lang_Object(): Unit = {
    // Test for method boolean
    // java.Vector.removeElement(java.lang.Object)
    val v = vectorClone(tVector)
    v.removeElement("Test 98")
    assertEquals(
      "Element not removed",
      "Test 99",
      v.elementAt(98).asInstanceOf[String]
    )
    assertTrue("Vector is wrong size after removal: " + v.size, v.size == 99)
    tVector.addElement(null)
    v.removeElement(null)
    assertTrue(
      "Vector is wrong size after removing null: " + v.size,
      v.size == 99
    )
  }

  /** @tests
   *    java.Vector#removeElementAt(int)
   */
  @Test def test_removeElementAtI(): Unit = {
    // Test for method void java.Vector.removeElementAt(int)
    val v = vectorClone(tVector)
    var size = v.size
    v.removeElementAt(50)
    assertEquals("Failed to remove element", -1, v.indexOf("Test 50", 0))
    assertEquals("Test 51", v.get(50))
    assertEquals(size - 1, v.size)
    tVector.insertElementAt(null, 60)
    assertNull(tVector.get(60))
    size = tVector.size
    tVector.removeElementAt(60)
    assertNotNull(
      "Element at 60 should not be null after removal",
      tVector.elementAt(60)
    )
    assertEquals(size - 1, tVector.size)
    try {
      tVector.removeElementAt(-1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.removeElementAt(tVector.size)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    {@link java.Vector# removeRange ( int, int)}
   */
  @Test def test_removeRange(): Unit = {
    val myVector = new MockVector()
    myVector.removeRange(0, 0)
    try {
      myVector.removeRange(0, 1)
      fail("Should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException =>

      // Excepted
    }
    val data = Array[Integer](1, 2, 3, 4)
    for (i <- 0 until data.length) {
      myVector.add(i, data(i))
    }
    myVector.removeRange(0, 2)
    assertEquals(data(2), myVector.get(0))
    assertEquals(data(3), myVector.get(1))
    try {
      myVector.removeRange(-1, 1)
      fail("Should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException =>

      // Excepted
    }
    try {
      myVector.removeRange(0, -1)
      fail("Should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException =>
      // Excepted
    }
    AssumesHelper.assumeNotJVMCompliant()
    try {
      myVector.removeRange(1, 0)
      fail("Should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException => // Excepted
    }
    try {
      myVector.removeRange(2, 1)
      fail("Should throw IndexOutOfBoundsException")
    } catch {
      case e: IndexOutOfBoundsException => // Excepted
    }
  }

  /** @tests
   *    java.Vector#retainAll(java.Collection)
   */
  @Test def test_retainAllLjava_util_Collection(): Unit = {
    // Test for method boolean
    // java.Vector.retainAll(java.Collection)
    val o = tVector.firstElement
    tVector.add(null)
    val s = new HashSet[AnyRef]
    s.add(o)
    s.add(null)
    tVector.retainAll(s)
    assertTrue(
      "Retained items other than specified",
      tVector.size == 2 && tVector.contains(o) && tVector.contains(null)
    )
  }

  /** @tests
   *    java.Vector#set(int, java.lang.Object)
   */
  @Test def test_setILjava_lang_Object(): Unit = {
    // Test for method java.lang.Object java.Vector.set(int,
    // java.lang.Object)
    val o = new AnyRef
    var previous = tVector.get(23)
    var result = tVector.set(23, o)
    assertEquals(
      "Should return the element previously at the specified position",
      previous,
      result
    )
    assertTrue("Failed to set Object", tVector.get(23) eq o)
    previous = tVector.get(0)
    result = tVector.set(0, null)
    assertEquals(
      "Should return the element previously at the specified position",
      previous,
      result
    )
    assertNull("Failed to set Object", tVector.get(0))
    try {
      tVector.set(-1, o)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.set(-1, null)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.set(tVector.size, o)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      tVector.set(tVector.size, null)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#setElementAt(java.lang.Object, int)
   */
  @Test def test_setElementAtLjava_lang_ObjectI(): Unit = {
    // Test for method void java.Vector.setElementAt(java.lang.Object,
    // int)
    val v = vectorClone(tVector)
    v.setElementAt("Inserted Element", 99)
    assertEquals(
      "Element not set",
      "Inserted Element",
      v.elementAt(99).asInstanceOf[String]
    )
    v.setElementAt(null, 0)
    assertNull("Null element not set", v.elementAt(0))
    try {
      v.setElementAt("Inserted Element", -1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      v.setElementAt(null, -1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      v.setElementAt("Inserted Element", v.size)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
    try {
      v.setElementAt(null, v.size)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#setSize(int)
   */
  @Test def test_setSizeI(): Unit = {
    // Test for method void java.Vector.setSize(int)
    val v = vectorClone(tVector)
    var oldSize = v.size
    val preElement = v.get(10)
    v.setSize(10)
    assertEquals("Failed to set size", 10, v.size)
    assertEquals(
      "All components at index newSize and greater should be discarded",
      -1,
      v.indexOf(preElement)
    )
    try v.get(oldSize - 1)
    catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted;
    }
    oldSize = v.size
    v.setSize(20)
    assertEquals("Failed to set size", 20, v.size)
    for (i <- oldSize until v.size) {
      assertNull(v.get(i))
    }
    try {
      v.setSize(-1)
      fail("Should throw ArrayIndexOutOfBoundsException")
    } catch {
      case e: ArrayIndexOutOfBoundsException =>

      // Excepted
    }
  }

  /** @tests
   *    java.Vector#subList(int, int)
   */
  @Ignore("SynchronizedRandomAccessList not implemented")
  @Test def test_subListII(): Unit = {
    // Test for method java.List java.Vector.subList(int, int)
    val sl = tVector.subList(10, 25)
    assertEquals("Returned sublist of incorrect size", 15, sl.size)
    for (i <- 10 until 25) {
      assertTrue("Returned incorrect sublist", sl.contains(tVector.get(i)))
    }
    assertEquals(
      "Not synchronized random access",
      "java.util.Collections$SynchronizedRandomAccessList",
      sl.getClass.getName
    )
  }

  /** @tests
   *    java.Vector#toArray()
   */
  @Test def test_toArray(): Unit = {
    // Test for method java.lang.Object [] java.Vector.toArray()
    assertTrue(
      "Returned incorrect array",
      Arrays.equals(objArray, tVector.toArray)
    )
  }

  /** @tests
   *    java.Vector#toArray(java.lang.Object[])
   */
  @Test def test_toArray$Ljava_lang_Object(): Unit = {
    // Test for method java.lang.Object []
    // java.Vector.toArray(java.lang.Object [])
    val o = new Array[AnyRef](1000)
    val f = new AnyRef
    for (i <- 0 until o.length) {
      o(i) = f
    }
    tVector.toArray(o)
    assertNull("Failed to set slot to null", o(100))
    for (i <- 0 until tVector.size) {
      assertTrue("Returned incorrect array", tVector.elementAt(i) eq o(i))
    }
  }

  @SerialVersionUID(1L)
  private[util] class SubVector[E] extends Vector[E] {
    override def add(obj: E): Boolean = {
      super.addElement(obj)
      true
    }

    override def addElement(obj: E): Unit = {
      super.add(obj)
    }

    /** @tests
     *    java.Vector#add(Object)
     */
    @SuppressWarnings(Array("nls")) def test_add(): Unit = {
      val subvector = new SubVector[String]
      subvector.add("foo")
      subvector.addElement("bar")
      assertEquals("Expected two elements in vector", 2, subvector.size)
    }
  }

  /** @tests
   *    java.Vector#toString()
   */
  @Test def test_toString(): Unit = {
    // Ensure toString works with self-referencing elements.
    val vec = new Vector[AnyRef](3)
    vec.add(null)
    vec.add(new AnyRef)
    vec.add(vec)
    assertNotNull(vec.toString)
    // Test for method java.lang.String java.Vector.toString()
    assertTrue("Incorrect String returned", tVector.toString == vString)
    val v = new Vector[AnyRef]
    v.addElement("one")
    v.addElement(v)
    v.addElement("3")
    // test last element
    v.addElement(v)
    val result = v.toString
    assertTrue("should contain self ref", result.indexOf("(this") > -1)
  }

  @throws[Exception]
  @Test def test_override_size(): Unit = {
    val v = new Vector[AnyRef]
    val testv = new MockVector
    // though size is overriden, it should passed without exception
    testv.add(1: Integer)
    testv.add(2: Integer)
    testv.clear()
    testv.add(1: Integer)
    testv.add(2: Integer)
    v.add(1: Integer)
    v.add(2: Integer)
    // RI's bug here
    assertTrue(testv == v)
  }

  /** @tests
   *    java.Vector#trimToSize()
   */
  @Test def test_trimToSize(): Unit = {
    // Test for method void java.Vector.trimToSize()
    val v = new Vector[AnyRef](10)
    v.addElement(new AnyRef)
    v.trimToSize()
    assertEquals("Failed to trim capacity", 1, v.capacity)
  }

  protected def vectorClone(s: Vector[AnyRef]): Vector[AnyRef] =
    s.clone.asInstanceOf[Vector[AnyRef]]

  class MockVector extends Vector[AnyRef] {
    override def size() = 0

    override def removeRange(start: Int, end: Int): Unit = {
      super.removeRange(start, end)
    }
  }

}
