package scala.scalanative
package unsafe

import org.junit.Test

import scalanative.junit.utils.AssertThrows.assertThrows
import org.junit.Assert._

import scalanative.libc._

class CFuncPtrOpsTest {

  def randFunc: CFuncPtr0[Int] = () => stdlib.rand()

  @Test def cFuncPtrCastAndCallWithGivenSignature(): Unit = {
    assertThrows(
      classOf[ClassCastException],
      randFunc.asInstanceOf[CFuncPtr1[Int, Int]] // wrong arity
    )
  }

  // issue #2564
  @Test def canBeSetToNull(): Unit = {
    type CFuncUnit = CFuncPtr0[Unit]
    type FuncStruct = CStruct2[CFuncUnit, Ptr[CFuncUnit]]

    // In the folloing we just want to check in NullPointerException is not being thrown
    val funcPtr = stackalloc[CFuncUnit]()
    assertNotNull("Can assign null to Ptr[CFuncPtr]", !funcPtr = null)

    val struct = stackalloc[FuncStruct]()
    assertNotNull(
      "Can assign null to Ptr[CFuncPtr] in struct",
      struct._1 = null
    )
    assertNotNull(
      "Can assign null to Ptr[CFuncPtr] in struct 2",
      (!struct)._1 = null
    )
    assertNotNull("Can assign null to CFuncPtr in struct", struct._2 = null)
    assertNotNull(
      "Can assign null to CFuncPtr in struct 2",
      (!struct)._2 = null
    )
  }
}
