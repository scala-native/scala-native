package scala.scalanative.posix.sys

import scala.scalanative.native.Nat._2
import scala.scalanative.native._
import scala.scalanative.posix.sys.types.ssize_t

/**
 * Created by remi on 01/03/17.
 */
@extern
object socket {

  def socket(domain: CInt, _type: CInt, protocol: CInt): CInt = extern
  def socketpair(domain: CInt,
                 _type: CInt,
                 protocol: CInt,
                 sv: CArray[CInt, _2]): CInt = extern
  def bind(sockfd: CInt, addr: Ptr[CStruct2], addrlen: socklen_t): CInt =
    extern
  def getsockname(sockfd: CInt,
                  addr: Ptr[CStruct2],
                  addrlen: Ptr[socklen_t]): CInt = extern
  def connect(sockfd: CInt, addr: Ptr[CStruct2], addrlen: socklen_t): CInt =
    extern
  def getpeername(sockfd: CInt,
                  addr: Ptr[CStruct2],
                  addrlen: Ptr[socklen_t]): CInt = extern
  def send(sockfd: CInt, buf: Ptr[Byte], len: CSize, flags: CInt): ssize_t =
    extern
  def sendto(sockfd: CInt,
             buf: Ptr[Byte],
             len: CSize,
             flags: CInt,
             dest_addr: Ptr[CStruct2],
             addrlen: socklen_t): ssize_t                             = extern
  def sendmsg(sockfd: CInt, msg: Ptr[CStruct7], flags: CInt): ssize_t = extern
  def sendmmsg(sockfd: CInt,
               msgvec: Ptr[CStruct7],
               vlen: CUnsignedInt,
               flags: CUnsignedInt): CInt = extern
  def recv(sockfd: CInt, buf: Ptr[Byte], len: CSize, flags: CInt): ssize_t =
    extern
  def recvfrom(sockfd: CInt,
               buf: Ptr[Byte],
               len: CSize,
               flags: CInt,
               src_addr: Ptr[CStruct2],
               addrlen: socklen_t): ssize_t                           = extern
  def recvmsg(sockfd: CInt, msg: Ptr[CStruct7], flags: CInt): ssize_t = extern
  def recvmmsg(sockfd: CInt,
               msgvec: Ptr[CStruct7],
               vlen: CUnsignedInt,
               flags: CUnsignedInt,
               timeout: Ptr[CStruct2]): CInt = extern
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
  def accept(sockfd: CInt, addr: Ptr[CStruct2], addlen: Ptr[socklen_t]): CInt =
    extern
  def accept4(sockfd: CInt,
              addr: Ptr[CStruct2],
              addlen: Ptr[socklen_t],
              flags: CInt): CInt              = extern
  def shutdown(sockfd: CInt, how: CInt): CInt = extern

  // Types
  type socklen_t = CInt

  //Macros
  @name("scalanative_af_unix")
  def AF_UNIX = extern
  @name("scalanative_af_local")
  def AF_LOCAL = extern
  @name("scalanative_af_inet")
  def AF_INET = extern
  @name("scalanative_af_inet6")
  def AF_INET6 = extern
  @name("scalanative_af_ipx")
  def AF_IPX = extern
  @name("scalanative_af_netlink")
  def AF_NETLINK = extern
  @name("scalanative_af_x25")
  def AF_X25 = extern
  @name("scalanative_af_ax25")
  def AF_AX25 = extern
  @name("scalanative_af_atmpvc")
  def AF_ATMPVC = extern
  @name("scalanative_af_appletalk")
  def AF_APPLETALK = extern
  @name("scalanative_af_packet")
  def AF_PACKET = extern
  @name("scalanative_af_alg")
  def AF_ALG = extern
  @name("scalanative_sock_stream")
  def SOCK_STREAM = extern
  @name("scalanative_sock_dgram")
  def SOCK_DGRAM = extern
  @name("scalanative_sock_seqpacket")
  def SOCK_SEQPACKET = extern
  @name("scalanative_sock_raw")
  def SOCK_RAW = extern
  @name("scalanative_sock_rdm")
  def SOCK_RDM = extern
  @name("scalanative_sock_packet")
  def SOCK_PACKET = extern
  @name("scalanative_sock_nonblock")
  def SOCK_NONBLOCK = extern
  @name("scalanative_sock_cloexec")
  def SOCK_CLOEXEC = extern
  @name("scalanative_sock_sock_ee_offender")
  def SOCK_EE_OFFENDER = extern
  @name("scalanative_msg_confirm")
  def MSG_CONFIRM = extern
  @name("scalanative_msg_dontroute")
  def MSG_DONTROUTE = extern
  @name("scalanative_msg_dontwait")
  def MSG_DONTWAIT = extern
  @name("scalanative_msg_eor")
  def MSG_EOR = extern
  @name("scalanative_msg_more")
  def MSG_MORE = extern
  @name("scalanative_msg_nosignal")
  def MSG_NOSIGNAL = extern
  @name("scalanative_msg_oob")
  def MSG_OOB = extern
  @name("scalanative_msg_cmsg_cloexec")
  def MSG_CMSG_CLOEXEC = extern
  @name("scalanative_msg_errqueue")
  def MSG_ERRQUEUE = extern
  @name("scalanative_msg_peek")
  def MSG_PEEK = extern
  @name("scalanative_msg_trunc")
  def MSG_TRUNC = extern
  @name("scalanative_msg_waitall")
  def MSG_WAITALL = extern
  @name("scalanative_msg_ctrunc")
  def MSG_CTRUNC = extern
  @name("scalanative_msg_waitforone")
  def MSG_WAITFORONE = extern
}
