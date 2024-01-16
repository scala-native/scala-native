package org.scalanative.testsuite.javalib.nio

import java.nio._

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.Platform._

// Extended Scala Native API
import scala.scalanative.memory.PointerBuffer
import scala.scalanative.memory.PointerBufferOps._

trait ScalaNativeBufferTests { self: BaseBufferTest =>
  // TODO: It should not be shared with JVM, but it's required due to BaseBuffer coupling with JDK8

  import factory._
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
