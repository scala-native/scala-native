/*
 * Ported from https://github.com/junit-team/junit
 */
package org.junit

import java.lang.annotation.*

class Ignore(val value: java.lang.String)
    extends scala.annotation.StaticAnnotation
    with java.lang.annotation.Annotation {

  def this() = this("")

  def annotationType(): Class[? <: Annotation] = classOf[Ignore]
}
