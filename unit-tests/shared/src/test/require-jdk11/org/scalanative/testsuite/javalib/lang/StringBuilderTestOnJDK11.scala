package org.scalanative.testsuite.javalib.lang

import java.lang.StringBuilder

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringBuilderTestOnJDK11 {

  @Test def compareTo(): Unit = {

    val dataA = "abcDef"
    val bldrA = new StringBuilder(dataA)
    val bldrAClone = new StringBuilder(dataA)

    val alteredA = "abcdef"
    val bldrB = new StringBuilder(alteredA)

    val shortenedA = "abcDe"
    val bldrC = new StringBuilder(shortenedA)

    val shortenedAndChangedA = "abcde"
    val bldrD = new StringBuilder(shortenedAndChangedA)

    assertThrows(
      "null argument",
      classOf[NullPointerException],
      bldrA.compareTo(null)
    )

    /* StringBuilder extends Object and does not override equals
     * so == and .eq both use reference equality.
     * The intent here is to prove reference inequality. Use .eq to make
     * that intent evident to the hasty or novice reader.
     */
    assertFalse("eq", bldrA.eq(bldrAClone))

    // Compare contents
    assertTrue("A == A", bldrAClone.compareTo(bldrA) == 0) // reflexive
    assertTrue("A == AClone", bldrA.compareTo(bldrAClone) == 0)

    assertTrue("A < B", bldrA.compareTo(bldrB) < 0) // symmetrical
    assertTrue("B > A", bldrB.compareTo(bldrA) > 0)

    assertTrue("A > C", bldrA.compareTo(bldrC) > 0)
    assertTrue("B > C", bldrB.compareTo(bldrC) > 0) // transitive

    assertTrue("D shorter but greater than A", bldrD.compareTo(bldrA) > 0)
  }
}
