package java.util
package function

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
