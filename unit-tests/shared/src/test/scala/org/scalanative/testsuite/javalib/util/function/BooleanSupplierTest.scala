// Ported from Scala.js, commit SHA: db63dabed dated: 2020-10-06
package org.scalanative.testsuite.javalib.util.function

import java.util.function.BooleanSupplier

import org.junit.Assert._
import org.junit.Test

class BooleanSupplierTest {
  import BooleanSupplierTest._

  @Test def getAsBoolean(): Unit = {
    assertEquals(true, makeSupplier(true).getAsBoolean())
    assertEquals(false, makeSupplier(false).getAsBoolean())
  }
}

object BooleanSupplierTest {
  def makeSupplier(f: => Boolean): BooleanSupplier = {
    new BooleanSupplier {
      def getAsBoolean(): Boolean = f
    }
  }
}
