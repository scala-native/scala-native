package org.scalanative.testsuite.javalib.lang

import java.lang.reflect.{Array => JArray}
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.{util => ju}

import scala.reflect.{ClassTag, classTag}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

object ArrayAllocationTypeTest {
  class Elem(val id: Int)
  class SubElem(id: Int) extends Elem(id)
  class OnlyAtRuntime(val id: Int)
  class ConcurrentlyCreated

  def generic[T: ClassTag](n: Int): Array[T] = new Array[T](n)
}

class ArrayAllocationTypeTest {
  import ArrayAllocationTypeTest._

  private def assertArrayClass(expected: Class[_], array: AnyRef): Unit = {
    assertNotNull(array)
    assertSame(
      s"expected ${expected.getName}, got ${array.getClass.getName}",
      expected,
      array.getClass
    )
  }

  @Test def newArray(): Unit = {
    assertArrayClass(classOf[Array[String]], new Array[String](2))
    assertArrayClass(classOf[Array[Elem]], new Array[Elem](2))
    assertArrayClass(classOf[Array[Object]], new Array[Object](2))
    assertArrayClass(classOf[Array[Object]], new Array[Any](2))
    assertArrayClass(classOf[Array[Object]], new Array[AnyRef](2))
    assertArrayClass(classOf[Array[Runnable]], new Array[Runnable](2))
    assertArrayClass(classOf[Array[Array[Int]]], new Array[Array[Int]](2))
    assertArrayClass(
      classOf[Array[Array[String]]],
      new Array[Array[String]](2)
    )
  }

  @Test def arrayLiterals(): Unit = {
    // Constant and non constant elements take different allocation paths
    val s = String.valueOf(42)
    assertArrayClass(classOf[Array[String]], Array("a", "b"))
    assertArrayClass(classOf[Array[String]], Array(s, s))
    assertArrayClass(classOf[Array[Elem]], Array(new Elem(1), new SubElem(2)))
    assertArrayClass(classOf[Array[SubElem]], Array(new SubElem(2)))
    assertArrayClass(classOf[Array[Object]], Array[AnyRef]("a", s))
    assertArrayClass(classOf[Array[Array[Int]]], Array(Array(1), Array(2)))
    assertArrayClass(classOf[Array[Array[String]]], Array(Array("a")))
  }

  @Test def emptyArrays(): Unit = {
    assertArrayClass(classOf[Array[String]], Array.empty[String])
    assertArrayClass(classOf[Array[String]], Array[String]())
    assertArrayClass(classOf[Array[Elem]], Array.empty[Elem])
    assertArrayClass(classOf[Array[Array[Int]]], Array.empty[Array[Int]])
  }

  @Test def ofDim(): Unit = {
    val strings = Array.ofDim[String](2, 3)
    assertArrayClass(classOf[Array[Array[String]]], strings)
    strings.foreach(assertArrayClass(classOf[Array[String]], _))
    assertNotSame(strings(0), strings(1))

    val ints = Array.ofDim[Int](2, 3, 4)
    assertArrayClass(classOf[Array[Array[Array[Int]]]], ints)
    ints.foreach { plane =>
      assertArrayClass(classOf[Array[Array[Int]]], plane)
      plane.foreach(assertArrayClass(classOf[Array[Int]], _))
    }
    assertNotSame(ints(0)(0), ints(1)(0))
  }

  @Test def scalaCollectionConversions(): Unit = {
    assertArrayClass(classOf[Array[String]], List("a", "b").toArray)
    assertArrayClass(classOf[Array[String]], Array.fill(2)("a"))
    assertArrayClass(classOf[Array[Elem]], Vector(new Elem(1)).toArray)
    assertArrayClass(classOf[Array[Object]], List("a").toArray[AnyRef])
    assertArrayClass(classOf[Array[Array[Int]]], List(Array(1)).toArray)
    assertArrayClass(classOf[Array[String]], Array("b", "a").sorted)
    assertArrayClass(classOf[Array[String]], Array("a", "b").map(_ * 2))
    assertArrayClass(classOf[Array[String]], Array("a") ++ Array("b"))
    assertArrayClass(
      classOf[Array[Elem]],
      Array(new SubElem(1)).map(e => e: Elem)
    )
  }

