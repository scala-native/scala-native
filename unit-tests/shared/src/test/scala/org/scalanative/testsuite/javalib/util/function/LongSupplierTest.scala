// Ported from Scala.js, commit SHA: db63dabed dated: 2020-10-06
package org.scalanative.testsuite.javalib.util.function

import java.util.function.LongSupplier

import org.junit.Assert._
import org.junit.Test

class LongSupplierTest {
  import LongSupplierTest._

  @Test def getAsLong(): Unit = {
    assertEquals(Long.MinValue, makeSupplier(Long.MinValue).getAsLong())
    assertEquals(1024L, makeSupplier(1024L).getAsLong())
    assertEquals(Long.MaxValue, makeSupplier(Long.MaxValue).getAsLong())
  }
}

object LongSupplierTest {
  def makeSupplier(f: => Long): LongSupplier = {
    new LongSupplier {
      def getAsLong(): Long = f
    }
  }
}
