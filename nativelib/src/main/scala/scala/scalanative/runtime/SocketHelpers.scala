package scala.scalanative
package runtime

import scala.scalanative.native.{CString, Ptr, CBool, CInt, extern, name}

@extern
object SocketHelpers {

  @name("scalanative_host_to_ip")
  def hostToIp(host: CString): CString = extern

  @name("scalanative_ip_to_host")
  def ipToHost(ip: CString, isv4: CBool): CString = extern

  @name("scalanative_host_to_ip_array")
  def hostToIpArray(host: CString): Ptr[CString] = extern

  @name("scalanative_is_reachable_by_icmp")
  def isReachableByICMP(ipString: CString, timeout: CInt, v6: CBool): CBool =
    extern

  @name("scalanative_is_reachable_by_echo")
  def isReachableByEcho(ipString: CString, timeout: CInt, v6: CBool): CBool =
    extern

}
