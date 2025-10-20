package org.scalanative.testsuite.niobuffer

import scala.scalanative.unsafe.*
import scala.scalanative.memory.PointerBuffer
import java.nio.*

import org.scalanative.testsuite.javalib.nio.BufferFactory.{
  ByteBufferFactory,
  WrappedPointerBufferFactory
}

object ByteBufferNativeFactories {
  class WrappedPointerByteBufferFactory
      extends ByteBufferFactory
      with WrappedPointerBufferFactory {
    override val createsPointerBuffer: Boolean = true

    def baseWrap(array: Array[Byte]): ByteBuffer = {
      val buf = PointerBuffer.wrap(array.atUnsafe(0), array.length)
      buf.order(ByteOrder.BIG_ENDIAN)
      buf
    }
  }
}
