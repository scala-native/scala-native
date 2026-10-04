package org.scalanative.testsuite.javalib.util.stream

import java.util.function.Consumer
import java.util.stream.Stream

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StreamBuilderConsumerTest {
  @Test def builderAsConsumer(): Unit = {
    val builder = Stream.builder[String]()
    val consumer = (builder: AnyRef).asInstanceOf[Consumer[String]]
    var observed: String = null

    consumer.accept(null)
    consumer.andThen((value: String) => observed = value).accept("value")

    val stream = builder.build()
    try {
      assertArrayEquals(Array[Object](null, "value"), stream.toArray())
      assertEquals("value", observed)
    } finally stream.close()

    assertThrows(classOf[IllegalStateException], consumer.accept("late"))
  }
}
