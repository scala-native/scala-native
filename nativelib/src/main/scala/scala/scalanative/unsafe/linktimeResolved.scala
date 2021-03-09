package scala.scalanative.unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter}
import scala.scalanative.runtime.intrinsic
import scala.scalanative.unsafe.linktimeResolved.generate

@field @getter
class linktimeResolved(fromProperty: String = generate,
                       fromEnv: String = generate)
    extends StaticAnnotation

object linktimeResolved {
  def generate: String = intrinsic
  def disabled: String = null
}

class resolveAtLinktime() extends StaticAnnotation
