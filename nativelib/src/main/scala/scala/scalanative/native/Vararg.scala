package scala.scalanative
package native

import scala.language.implicitConversions
import runtime.undefined
import reflect.ClassTag

final class Vararg private ()

object Vararg {
  implicit def apply[T](value: T)(implicit ct: ClassTag[T]): Vararg = undefined
}
