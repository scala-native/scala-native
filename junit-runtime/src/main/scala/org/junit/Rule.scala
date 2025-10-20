package org.junit

import java.lang.annotation.*

trait Rule extends Annotation {
  def annotationType(): Class[? <: Annotation] = classOf[Rule]
}
