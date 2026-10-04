package org.scalanative.testsuite.javalib.io

import java.io.Serializable
import java.lang.{Boolean => JBoolean, StackTraceElement}
import java.math.MathContext
import java.util.regex.Pattern

import org.junit.Assert._
import org.junit.Test

class SerializableTest {
  // Keep the argument erased to Object, so the test exercises the runtime cast
  // that also occurs when a compiler chooses Serializable as a union's erasure.
  private def check(value: AnyRef): Unit = {
    assertTrue(value.isInstanceOf[Serializable])
    assertSame(value, value.asInstanceOf[Serializable])
    assertTrue(classOf[Serializable].isAssignableFrom(value.getClass()))
  }

  @Test def boxedBoolean(): Unit = check(JBoolean.TRUE)

  @Test def stackTraceElement(): Unit =
    check(new StackTraceElement("Example", "method", "Example.scala", 1))

  @Test def mathContext(): Unit = check(MathContext.DECIMAL64)

  @Test def pattern(): Unit = check(Pattern.compile("a"))
}
