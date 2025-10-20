package org.junit

import java.lang.annotation.*

class Before
    extends scala.annotation.StaticAnnotation
    with java.lang.annotation.Annotation {
  def annotationType(): Class[? <: Annotation] = classOf[Before]
}
