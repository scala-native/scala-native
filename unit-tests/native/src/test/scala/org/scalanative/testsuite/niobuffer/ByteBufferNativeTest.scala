package org.scalanative.testsuite.niobuffer

import scala.scalanative.memory._

import org.scalanative.testsuite.javalib.nio.BufferFactory.ByteBufferFactory
import org.scalanative.testsuite.javalib.nio.ByteBufferTest

class PointerByteBufferTest extends ByteBufferTest {
  val factory: ByteBufferFactory = new ByteBufferNativeFactories.WrappedPointerByteBufferFactory
}
