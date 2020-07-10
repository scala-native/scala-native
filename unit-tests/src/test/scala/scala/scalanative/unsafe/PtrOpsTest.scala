package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

import scalanative.libc._

class PtrOpsTest {

  @Test def substraction(): Unit = {
    Zone { implicit z =>
      val carr: Ptr[CChar] = toCString("abcdefg")
      val cptr: Ptr[CChar] = string.strchr(carr, 'd')
      assertTrue(cptr - carr == 3)
      assertTrue(carr - cptr == -3)

      val iarr: Ptr[CInt] = stackalloc[CInt](8)
      val iptr: Ptr[CInt] = iarr + 4
      assertTrue(iptr - iarr == 4)
      assertTrue(iarr - iptr == -4)

      type StructType = CStruct4[CChar, CInt, CLong, CDouble]
      val sarr: Ptr[StructType] = stackalloc[StructType](8)
      val sptr: Ptr[StructType] = sarr + 7
      assertTrue(sptr - sarr == 7)
      assertTrue(sarr - sptr == -7)
    }
  }

  val fn1: CFuncPtr0[CInt] = new CFuncPtr0[CInt] {
    override def apply(): CInt = 1
  }

  testFails("casts Ptr[Byte] to CFuncPtr", 1850) {
    val fnPtr          = Ptr.cFuncPtrToPtr(fn1)
    val x              = Ptr.ptrToCFuncPtr[CFuncPtr0[CInt]](fnPtr)
    val expectedResult = 1
    assertEquals(expectedResult, fn1())
    assertEquals(expectedResult, x())
  }
}
