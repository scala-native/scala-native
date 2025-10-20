package org.junit

import java.lang.annotation.*

trait ClassRule extends Annotation {
  def annotationType(): Class[? <: Annotation] = classOf[ClassRule]
}
