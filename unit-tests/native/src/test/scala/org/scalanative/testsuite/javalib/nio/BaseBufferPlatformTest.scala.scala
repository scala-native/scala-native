package org.scalanative.testsuite.javalib.nio

import java.nio._

// Ported from Scala.js

import org.junit.Test
import org.junit.Assert._

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
