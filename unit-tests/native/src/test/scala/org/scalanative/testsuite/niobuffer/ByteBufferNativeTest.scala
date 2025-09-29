package org.scalanative.testsuite.niobuffer

import org.scalanative.testsuite.javalib.nio.BufferFactory.ByteBufferFactory
import org.scalanative.testsuite.javalib.nio.ByteBufferTest

import scala.scalanative.memory._

class PointerByteBufferTest extends ByteBufferTest {
  val factory: ByteBufferFactory =
    new ByteBufferNativeFactories.WrappedPointerByteBufferFactory
}
