package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._

import java.{util => ju}
import java.util.ArrayList

import java.util.stream.Collector.Characteristics

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CollectorsTestOnJDK10 {

  private def requireEmptyCharacteristics(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals(
      s"unexpected extra characteristics: ${differentia}",
      0,
      differentia.size()
    )
  }

  private def requireUnorderedCharacteristicOnly(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals("characteristics set size", 1, differentia.size())

    assertTrue(
      "Characteristics.UNORDERED is missing",
      differentia.contains(Characteristics.UNORDERED)
    )
  }
  // Since: Java 10
  @Test def collectorsToUnmodifiableList(): Unit = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val s = sisters.stream()

    val collector = Collectors.toUnmodifiableList[String]()

    requireEmptyCharacteristics(collector.characteristics())

    val collected = s.collect(collector)

    assertEquals("list size", nElements, collected.size())

    // Unmodifiable
    assertThrows(classOf[UnsupportedOperationException], collected.remove(0))

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("list element", sisters.get(j), collected.get(j))
  }

  // Since: Java 10
  @Test def collectorsToUnmodifiableMap_2Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Celaeno", 4))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val s = employees.stream()

    val collector =
      Collectors.toUnmodifiableMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber
      )

    requireEmptyCharacteristics(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", nElements, map.size())
    // Unmodifiable
    assertThrows(classOf[UnsupportedOperationException], map.remove(0))

    map.forEach((k: String, v: Int) =>
      assertEquals(
        s"contents: key: '${k}' value: ${v}",
        employees.get(v).badgeNumber,
        v
      )
    )
  }

  // Since: Java 10
  @Test def collectorsToUnmodifiableMap_3Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Merope", -6))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    // One entry, "Merope", will be merged.
    val expectedCount = nElements - 1

    val expectedReplacement = -36

    val s = employees.stream()

    val collector =
      Collectors.toUnmodifiableMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber,
        (found1: Int, found2: Int) => found1 * found2
      )

    requireEmptyCharacteristics(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", expectedCount, map.size())

    // Unmodifiable
    assertThrows(classOf[UnsupportedOperationException], map.remove(0))

    map.forEach((k: String, v: Int) =>
      k match {
        case k if (k == "Merope") =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            expectedReplacement,
            v
          )

        case _ =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            employees.get(v).badgeNumber,
            v
          )
      }
    )
  }

  // Since: Java 10
  @Test def collectorsToUnmodifiableSet(): Unit = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val s = sisters.stream()

    val collector = Collectors.toUnmodifiableSet[String]()

    requireUnorderedCharacteristicOnly(collector.characteristics())

    val collected = s.collect(collector)

    assertEquals("set size", nElements, collected.size())

    // Unmodifiable
    assertThrows(
      classOf[UnsupportedOperationException],
      collected.remove(sisters.get(0))
    )

    // Proper elements
    for (j <- 0 until nElements) {
      val expected = sisters.get(j)
      assertTrue(
        "set element not in Set: ${expected}",
        collected.contains(expected)
      )
    }
  }

}
