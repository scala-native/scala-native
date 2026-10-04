package org.scalanative.testsuite.javalib.io

import java.lang.{Boolean as JBoolean, StackTraceElement}
import java.math.MathContext
import java.util.regex.Pattern

import org.junit.Assert.*
import org.junit.Test

class SerializableUnionTest {
  private case class Box(value: Int)

  private def same[A](value: A): A = value

  @Test def genericResultsCastToUnionErasure(): Unit = {
    val boolean = JBoolean.TRUE
    val booleanUnion: JBoolean | Box = same(boolean)
    assertSame(boolean, booleanUnion)

    val element = new StackTraceElement("Example", "method", "Example.scala", 1)
    val elementUnion: StackTraceElement | Box = same(element)
    assertSame(element, elementUnion)

    val context = MathContext.DECIMAL64
    val contextUnion: MathContext | Box = same(context)
    assertSame(context, contextUnion)

    val pattern = Pattern.compile("a")
    val patternUnion: Pattern | Box = same(pattern)
    assertSame(pattern, patternUnion)
  }
}
