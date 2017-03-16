package scala.scalanative
package posix.sys

import native.{CSize, Ptr, CStruct2, extern}

/**
 * Created by remi on 02/03/17.
 */
@extern
object uio {

  // Types
  type iovec = CStruct2[Ptr[Byte], CSize]

}
