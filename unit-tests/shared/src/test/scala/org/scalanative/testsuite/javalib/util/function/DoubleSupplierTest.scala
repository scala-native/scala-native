// Ported from Scala.js, commit SHA: db63dabed dated: 2020-10-06
package org.scalanative.testsuite.javalib.util.function

import java.util.function.DoubleSupplier

import org.junit.Assert._
import org.junit.Test

class DoubleSupplierTest {
  import DoubleSupplierTest._

  @Test def getAsDouble(): Unit = {
    assertEquals(1.234d, makeSupplier(1.234d).getAsDouble(), 0.0d)
  }
}

object DoubleSupplierTest {
  def makeSupplier(f: => Double): DoubleSupplier = {
    new DoubleSupplier {
      def getAsDouble(): Double = f
    }
  }
}
