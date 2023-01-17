package org.scalanative.testsuite.javalib.util
package function

import java.util.function._
import java.util._

import org.junit.Test
import org.junit.Assert._

class SupplierTest {
  @Test def testGet(): Unit = {
    val string = new Supplier[String] {
      override def get(): String = "scala"
    }
    assertTrue(string.get() == "scala")
  }
}
