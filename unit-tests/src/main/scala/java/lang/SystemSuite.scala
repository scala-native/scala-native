package java.lang

object SystemSuite extends tests.Suite {
  test("System.nanoTime is monotonically increasing") {
    var t0 = 0L
    var t1 = 0L

    for (_ <- 1 to 100000) {
      t1 = System.nanoTime()
      assert(t0 - t1 < 0L)
      t0 = t1
    }
  }
}