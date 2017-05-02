package scala.scalanative
package posix.sys

import native.{CSize, Ptr, CStruct2, extern}

@extern
object uio {

  // Types
  type iovec = CStruct2[Ptr[Byte], CSize]

}
