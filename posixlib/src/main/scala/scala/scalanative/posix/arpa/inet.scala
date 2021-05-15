package scala.scalanative
package posix
package arpa

import scalanative.unsafe._
import scalanative.posix.inttypes._
import scalanative.posix.sys.socket.socklen_t
import scalanative.posix.netinet.in.{in_addr, in_addr_t}

@extern
object inet {
  def htonl(arg: uint32_t): uint32_t = extern

  def htons(arg: uint16_t): uint16_t = extern

  def ntohl(arg: uint32_t): uint32_t = extern

  def ntohs(arg: uint16_t): uint16_t = extern

  def inet_ntoa(in: Ptr[in_addr]): CString = extern

  def inet_ntop(af: CInt,
                src: Ptr[Byte],
                dst: CString,
                size: socklen_t): CString = extern

  def inet_pton(af: CInt, src: CString, dst: Ptr[Byte]): CInt = extern

  def inet_addr(in: CString): in_addr_t = extern
}
