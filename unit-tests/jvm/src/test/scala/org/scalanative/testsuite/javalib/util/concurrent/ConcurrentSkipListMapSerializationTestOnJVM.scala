package org.scalanative.testsuite.javalib.util.concurrent

import java.io.{
  ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream,
  ObjectOutputStream
}
import java.util.NavigableMap
import java.util.concurrent.{ConcurrentNavigableMap, ConcurrentSkipListMap}

import org.junit.Assert._
import org.junit.Test

class ConcurrentSkipListMapSerializationTestOnJVM extends JSR166Test {
  import JSR166Test._

  private val iZero = itemFor(0)
  private val iOne = itemFor(1)
  private val iTwo = itemFor(2)
  private val iThree = itemFor(3)
  private val iFour = itemFor(4)
  private val iFive = itemFor(5)
  private val iSeven = itemFor(7)

  private val nOne = itemFor(-1)
  private val nTwo = itemFor(-2)
  private val nThree = itemFor(-3)
  private val nFour = itemFor(-4)
  private val nFive = itemFor(-5)

  private def serialClone[T](x: T): T = {
    val bytes = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(bytes)
    try out.writeObject(x)
    finally out.close()

    val in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray))
    try in.readObject().asInstanceOf[T]
    finally in.close()
  }

  private def map5(): ConcurrentSkipListMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    map.put(iOne, "A")
    map.put(iFive, "E")
    map.put(iThree, "C")
    map.put(iTwo, "B")
    map.put(iFour, "D")
    map
  }

  private def subMap5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    map.put(iZero, "Z")
    map.put(iOne, "A")
    map.put(iFive, "E")
    map.put(iThree, "C")
    map.put(iTwo, "B")
    map.put(iFour, "D")
    map.put(iSeven, "F")
    map.subMap(iOne, true, iSeven, false)
  }

  private def descendingMap5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    map.put(nOne, "A")
    map.put(nFive, "E")
    map.put(nThree, "C")
    map.put(nTwo, "B")
    map.put(nFour, "D")
    map.descendingMap()
  }

  @Test def testMapSerialization(): Unit = {
    val x = map5()
    val y = serialClone(x)

    assertNotSame(x, y)
    mustEqual(x.size(), y.size())
    mustEqual(x.toString(), y.toString())
    mustEqual(x, y)
    mustEqual(y, x)
    y.clear()
    assertTrue(y.isEmpty())
    assertFalse(x.equals(y))
  }

  @Test def testSubMapSerialization(): Unit = {
    val x = subMap5()
    val y = serialClone(x)

    assertNotSame(x, y)
    mustEqual(x.size(), y.size())
    mustEqual(x.toString(), y.toString())
    mustEqual(x, y)
    mustEqual(y, x)
  }

  @Test def testDescendingSubMapSerialization(): Unit = {
    val x = descendingMap5()
    val y = serialClone(x)

    assertNotSame(x, y)
    mustEqual(x.size(), y.size())
    mustEqual(x.toString(), y.toString())
    mustEqual(x, y)
    mustEqual(y, x)
  }
}
