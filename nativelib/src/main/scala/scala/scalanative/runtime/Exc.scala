package scala.scalanative
package runtime

import native._

@struct class Exc(exc: Ptr[_], typeid: Int)
