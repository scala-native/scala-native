package foobar

import utest._

object UTestTest extends TestSuite {
  val tests = this {
    'test1 {
      println("Printing something from test...")
      (1 to 10).sum ==> 55
    }
  }
}
