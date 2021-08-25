package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

import scalanative.unsafe.Nat._

class CArrayOpsTest {

  @Test def atN(): Unit = {
    val alloc = stackalloc[CArray[Int, Digit2[_3, _2]]]
    val carr = !alloc
    val ptr = alloc.asInstanceOf[Ptr[Int]]

    assertTrue(ptr == carr.at(0))
    (1 to 31).foreach { i => assertTrue((ptr + i) == carr.at(i)) }
  }

  @Test def applyUpdate(): Unit = {
    val alloc = stackalloc[CArray[Int, Digit2[_3, _2]]]
    val carr = !alloc
    val ptr = alloc.asInstanceOf[Ptr[Int]]

    (0 until 32).foreach { i =>
      carr(i) = i
      assertTrue(carr(i) == i)
      assertTrue(ptr(i) == i)
      ptr(i) = i * 2
      assertTrue(carr(i) == i * 2)
      assertTrue(ptr(i) == i * 2)
    }
  }

  @Test def testLength(): Unit = {
    val carr0 = stackalloc[CArray[Int, _0]]
    assertTrue(carr0.length == 0)
    val carr8 = stackalloc[CArray[Int, _8]]
    assertTrue(carr8.length == 8)
    val carr16 = stackalloc[CArray[Int, Digit2[_1, _6]]]
    assertTrue(carr16.length == 16)
    val carr32 = stackalloc[CArray[Int, Digit2[_3, _2]]]
    assertTrue(carr32.length == 32)
    val carr128 = stackalloc[CArray[Int, Digit3[_1, _2, _8]]]
    assertTrue(carr128.length == 128)
    val carr256 = stackalloc[CArray[Int, Digit3[_2, _5, _6]]]
    assertTrue(carr256.length == 256)
    val carr1024 = stackalloc[CArray[Int, Digit4[_1, _0, _2, _4]]]
    assertTrue(carr1024.length == 1024)
    val carr4096 = stackalloc[CArray[Int, Digit4[_4, _0, _9, _6]]]
    assertTrue(carr4096.length == 4096)
  }
}
