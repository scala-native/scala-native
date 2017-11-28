package scala.scalanative
package native

import scalanative.posix.sys.socket

@extern
object ifaddrs {
  type ifaddrs = CStruct6[Ptr[Byte], // ifa_next
                          CString, // ifa_name
                          CUnsignedInt, // ifa_flags
                          Ptr[socket.sockaddr], //ifa_addr
                          Ptr[socket.sockaddr], //ifa_netmask
                          Ptr[socket.sockaddr]] //ifu_broadaddr

  @name("scalanative_getifaddrs")
  def getifaddrs(ifap: Ptr[Ptr[ifaddrs]]): CInt = extern

  @name("scalanative_freeifaddrs")
  def freeifaddrs(ifa: Ptr[ifaddrs]): Unit = extern
}

object ifaddrsOps {
  import ifaddrs._

  implicit class ifaddrsOps(val ptr: Ptr[ifaddrs.ifaddrs]) extends AnyVal {
    def ifa_next: Ptr[Byte]                 = !ptr._1
    def ifa_name: CString                   = !ptr._2
    def ifa_flags: CUnsignedInt             = !ptr._3
    def ifa_addr: Ptr[socket.sockaddr]      = !ptr._4
    def ifa_netmask: Ptr[socket.sockaddr]   = !ptr._5
    def ifa_broadaddr: Ptr[socket.sockaddr] = !ptr._6
    def ifa_dstaddr: Ptr[socket.sockaddr]   = !ptr._6

    def ifa_next_=(v: Ptr[Byte])                 = !ptr._1 = v
    def ifa_name_=(v: CString)                   = !ptr._2 = v
    def ifa_flags_=(v: CUnsignedInt)             = !ptr._3 = v
    def ifa_addr_=(v: Ptr[socket.sockaddr])      = !ptr._4 = v
    def ifa_netmask_=(v: Ptr[socket.sockaddr])   = !ptr._5 = v
    def ifa_broadaddr_=(v: Ptr[socket.sockaddr]) = !ptr._6 = v
    def ifa_dstaddr_=(v: Ptr[socket.sockaddr])   = !ptr._6 = v
  }
}
