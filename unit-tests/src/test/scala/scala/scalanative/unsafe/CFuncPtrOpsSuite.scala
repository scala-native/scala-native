package scala.scalanative
package unsafe

import scalanative.libc._

object CFuncPtrOpsSuite extends tests.Suite {

  def randFunc = new CFuncPtr0[CInt] {
    def apply(): CInt = stdlib.rand()
  }

  test("CFuncPtr cast and call with given signature") {
    assertThrows[ClassCastException] {
      randFunc.asInstanceOf[CFuncPtr1[Int, Int]] // wrong arity
    }
  }
}
