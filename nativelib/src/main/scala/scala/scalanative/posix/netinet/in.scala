package scala.scalanative
package posix
package netinet

import scalanative.native._
import scalanative.posix.inttypes._
import scalanative.posix.sys.socket

@extern
object in {
  type in_port_t = uint16_t
  type in_addr_t = uint32_t
  type _16       = Nat.Digit[Nat._1, Nat._6]

  type in_addr = CStruct1[in_addr_t] // s_addr
  type sockaddr_in = CStruct3[socket.sa_family_t, // sin_family
                              in_port_t, // sin_port
                              in_addr] // sin_addr

  type in6_addr = CStruct1[CArray[uint8_t, _16]]
  type sockaddr_in6 = CStruct5[in6_addr, // sin6_addr
                               socket.sa_family_t, // sin6_family
                               in_port_t, // sin6_port
                               uint32_t, // sin6_flowinfo
                               uint32_t] // sin6_scope_id

  type ipv6_mreq = CStruct2[in6_addr, // ipv6mr_multiaddr
                            CUnsignedInt] // ipv6mr_interface

  @name("scalanative_IPROTO_IP")
  def IPROTO_IP: CInt = extern

  @name("scalanative_IPPROTO_IPV6")
  def IPPROTO_IPV6: CInt = extern

  @name("scalanative_IPPROTO_ICMP")
  def IPPROTO_ICMP: CInt = extern

  @name("scalanative_IPPROTO_RAW")
  def IPPROTO_RAW: CInt = extern

  @name("scalanative_IPPROTO_TCP")
  def IPPROTO_TCP: CInt = extern

  @name("scalanative_IPPROTO_UDP")
  def IPPROTO_UDP: CInt = extern

  @name("scalanative_INADDR_ANY")
  def INADDR_ANY: uint32_t = extern

  @name("scalanative_INADDR_BROADCAST")
  def INADDR_BROADCAST: uint32_t = extern

  @name("scalanative_INET6_ADDRSTRLEN")
  def INET6_ADDRSTRLEN: CInt = extern

  @name("scalanative_INET_ADDRSTRLEN")
  def INET_ADDRSTRLEN: CInt = extern

  @name("scalanative_IPV6_JOIN_GROUP")
  def IPV6_JOIN_GROUP: CInt = extern

  @name("scalanative_IPV6_LEAVE_GROUP")
  def IPV6_LEAVE_GROUP: CInt = extern

  @name("scalanative_IPV6_MULTICAST_HOPS")
  def IPV6_MULTICAST_HOPS: CInt = extern

  @name("scalanative_IPV6_MULTICAST_IF")
  def IPV6_MULTICAST_IF: CInt = extern

  @name("scalanative_IPV6_MULTICAST_LOOP")
  def IPV6_MULTICAST_LOOP: CInt = extern

  @name("scalanative_IPV6_UNICAST_HOPS")
  def IPV6_UNICAST_HOPS: CInt = extern

  @name("scalanative_IPV6_V6ONLY")
  def IPV6_V6ONLY: CInt = extern

  @name("scalanative_IN6_IS_ADDR_UNSPECIFIED")
  def IN6_IS_ADDR_UNSPECIFIED(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_LOOPBACK")
  def IN6_IS_ADDR_LOOPBACK(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_MULTICAST")
  def IN6_IS_ADDR_MULTICAST(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_LINKLOCAL")
  def IN6_IS_ADDR_LINKLOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_SITELOCAL")
  def IN6_IS_ADDR_SITELOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_V4MAPPED")
  def IN6_IS_ADDR_V4MAPPED(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_V4COMPAT")
  def IN6_IS_ADDR_V4COMPAT(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_MC_NODELOCAL")
  def IN6_IS_ADDR_MC_NODELOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_MC_LINKLOCAL")
  def IN6_IS_ADDR_MC_LINKLOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_MC_SITELOCAL")
  def IN6_IS_ADDR_MC_SITELOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_MC_ORGLOCAL")
  def IN6_IS_ADDR_MC_ORGLOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_IN6_IS_ADDR_MC_GLOBAL")
  def IN6_IS_ADDR_MC_GLOBAL(arg: Ptr[in6_addr]): CInt = extern

}
