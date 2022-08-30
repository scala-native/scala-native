package scala.scalanative
package posix
package sys

import scalanative.runtime.Platform
import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.meta.LinktimeInfo.isWindows

@extern
object socket {
  type _14 = Nat.Digit2[Nat._1, Nat._4]
  type _15 = Nat.Digit2[Nat._1, Nat._5]

  /* Design Note:
   *  C11 _Static_assert() statements in socket.c check that the
   *  Scala Native structures declared below match, closely enough for
   *  purpose, the corresponding structure declared by the operating
   *  system.
   *
   *  The transcription from Scala declarations here to the corresponding
   *  'scalanative_foo' declarations is manual, not automatic.
   *  If you change a declaration here, please also check/add/delete
   *  the C declaration & checks.
   *
   *  Keeping Scala & operating system structures synchronized allows
   *  the vast majority of structures to be passed to & from without
   *  needing an expensive "glue" conversion layer.  The C compiler
   *  does the work once at compilation, rather than at each runtime call.
   */

  type socklen_t = CUnsignedInt

  type sa_family_t = CUnsignedShort

  type sockaddr = CStruct2[
    sa_family_t, // sa_family, sa_len is synthisized if needed
    CArray[CChar, _14] // sa_data, size = 14 in OS X and Linux
  ]

  /* The declaration of sockaddr_storage should yield 128 bytes,
   * with an overall alignment so that pointers have natural (64 )alignment.
   */
  type sockaddr_storage = CStruct4[
    sa_family_t, // ss_family, // ss_family, sa_len is synthisized if needed
    CUnsignedShort, // __opaquePadTo32
    CUnsignedInt, // opaque, __opaquePadTo64
    CArray[CUnsignedLongLong, _15] // __opaqueAlignStructure to 8 bytes
  ]

  type msghdr = CStruct7[
    Ptr[Byte], // msg_name
    socklen_t, // msg_namelen
    Ptr[uio.iovec], // msg_iov
    CInt, // msg_iovlen
    Ptr[Byte], // msg_control
    socklen_t, // msg_crontrollen
    CInt // msg_flags
  ]

  type cmsghdr = CStruct3[
    socklen_t, // cmsg_len
    CInt, // cmsg_level
    CInt // cmsg_type
  ]

  type iovec = uio.iovec

  type linger = CStruct2[
    CInt, // l_onoff
    CInt // l_linger
  ]

  @name("scalanative_scm_rights")
  def SCM_RIGHTS: CInt = extern

  @name("scalanative_sock_dgram")
  def SOCK_DGRAM: CInt = extern

  @name("scalanative_sock_raw")
  def SOCK_RAW: CInt = extern

  @name("scalanative_sock_seqpacket")
  def SOCK_SEQPACKET: CInt = extern

  @name("scalanative_sock_stream")
  def SOCK_STREAM: CInt = extern

  @name("scalanative_sol_socket")
  def SOL_SOCKET: CInt = extern

  @name("scalanative_so_acceptconn")
  def SO_ACCEPTCONN: CInt = extern

  @name("scalanative_so_broadcast")
  def SO_BROADCAST: CInt = extern

  @name("scalanative_so_debug")
  def SO_DEBUG: CInt = extern

  @name("scalanative_so_dontroute")
  def SO_DONTROUTE: CInt = extern

  @name("scalanative_so_error")
  def SO_ERROR: CInt = extern

  @name("scalanative_so_keepalive")
  def SO_KEEPALIVE: CInt = extern

  @name("scalanative_so_linger")
  def SO_LINGER: CInt = extern

  @name("scalanative_so_oobinline")
  def SO_OOBINLINE: CInt = extern

  @name("scalanative_so_rcvbuf")
  def SO_RCVBUF: CInt = extern

  @name("scalanative_so_rcvlowat")
  def SO_RCVLOWAT: CInt = extern

  @name("scalanative_so_rcvtimeo")
  def SO_RCVTIMEO: CInt = extern

  @name("scalanative_so_reuseaddr")
  def SO_REUSEADDR: CInt = extern

  @name("scalanative_so_sndbuf")
  def SO_SNDBUF: CInt = extern

