package scala.scalanative.posix

import scalanative.unsafe._
import scalanative.posix.sys.socket
import scalanative.posix.netinet.in

import scala.scalanative.meta.LinktimeInfo

/** netdb.h for Scala
 *  @see
 *    [[https://scala-native.readthedocs.io/en/latest/lib/posixlib.html]]
 */
@extern
object netdb {
  type addrinfo = CStruct8[CInt, CInt, CInt, CInt, socket.socklen_t, Ptr[
    socket.sockaddr
  ], Ptr[CChar], Ptr[Byte]]

  @name("scalanative_freeaddrinfo")
  def freeaddrinfo(addr: Ptr[addrinfo]): Unit = extern

  // direct call to C
  def gai_strerror(errcode: CInt): CString = extern

  @name("scalanative_getaddrinfo")
  def getaddrinfo(
      name: CString,
      service: CString,
      hints: Ptr[addrinfo],
      res: Ptr[Ptr[addrinfo]]
  ): CInt = extern

  @name("scalanative_getnameinfo")
  def getnameinfo(
      addr: Ptr[socket.sockaddr],
      addrlen: socket.socklen_t,
      host: CString,
      hostlen: socket.socklen_t,
      service: CString,
      servlen: socket.socklen_t,
      flags: CInt
  ): CInt = extern

  @name("scalanative_ai_numerichost")
  def AI_NUMERICHOST: CInt = extern

  @name("scalanative_ai_passive")
  def AI_PASSIVE: CInt = extern

  @name("scalanative_ai_numericserv")
  def AI_NUMERICSERV: CInt = extern

  @name("scalanative_ai_addrconfig")
  def AI_ADDRCONFIG: CInt = extern

  @name("scalanative_ai_v4mapped")
  def AI_V4MAPPED: CInt = extern

  @name("scalanative_ai_canonname")
  def AI_CANONNAME: CInt = extern

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
}

object netdbOps {
  import netdb._

  @resolvedAtLinktime
  def useBsdAddrinfo = (LinktimeInfo.isMac ||
    LinktimeInfo.isFreeBSD ||
    LinktimeInfo.isWindows)

  implicit class addrinfoOps(private val ptr: Ptr[addrinfo]) extends AnyVal {
    def ai_flags: CInt = ptr._1
    def ai_family: CInt = ptr._2
    def ai_socktype: CInt = ptr._3
    def ai_protocol: CInt = ptr._4
    def ai_addrlen: socket.socklen_t = ptr._5
    def ai_addr: Ptr[socket.sockaddr] = ptr._6
    def ai_canonname: Ptr[CChar] = ptr._7
    def ai_next: Ptr[Byte] = ptr._8
    def ai_flags_=(v: CInt): Unit = ptr._1 = v
    def ai_family_=(v: CInt): Unit = ptr._2 = v
    def ai_socktype_=(v: CInt): Unit = ptr._3 = v
    def ai_protocol_=(v: CInt): Unit = ptr._4 = v
    def ai_addrlen_=(v: socket.socklen_t): Unit = ptr._5 = v
    def ai_addr_=(v: Ptr[socket.sockaddr]): Unit = ptr._6 = v
    def ai_canonname_=(v: Ptr[CChar]): Unit = ptr._7 = v
    def ai_next_=(v: Ptr[Byte]): Unit = ptr._8 = v
  }
}
