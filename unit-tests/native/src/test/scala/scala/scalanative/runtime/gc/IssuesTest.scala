package scala.scalanative.runtime.gc

import org.junit.Assert._
import org.junit.{Assume, Test}

import scala.scalanative.junit.utils.AssumesHelper

class IssueTests {

  // Commix GC crash on huge ref array
  @Test def issue4445(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    AssumesHelper.assumeNot32Bit()

    val size = 0x01ffffff
    val arr = new Array[AnyRef](size)
    val stride = 65536
    var i = 0
    while (i < size) {
      arr(i) = "foo"
      i += stride
    }
    arr(size / 2) = "bar"
    arr(size - 1) = "baz"
    System.gc()
    assertEquals("bar", arr(size / 2))
    assertEquals("baz", arr(size - 1))
  }
}
