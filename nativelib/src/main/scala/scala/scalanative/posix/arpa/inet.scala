package scala.scalanative
package posix
package arpa

import scalanative.native._
import scalanative.posix.inttypes._
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

  @name("scalanative_inet_ntoa")
  def inet_ntoa(in: Ptr[in_addr]): CString = extern

  @name("scalanative_inet_addr")
  def inet_addr(in: CString): in_addr_t = extern

}
