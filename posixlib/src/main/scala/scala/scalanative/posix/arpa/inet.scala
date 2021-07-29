package scala.scalanative
package posix
package arpa

import scalanative.unsafe._
import scalanative.posix.inttypes._
import scalanative.posix.sys.socket.socklen_t
import scalanative.posix.netinet.in.{in_addr, in_addr_t}

@extern
object inet {

  @name("scalanative_htonl")
  def htonl(arg: uint32_t): uint32_t = extern

  @name("scalanative_htons")
  def htons(arg: uint16_t): uint16_t = extern

  @name("scalanative_ntohl")
  def ntohl(arg: uint32_t): uint32_t = extern

  @name("scalanative_ntohs")
  def ntohs(arg: uint16_t): uint16_t = extern

  /* The argument for inet_ntoa() differs from the POSIX specification
   * because Scala Native supports only passing structures by reference,
   * not value.
   *
   * It is hard to obtain a in_addr structure without a pointer being
   * involved.  If a Ptr[in_addr] is not immediately available, a
   * Ptr[sockaddr_in] or Ptr[sockaddr_in6] may be.  In that case, try
   * ptr.at3, for sockaddr_in or ptr.at1 for sockaddr_in6 (
   * (casting of the resultant in6_addr will be needed).
   *
   * The standard compliant inet_ntop() may be more useful.
   */

  @name("scalanative_inet_ntoa")
  def inet_ntoa(in: Ptr[in_addr]): CString = extern

  @name("scalanative_inet_ntop")
  def inet_ntop(
      af: CInt,
      src: Ptr[Byte],
      dst: CString,
      size: socklen_t
  ): CString = extern

  @name("scalanative_inet_pton")
  def inet_pton(af: CInt, src: CString, dst: Ptr[Byte]): CInt = extern

  @name("scalanative_inet_addr")
  def inet_addr(in: CString): in_addr_t = extern

}
