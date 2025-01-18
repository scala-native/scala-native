// Original Scala Native work to verify local changes, especially
// corrections to Scala Native Issues.

package org.scalanative.testsuite.javalib.math

import java.{lang => jl}
import java.{math => jm}

import org.junit.Test
import org.junit.Assert._

class BigDecimalScalaNativeIssuesTest {

  @Test def Issue4168_FloatRounding(): Unit = {
    /* example.sc in the Issue uses "val f = bd.toFloat". That toFloat
     * is a Scala extension which eventually calls the Java BigDecimal class
     * floatValue() method. This test uses the latter to make the ultimate
     * culprit in the Issue explicit and easier to fix.
     */

    // Fully qualify to be certain that Java is used, not Scala extensions.
    val jbd = jm.BigDecimal("1.199999988079071")
    val f = jbd.floatValue()

    val hexString = jl.Float.floatToIntBits(f).toHexString

    // The right bits
    assertEquals("float as hex String", "3f999999", hexString)

    // same String conversion as JVM, no extraneous digits.
    assertEquals("float as decimal String", "1.1999999", f.toString())
  }

}
