package scala.scalanative
package native

import scala.language.implicitConversions
import scalanative.runtime.undefined

/** Type of a C-style vararg in an extern method. */
final abstract class CVararg

object CVararg {
  implicit def apply[T](value: T)(implicit tag: Tag[T]): CVararg = undefined
}
