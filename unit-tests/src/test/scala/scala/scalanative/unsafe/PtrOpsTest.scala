package scala.scalanative
package unsafe

import scala.scalanative.unsigned.ULong
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

  val fn0: CFuncPtr0[CInt] = new CFuncPtr0[CInt] {
    override def apply(): CInt = 1
  }

  @Test def castsPtrByteToCFuncPtr(): Unit = {
    val fnPtr: Ptr[Byte] = Ptr.cFuncPtrToPtr(fn0)
    val fnFromPtr        = Ptr.ptrToCFuncPtr[CFuncPtr0[CInt]](fnPtr)
    val expectedResult   = 1
    assertEquals(expectedResult, fn0())
    assertEquals(expectedResult, fnFromPtr())
  }

  val fn1 = new CFuncPtr1[CInt, CInt] {
    override def apply(n: Int): Int = n + 1
  }

  @Test def castedCFuncPtrHandlesArguments(): Unit = {
    type Add1Fn = CFuncPtr1[Int, Int]
    val ptr: Ptr[Byte] = Ptr.cFuncPtrToPtr(fn1)
    val fnFromPtr      = Ptr.ptrToCFuncPtr[CFuncPtr1[Int, Int]](ptr)
    val aliasedFn      = Ptr.ptrToCFuncPtr[Add1Fn](ptr)

    val res0 = fn1(1)
    val res1 = fnFromPtr(1)
    val res2 = aliasedFn(1)

    assertEquals(res0, res1)
    assertEquals(res0, res2)
  }

  type StructA = CStruct2[CInt, CString]
  val fn2 = new CFuncPtr2[CString, StructA, StructA] {
    override def apply(arg1: CString, arg2: StructA): StructA = {
      arg2._2 = arg1
      arg2._1 = 42
      arg2
    }
  }

  @Test def castedCFuncPtrHandlesPointersAndStructs(): Unit = {
    type AssignCString = CFuncPtr2[CString, StructA, StructA]
    val ptr       = Ptr.cFuncPtrToPtr(fn2)
    val fnFromPtr = Ptr.ptrToCFuncPtr[CFuncPtr2[CString, StructA, StructA]](ptr)
    val aliasedFn = Ptr.ptrToCFuncPtr[AssignCString](ptr)

    def test(fn: CFuncPtr2[CString, StructA, StructA]): Unit = Zone {
      implicit z =>
        val str     = alloc[StructA]
        val charset = java.nio.charset.StandardCharsets.UTF_8

        str._1 = 1
        str._2 = toCString("hello_world", charset)

        val sarg      = toCString("fourty_two", charset)
        val expected  = fromCString(sarg, charset)
        val res       = fn(sarg, str)
        val strResult = fromCString(res._2, charset)
        assertEquals(res._1, 42)
        assertEquals(strResult, expected)
    }
    test(fn2)
    test(fnFromPtr)
    test(aliasedFn)
  }

  type ArrLen = Nat.Digit3[Nat._1, Nat._2, Nat._9]
  type LLArr  = CArray[CUnsignedLongLong, ArrLen]
  val fn3 = new CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr] {
    override def apply(idx: CInt,
                       value: CUnsignedLongLong,
                       arr: LLArr): LLArr = {
      arr.update(idx, value)
      arr
    }
  }

  @Test def castedCFuncPtrHandlesArrays(): Unit = {
    def test(fn: CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr]) = Zone {
      implicit z =>
        val arr = alloc[LLArr]

        val value = ULong.MaxValue
        val idx   = 5

        val resultArray = fn(idx, value, arr)
        val result      = !resultArray.at(idx)
        // Some strange thing occurred here: assertEquals resulted in assertionFailed
        assert(result == value)
    }

    type FnAlias = CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr]
    val fn  = fn3
    val ptr = Ptr.cFuncPtrToPtr(fn)
    val fnFromPtr =
      Ptr.ptrToCFuncPtr[CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr]](ptr)
    val aliasedFn = Ptr.ptrToCFuncPtr[FnAlias](ptr)
    test(fn)
    test(fnFromPtr)
    test(aliasedFn)
  }

}
