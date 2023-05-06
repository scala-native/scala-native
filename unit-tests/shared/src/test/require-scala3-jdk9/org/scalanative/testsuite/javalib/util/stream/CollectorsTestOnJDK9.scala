package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._

import java.{util => ju}
import java.util.ArrayList
import java.util.Map

import java.util.stream.Collector.Characteristics

import org.junit.Test
import org.junit.Assert._

class CollectorsTestOnJDK9 {

  private def requireEmptyCharacteristics(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals(
      s"unexpected extra characteristics: ${differentia}",
      0,
      differentia.size()
    )
  }

  private def requireAll3Characteristics(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertTrue(
      "Characteristics.CONCURRENT is missing",
      differentia.contains(Characteristics.CONCURRENT)
    )

    assertTrue(
      "Characteristics.UNORDERED is missing",
      differentia.contains(Characteristics.UNORDERED)
    )

    assertTrue(
      "Characteristics.IDENTITY_FINISH is missing",
      differentia.contains(Characteristics.IDENTITY_FINISH)
    )
  }

  // Since: Java 9
  @Test def collectorsFiltering(): Unit = {
    val nElements = 100
    val nEvenElements = nElements / 2

    // K. F. Gauss formula for sum of even integers within a range.
    val expectedFilteredSum = ((2 + 100) / 2) * nEvenElements

    val s = Stream
      .iterate[Int](1, e => e + 1)
      .limit(nElements)

    val collector =
      Collectors.filtering(
        (e: Int) => (e % 2 == 0),
        Collectors.summingInt((e: Int) => e)
      )

    requireEmptyCharacteristics(collector.characteristics())

    val sumOfEvens = s.collect(collector)

    assertEquals("unexpected filteredSum", expectedFilteredSum, sumOfEvens)
  }

  @Test def collectorsFiltering_PreservesCharacteristics(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val collector1 =
      Collectors.toConcurrentMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber
      )

    requireAll3Characteristics(collector1.characteristics())

    // Pick a downstream that is now known to have characteristics.
    val collector2 =
      Collectors.filtering(
        (e: Map.Entry[String, Int]) => (e.getValue() <= 3),
        collector1
      )

    // Are the downstreamCharacteristics inherited correctly? JVM does that.
    requireAll3Characteristics(collector2.characteristics())
  }

  // Since: Java 9
  @Test def collectorsFlatMapping(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Accumulate names into a List
     */
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val expectedSum = 45 * 2

    val s = sisters.stream()

    // A demo transformation just for the fun of it.
    val collector = Collectors.flatMapping(
      (e: String) => {
        val n = e.length()
        Stream.of(n, n)
      },
      Collectors.summingInt((e: Int) => e)
    )

    requireEmptyCharacteristics(collector.characteristics())

    val sum = s.collect(collector)

    assertEquals("sum", expectedSum, sum)
  }

  @Test def collectorsFlatMapping_PreservesCharacteristics(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val collector1 =
      Collectors.toConcurrentMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber
      )

    requireAll3Characteristics(collector1.characteristics())

    // Pick a downstream that is now known to have characteristics.
    val collector2 =
      Collectors.flatMapping(
        (e: Map.Entry[String, Int]) =>
          Stream.of(
            Employee(e.getKey(), e.getValue()),
            Employee(e.getValue().toString(), e.getValue() * 2) // nonesense
          ),
        collector1
      )

    // Are the downstreamCharacteristics inherited correctly? JVM does that.
    requireAll3Characteristics(collector2.characteristics())
  }

}
