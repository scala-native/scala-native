package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert.*

import java.util.ArrayList

class CollectionTestOnJDK11 {
  /* Test the documented Collections#toArray(IntFunction),
   * ensuring that any possible overrides in java.* concrete classes
   * are not inadvertently reached and used.
   */

  @Test def toArray_IntFunction(): Unit = {
    type T = Array[String]

    val nElements = 3
    val expected = new T(nElements)
    expected(0) = "Selene"
    expected(1) = "Helios"
    expected(2) = "Eos"

    val collection = TrivialImmutableCollection(expected*)

    val result = collection.toArray((n) => new T(n))

    assertTrue("type", result.isInstanceOf[T])
    assertEquals("size", nElements, result.length)

    for (j <- 0 until nElements)
      assertEquals(s"element(${j})", expected(j), result(j))
  }
}
