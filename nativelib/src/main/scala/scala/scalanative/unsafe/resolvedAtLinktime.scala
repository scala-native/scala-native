package scala.scalanative.unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter}

/** Used to annotate methods which should be evaluated in linktime, allowing to
 *  remove unused paths and symbols, e.g. whe cross compiling for different OS
 *  Annotated methods needs to operate only on literal values, other methods
 *  with this annotation.
 */
@field @getter
class resolvedAtLinktime() extends StaticAnnotation {

  /** Used to annotate that given value should be resolved at link-time, based
   *  on provided `withName` parameter provided by the build tool.
   */
  def this(withName: String) = this()
}
