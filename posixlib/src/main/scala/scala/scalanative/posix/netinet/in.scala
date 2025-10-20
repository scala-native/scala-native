package scala.scalanative
package posix
package netinet

import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.posix.inttypes.*
import scalanative.posix.sys.socket
import scalanative.posix.sys.socketOps

@extern
@define("__SCALANATIVE_POSIX_NETINET_IN")
object in {
  type _8 = Nat._8
  type _16 = Nat.Digit2[Nat._1, Nat._6]

  type in_port_t = uint16_t
  type in_addr_t = uint32_t

  type in_addr = CStruct1[in_addr_t] // s_addr

  type sockaddr_in = CStruct4[
    socket.sa_family_t, // sin_family, sin_len is synthesized if needed
    in_port_t, // sin_port
    in_addr, // sin_addr
    CArray[Byte, _8] // sin_zero, Posix allowed
  ]

  type in6_addr = CStruct1[CArray[uint8_t, _16]] // s6_addr

  type sockaddr_in6 = CStruct5[
    socket.sa_family_t, // sin6_family, sin6_len is synthesized if needed
    in_port_t, // sin6_port
    uint32_t, // sin6_flowinfo
    in6_addr, // sin6_addr
    uint32_t // sin6_scope_id
  ]

  type ipv6_mreq = CStruct2[
    in6_addr, // ipv6mr_multiaddr
    CUnsignedInt // ipv6mr_interface
  ]

  @name("scalanative_ipproto_ip")
  def IPPROTO_IP: CInt = extern

  @name("scalanative_ipproto_ipv6")
  def IPPROTO_IPV6: CInt = extern

  @name("scalanative_ipproto_icmp")
  def IPPROTO_ICMP: CInt = extern

  @name("scalanative_ipproto_raw")
  def IPPROTO_RAW: CInt = extern

  @name("scalanative_ipproto_tcp")
  def IPPROTO_TCP: CInt = extern

  @name("scalanative_ipproto_udp")
  def IPPROTO_UDP: CInt = extern

  @name("scalanative_inaddr_any")
  def INADDR_ANY: uint32_t = extern

  @name("scalanative_inaddr_broadcast")
  def INADDR_BROADCAST: uint32_t = extern

  @name("scalanative_inet6_addrstrlen")
  def INET6_ADDRSTRLEN: CInt = extern

  @name("scalanative_inet_addrstrlen")
  def INET_ADDRSTRLEN: CInt = extern

  @name("scalanative_ipv6_join_group")
  def IPV6_JOIN_GROUP: CInt = extern

  @name("scalanative_ipv6_leave_group")
  def IPV6_LEAVE_GROUP: CInt = extern

  @name("scalanative_ipv6_multicast_hops")
  def IPV6_MULTICAST_HOPS: CInt = extern

  @name("scalanative_ipv6_multicast_if")
  def IPV6_MULTICAST_IF: CInt = extern

  @name("scalanative_ipv6_multicast_loop")
  def IPV6_MULTICAST_LOOP: CInt = extern

  @name("scalanative_ipv6_unicast_hops")
  def IPV6_UNICAST_HOPS: CInt = extern

  @name("scalanative_ipv6_v6only")
  def IPV6_V6ONLY: CInt = extern

  @name("scalanative_ip_multicast_if")
  def IP_MULTICAST_IF: CInt = extern

  @name("scalanative_ip_multicast_loop")
  def IP_MULTICAST_LOOP: CInt = extern

  @name("scalanative_ip_tos")
  def IP_TOS: CInt = extern

