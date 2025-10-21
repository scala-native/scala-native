package org.scalanative.testsuite.javalib.util

import java.{lang => jl, util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ObjectsTestOnJDK19 {

  @Test def testToIdendityString(): Unit = {

    assertThrows(
      "null arg",
      classOf[NullPointerException],
      ju.Objects.toIdentityString(null.asInstanceOf[String])
    )

    val src = "Mut"

    val expected = new jl.StringBuilder(src.getClass().getName())
      .append('@')
      .append(Integer.toHexString(System.identityHashCode(src)))
      .toString()

    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.toIdentityString(src)
    )
  }
}
