package scala.scalanative.runtime.gc

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils

import scala.scalanative.libc.stdlib.{malloc, free}
import scala.scalanative.libc.string.memset
import scala.scalanative.runtime.toRawPtr
import scala.scalanative.runtime.GC
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsigned._
import scala.scalanative.unsafe._

class CustomGCRootsTest {
  import CustomGCRootsTest._
  @Test def `can mark objects referenced from non GC managed memory regions`()
      : Unit = {
    case class Node(var v: Int, var next: Node)
    val sizeOfNode = sizeof[Node]
    // It should be take at least 12 bytes on 32bit, or 20 bytes on 64bit arch
    assert(sizeOfNode.toInt > 8)
    val zone = new CustomGCRootsTest.Zone(10.toUSize * sizeOfNode)

    @noinline def allocNode() = zone.alloc(sizeOfNode, classOf[Node])
    def leafValue = 42

    @noinline def allocNodes(): Node = {
      // Make sure allocated objects would not be reachable by traversing stack
      // Top level node in zone
      val x = allocNode()
      x.v = 1
      x.next = {
        // Inner node in the zone
        val y = allocNode()
        y.v = 2
        y.next = {
          // List of Inner object allocated on the heap
          // reachable only through the memory allocated using the zone
          val local = new Node(leafValue, next = null)
          val local2 = new Node(43, next = local)
          val local3 = new Node(44, next = local2)
          /* Bug workaround:
           * Objects not allocted using the `new` operator, and though not executing their constructor might be assumed to never use their accessors
           * Becouse of that if accessors are never used the underlying field might be treated as unreachable. This would further lead to not including it in the final
           * memory layout of the class.
           * Make sure to use at least accessors of all the fields at least once for memory allocated on the heap using `new` operator
           */
          val _ = local3.toString()
          assertEquals(leafValue, local.v)
          assertEquals(44, local3.v)
          assertEquals(local2, local3.next)

          /*y.next=*/
          local3
        }
        /*x.next=*/
        y
      }

      // Allocate additional objects to move cursor away from the last object pointing to memory on the heap
      Seq.fill(8)(allocNode())
      // Sanity check - make sure the zone cannot allocate more memory then the amount passed in it's ctor
      org.junit.Assert.assertThrows(
        classOf[OutOfMemoryError],
        () => allocNode()
      )

      x
    }

    // head is the object allocated in the zone, its childs can point to memory allocated using GC on the heap
    try {
      val head = allocNodes()
      assertNotSame(head, head.next)

      for {
        iteration <- 0 until 5
        _ = Seq.fill(50)(genGarbage())
        // Make sure leaf node (allocated on the heap using GC is reachable)
        _ <- 0 until 20
      } {
        System.gc()
        var local = new Node(-1, head)
        while (local.v != leafValue) {
          assertNotNull(local.next)
          local = local.next
        }
      }
    } finally zone.close()
  }
}

object CustomGCRootsTest {
  private def genGarbage(): AnyRef = scala.util.Random.alphanumeric
    .take(128)
    .map(_.toUpper)
    .mkString
    .take(10)

  private class Zone(size: CSize) {
    private var cursor = {
      val chunk = malloc(size)
      memset(chunk, 0, size)
      chunk
    }
    private val start = cursor
    private val limit = cursor + size
    // Notify the GC about the range of created zone
    GC.addRoots(cursor, limit)

    def close(): Unit = {
      // Notify the GC about removal of the zone
      GC.removeRoots(start, limit)
      memset(start, 0, size)
      free(start)
    }

    def alloc[T](size: CSize, cls: Class[T]): T = {
      val ptr = cursor
      cursor += size
      if (cursor.toLong > limit.toLong)
        throw new OutOfMemoryError()

      !(ptr.asInstanceOf[Ptr[Class[_]]]) = cls
      castRawPtrToObject(toRawPtr(ptr)).asInstanceOf[T]
    }
  }

}
