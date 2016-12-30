package scala.scalanative
package native

import scala.language.implicitConversions
import runtime.{undefined, Tag}

/** Type of a C-style vararg in an extern method. */
final class Vararg private ()

object Vararg {
  implicit def apply[T](value: T)(implicit tag: Tag[T]): Vararg = undefined
}
