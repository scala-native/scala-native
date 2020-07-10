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

  test("cast to CFuncPtr") {
    type ExpectedFnType = CFuncPtr0[CInt]
    type OtherFnType    = CFuncPtr1[CInt, CString]
    val funcPtr: Ptr[Byte] = new ExpectedFnType {
      override def apply(): CInt = stdlib.rand()
    }.toPtr

    assertTrue {
      Ptr
        .ptrToCFuncPtr[ExpectedFnType](funcPtr)
        .isInstanceOf[ExpectedFnType]
    }
    assertTrue {
      Ptr
        .ptrToCFuncPtr[OtherFnType](funcPtr)
        .isInstanceOf[OtherFnType]
    }
  }
}
