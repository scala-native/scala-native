package foobar

import utest._

object MoreTest extends TestSuite {
  val tests = this {
    'test2 {
      sandbox.Test.foo ==> 42
      // throw new Exception
    }
  }
}
