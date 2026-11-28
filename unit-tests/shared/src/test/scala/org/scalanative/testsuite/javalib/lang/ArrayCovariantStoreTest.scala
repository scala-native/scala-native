package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Before, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

// Plain stores through a covariant array type (JLS 10.5)
class ArrayCovariantStoreTest {

  @Before def storesAreChecked(): Unit =
    assumeTrue(
      "Stores into arrays of references are not type checked",
      Platform.executingInJVM
    )

  @Test def storeOfIncompatibleElementThroughSupertype(): Unit = {
    val strings = new Array[String](2)
    val objects = strings.asInstanceOf[Array[Object]]
    objects(0) = "a"
    objects(1) = null
    assertEquals("a", strings(0))
    assertThrows(
      classOf[ArrayStoreException],
      objects(1) = Integer.valueOf(1)
    )
    assertNull(strings(1))
  }

  @Test def storeIntoMultiDimensionalArrayThroughSupertype(): Unit = {
    val matrix = Array.ofDim[Int](2, 2)
    val rows = matrix.asInstanceOf[Array[Object]]
    rows(0) = Array(1, 2)
    assertThrows(classOf[ArrayStoreException], rows(1) = Array(1L))
    assertThrows(classOf[ArrayStoreException], rows(1) = "a")
  }

  @Test def storeThroughGenericCode(): Unit = {
    def put[T](array: Array[T], value: Any): Unit =
      array(0) = value.asInstanceOf[T]
    val strings = new Array[String](1)
    put(strings, "a")
    assertThrows(classOf[ArrayStoreException], put(strings, new Object))
    assertEquals("a", strings(0))
  }
}