  @Test def classTags(): Unit = {
    assertArrayClass(classOf[Array[String]], generic[String](1))
    assertArrayClass(classOf[Array[Int]], generic[Int](1))
    assertArrayClass(classOf[Array[Array[String]]], generic[Array[String]](1))
    assertArrayClass(classOf[Array[String]], classTag[String].newArray(1))
    assertArrayClass(
      classOf[Array[Array[Elem]]],
      classTag[Array[Elem]].newArray(1)
    )
    assertSame(classOf[Array[String]], classTag[String].wrap.runtimeClass)
    assertSame(
      classOf[Array[Array[Int]]],
      classTag[Array[Int]].wrap.runtimeClass
    )
    assertSame(classOf[Array[String]], classTag[Array[String]].runtimeClass)
    assertEquals(classTag[Array[String]], ClassTag(classOf[Array[String]]))
    assertNotEquals(classTag[Array[String]], classTag[Array[Object]])
    assertNotEquals(classTag[Array[Array[Int]]], classTag[Array[Object]])
  }

  @Test def reflectArrayNewInstance(): Unit = {
    assertArrayClass(
      classOf[Array[String]],
      JArray.newInstance(classOf[String], 1)
    )
    assertArrayClass(classOf[Array[Elem]], JArray.newInstance(classOf[Elem], 1))
    val matrix = JArray.newInstance(classOf[String], 2, 3)
    assertArrayClass(classOf[Array[Array[String]]], matrix)
    matrix
      .asInstanceOf[Array[Array[String]]]
      .foreach(assertArrayClass(classOf[Array[String]], _))
    // A zero length leaves the inner dimensions unallocated
    val partial = JArray.newInstance(classOf[Int], 2, 0, 3)
    assertArrayClass(classOf[Array[Array[Array[Int]]]], partial)
    partial
      .asInstanceOf[Array[Array[Array[Int]]]]
      .foreach(assertArrayClass(classOf[Array[Array[Int]]], _))
  }

  @Test def cloneKeepsTheClass(): Unit = {
    assertArrayClass(classOf[Array[String]], Array("a").clone())
    assertArrayClass(classOf[Array[Elem]], Array(new Elem(1)).clone())
    assertArrayClass(classOf[Array[Array[Int]]], Array.ofDim[Int](1, 1).clone())
    val reflective = JArray.newInstance(classOf[OnlyAtRuntime], 1)
    assertArrayClass(
      reflective.getClass,
      reflective.asInstanceOf[Array[OnlyAtRuntime]].clone()
    )
  }

  @Test def javaUtilArraysCopies(): Unit = {
    val strings = Array("a", "b", "c")
    assertArrayClass(classOf[Array[String]], ju.Arrays.copyOf(strings, 5))
    assertArrayClass(
      classOf[Array[String]],
      ju.Arrays.copyOfRange(strings, 1, 2)
    )
    assertArrayClass(
      classOf[Array[Object]],
      ju.Arrays.copyOf(strings, 2, classOf[Array[Object]])
    )
    assertArrayClass(
      classOf[Array[CharSequence]],
      ju.Arrays.copyOf[CharSequence, String](
        strings,
        2,
        classOf[Array[CharSequence]]
      )
    )
    val objects: Array[Object] = Array("a", "b")
    assertArrayClass(
      classOf[Array[String]],
      ju.Arrays.copyOf[String, Object](objects, 2, classOf[Array[String]])
    )
  }

