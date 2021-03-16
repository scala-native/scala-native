package scala.scalanative.unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter}
import scala.scalanative.runtime.intrinsic

@field @getter
class linktimeResolved(fromProperty: String = intrinsic,
                       fromEnv: String = intrinsic)
    extends StaticAnnotation
