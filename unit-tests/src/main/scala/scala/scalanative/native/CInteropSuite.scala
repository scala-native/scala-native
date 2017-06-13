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

  test("pointer substraction") {
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
