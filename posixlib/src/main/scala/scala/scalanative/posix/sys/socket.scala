package scala.scalanative
package posix
package sys

import scalanative.runtime.Platform

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.meta.LinktimeInfo._

/** socket.h for Scala
 *  @see
 *    [[https://scala-native.readthedocs.io/en/latest/lib/posixlib.html]]
 */
@extern
object socket {
  type _14 = Nat.Digit2[Nat._1, Nat._4]
  type _31 = Nat.Digit2[Nat._3, Nat._1]

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

  // posix requires this file declares these types. Use single point of truth.
  type size_t = types.size_t
  type ssize_t = types.ssize_t

  type socklen_t = CUnsignedInt

  type sa_family_t = CUnsignedShort

  /* Code in socket.c checks that shadow copies of the Scala Native structures
   * here and those used by the operating system match, close enough for
   * purpose.
   * For this to work, the scalanative_foo "shadow" C declarations in
   * socket.c must match the Scala ones here. If you change a structure
   * in this file, you must change the structure in socket.c.
   * Check also if the structure exists in TagTest.scala and needs
   * synchronization.
   */

  type sockaddr = CStruct2[
    sa_family_t, // sa_family, sa_len is synthesized if needed
    CArray[CChar, _14] // sa_data, size = 14 in OS X and Linux
  ]

  /* The declaration of sockaddr_storage should yield 256 bytes,
   * with an overall alignment so that pointers have natural (64) alignment.
   */
  type sockaddr_storage = CStruct4[
    sa_family_t, // ss_family, // ss_family, sa_len is synthesized if needed
    CUnsignedShort, // __opaquePadTo32
    CUnsignedInt, // opaque, __opaquePadTo64
    CArray[CUnsignedLongLong, _31] // __opaqueAlignStructure to 8 bytes.
  ]

  /* This is the POSIX 2018 & prior definition. Because SN 'naturally'
   * pads, the way that C would, this is 48 bytes on 64 bit and 40 on 32 bit
   * machines.
   * POSIX specifies socklen_t for fields msg_iovlen and msg_controllen.
   *
   * Linux varies by using size_t for those two fields.
   * size_t is 64 bits on 64 bit Linux, so the resultant size is 56
   * bytes and everything after msg_iov has the 'wrong' offset.
   *
   * See comments below on methods sendmsg() and recvmsg() about
   * using those routines on 64 bit Linux & like.
   */

  type msghdr = CStruct7[
    Ptr[Byte], // msg_name
    socklen_t, // msg_namelen
    Ptr[uio.iovec], // msg_iov
    CInt, // msg_iovlen
    Ptr[Byte], // msg_control
    socklen_t, // msg_crontrollen
    CInt // msg_flags
  ]

  /* The Open Group recommends using the CMSG macros below
   * for parsing a buffer of cmsghdrs. See comments above the
   * declaration of those macros, especially if using 64 bit Linux.
   */

  /* POSIX 2018 specifies cmsg_len as socklen_t, which is usually 32 bits.
   *
   * Linux defines cmsg_len as size_t, because of its kernel definition.
   * On 64 bit Linux size_t is 64 bits, not 32.
   * Linux code can use the CMSG macros below to parse and read parts
   * of a buffer of (OS) cmsghdrs passed back by the OS.  It must use
   * recommended against, OS specific, hand parsing to access the
   * cmsg_level & cmsg_type fields. That access is usually done to
   * check if the cmsg returned is the one expected or to parse
   * through a buffer of (OS) cmsg to get to the one expected.
   */

  // POSIX 2018 & prior 12 byte definition, Linux uses 16 bytes.
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

// Symbolic constants, roughly in POSIX declaration order

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

  @name("scalanative_so_reuseport")
  def SO_REUSEPORT: CInt = extern

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

  @name("scalanative_shut_rd")
  def SHUT_RD: CInt = extern

  @name("scalanative_shut_rdwr")
  def SHUT_RDWR: CInt = extern

