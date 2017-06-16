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

  type in6_addr = CStruct1[CArray[uint8_t, _16]] // s6_addr
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

object inOps {
  import in._

  implicit class sockaddr_inOps(val ptr: Ptr[sockaddr_in]) extends AnyVal {
    def sin_family: socket.sa_family_t = !ptr._1
    def sin_port: in_port_t            = !ptr._2
    def sin_addr: Ptr[in_addr]         = ptr._3

    def sin_family_=(v: socket.sa_family_t): Unit = !ptr._1 = v
    def sin_port_=(v: in_port_t): Unit            = !ptr._2 = v
    def sin_addr_=(v: Ptr[in_addr]): Unit         = !ptr._3 = !v
  }

  implicit class sockaddr_in6Ops(val ptr: Ptr[sockaddr_in6]) extends AnyVal {
    def sin6_addr: Ptr[in6_addr]        = ptr._1
    def sin6_family: socket.sa_family_t = !ptr._2
    def sin6_port: in_port_t            = !ptr._3
    def sin6_flowinfo: uint32_t         = !ptr._4
    def sin6_scope_id: uint32_t         = !ptr._5

    def sin6_addr_=(v: Ptr[in6_addr]): Unit        = !ptr._1 = !v
    def sin6_family_=(v: socket.sa_family_t): Unit = !ptr._2 = v
    def sin6_port_=(v: in_port_t): Unit            = !ptr._3 = v
    def sin6_flowinfo_=(v: uint32_t): Unit         = !ptr._4 = v
    def sin6_scope_id_=(v: uint32_t): Unit         = !ptr._5 = v
  }

  implicit class in_addrOps(val ptr: Ptr[in_addr]) extends AnyVal {
    def in_addr: in_addr_t            = !ptr._1
    def in_addr_=(v: in_addr_t): Unit = !ptr._1 = v
  }

  implicit class in6_addrOps(val ptr: Ptr[in6_addr]) extends AnyVal {
    def s6_addr: CArray[uint8_t, _16]            = !ptr._1
    def s6_addr_=(v: CArray[uint8_t, _16]): Unit = !ptr._1 = v
  }

  implicit class ipv6_mreqOps(val ptr: Ptr[ipv6_mreq]) extends AnyVal {
    def ipv6mr_multiaddr: Ptr[in6_addr] = ptr._1
    def ipv6mr_interface: CUnsignedInt  = !ptr._2

    def ipv6mr_multiaddr_=(v: Ptr[in6_addr]): Unit = !ptr._1 = !v
    def ipv6mr_interface_=(v: CUnsignedInt): Unit  = !ptr._2 = v
  }
}
