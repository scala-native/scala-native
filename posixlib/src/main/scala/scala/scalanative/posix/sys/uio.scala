package scala.scalanative
package posix
package sys

import scalanative.native._

@extern
object uio {
  type iovec = CStruct2[Ptr[Byte], // iov_base
                        CSize] // iov_len

  @name("scalanative_readv")
  def readv(d: CInt, buf: Ptr[iovec], iovcnt: CInt): CSSize = extern

  @name("scalanative_writev")
  def writev(fildes: CInt, iov: Ptr[iovec], iovcnt: CInt): CSSize = extern

}
