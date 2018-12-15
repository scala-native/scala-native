package java.util
package function

object SupplierSuite extends tests.Suite {
  test("get") {
    val string = new Supplier[String] {
      override def get(): String = "scala"
    }
    assert(string.get() == "scala")
  }
  test("getAsBoolean") {
    val booleanSupplier = new BooleanSupplier {
      override def getAsBoolean(): Boolean = false
    }
    assert(booleanSupplier.getAsBoolean() == false)
  }
  test("getAsDouble") {
    val doubleSupplier = new DoubleSupplier {
      override def getAsDouble(): Double = 0.1d
    }
    assert(doubleSupplier.getAsDouble() == 0.1d)
  }
  test("getAsInt") {
    val intSupplier = new IntSupplier {
      override def getAsInt: Int = 1
    }
    assert(intSupplier.getAsInt == 1)
  }
  test("getAsLong") {
    val longSupplier = new LongSupplier {
      override def getAsLong: Long = 1L
    }
    assert(longSupplier.getAsLong == 1L)
  }
}
