package org.scalanative.testsuite.javalib.lang

import java.lang.StringBuilder

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringBufferTestOnJDK11 {

  @Test def compareTo(): Unit = {

    val dataA = "abcDef"
    val bufrA = new StringBuffer(dataA)
    val bufrAClone = new StringBuffer(dataA)

    val alteredA = "abcdef"
    val bufrB = new StringBuffer(alteredA)

    val shortenedA = "abcDe"
    val bufrC = new StringBuffer(shortenedA)

    val shortenedAndChangedA = "abcde"
    val bufrD = new StringBuffer(shortenedAndChangedA)

    assertThrows(
      classOf[NullPointerException],
      bufrA.compareTo(null)
    )

    /* StringBuffer extends Object and does not override equals
     * so == and .eq both use reference equality.
     * The intent here is to prove reference inequality. Use .eq to make
     * that intent evident to the hasty or novice reader.
     */
    assertFalse("eq", bufrA.eq(bufrAClone))

    // Compare contents
    assertTrue("A == A", bufrAClone.compareTo(bufrA) == 0) // reflexive
    assertTrue("A == AClone", bufrA.compareTo(bufrAClone) == 0)

    assertTrue("A < B", bufrA.compareTo(bufrB) < 0) // symmetrical
    assertTrue("B > A", bufrB.compareTo(bufrA) > 0)

    assertTrue("A > C", bufrA.compareTo(bufrC) > 0)
    assertTrue("B > C", bufrB.compareTo(bufrC) > 0) // transitive

    assertTrue("D shorter but greater than A", bufrD.compareTo(bufrA) > 0)
  }
}
