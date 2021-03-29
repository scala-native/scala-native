package scala.scalanative.unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter}
import scala.scalanative.runtime.intrinsic

/** Used to annotate that given value should be resolved at link-time,
 *  based on provided `withName` parameter or on fully-qualified name otherwise */
@field @getter
private[scalanative] class resolvedAtLinktime(withName: String = intrinsic)
    extends StaticAnnotation
