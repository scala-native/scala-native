package scala.scalanative
package unsafe

import org.junit.Test

import scalanative.junit.utils.AssertThrows._

import scalanative.libc._

class CFuncPtrOpsTest {

  def randFunc = new CFuncPtr0[CInt] {
    def apply(): CInt = stdlib.rand()
  }

  @Test def cFuncPtrCastAndCallWithGivenSignature(): Unit = {
    assertThrows(classOf[ClassCastException],
                 randFunc.asInstanceOf[CFuncPtr1[Int, Int]] // wrong arity
    )
  }
}
