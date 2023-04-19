package scala.scalanative.unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter}

/** Used to annotate that given value should be resolved at link-time, based on
 *  provided `withName` parameter
 */
@field @getter
private[scalanative] class resolvedAtLinktime(withName: String)
    extends StaticAnnotation