  @name("scalanative_so_sndlowat")
  def SO_SNDLOWAT: CInt = extern

  @name("scalanative_so_sndtimeo")
  def SO_SNDTIMEO: CInt = extern

  @name("scalanative_so_type")
  def SO_TYPE: CInt = extern

  @name("scalanative_somaxconn")
  def SOMAXCONN: CInt = extern

  @name("scalanative_msg_ctrunc")
  def MSG_CTRUNC: CInt = extern

  @name("scalanative_msg_dontroute")
  def MSG_DONTROUTE: CInt = extern

  @name("scalanative_msg_eor")
  def MSG_EOR: CInt = extern

  @name("scalanative_msg_oob")
  def MSG_OOB: CInt = extern

  @name("scalanative_msg_nosignal")
  def MSG_NOSIGNAL: CInt = extern // returns 0 on macOS

  @name("scalanative_msg_peek")
  def MSG_PEEK: CInt = extern

  @name("scalanative_msg_trunc")
  def MSG_TRUNC: CInt = extern

  @name("scalanative_msg_waitall")
  def MSG_WAITALL: CInt = extern

  @name("scalanative_af_inet")
  def AF_INET: CInt = extern

  @name("scalanative_af_inet6")
  def AF_INET6: CInt = extern

  @name("scalanative_af_unix")
  def AF_UNIX: CInt = extern

  @name("scalanative_af_unspec")
  def AF_UNSPEC: CInt = extern

  /* Most methods which do not have arguments which are structures
   * can be direct calls to C or another implementation language.
   *
   * Methods which have _Static_assert statements in socket.c which validate
   * that the Scala Native structures match the operating system structures
   * can also be direct calls.
   *
   * The other methods need an "@name scalanative_foo" intermediate
   * layer to handle required conversions. Usually the structure
   * in question is a sockaddr or pointer to one.
   */
  /* Design Note:
   *   Most of these are fast, direct call to C. See 'Design Note' at
   *   top of this file.
   *
   *   Scalar/primitive values passed directly to/from the
   *   operating system (OS) cause no problems.
   *
   *   When one has assurance from the C compiler that the Scala Native
   *   that Scala Native structures match, closely enough, the corresponding
   *   structure used by the operating system, one can use the former
   *   as arguments in direct calls to the OS.
   *
   *   Scala methods which need their arguments transformed use a
   *   '@name' annotation. See (Pending future) methods 'sendmsg()' &
   *   'recvmsg'.
   */

