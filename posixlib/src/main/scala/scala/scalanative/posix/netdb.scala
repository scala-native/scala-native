package scala.scalanative.posix

import scalanative.unsafe._

import scalanative.posix.sys.socket

import scalanative.runtime.Platform

/** netdb.h for Scala
 *  @see
 *    [[https://scala-native.readthedocs.io/en/latest/lib/posixlib.html]]
 */
@extern
object netdb {
  /* This is the Linux layout. FreeBSD, macOS, and Windows have the same
   * size but swap ai_addr and ai_canonname. FreeBSD & Windows document this.
   * macOS tells whoppers: it documents the Linux order in man pages and
   * implements the FreeBSD layout.
   *
   * Access to the proper field for the OS is handled in netdbOps below.
   */

  type socklen_t = socket.socklen_t
  type uint32_t = inttypes.uint32_t

  /* _Static_assert code in netdb.c checks that Scala Native and operating
   * system structure definitions match "close enough". If you change
   * something here, please make the corresponding changes there.
   */

  type addrinfo = CStruct8[
    CInt, // ai_flags
    CInt, // ai_family
    CInt, // ai_socktype
    CInt, // ai_protocol
    socket.socklen_t, // ai_addrlen
    Ptr[socket.sockaddr], // ai_addr
    Ptr[CChar], // ai_canonname
    Ptr[Byte] // ai_next
  ]

  def freeaddrinfo(addr: Ptr[addrinfo]): Unit = extern

  def gai_strerror(errcode: CInt): CString = extern

  /* To comply with POSIX, GAI needs scalaNative C help with null hints arg.
   * One can not have executable code in an 'extern' object.
   */
  @name("scalanative_getaddrinfo")
  def getaddrinfo(
      name: CString,
      service: CString,
      hints: Ptr[addrinfo],
      res: Ptr[Ptr[addrinfo]]
  ): CInt = extern

  def getnameinfo(
      addr: Ptr[socket.sockaddr],
      addrlen: socket.socklen_t,
      host: CString,
      hostlen: socket.socklen_t,
      service: CString,
      servlen: socket.socklen_t,
      flags: CInt
  ): CInt = extern

  // AI_* items are declared in the order of Posix specification

  @name("scalanative_ai_passive")
  def AI_PASSIVE: CInt = extern

  @name("scalanative_ai_canonname")
  def AI_CANONNAME: CInt = extern

  @name("scalanative_ai_numerichost")
  def AI_NUMERICHOST: CInt = extern

  @name("scalanative_ai_numericserv")
  def AI_NUMERICSERV: CInt = extern

  @name("scalanative_ai_v4mapped")
  def AI_V4MAPPED: CInt = extern

  @name("scalanative_ai_all")
  def AI_ALL: CInt = extern

  @name("scalanative_ai_addrconfig")
  def AI_ADDRCONFIG: CInt = extern

  // NI_* items are declared in the order of Posix specification

  @name("scalanative_ni_nofqdn")
  def NI_NOFQDN: CInt = extern

  @name("scalanative_ni_numerichost")
  def NI_NUMERICHOST: CInt = extern

  @name("scalanative_ni_namereqd")
  def NI_NAMEREQD: CInt = extern

  @name("scalanative_ni_numericserv")
  def NI_NUMERICSERV: CInt = extern

  @name("scalanative_ni_NI_numericscope")
  def NI_NUMERICSCOPE: CInt = extern

  @name("scalanative_ni_dgram")
  def NI_DGRAM: CInt = extern

  // EAI_* items are declared in the order of Posix specification

  @name("scalanative_eai_again")
  def EAI_AGAIN: CInt = extern

  @name("scalanative_eai_badflags")
  def EAI_BADFLAGS: CInt = extern

  @name("scalanative_eai_fail")
  def EAI_FAIL: CInt = extern

  @name("scalanative_eai_family")
  def EAI_FAMILY: CInt = extern

  @name("scalanative_eai_memory")
  def EAI_MEMORY: CInt = extern

  @name("scalanative_eai_noname")
  def EAI_NONAME: CInt = extern

  @name("scalanative_eai_service")
  def EAI_SERVICE: CInt = extern

  @name("scalanative_eai_socktype")
  def EAI_SOCKTYPE: CInt = extern

  @name("scalanative_eai_system")
  def EAI_SYSTEM: CInt = extern

  @name("scalanative_eai_overflow")
  def EAI_OVERFLOW: CInt = extern

  @name("scalanative_ipport_reserved")
  def IPPORT_RESERVED: CInt = extern
}

/** Allow using C names to access 'addrinfo' structure fields.
 */
object netdbOps {
  import netdb._

  final val useBsdAddrinfo = (Platform.isMac() ||
    Platform.isFreeBSD() ||
    Platform.isWindows())

  implicit class addrinfoOps(private val ptr: Ptr[addrinfo]) extends AnyVal {
    def ai_flags: CInt = ptr._1
    def ai_family: CInt = ptr._2
    def ai_socktype: CInt = ptr._3
    def ai_protocol: CInt = ptr._4
    def ai_addrlen: socket.socklen_t = ptr._5

    def ai_addr: Ptr[socket.sockaddr] =
      if (!useBsdAddrinfo) ptr._6
      else ptr._7.asInstanceOf[Ptr[socket.sockaddr]]

    def ai_canonname: Ptr[CChar] =
      if (!useBsdAddrinfo) ptr._7
      else ptr._6.asInstanceOf[Ptr[CChar]]

    def ai_next: Ptr[Byte] = ptr._8

    def ai_flags_=(v: CInt): Unit = ptr._1 = v
    def ai_family_=(v: CInt): Unit = ptr._2 = v
    def ai_socktype_=(v: CInt): Unit = ptr._3 = v
    def ai_protocol_=(v: CInt): Unit = ptr._4 = v
    def ai_addrlen_=(v: socket.socklen_t): Unit = ptr._5 = v

    def ai_addr_=(v: Ptr[socket.sockaddr]): Unit =
      if (!useBsdAddrinfo) ptr._6 = v
      else ptr._7 = v.asInstanceOf[Ptr[CChar]]

    def ai_canonname_=(v: Ptr[CChar]): Unit =
      if (!useBsdAddrinfo) ptr._7 = v
      else ptr._6 = v.asInstanceOf[Ptr[socket.sockaddr]]

    def ai_next_=(v: Ptr[Byte]): Unit = ptr._8 = v
  }
}
