package scala.scalanative
package unsafe

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.unsigned._

/* A quick run through of the package scope 'alloc' and 'calloc'
 * methods. 'alloc' especially is used in a number of other Tests.
 * 
 * Exercise the implementation introduced to fix SN Issue #4700
 */
/* Exercise the 'alloc' and 'calloc' implementations introduced to
 * fix SN Issue #4700.
 * 
 * 'alloc', especially, is used in a number of other Tests. Check against
 * its memory clearing semantics ever changing.
 */

class UnsafePackageTest {

  final val maxSize = 10

  @Test def packageAllocZerosMemory(): Unit = {
    Zone.acquire { implicit z =>
      val maxSize = 10

      locally {
        val ptr: Ptr[Int] = alloc[Int]()
        assertEquals("alloc()", 0, !ptr)
      }

      locally {
        val ptr: Ptr[Int] = alloc[Int](maxSize.toCSize)
        for (j <- 0 until maxSize)
          assertEquals("alloc(CSize) ${j}", 0, ptr(j))
      }

      locally {
        val ptr: Ptr[Int] = alloc[Int](maxSize)
        for (j <- 0 until maxSize)
          assertEquals("alloc(Int) ${j}", 0, ptr(j))
      }
    }
  }

  @Test def packageCallocZerosMemory(): Unit = {
    Zone.acquire { implicit z =>
      locally {
        val ptr: Ptr[Long] = calloc[Long]()
        assertEquals("calloc()", 0L, !ptr)
      }

      locally {
        val ptr: Ptr[Long] = calloc[Long](maxSize.toCSize)
        for (j <- 0 until maxSize)
          assertEquals("calloc(CSize) ${j}", 0L, ptr(j))
      }

      locally {
        val ptr: Ptr[Long] = calloc[Long](maxSize)
        for (j <- 0 until maxSize)
          assertEquals("calloc(Long) ${j}", 0L, ptr(j))
      }
    }
  }

}
