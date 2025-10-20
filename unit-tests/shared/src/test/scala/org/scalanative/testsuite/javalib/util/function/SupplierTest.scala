// Ported from Scala.js, commit SHA: 5df5a4142 dated: 2020-09-06
package org.scalanative.testsuite.javalib.util.function

import java.util.function.Supplier

import org.junit.Assert.*
import org.junit.Test

class SupplierTest {
  import SupplierTest.*

  @Test def get(): Unit = {
    val supplier: Supplier[String] = makeSupplier("scala")

    assertEquals("scala", supplier.get())
  }
}

object SupplierTest {
  def makeSupplier[T](f: => T): Supplier[T] = {
    new Supplier[T] {
      def get(): T = f
    }
  }
}
