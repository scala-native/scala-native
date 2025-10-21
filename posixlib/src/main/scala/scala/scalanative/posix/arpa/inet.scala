package scala.scalanative
package posix
package arpa

import scalanative.posix.inttypes._
import scalanative.posix.netinet.in.{in_addr, in_addr_t}
import scalanative.posix.sys.socket.socklen_t
import scalanative.unsafe._

@extern
@define("__SCALANATIVE_POSIX_ARPA_INET")
object inet {
  /* Declarations where the arguments are passed to and from the
   * implementing extern code do not need "@name" intermediate code.
   * "inet_ntoa()" below transforms its argument before passing it down,
   * so it requires the annotation and "glue" code.
   */

  def htonl(arg: uint32_t): uint32_t = extern

  def htons(arg: uint16_t): uint16_t = extern

  def ntohl(arg: uint32_t): uint32_t = extern

  def ntohs(arg: uint16_t): uint16_t = extern

  /* The argument for inet_ntoa() differs from the POSIX specification
   * because Scala Native supports only passing structures by reference,
   * not value.
   *
   * It is hard to obtain an in_addr structure without a pointer being
   * involved.  If a Ptr[in_addr] is not immediately available, a
   * Ptr[sockaddr_in] or Ptr[sockaddr_in6] may be.  In that case, try
   * ptr.at3, for sockaddr_in or ptr.at1 for sockaddr_in6 (
   * (casting of the resultant in6_addr will be needed).
   *
   * The standard compliant inet_ntop() may be more useful.
   */

  @name("scalanative_inet_ntoa")
  def inet_ntoa(in: Ptr[in_addr]): CString = extern

  def inet_ntop(
      af: CInt,
      src: CVoidPtr,
      dst: CString,
      size: socklen_t
  ): CString = extern

  def inet_pton(af: CInt, src: CString, dst: CVoidPtr): CInt = extern

  def inet_addr(in: CString): in_addr_t = extern
}
