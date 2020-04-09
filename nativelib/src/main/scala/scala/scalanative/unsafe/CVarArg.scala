package scala.scalanative
package unsafe

import scala.language.implicitConversions

/** Type of a C-style vararg in an extern method. */
final class CVarArg(val value: Any, val tag: Tag[Any])

object CVarArg {
  implicit def materialize[T: Tag](value: T): CVarArg =
    new CVarArg(value, implicitly[Tag[T]].asInstanceOf[Tag[Any]])
}
