package org.scalanative.testsuite.javalib.nio

import java.nio._

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.memory.PointerBuffer
import scala.scalanative.memory.PointerBufferOps._

trait BaseBufferPlatformTest { self: BaseBufferTest =>
  import factory._

  // Extended Scala Native API
  @Test def hasPointer(): Unit = {
    val buf = factory.allocBuffer(8)
    if (createsReadOnly)
      assertFalse("read-only, access to pointer", buf.hasPointer())
    else assertEquals("hasPointer", createsPointerBuffer, buf.hasPointer())
  }

  @Test def getPointer(): Unit = {
    val buf = factory.allocBuffer(8)
    if (createsReadOnly || !createsPointerBuffer)
      assertThrows(
        classOf[UnsupportedOperationException],
        () => buf.pointer()
      )
    else assertNotNull(buf.pointer())
  }
}
