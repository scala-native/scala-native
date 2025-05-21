/* Ported from Scala.js commit: 7569c24 dated: 2025-05-20
 * 
 * For reasons internal to Scala.js practice, the Scala.js file was
 * named MathTestOnJDK21.
 * "require-jdk21/org/scalajs/testsuite/javalib/lang/MathTestOnJDK21.scala
 *
 * This file is in "required-jdk18/mumble/MathTestOnJDK18.scala" since
 * the method was introduced in JDK 18.
 */

package org.scalanative.testsuite.javalib.lang

import java.math.BigInteger
import java.util.SplittableRandom

import org.junit.Test
import org.junit.Assert._

class MathTestOnJDK18 {

  @Test def testUnsignedMultiplyHigh(): Unit = {
    /* We fuzz-test by comparing to the "obvious" implementations based on
     * BigIntegers. We use a SplittableRandom generator, because Random cannot
     * generate all Long values.
     */

    val Seed = 909209754851418882L
    val Rounds = 1024

    val gen = new SplittableRandom(Seed)

    val mask = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)

    def ulongToBigInteger(a: Long): BigInteger =
      BigInteger.valueOf(a).and(mask)

    for (round <- 1 to Rounds) {
      val x = gen.nextLong()
      val y = gen.nextLong()

      val expected = {
        ulongToBigInteger(x)
          .multiply(ulongToBigInteger(y))
          .shiftRight(64)
          .longValue()
      }

      assertEquals(
        s"round $round, x = $x, y = $y",
        expected,
        Math.unsignedMultiplyHigh(x, y)
      )
    }
  }

}
