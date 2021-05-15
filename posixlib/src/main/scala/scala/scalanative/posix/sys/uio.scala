package scala.scalanative
package posix
package sys

import scalanative.unsafe._

@extern
object uio {
  type iovec = CStruct2[Ptr[Byte], // iov_base
                        CSize] // iov_len

  def readv(d: CInt, buf: Ptr[iovec], iovcnt: CInt): CSSize = extern

  def writev(fildes: CInt, iov: Ptr[iovec], iovcnt: CInt): CSSize = extern
}
