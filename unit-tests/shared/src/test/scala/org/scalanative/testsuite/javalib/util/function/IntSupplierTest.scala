// Ported from Scala.js, commit SHA: db63dabed dated: 2020-10-06
package org.scalanative.testsuite.javalib.util.function

import java.util.function.IntSupplier

import org.junit.Assert._
import org.junit.Test

class IntSupplierTest {
  import IntSupplierTest._

  @Test def getAsInt(): Unit = {
    assertEquals(Int.MinValue, makeSupplier(Int.MinValue).getAsInt())
    assertEquals(1024, makeSupplier(1024).getAsInt())
    assertEquals(Int.MaxValue, makeSupplier(Int.MaxValue).getAsInt())
  }
}

object IntSupplierTest {
  def makeSupplier(f: => Int): IntSupplier = {
    new IntSupplier {
      def getAsInt(): Int = f
    }
  }
}
