package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._
import java.util.function.{Consumer, Function}

import org.junit.Test
import org.junit.Assert._

class StreamTest {
  @Test def streamBuilderCanBuildAnEmptyStream(): Unit = {
    val s = Stream.builder().build()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def streamEmptyIsEmpty(): Unit = {
    val s = Stream.empty[Int]()
    val it = s.iterator()
    assertFalse(it.hasNext())
  }

  @Test def streamOfCanPutElementsInStream(): Unit = {
    val s = Stream.of(1, 2, 3)
    val it = s.iterator()
    assertTrue(it.next() == 1)
    assertTrue(it.next() == 2)
    assertTrue(it.next() == 3)
    assertFalse(it.hasNext())
  }

  @Test def streamFlatMapWorks(): Unit = {
    val s = Stream.of(1, 2, 3)
    val mapper = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((1 to v): _*)
    }
    val s2 = s.flatMap(mapper)
    val it = s2.iterator()

    assertTrue(it.next() == 1)
    assertTrue(it.next() == 1)
    assertTrue(it.next() == 2)
    assertTrue(it.next() == 1)
    assertTrue(it.next() == 2)
    assertTrue(it.next() == 3)
    assertFalse(it.hasNext())
  }

  @Test def streamForeachWorks(): Unit = {
    val s = Stream.of(1, 2, 3)
    var sum = 0
    val consumer = new Consumer[Int] {
      def accept(i: Int): Unit = sum += i
    }
    s.forEach(consumer)
    assertEquals(6, sum)
  }

  @Test def streamFlatMapWorksTwice(): Unit = {
    val stream = Stream.of(1, 2, 3)
    val mapper1 = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((v to 3): _*)
    }
    val mapper2 = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((5 to v by -1): _*)
    }
    val s2 = stream.flatMap(mapper1).flatMap(mapper2)
    val expected =
      Seq(5, 4, 3, 2, 1, 5, 4, 3, 2, 5, 4, 3, 5, 4, 3, 2, 5, 4, 3, 5, 4, 3)
    val result = scala.collection.mutable.ArrayBuffer.empty[Int]
    val it = s2.iterator()
    while (it.hasNext()) {
      result += it.next()
    }
    assertTrue(result == expected)
  }

  @Test def streamOnCloseWorks(): Unit = {
    var success = false
    val handler = new Runnable { override def run(): Unit = success = true }
    val s = Stream.empty[Int]().onClose(handler)
    assertFalse(success)
    s.close()
    assertTrue(success)
  }
}
