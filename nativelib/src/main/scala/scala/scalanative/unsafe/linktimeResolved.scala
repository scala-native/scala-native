package scala.scalanative.unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter}
import scala.scalanative.runtime.intrinsic

@field @getter
class linktimeResolved(withName: String = intrinsic) extends StaticAnnotation
