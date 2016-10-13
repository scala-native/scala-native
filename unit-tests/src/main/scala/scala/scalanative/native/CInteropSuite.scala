package scala.scalanative.native

import stdio._

object CInteropSuite extends tests.Suite {

  test("varargs") {
    val buff = stackalloc[CChar](64)
    sprintf(buff, c"%d %d %d", 1, 2, 3)
    for ((c, i) <- "1 2 3".zipWithIndex) {
      assert(buff(i) == c)
    }
  }

}
