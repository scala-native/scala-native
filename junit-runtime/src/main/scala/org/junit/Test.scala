/*
 * Ported from https://github.com/junit-team/junit
 */
package org.junit

import java.lang.annotation.*

class Test(val expected: Class[? <: Throwable], val timeout: Long)
    extends scala.annotation.StaticAnnotation
    with Annotation {

  def this(expected: Class[? <: Throwable]) = this(expected, 0L)
  def this(timeout: Long) = this(classOf[Test.None], timeout)
  def this() = this(0L)

  def annotationType(): Class[? <: Annotation] =
    classOf[Test]
}

object Test {
  final class None private () extends Throwable
}