  @name("scalanative_in6_is_addr_unspecified")
  def IN6_IS_ADDR_UNSPECIFIED(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_loopback")
  def IN6_IS_ADDR_LOOPBACK(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_multicast")
  def IN6_IS_ADDR_MULTICAST(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_linklocal")
  def IN6_IS_ADDR_LINKLOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_sitelocal")
  def IN6_IS_ADDR_SITELOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_v4mapped")
  def IN6_IS_ADDR_V4MAPPED(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_v4compat")
  def IN6_IS_ADDR_V4COMPAT(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_mc_nodelocal")
  def IN6_IS_ADDR_MC_NODELOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_mc_linklocal")
  def IN6_IS_ADDR_MC_LINKLOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_mc_sitelocal")
  def IN6_IS_ADDR_MC_SITELOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_mc_orglocal")
  def IN6_IS_ADDR_MC_ORGLOCAL(arg: Ptr[in6_addr]): CInt = extern

  @name("scalanative_in6_is_addr_mc_global")
  def IN6_IS_ADDR_MC_GLOBAL(arg: Ptr[in6_addr]): CInt = extern
}

object inOps {
  import in.*
  import socketOps.useSinXLen

  implicit class sockaddr_inOps(val ptr: Ptr[sockaddr_in]) extends AnyVal {
    def sin_len: uint8_t = if (!useSinXLen) {
      sizeof[sockaddr_in].toUByte // length is synthesized
    } else {
      ptr._1.toUByte
    }

    def sin_family: socket.sa_family_t = if (!useSinXLen) {
      ptr._1
    } else {
      (ptr._1 >>> 8).toUByte
    }

    def sin_port: in_port_t = ptr._2
    def sin_addr: in_addr = ptr._3

    def sin_len_=(v: uint8_t): Unit = if (useSinXLen) {
      ptr._1 = ((ptr._1 & 0xff00.toUShort) + v).toUShort
    } // else silently do nothing

    def sin_family_=(v: socket.sa_family_t): Unit =
      if (!useSinXLen) {
        ptr._1 = v
      } else {
        ptr._1 = ((v << 8) + ptr.sin_len).toUShort
      }

    def sin_port_=(v: in_port_t): Unit = ptr._2 = v
    def sin_addr_=(v: in_addr): Unit = ptr._3 = v
  }

  implicit class sockaddr_inAddrOps(val value: in_addr) extends AnyVal {
    def s_addr: in_addr_t = value._1
    def s_addr_=(v: in_addr_t): Unit = value._1 = v
  }

  implicit class sockaddr_in6Ops(val ptr: Ptr[sockaddr_in6]) extends AnyVal {
    def sin6_len: uint8_t = if (!useSinXLen) {
      sizeof[sockaddr_in6].toUByte // length is synthesized
    } else {
      ptr._1.toUByte
    }

    def sin6_family: socket.sa_family_t = if (!useSinXLen) {
      ptr._1
    } else {
      (ptr._1 >>> 8).toUByte
    }

    def sin6_port: in_port_t = ptr._2
    def sin6_flowinfo: uint32_t = ptr._3
    def sin6_addr: in6_addr = ptr._4
    def sin6_scope_id: uint32_t = ptr._5

    def sin6_len_=(v: uint8_t): Unit = if (useSinXLen) {
      ptr._1 = ((ptr._1 & 0xff00.toUShort) + v).toUShort
    } // else silently do nothing

    def sin6_family_=(v: socket.sa_family_t): Unit =
      if (!useSinXLen) {
        ptr._1 = v
      } else {
        ptr._1 = ((v << 8) + ptr.sin6_len).toUShort
      }

    def sin6_port_=(v: in_port_t): Unit = ptr._2 = v
    def sin6_flowinfo_=(v: uint32_t): Unit = ptr._3 = v
    def sin6_addr_=(v: in6_addr): Unit = ptr._4 = v
    def sin6_scope_id_=(v: uint32_t): Unit = ptr._5 = v
  }

  implicit class in_addrOps(val ptr: Ptr[in_addr]) extends AnyVal {
    def in_addr: in_addr_t = ptr._1
    def in_addr_=(v: in_addr_t): Unit = ptr._1 = v
  }

  implicit class in6_addrOps(val ptr: Ptr[in6_addr]) extends AnyVal {
    def s6_addr: CArray[uint8_t, _16] = ptr._1
    def s6_addr_=(v: CArray[uint8_t, _16]): Unit = ptr._1 = v
  }

  implicit class ipv6_mreqOps(val ptr: Ptr[ipv6_mreq]) extends AnyVal {
    def ipv6mr_multiaddr: in6_addr = ptr._1
    def ipv6mr_interface: CUnsignedInt = ptr._2
    def ipv6mr_multiaddr_=(v: in6_addr): Unit = ptr._1 = v
    def ipv6mr_interface_=(v: CUnsignedInt): Unit = ptr._2 = v
  }
}
