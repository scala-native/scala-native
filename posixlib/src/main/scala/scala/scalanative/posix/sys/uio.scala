package scala.scalanative
package posix
package sys

import scalanative.unsafe._

@extern
object uio {
  type iovec = CStruct2[
    Ptr[Byte], // iov_base
    CSize // iov_len
  ]

  @blocking def readv(d: CInt, buf: Ptr[iovec], iovcnt: CInt): CSSize = extern

  @blocking def writev(fildes: CInt, iov: Ptr[iovec], iovcnt: CInt): CSSize =
    extern
}

object uioOps {
  import uio.iovec

  implicit class iovecOps(val ptr: Ptr[iovec]) extends AnyVal {
    def iov_base: Ptr[Byte] = ptr._1
    def iov_len: CSize = ptr._2
    def iov_base_=(v: Ptr[Byte]): Unit = ptr._1 = v
    def iov_len_=(v: CSize): Unit = ptr._2 = v
  }

  implicit class iovecValOps(val vec: iovec) extends AnyVal {
    def iov_base: Ptr[Byte] = vec._1
    def iov_len: CSize = vec._2
    def iov_base_=(v: Ptr[Byte]): Unit = vec._1 = v
    def iov_len_=(v: CSize): Unit = vec._2 = v
  }
}
