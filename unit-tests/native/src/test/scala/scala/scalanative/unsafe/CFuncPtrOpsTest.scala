package scala.scalanative
package unsafe

import org.junit.Test

import scalanative.junit.utils.AssertThrows.assertThrows

import scalanative.libc._

class CFuncPtrOpsTest {

  def randFunc: CFuncPtr0[Int] = () => stdlib.rand()

  @Test def cFuncPtrCastAndCallWithGivenSignature(): Unit = {
    assertThrows(
      classOf[ClassCastException],
      randFunc.asInstanceOf[CFuncPtr1[Int, Int]] // wrong arity
    )
  }
}
