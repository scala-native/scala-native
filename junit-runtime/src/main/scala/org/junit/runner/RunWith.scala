/*
 * Ported from https://github.com/junit-team/junit
 */
package org.junit.runner

import java.lang.annotation.*

class RunWith(value: Class[? <: Runner])
    extends scala.annotation.StaticAnnotation
    with Annotation {

  override def annotationType(): Class[? <: Annotation] = classOf[RunWith]
}
