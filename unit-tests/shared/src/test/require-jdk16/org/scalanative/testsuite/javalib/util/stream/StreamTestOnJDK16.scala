package org.scalanative.testsuite.javalib.util.stream

import java.util.Arrays
import java.util.function.Consumer
import java.util.stream._

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StreamTestOnJDK16 {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

  // Since: Java 16
  @Test def streamMapMulti_Eliding(): Unit = {
    // By design, the mapper will return empty results for several items.

    val initialCount = 6
    val expectedCount = 3

    val data = new Array[String](initialCount)
    data(0) = "Hydrogen"
    data(1) = "Helium"
    data(2) = ""
    data(3) = "Rabbit"
    data(4) = "Beryllium"
    data(5) = "Boron"

    val s = Arrays.stream(data)

    // Here the result type matches the element type.
    // Next challenge, make the types differ.
    val mappedMulti =
      s.mapMulti((element: String, consumer: Consumer[_ >: String]) =>
        if (element == "Rabbit") {
          for (j <- 1 to 3)
            consumer.accept(s"Rabbit_${j}")
        }
      )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // Since: Java 16
  @Test def streamMapMulti_DifferingTypes(): Unit = {
    // Test the Java mapMulti() use case description.
    // expand one input element to zero or multiple output elements.

    case class Item(name: String, upc: Int)

    val initialCount = 6
    val expectedCount = 2

    val data = new Array[Item](initialCount)
    data(0) = Item("Hydrogen", 1)
    data(1) = Item("Helium", 2)
    data(2) = Item("", 3)
    data(3) = Item("Rabbit", 4)
    data(4) = Item("Beryllium", 5)
    data(5) = Item("Boron", 6)

    val s = Arrays.stream(data)

    // By design & intent, the element and result types differ.
    val mappedMulti =
      s.mapMulti((element: Item, consumer: Consumer[_ >: String]) =>
        if (element.upc == 6) {
          for (j <- 1 to 2)
            consumer.accept(s"${element.name}_${j}")
        }
      )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // Since: Java 16
  @Test def streamMapMultiToDouble(): Unit = {
    case class Item(name: String, upc: Int)

    val phi = 1.61803
    val expectedSum = 87.37362 // sum of after-mapped values, not pre-mapped

    val initialCount = 6

    val data = new Array[Item](initialCount)
    data(0) = Item("Hydrogen", 1)
    data(1) = Item("Helium", 2)
    data(2) = Item("", 3)
    data(3) = Item("Rabbit", 4)
    data(4) = Item("Beryllium", 5)
    data(5) = Item("Boron", 6)

    val s = Arrays.stream(data)

    // By design & intent, the element and result types differ.
    val mappedMultiToDouble = s.mapMultiToDouble((element, doubleConsumer) =>
      if (element.upc >= 3) {
        for (j <- 1 to 2) // One way to increase your gold.
          doubleConsumer.accept(j * element.upc * phi)
      }
    )

    var sum = mappedMultiToDouble.sum()

    assertEquals("unexpected sum", expectedSum, sum, epsilon)
  }

  // Since: Java 16
  @Test def streamToList_Empty(): Unit = {
    val expectedCount = 0
    val data = new Array[Object](expectedCount)

    val s = Arrays.stream(data)

    val list = s.toList()

    val it = list.iterator()
    assertFalse("unexpected non-empty list", it.hasNext())
  }

  // Since: Java 16
  @Test def streamToList_String(): Unit = {
    val expectedCount = 7

    val data = new Array[String](expectedCount)
    data(0) = "The"
    data(1) = "Difference"
    data(2) = "Between"
    data(3) = "me"
    data(4) = "and"
    data(5) = "a"
    data(6) = "madman"

    val s = Arrays.stream(data)

    val list = s.toList()

    var count = 0

    for (j <- 0 until data.size) {
      assertEquals("mismatched element", data(j), list.get(j).toString())
      count += 1
    }

    assertTrue("unexpected empty list", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // Since: Java 16
  @Test def streamToList_ResultisUnmodifiable(): Unit = {
    val expectedCount = 7

    val data = new Array[String](expectedCount)
    data(0) = "is"
    data(1) = "that"
    data(2) = "I"
    data(3) = "am"
    data(4) = "not"
    data(5) = "mad"
    data(6) = "!"

    val s = Arrays.stream(data)

    val list = s.toList()

    // can read
    val j = 3
    assertEquals("", data(j), list.get(j).toString())

    // but not modify
    assertThrows(
      classOf[UnsupportedOperationException],
      list.set(6, "melted clock")
    )

    assertThrows(classOf[UnsupportedOperationException], list.remove(6))
  }

}
