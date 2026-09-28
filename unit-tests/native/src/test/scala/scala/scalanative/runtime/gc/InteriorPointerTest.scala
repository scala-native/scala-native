package scala.scalanative.runtime.gc

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.unsafe._

class InteriorPointerTest {
  @Test def boxedInteriorPointersKeepArraysAlive(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val size = 2048
    val pointers = new Array[Ptr[Byte]](16)
    val allocator = new Thread(new Runnable {
      def run(): Unit = {
        var offset = 0
        while (offset < pointers.length) {
          val bytes = Array.fill[Byte](size)(90.toByte)
          pointers(offset) = bytes.at(offset)
          offset += 1
        }
      }
    })
    allocator.start()
    allocator.join()

    var round = 0
    while (round < 3) {
      System.gc()
      round += 1
    }

    var offset = 0
    while (offset < pointers.length) {
      var index = 0
      while (index < size - offset) {
        assertEquals(
          s"offset=$offset index=$index",
          90,
          pointers(offset)(index).toInt
        )
        index += 1
      }
      offset += 1
    }
  }
}
