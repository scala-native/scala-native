package scala.scalanative
package unsafe

import org.junit.Assert._
import org.junit.Test

import scalanative.unsafe.Nat._

object ExternGlobalsTest {
  type CharsLength = Digit2[_1, _1]

  @extern
  object ExternGlobals {
    @name("scalanative_test_extern_chars")
    var chars: CArray[CChar, CharsLength] = extern
    @address
    @name("scalanative_test_extern_unsized")
    var unsized: CString = extern
    @name("scalanative_test_extern_ints")
    var ints: CArray[CInt, _4] = extern
    @name("scalanative_test_extern_struct")
    var struct: CStruct2[CLongLong, CInt] = extern

    @name("scalanative_test_extern_chars_address")
    def charsAddress(): CVoidPtr = extern
    @name("scalanative_test_extern_unsized_address")
    def unsizedAddress(): CVoidPtr = extern
    @name("scalanative_test_extern_ints_address")
    def intsAddress(): CVoidPtr = extern
    @name("scalanative_test_extern_struct_address")
    def structAddress(): CVoidPtr = extern
  }
}

class ExternGlobalsTest {
  import ExternGlobalsTest.ExternGlobals._

  @Test def charArrayGlobalIsBoundToItsAddress(): Unit = {
    assertEquals(charsAddress(), chars.at(0))
    assertEquals("fourty-two", fromCString(chars.at(0)))
  }

  @Test def addressAnnotatedGlobalIsBoundToItsAddress(): Unit = {
    assertEquals(unsizedAddress(), unsized)
    assertEquals("unsized", fromCString(unsized))
  }

  @Test def intArrayGlobalIsBoundToItsAddress(): Unit = {
    assertEquals(intsAddress(), ints.at(0))
    assertEquals(1, ints(0))
    assertEquals(4, ints(3))
  }

  @Test def intArrayGlobalCanBeMutatedInPlace(): Unit = {
    val previous = ints(2)
    try {
      ints(2) = 42
      assertEquals(42, ints(2))
      assertEquals(42, intsAddress().asInstanceOf[Ptr[CInt]](2))
    } finally ints(2) = previous
  }

  @Test def structGlobalIsBoundToItsAddress(): Unit = {
    assertEquals(structAddress(), struct.at1)
    assertEquals(84L, struct._1)
    assertEquals(42, struct._2)
  }

  @Test def structGlobalCanBeMutatedInPlace(): Unit = {
    val previous = struct._1
    try {
      struct._1 = 7L
      assertEquals(7L, struct._1)
      assertEquals(7L, !structAddress().asInstanceOf[Ptr[CLongLong]])
    } finally struct._1 = previous
  }
}