  def getsockname(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CInt = extern

  def socket(domain: CInt, tpe: CInt, protocol: CInt): CInt = extern

  def connect(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: socklen_t
  ): CInt = extern

  def bind(socket: CInt, address: Ptr[sockaddr], address_len: socklen_t): CInt =
    extern

  def listen(socket: CInt, backlog: CInt): CInt = extern

  def accept(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CInt = extern

  def setsockopt(
      socket: CInt,
      level: CInt,
      option_name: CInt,
      options_value: Ptr[Byte],
      option_len: socklen_t
  ): CInt = extern

  def getsockopt(
      socket: CInt,
      level: CInt,
      option_name: CInt,
      options_value: Ptr[Byte],
      option_len: Ptr[socklen_t]
  ): CInt = extern

  def recv(
      socket: CInt,
      buffer: Ptr[Byte],
      length: CSize,
      flags: CInt
  ): CSSize = extern

  def recvfrom(
      socket: CInt,
      buffer: Ptr[Byte],
      length: CSize,
      flags: CInt,
      dest_addr: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CSSize = extern

  def send(
      socket: CInt,
      buffer: Ptr[Byte],
      length: CSize,
      flags: CInt
  ): CSSize = extern

  def sendto(
      socket: CInt,
      buffer: Ptr[Byte],
      length: CSize,
      flags: CInt,
      dest_addr: Ptr[sockaddr],
      address_len: socklen_t
  ): CSSize = extern

  def shutdown(socket: CInt, how: CInt): CInt = extern
}

object socketOps {
  import socket._
  import posix.inttypes.uint8_t

  // Also used by posixlib netinet/in.scala
  val useSinXLen = !Platform.isLinux() &&
    (Platform.isMac() || Platform.isFreeBSD())

  implicit class sockaddrOps(val ptr: Ptr[sockaddr]) extends AnyVal {
    def sa_len: uint8_t = if (!useSinXLen) {
      sizeof[sockaddr].toUByte // length is synthesized
    } else {
      ptr._1.toUByte
    }

    def sa_family: sa_family_t = if (!useSinXLen) {
      ptr._1
    } else {
      (ptr._1 >>> 8).toUByte
    }

    def sa_data: CArray[CChar, _14] = ptr._2

    def sa_len_=(v: uint8_t): Unit = if (useSinXLen) {
      ptr._1 = ((ptr._1 & 0xff00.toUShort) + v).toUShort
    } // else silently do nothing

    def sa_family_=(v: sa_family_t): Unit =
      if (!useSinXLen) {
        ptr._1 = v
      } else {
        ptr._1 = ((v << 8) + ptr.sa_len).toUShort
      }

    def sa_data_=(v: CArray[CChar, _14]): Unit = ptr._2 = v
  }

  implicit class sockaddr_storageOps(val ptr: Ptr[sockaddr_storage])
      extends AnyVal {
    def ss_len: uint8_t = if (!useSinXLen) {
      sizeof[sockaddr].toUByte // length is synthesized
    } else {
      ptr._1.toUByte
    }

    def ss_family: sa_family_t = if (!useSinXLen) {
      ptr._1
    } else {
      (ptr._1 >>> 8).toUByte
    }

    def ss_len_=(v: uint8_t): Unit = if (useSinXLen) {
      ptr._1 = ((ptr._1 & 0xff00.toUShort) + v).toUShort
    } // else silently do nothing

    def ss_family_=(v: sa_family_t): Unit =
      if (!useSinXLen) {
        ptr._1 = v
      } else {
        ptr._1 = ((v << 8) + ptr.ss_len).toUShort
      }
  }

  implicit class msghdrOps(val ptr: Ptr[msghdr]) extends AnyVal {
    def msg_name: Ptr[Byte] = ptr._1
    def msg_namelen: socklen_t = ptr._2
    def msg_iov: Ptr[uio.iovec] = ptr._3
    def msg_iovlen: CInt = ptr._4
    def msg_control: Ptr[Byte] = ptr._5
    def msg_controllen: socklen_t = ptr._6
    def msg_flags: CInt = ptr._7

    def msg_name_=(v: Ptr[Byte]): Unit = ptr._1 = v
    def msg_namelen_=(v: socklen_t): Unit = ptr._2 = v
    def msg_iov_=(v: Ptr[uio.iovec]): Unit = ptr._3 = v
    def msg_iovlen_=(v: CInt): Unit = ptr._4 = v
    def msg_control_=(v: Ptr[Byte]): Unit = ptr._5 = v
    def msg_controllen_=(v: socklen_t): Unit = ptr._6 = v
    def msg_flags_=(v: CInt): Unit = ptr._7 = v
  }

  implicit class cmsghdrOps(val ptr: Ptr[cmsghdr]) extends AnyVal {
    def cmsg_len: socklen_t = ptr._1
    def cmsg_level: CInt = ptr._2
    def cmsg_type: CInt = ptr._3

    def cmsg_len_=(v: socklen_t): Unit = ptr._1 = v
    def cmsg_level_=(v: CInt): Unit = ptr._2 = v
    def cmsg_type_=(v: CInt): Unit = ptr._3 = v
  }

  implicit class lingerOps(val ptr: Ptr[linger]) extends AnyVal {
    private type WindowsLinger = CStruct2[UShort, UShort]
    private def asWinLinger = ptr.asInstanceOf[Ptr[WindowsLinger]]

    def l_onoff: CInt = if (isWindows) asWinLinger._1.toInt else ptr._1
    def l_linger: CInt = if (isWindows) asWinLinger._2.toInt else ptr._2

    def l_onoff_=(v: CInt): Unit =
      if (isWindows) asWinLinger._1 = v.toUShort
      else ptr._1 = v
    def l_linger_=(v: CInt): Unit =
      if (isWindows) asWinLinger._2 = v.toUShort
      else ptr._2 = v
  }
}
