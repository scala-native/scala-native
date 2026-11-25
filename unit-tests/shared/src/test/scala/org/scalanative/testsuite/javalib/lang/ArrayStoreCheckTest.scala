package org.scalanative.testsuite.javalib.lang

import java.lang.reflect.{Array => JArray}
import java.{util => ju}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Library operations that store into an array whose element type is only
 * known at runtime, and must reject incompatible elements: System.arraycopy,
 * java.lang.reflect.Array.set, Arrays.copyOf with a new type and
 * Collection.toArray(T[]). See #209, #1694, #1695 and #4845.
 */
object ArrayStoreCheckTest {
  class Item(val id: Int)
}

class ArrayStoreCheckTest {
  import ArrayStoreCheckTest._

  // System.arraycopy

  @Test def arraycopyBetweenCompatibleReferenceArrays(): Unit = {
    val strings = Array("a", "b", "c")
    val objects = new Array[Object](3)
    System.arraycopy(strings, 0, objects, 0, 3)
    assertArrayEquals(Array[Object]("a", "b", "c"), objects)

    // Elements are checked one by one, not by the source array type
    val onlyStrings: Array[Object] = Array("x", null, "z")
    val target = new Array[String](3)
    System.arraycopy(onlyStrings, 0, target, 0, 3)
    assertArrayEquals(
      Array[Object]("x", null, "z"),
      target.asInstanceOf[Array[Object]]
    )

    val rows: Array[Object] = Array(Array(1), Array(2))
    val matrix = new Array[Array[Int]](2)
    System.arraycopy(rows, 0, matrix, 0, 2)
    assertSame(rows(1), matrix(1))

    val intMatrix = Array.ofDim[Int](2, 2)
    val asObjects = new Array[Object](2)
    System.arraycopy(intMatrix, 0, asObjects, 0, 2)
    assertSame(intMatrix(0), asObjects(0))
  }

  @Test def arraycopyStopsAtFirstIncompatibleElement(): Unit = {
    val src: Array[Object] =
      Array("a", "b", Integer.valueOf(3), "d")
    val dst = Array("0", "1", "2", "3", "4")
    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(src, 0, dst, 1, 4)
    )
    // Elements before the incompatible one are copied, no others modified
    assertArrayEquals(
      Array[Object]("0", "a", "b", "3", "4"),
      dst.asInstanceOf[Array[Object]]
    )
  }

  @Test def arraycopyRejectsIncompatibleArrayTypes(): Unit = {
    val ints = Array(1, 2)
    val longs = new Array[Long](2)
    val objects = new Array[Object](2)
    val strings = Array("a", "b")
    val intMatrix = Array.ofDim[Int](1, 1)

    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(ints, 0, longs, 0, 2)
    )
    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(ints, 0, objects, 0, 2)
    )
    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(objects, 0, ints, 0, 2)
    )
    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(strings, 0, ints, 0, 2)
    )
    // Even when nothing is copied
    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(ints, 0, longs, 0, 0)
    )
    val stringMatrix = new Array[Array[String]](1)
    assertThrows(
      classOf[ArrayStoreException],
      System.arraycopy(intMatrix, 0, stringMatrix, 0, 1)
    )
  }

  // java.lang.reflect.Array.set

  @Test def reflectSetChecksTheElementType(): Unit = {
    val strings = new Array[String](2)
    JArray.set(strings, 0, "a")
    assertEquals("a", strings(0))
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.set(strings, 1, Integer.valueOf(1))
    )
    assertNull(strings(1))

    val numbers = new Array[Number](2)
    JArray.set(numbers, 0, Integer.valueOf(1))
    JArray.set(numbers, 1, java.lang.Long.valueOf(2L))
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.set(numbers, 0, "a")
    )

    val items = JArray.newInstance(classOf[Item], 1)
    JArray.set(items, 0, new Item(1))
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.set(items, 0, new Object)
    )

    val matrix = new Array[Array[Int]](1)
    JArray.set(matrix, 0, Array(1))
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.set(matrix, 0, Array(1L))
    )
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.set(matrix, 0, Array[Object](Array(1)))
    )
  }

  // java.util.Arrays

  @Test def arraysCopyOfIntoIncompatibleType(): Unit = {
    val objects: Array[Object] = Array("a", Integer.valueOf(1))
    assertThrows(
      classOf[ArrayStoreException],
      ju.Arrays.copyOf[String, Object](objects, 2, classOf[Array[String]])
    )
    // Only the copied range is checked
    val prefix =
      ju.Arrays.copyOf[String, Object](objects, 1, classOf[Array[String]])
    assertSame(classOf[Array[String]], prefix.getClass)
    assertEquals("a", prefix(0))
  }

  // Collection.toArray(T[]), see #1695 and #4845

  private def collectionsOfItems(): Seq[ju.Collection[Object]] = Seq(
    new ju.ArrayList[Object](),
    new ju.LinkedList[Object](),
    new ju.ArrayDeque[Object](),
    new ju.HashSet[Object](),
    new ju.Vector[Object](),
    new ju.concurrent.CopyOnWriteArrayList[Object](),
    new ju.concurrent.CopyOnWriteArraySet[Object](),
    new ju.concurrent.ConcurrentLinkedQueue[Object](),
    new ju.concurrent.ConcurrentLinkedDeque[Object](),
    new ju.concurrent.LinkedBlockingQueue[Object](),
    new ju.concurrent.ArrayBlockingQueue[Object](4)
  ).map { coll =>
    coll.add(new Item(1))
    coll.add(new Item(2))
    coll
  }

  @Test def collectionToArrayHasTheTypeOfItsArgument(): Unit =
    collectionsOfItems().foreach { coll =>
      val name = coll.getClass.getName
      val items = coll.toArray(new Array[Item](0))
      assertSame(name, classOf[Array[Item]], items.getClass)
      assertEquals(name, 2, items.length)
      val larger = new Array[Item](5)
      assertSame(name, larger, coll.toArray(larger))
    }

  private def assertToArrayOfIncompatibleTypeThrows(
      coll: ju.Collection[Object]
  ): Unit = {
    val name = coll.getClass.getName
    assertThrows(
      name,
      classOf[ArrayStoreException],
      coll.toArray(new Array[java.lang.Long](0))
    )
    assertThrows(
      name,
      classOf[ArrayStoreException],
      coll.toArray(new Array[java.lang.Long](5))
    )
  }

  // Collections copying their elements with System.arraycopy or Arrays.copyOf
  @Test def collectionToArrayOfIncompatibleType(): Unit =
    collectionsOfItems()
      .filter {
        case _: ju.LinkedList[_] | _: ju.HashSet[_] |
            _: ju.concurrent.ConcurrentLinkedQueue[_] |
            _: ju.concurrent.ConcurrentLinkedDeque[_] |
            _: ju.concurrent.LinkedBlockingQueue[_] =>
          false
        case _ => true
      }
      .foreach(assertToArrayOfIncompatibleTypeThrows)

  // Collections storing their elements one by one
  @Ignore("Stores into arrays of references are not type checked")
  @Test def collectionToArrayOfIncompatibleTypeStoredOneByOne(): Unit =
    Seq(
      new ju.LinkedList[Object](),
      new ju.HashSet[Object](),
      new ju.concurrent.ConcurrentLinkedQueue[Object](),
      new ju.concurrent.ConcurrentLinkedDeque[Object](),
      new ju.concurrent.LinkedBlockingQueue[Object]()
    ).foreach { coll =>
      coll.add(new Item(1))
      assertToArrayOfIncompatibleTypeThrows(coll)
    }
}
