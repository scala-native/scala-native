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
