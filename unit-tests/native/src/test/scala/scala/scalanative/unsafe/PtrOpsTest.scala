package scala.scalanative
package unsafe

import scala.scalanative.unsigned.ULong
import org.junit.Test
import org.junit.Assert._

import scalanative.libc._
import scalanative.unsigned._
// Scala 2.13.7 needs explicit import for implicit conversions
import scalanative.unsafe.Ptr.ptrToCArray

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

  val fn0: CFuncPtr0[CInt] = () => 1

  @Test def castsPtrByteToCFuncPtr(): Unit = {
    val fnPtr: Ptr[Byte] = CFuncPtr.toPtr(fn0)
    val fnFromPtr = CFuncPtr.fromPtr[CFuncPtr0[CInt]](fnPtr)
    val expectedResult = 1

    assertEquals(expectedResult, fn0())
    assertEquals(expectedResult, fnFromPtr())
  }

  val fn1: CFuncPtr1[Int, Int] = (n: Int) => n + 1

  @Test def castedCFuncPtrHandlesArguments(): Unit = {
    type Add1Fn = CFuncPtr1[Int, Int]
    val ptr: Ptr[Byte] = CFuncPtr.toPtr(fn1)
    val fnFromPtr = CFuncPtr.fromPtr[CFuncPtr1[Int, Int]](ptr)
    val aliasedFn = CFuncPtr.fromPtr[Add1Fn](ptr)

    val in = 1
    val expectedOut = 2

    val res0 = fn1(in)
    val res1 = fnFromPtr(in)
    val res2 = aliasedFn(in)

    assertEquals(expectedOut, res0)
    assertEquals(expectedOut, res1)
    assertEquals(expectedOut, res2)
  }

  type StructA = CStruct2[CInt, CString]
  val fn2: CFuncPtr2[CString, StructA, StructA] =
    CFuncPtr2.fromScalaFunction { (arg1: CString, arg2: StructA) =>
      arg2._2 = arg1
      arg2._1 = 42
      arg2
    }

  @Test def castedCFuncPtrHandlesPointersAndStructs(): Unit = {
    type AssignCString = CFuncPtr2[CString, StructA, StructA]
    val ptr = CFuncPtr.toPtr(fn2)
    val fnFromPtr = CFuncPtr.fromPtr[CFuncPtr2[CString, StructA, StructA]](ptr)
    val aliasedFn = CFuncPtr.fromPtr[AssignCString](ptr)

    def test(fn: CFuncPtr2[CString, StructA, StructA]): Unit = Zone {
      implicit z =>
        val str = alloc[StructA]()
        val charset = java.nio.charset.StandardCharsets.UTF_8

        str._1 = 1
        str._2 = toCString("hello_world", charset)

        val sarg = toCString("fourty_two", charset)
        val expected = fromCString(sarg, charset)
        val res = fn(sarg, str)
        val strResult = fromCString(res._2, charset)
        assertEquals(res._1, 42)
        assertEquals(strResult, expected)
    }
    test(fn2)
    test(fnFromPtr)
    test(aliasedFn)
  }

  type ArrLen = Nat.Digit3[Nat._1, Nat._2, Nat._9]
  type LLArr = CArray[CUnsignedLongLong, ArrLen]
  val fn3: CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr] =
    (idx: CInt, value: CUnsignedLongLong, arr: LLArr) => {
      arr.update(idx, value)
      arr
    }
  @Test def castedCFuncPtrHandlesArrays(): Unit = {
    def test(fn: CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr]) = Zone {
      implicit z =>
        val arr = alloc[LLArr]()

        val value = ULong.MaxValue
        val idx = 5

        val resultArray = fn(idx, value, arr)
        val result = !resultArray.at(idx)
        // Some strange thing occurred here: assertEquals resulted in assertionFailed
        assert(result == value)
    }

    type FnAlias = CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr]
    val fn = fn3
    val ptr = CFuncPtr.toPtr(fn)
    val fnFromPtr =
      CFuncPtr.fromPtr[CFuncPtr3[CInt, CUnsignedLongLong, LLArr, LLArr]](ptr)
    val aliasedFn = CFuncPtr.fromPtr[FnAlias](ptr)
    test(fn)
    test(fnFromPtr)
    test(aliasedFn)
  }

}
