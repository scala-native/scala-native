package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

class CStructOpsTest {

  @Test def atN(): Unit = {
    val alloc = stackalloc[CStruct4[Byte, Short, Int, Long]]
    val struct = !alloc
    val ptr = alloc.asInstanceOf[Ptr[Byte]]

    assertTrue(ptr == struct.at1)
    assertTrue(ptr + 2 == struct.at2)
    assertTrue(ptr + 4 == struct.at3)
    assertTrue(ptr + 8 == struct.at4)
  }

  @Test def applyUpdate(): Unit = {
    val alloc = stackalloc[CStruct4[Int, Int, Int, Int]]
    val struct = !alloc
    val ptr = alloc.asInstanceOf[Ptr[Int]]

    struct._1 = 1
    assertTrue(struct._1 == 1)
    assertTrue(ptr(0) == 1)
    ptr(0) = 10
    assertTrue(struct._1 == 10)
    assertTrue(ptr(0) == 10)

    struct._2 = 2
    assertTrue(struct._2 == 2)
    assertTrue(ptr(1) == 2)
    ptr(1) = 20
    assertTrue(struct._2 == 20)
    assertTrue(ptr(1) == 20)

    struct._3 = 3
    assertTrue(struct._3 == 3)
    assertTrue(ptr(2) == 3)
    ptr(2) = 30
    assertTrue(struct._3 == 30)
    assertTrue(ptr(2) == 30)

    struct._4 = 4
    assertTrue(struct._4 == 4)
    assertTrue(ptr(3) == 4)
    ptr(3) = 40
    assertTrue(struct._4 == 40)
    assertTrue(ptr(3) == 40)
  }
}