  @Test def collectionToArray(): Unit = {
    val list = new ju.ArrayList[String]()
    list.add("a")
    list.add("b")
    assertArrayClass(classOf[Array[Object]], list.toArray())
    assertArrayClass(classOf[Array[String]], list.toArray(new Array[String](0)))
    assertArrayClass(classOf[Array[String]], list.toArray(new Array[String](5)))
    assertArrayClass(
      classOf[Array[CharSequence]],
      list.toArray(new Array[CharSequence](0))
    )

    val set = new ju.HashSet[String](list)
    assertArrayClass(classOf[Array[String]], set.toArray(new Array[String](0)))
    val deque = new ju.ArrayDeque[String](list)
    assertArrayClass(
      classOf[Array[String]],
      deque.toArray(new Array[String](0))
    )
    val queue = new ju.PriorityQueue[String](list)
    assertArrayClass(
      classOf[Array[String]],
      queue.toArray(new Array[String](0))
    )
    val linked = new ju.LinkedList[String](list)
    assertArrayClass(
      classOf[Array[String]],
      linked.toArray(new Array[String](0))
    )
    val cow = new ju.concurrent.CopyOnWriteArrayList[String](list)
    assertArrayClass(classOf[Array[String]], cow.toArray(new Array[String](0)))
  }

  @Test def arraysReturnedByJavaLibrary(): Unit = {
    assertArrayClass(classOf[Array[String]], "a,b".split(","))
    assertArrayClass(classOf[Array[Char]], "ab".toCharArray)
    assertArrayClass(classOf[Array[Byte]], "ab".getBytes)
    assertArrayClass(
      classOf[Array[StackTraceElement]],
      new Throwable().getStackTrace
    )
    assertArrayClass(classOf[Array[Class[_]]], classOf[String].getInterfaces)
    assertArrayClass(classOf[Array[TimeUnit]], TimeUnit.values())
    assertArrayClass(classOf[Array[Thread.State]], Thread.State.values())
  }

  @Test def runtimeCreatedArrayTypesKeepElementsAliveAcrossGC(): Unit = {
    val arrays = Array.tabulate(64) { i =>
      val array = JArray
        .newInstance(classOf[OnlyAtRuntime], 128)
        .asInstanceOf[Array[OnlyAtRuntime]]
      for (j <- array.indices) array(j) = new OnlyAtRuntime(i * 1000 + j)
      array
    }
    for (_ <- 0 until 1000) new Array[Long](1024)
    System.gc()
    for ((array, i) <- arrays.zipWithIndex) {
      assertSame(arrays(0).getClass, array.getClass)
      for (j <- array.indices) assertEquals(i * 1000 + j, array(j).id)
    }
  }

  @Test def runtimeCreatedArrayTypesAreCanonicalAcrossThreads(): Unit = {
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    val threads = 8
    val start = new CountDownLatch(1)
    val results = new Array[Class[_]](threads * 2)
    val workers = Array.tabulate(threads) { t =>
      new Thread(() => {
        start.await()
        results(2 * t) =
          JArray.newInstance(classOf[ConcurrentlyCreated], 0).getClass
        results(2 * t + 1) =
          JArray.newInstance(classOf[ConcurrentlyCreated], 1, 1).getClass
      })
    }
    workers.foreach(_.start())
    start.countDown()
    workers.foreach(_.join())
    for (t <- 0 until threads) {
      assertSame(results(0), results(2 * t))
      assertSame(results(1), results(2 * t + 1))
    }
    assertSame(classOf[ConcurrentlyCreated], results(0).getComponentType)
    assertSame(results(0), results(1).getComponentType)
  }

  @Test def getClassOfArrays(): Unit = {
    // getClass on a statically typed array may be resolved at compile time
    val strings = Array("a")
    assertSame(classOf[Array[String]], strings.getClass)
    val elems: Array[Elem] = Array(new SubElem(1))
    assertSame(classOf[Array[Elem]], elems.getClass)
    val covariant = Array(new SubElem(1)).asInstanceOf[Array[Elem]]
    assertSame(classOf[Array[SubElem]], covariant.getClass)
    val matrix = Array.ofDim[Int](1, 1)
    assertSame(classOf[Array[Array[Int]]], matrix.getClass)
    assertSame(classOf[Array[Int]], matrix(0).getClass)
  }
}
