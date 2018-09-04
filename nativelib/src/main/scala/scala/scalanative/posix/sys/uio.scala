package scala.scalanative
package posix
package sys

import scalanative.native._

@extern
object uio {
  type iovec = CStruct2[Ptr[Byte], // iov_base
                        CSize] // iov_len

  def readv(d: CInt, buf: Ptr[iovec], iovcnt: CInt): CSSize = extern

  def writev(fildes: CInt, iov: Ptr[iovec], iovcnt: CInt): CSSize = extern

}

object uioOps {
  import uio._

  implicit class iovecOps(val ptr: Ptr[iovec]) extends AnyVal {
    def iov_base: Ptr[Byte] = !ptr._1
    def iov_len: CSize      = !ptr._2

    def iov_base_=(v: Ptr[Byte]): Unit = !ptr._1 = v
    def iov_len_=(v: CSize): Unit      = !ptr._2 = v
  }
}
