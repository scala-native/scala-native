package java.util
package function

object SupplierSuite extends tests.Suite {
  test("get") {
    val string = new Supplier[String] {
      override def get(): String = "scala"
    }
    assert(string.get() == "scala")
  }
}
