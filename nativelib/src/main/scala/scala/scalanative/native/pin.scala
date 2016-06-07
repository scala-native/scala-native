package scala.scalanative
package native

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter, setter}

/** An annotation that is used to indicate that given field
 *  or method should never be eliminated during link-time
 *  whole-program dead code elimination as long as enclosing
 *  class or object is reachable.
 */
@field
@getter
@setter
final class pin extends StaticAnnotation
