package scala.scalanative
package unsafe

import scalanative.libc._

object PtrOpsSuite extends tests.Suite {

  test("substraction") {
    Zone { implicit z =>
      val carr: Ptr[CChar] = toCString("abcdefg")
      val cptr: Ptr[CChar] = string.strchr(carr, 'd')
      assert(cptr - carr == 3)
      assert(carr - cptr == -3)

      val iarr: Ptr[CInt] = stackalloc[CInt](8)
      val iptr: Ptr[CInt] = iarr + 4
      assert(iptr - iarr == 4)
      assert(iarr - iptr == -4)

      type StructType = CStruct4[CChar, CInt, CLong, CDouble]
      val sarr: Ptr[StructType] = stackalloc[StructType](8)
      val sptr: Ptr[StructType] = sarr + 7
      assert(sptr - sarr == 7)
      assert(sarr - sptr == -7)
    }
  }
}
