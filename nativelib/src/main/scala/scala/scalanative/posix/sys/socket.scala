package scala.scalanative
package posix.sys

import native.Nat._
import native._
import native.time.timespec
import types.ssize_t
import uio.iovec

// http://man7.org/linux/man-pages/man7/socket.7.html

@extern
object socket {

  def socket(domain: CInt, _type: CInt, protocol: CInt): CInt = extern
  def socketpair(domain: CInt,
                 _type: CInt,
                 protocol: CInt,
                 sv: CArray[CInt, _2]): CInt = extern
  def bind(sockfd: CInt, addr: Ptr[sockaddr], addrlen: socklen_t): CInt =
    extern
  def getsockname(sockfd: CInt,
                  addr: Ptr[sockaddr],
                  addrlen: Ptr[socklen_t]): CInt = extern
  def connect(sockfd: CInt, addr: Ptr[sockaddr], addrlen: socklen_t): CInt =
    extern
  def getpeername(sockfd: CInt,
                  addr: Ptr[sockaddr],
                  addrlen: Ptr[socklen_t]): CInt = extern
  def send(sockfd: CInt, buf: Ptr[Byte], len: CSize, flags: CInt): ssize_t =
    extern
  def sendto(sockfd: CInt,
             buf: Ptr[Byte],
             len: CSize,
             flags: CInt,
             dest_addr: Ptr[sockaddr],
             addrlen: socklen_t): ssize_t                           = extern
  def sendmsg(sockfd: CInt, msg: Ptr[msghdr], flags: CInt): ssize_t = extern
  def sendmmsg(sockfd: CInt,
               msgvec: Ptr[mmsghdr],
               vlen: CUnsignedInt,
               flags: CUnsignedInt): CInt = extern
  def recv(sockfd: CInt, buf: Ptr[Byte], len: CSize, flags: CInt): ssize_t =
    extern
  def recvfrom(sockfd: CInt,
               buf: Ptr[Byte],
               len: CSize,
               flags: CInt,
               src_addr: Ptr[sockaddr],
               addrlen: socklen_t): ssize_t                         = extern
  def recvmsg(sockfd: CInt, msg: Ptr[msghdr], flags: CInt): ssize_t = extern
  def recvmmsg(sockfd: CInt,
               msgvec: Ptr[mmsghdr],
               vlen: CUnsignedInt,
               flags: CUnsignedInt,
               timeout: Ptr[timespec]): CInt = extern
  def getsockopt(sockfd: CInt,
                 level: CInt,
                 optname: CInt,
                 optval: Ptr[Byte],
                 optlen: Ptr[socklen_t]): CInt = extern
  def setsockopt(sockfd: CInt,
                 level: CInt,
                 optname: CInt,
                 optval: Ptr[Byte],
                 optlen: socklen_t): CInt       = extern
  def listen(sockfd: CInt, backlog: CInt): CInt = extern
  def accept(sockfd: CInt, addr: Ptr[sockaddr], addlen: Ptr[socklen_t]): CInt =
    extern
  def accept4(sockfd: CInt,
              addr: Ptr[sockaddr],
              addlen: Ptr[socklen_t],
              flags: CInt): CInt              = extern
  def shutdown(sockfd: CInt, how: CInt): CInt = extern

  // Types
  type socklen_t   = CInt
  type sa_family_t = CUnsignedInt
  type sockaddr    = CStruct2[sa_family_t, CArray[CChar, Digit[_1, _4]]]
  type msghdr = CStruct7[Ptr[Byte],
                         socklen_t,
                         Ptr[iovec],
                         CInt,
                         Ptr[Byte],
                         socklen_t,
                         CInt]
  type mmsghdr = CStruct2[msghdr, CUnsignedInt]

  //Macros
  @name("scalanative_af_unix")
  def AF_UNIX: CInt = extern
  @name("scalanative_af_local")
  def AF_LOCAL: CInt = extern
  @name("scalanative_af_inet")
  def AF_INET: CInt = extern
  @name("scalanative_af_inet6")
  def AF_INET6: CInt = extern
  @name("scalanative_af_ipx")
  def AF_IPX: CInt = extern
  @name("scalanative_af_netlink")
  def AF_NETLINK: CInt = extern
  @name("scalanative_af_x25")
  def AF_X25: CInt = extern
  @name("scalanative_af_ax25")
  def AF_AX25: CInt = extern
  @name("scalanative_af_atmpvc")
  def AF_ATMPVC: CInt = extern
  @name("scalanative_af_appletalk")
  def AF_APPLETALK: CInt = extern
  @name("scalanative_af_packet")
  def AF_PACKET: CInt = extern
  @name("scalanative_af_alg")
  def AF_ALG: CInt = extern
  @name("scalanative_sock_stream")
  def SOCK_STREAM: CInt = extern
  @name("scalanative_sock_dgram")
  def SOCK_DGRAM: CInt = extern
  @name("scalanative_sock_seqpacket")
  def SOCK_SEQPACKET: CInt = extern
  @name("scalanative_sock_raw")
  def SOCK_RAW: CInt = extern
  @name("scalanative_sock_rdm")
  def SOCK_RDM: CInt = extern
  @name("scalanative_sock_packet")
  def SOCK_PACKET: CInt = extern
  @name("scalanative_sock_nonblock")
  def SOCK_NONBLOCK: CInt = extern
  @name("scalanative_sock_cloexec")
  def SOCK_CLOEXEC: CInt = extern
  @name("scalanative_sock_sock_ee_offender")
  def SOCK_EE_OFFENDER: CInt = extern
  @name("scalanative_msg_confirm")
  def MSG_CONFIRM: CInt = extern
  @name("scalanative_msg_dontroute")
  def MSG_DONTROUTE: CInt = extern
  @name("scalanative_msg_dontwait")
  def MSG_DONTWAIT: CInt = extern
  @name("scalanative_msg_eor")
  def MSG_EOR: CInt = extern
  @name("scalanative_msg_more")
  def MSG_MORE: CInt = extern
  @name("scalanative_msg_nosignal")
  def MSG_NOSIGNAL: CInt = extern
  @name("scalanative_msg_oob")
  def MSG_OOB: CInt = extern
  @name("scalanative_msg_cmsg_cloexec")
  def MSG_CMSG_CLOEXEC: CInt = extern
  @name("scalanative_msg_errqueue")
  def MSG_ERRQUEUE: CInt = extern
  @name("scalanative_msg_peek")
  def MSG_PEEK: CInt = extern
  @name("scalanative_msg_trunc")
  def MSG_TRUNC: CInt = extern
  @name("scalanative_msg_waitall")
  def MSG_WAITALL: CInt = extern
  @name("scalanative_msg_ctrunc")
  def MSG_CTRUNC: CInt = extern
  @name("scalanative_msg_waitforone")
  def MSG_WAITFORONE: CInt = extern
}
