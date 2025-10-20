/*
 * Ported from https://github.com/junit-team/junit
 */
package org.junit

import java.lang.annotation.*

class AfterClass
    extends scala.annotation.StaticAnnotation
    with java.lang.annotation.Annotation {
  def annotationType(): Class[? <: Annotation] = classOf[AfterClass]
}
