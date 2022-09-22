package scala.scalanative
package posix

import scala.scalanative.unsafe._

@extern
object locale {

  /** This file/object is a less-than-minimal implementation. It provides only
   *  the type local_t. This allows a common definition of the type to be used
   *  by the several files required by POSIX to define that type.
   */

  type locale_t = Ptr[Byte]

}
