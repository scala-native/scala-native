package scala.scalanative
package posix
package sys

import scalanative.native._

@extern
object socket {
  type socklen_t   = CUnsignedInt
  type sa_family_t = CUnsignedShort
  type _14         = Nat.Digit[Nat._1, Nat._4]
  type sockaddr =
    CStruct2[sa_family_t, // sa_family
             CArray[CChar, _14]] // sa_data, size = 14 in OS X and Linux
  type sockaddr_storage = CStruct1[sa_family_t] // ss_family
  type msghdr = CStruct7[Ptr[Byte], // msg_name
                         socklen_t, // msg_namelen
                         Ptr[uio.iovec], // msg_iov
                         CInt, // msg_iovlen
                         Ptr[Byte], // msg_control
                         socklen_t, // msg_crontrollen
                         CInt] // msg_flags
  type cmsghdr = CStruct3[socklen_t, // cmsg_len
                          CInt, // cmsg_level
                          CInt] // cmsg_type
  type iovec = uio.iovec
  type linger = CStruct2[CInt, // l_onoff
                         CInt] // l_linger

  @name("scalanative_SCM_RIGHTS")
  def SCM_RIGHTS: CInt = extern

  @name("scalanative_SOCK_DGRAM")
  def SOCK_DGRAM: CInt = extern

  @name("scalanative_SOCK_RAW")
  def SOCK_RAW: CInt = extern

  @name("scalanative_SOCK_SEQPACKET")
  def SOCK_SEQPACKET: CInt = extern

  @name("scalanative_SOCK_STREAM")
  def SOCK_STREAM: CInt = extern

  @name("scalanative_SOL_SOCKET")
  def SOL_SOCKET: CInt = extern

  @name("scalanative_SO_ACCEPTCONN")
  def SO_ACCEPTCONN: CInt = extern

  @name("scalanative_SO_BROADCAST")
  def SO_BROADCAST: CInt = extern

  @name("scalanative_SO_DEBUG")
  def SO_DEBUG: CInt = extern

  @name("scalanative_SO_DONTROUTE")
  def SO_DONTROUTE: CInt = extern

  @name("scalanative_SO_ERROR")
  def SO_ERROR: CInt = extern

  @name("scalanative_SO_KEEPALIVE")
  def SO_KEEPALIVE: CInt = extern

  @name("scalanative_SO_LINGER")
  def SO_LINGER: CInt = extern

  @name("scalanative_SO_OOBINLINE")
  def SO_OOBINLINE: CInt = extern

  @name("scalanative_SO_RCVBUF")
  def SO_RCVBUF: CInt = extern

  @name("scalanative_SO_RCVLOWAT")
  def SO_RCVLOWAT: CInt = extern

  @name("scalanative_SO_RCVTIMEO")
  def SO_RCVTIMEO: CInt = extern

  @name("scalanative_SO_REUSEADDR")
  def SO_REUSEADDR: CInt = extern

  @name("scalanative_SO_SNDBUF")
  def SO_SNDBUF: CInt = extern

  @name("scalanative_SO_SNDLOWAT")
  def SO_SNDLOWAT: CInt = extern

  @name("scalanative_SO_SNDTIMEO")
  def SO_SNDTIMEO: CInt = extern

  @name("scalanative_SO_TYPE")
  def SO_TYPE: CInt = extern

  @name("scalanative_SOMAXCONN")
  def SOMAXCONN: CInt = extern

  @name("scalanative_MSG_CTRUNC")
  def MSG_CTRUNC: CInt = extern

  @name("scalanative_MSG_DONTROUTE")
  def MSG_DONTROUTE: CInt = extern

  @name("scalanative_MSG_EOR")
  def MSG_EOR: CInt = extern

  @name("scalanative_MSG_OOB")
  def MSG_OOB: CInt = extern

  // Surprisingly, this doesn't exist on MacOS
  // @name("scalanative_MSG_NOSIGNAL")
  // def MSG_NOSIGNAL: CInt = extern

  @name("scalanative_MSG_PEEK")
  def MSG_PEEK: CInt = extern

  @name("scalanative_MSG_TRUNC")
  def MSG_TRUNC: CInt = extern

  @name("scalanative_MSG_WAITALL")
  def MSG_WAITALL: CInt = extern