  @name("scalanative_shut_wr")
  def SHUT_WR: CInt = extern

// POSIX "Macros"

  @name("scalanative_cmsg_data")
  def CMSG_DATA(cmsg: Ptr[cmsghdr]): Ptr[CUnsignedChar] = extern

  @name("scalanative_cmsg_nxthdr")
  def CMSG_NXTHDR(mhdr: Ptr[msghdr], cmsg: Ptr[cmsghdr]): Ptr[cmsghdr] = extern

  @name("scalanative_cmsg_firsthdr")
  def CMSG_FIRSTHDR(mhdr: Ptr[msghdr]): Ptr[cmsghdr] = extern

// Methods

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
   *   '@name' annotation. See methods 'sendmsg()' & 'recvmsg()'.
   *
   *   Methods for which there is no direct Windows equivalent use '@name'
   *   to call into stubs. The stubs dispatch on executing OS, calling
   *   on non-Windows. On Windows, the stubs always return -1 and set errno
   *   to ENOTSUP.
   */

  @blocking
  def accept(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CInt = extern

  def bind(socket: CInt, address: Ptr[sockaddr], address_len: socklen_t): CInt =
    extern

  @blocking
  def connect(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: socklen_t
  ): CInt = extern

  def getpeername(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CInt = extern

  def getsockname(
      socket: CInt,
      address: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CInt = extern

  def getsockopt(
      socket: CInt,
      level: CInt,
      option_name: CInt,
      options_value: CVoidPtr,
      option_len: Ptr[socklen_t]
  ): CInt = extern

  def listen(socket: CInt, backlog: CInt): CInt = extern

  @blocking
  def recv(
      socket: CInt,
      buffer: CVoidPtr,
      length: CSize,
      flags: CInt
  ): CSSize = extern

  @blocking
  def recvfrom(
      socket: CInt,
      buffer: CVoidPtr,
      length: CSize,
      flags: CInt,
      dest_addr: Ptr[sockaddr],
      address_len: Ptr[socklen_t]
  ): CSSize = extern

  // See comments above msghdr declaration at top of file, re: fixup & sizeof
  @name("scalanative_recvmsg")
  @blocking
  def recvmsg(
      socket: CInt,
      buffer: Ptr[msghdr],
      flags: CInt
  ): CSSize = extern

  @blocking
  def send(
      socket: CInt,
      buffer: CVoidPtr,
      length: CSize,
      flags: CInt
  ): CSSize = extern

  // See comments above msghdr declaration at top of file, re: fixup & sizeof
  @name("scalanative_sendmsg")
  @blocking
  def sendmsg(
      socket: CInt,
      buffer: Ptr[msghdr],
      flags: CInt
  ): CSSize = extern

  @blocking
  def sendto(
      socket: CInt,
      buffer: CVoidPtr,
      length: CSize,
      flags: CInt,
      dest_addr: Ptr[sockaddr],
      address_len: socklen_t
  ): CSSize = extern

  def setsockopt(
      socket: CInt,
      level: CInt,
      option_name: CInt,
      options_value: CVoidPtr,
      option_len: socklen_t
  ): CInt = extern

  def shutdown(socket: CInt, how: CInt): CInt = extern

  @name("scalanative_sockatmark") // A stub on Win32, see top of file
  def sockatmark(socket: CInt): CInt = extern

  def socket(domain: CInt, tpe: CInt, protocol: CInt): CInt = extern

  @name("scalanative_socketpair") // A stub on Win32, see top of file
  def socketpair(domain: CInt, tpe: CInt, protocol: CInt, sv: Ptr[Int]): CInt =
    extern
}

/** Allow using C names to access socket structure fields.
 */
object socketOps {
  import socket._
  import posix.inttypes.uint8_t

  // Also used by posixlib netinet/in.scala
  @resolvedAtLinktime
  def useSinXLen = !isLinux &&
    (isMac || isFreeBSD || isOpenBSD)

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