  @name("scalanative_AF_INET")
  def AF_INET: CInt = extern

  @name("scalanative_AF_INET6")
  def AF_INET6: CInt = extern

  @name("scalanative_AF_UNIX")
  def AF_UNIX: CInt = extern

  @name("scalanative_AF_UNSPEC")
  def AF_UNSPEC: CInt = extern

  @name("scalanative_socket")
  def socket(domain: CInt, tpe: CInt, protocol: CInt): CInt = extern

  @name("scalanative_connect")
  def connect(socket: CInt,
              address: Ptr[sockaddr],
              address_len: socklen_t): CInt = extern

  @name("scalanative_bind")
  def bind(socket: CInt,
           address: Ptr[sockaddr],
           address_len: socklen_t): CInt = extern

  @name("scalanative_listen")
  def listen(socket: CInt, backlog: CInt): CInt = extern

  @name("scalanative_accept")
  def accept(socket: CInt,
             address: Ptr[sockaddr],
             address_len: Ptr[socklen_t]): CInt = extern

  @name("scalanative_setsockopt")
  def setsockopt(socket: CInt,
                 level: CInt,
                 option_name: CInt,
                 options_value: Ptr[Byte],
                 option_len: socklen_t): CInt = extern

  @name("scalanative_recv")
  def recv(socket: CInt,
           buffer: Ptr[Byte],
           length: CSize,
           flags: CInt): CSSize = extern

  @name("scalanative_send")
  def send(socket: CInt,
           buffer: Ptr[Byte],
           length: CSize,
           flags: CInt): CSSize = extern
}

object socketOps {
  import socket._

  implicit class sockaddrOps(val ptr: Ptr[sockaddr]) extends AnyVal {
    def sa_family: sa_family_t      = !ptr._1
    def sa_data: CArray[CChar, _14] = !ptr._2

    def sa_family_=(v: sa_family_t): Unit      = !ptr._1 = v
    def sa_data_=(v: CArray[CChar, _14]): Unit = !ptr._2 = v
  }

  implicit class sockaddr_storageOps(val ptr: Ptr[sockaddr_storage])
      extends AnyVal {
    def ss_family: sa_family_t            = !ptr._1
    def ss_family_=(v: sa_family_t): Unit = !ptr._1 = v
  }

  implicit class msghdrOps(val ptr: Ptr[msghdr]) extends AnyVal {
    def msg_name: Ptr[Byte]       = !ptr._1
    def msg_namelen: socklen_t    = !ptr._2
    def msg_iov: Ptr[uio.iovec]   = !ptr._3
    def msg_iovlen: CInt          = !ptr._4
    def msg_control: Ptr[Byte]    = !ptr._5
    def msg_controllen: socklen_t = !ptr._6
    def msg_flags: CInt           = !ptr._7

    def msg_name_=(v: Ptr[Byte]): Unit       = !ptr._1 = v
    def msg_namelen_=(v: socklen_t): Unit    = !ptr._2 = v
    def msg_iov_=(v: Ptr[uio.iovec]): Unit   = !ptr._3 = v
    def msg_iovlen_=(v: CInt): Unit          = !ptr._4 = v
    def msg_control_=(v: Ptr[Byte]): Unit    = !ptr._5 = v
    def msg_controllen_=(v: socklen_t): Unit = !ptr._6 = v
    def msg_flags_=(v: CInt): Unit           = !ptr._7 = v
  }

  implicit class cmsghdrOps(val ptr: Ptr[cmsghdr]) extends AnyVal {
    def cmsg_len: socklen_t = !ptr._1
    def cmsg_level: CInt    = !ptr._2
    def cmsg_type: CInt     = !ptr._3

    def cmsg_len_=(v: socklen_t): Unit = !ptr._1 = v
    def cmsg_level_=(v: CInt): Unit    = !ptr._2 = v
    def cmsg_type_=(v: CInt): Unit     = !ptr._3 = v
  }

  implicit class lingerOps(val ptr: Ptr[linger]) extends AnyVal {
    def l_onoff: CInt  = !ptr._1
    def l_linger: CInt = !ptr._2

    def l_onoff_=(v: CInt): Unit  = !ptr._1 = v
    def l_linger_=(v: CInt): Unit = !ptr._2 = v
  }

}
