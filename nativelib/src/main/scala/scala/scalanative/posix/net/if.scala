package scala.scalanative
package posix
package net

import scalanative.native._
import scalanative.posix.sys.socket

@extern
object _if {
  type _16 = Nat.Digit[Nat._1, Nat._6]

  type ifreq = CStruct1[CArray[CChar, _16]] // ifr_name

  type ifreq_hwaddr = CStruct2[CArray[CChar, _16], // ifr_name
                               socket.sockaddr]    // ifr_hwaddr

  type ifreq_flags = CStruct2[CArray[CChar, _16], // ifr_name
                              CShort]             // ifr_flags

  type ifreq_mtu = CStruct2[CArray[CChar, _16], // ifr_name
                            CInt]               // ifr_mtu

  type ifconf = CStruct2[CInt,      // ifc_len
                         Ptr[Byte]] // ifc_buf

  def if_nametoindex(ifname: CString): UInt = extern

  @name("scalanative_IFF_RUNNING")
  def IFF_RUNNING: CInt = extern

  @name("scalanative_IFF_LOOPBACK")
  def IFF_LOOPBACK: CInt = extern

  @name("scalanative_IFF_POINTOPOINT")
  def IFF_POINTOPOINT: CInt = extern

  @name("scalanative_IFF_MULTICAST")
  def IFF_MULTICAST: CInt = extern
}

object ifOps {
  import _if._

  implicit class ifOps(val ptr: Ptr[ifconf]) extends AnyVal {
    def ifc_len: CInt       = !ptr._1
    def ifc_buf: Ptr[Byte]  = !ptr._2
    def ifc_req: Ptr[ifreq] = (!ptr._2).cast[Ptr[ifreq]]

    def ifc_len_=(v: CInt)       = !ptr._1 = v
    def ifc_buf_=(v: Ptr[Byte])  = !ptr._2 = v
    def ifc_req_=(v: Ptr[ifreq]) = !ptr._2 = v.cast[Ptr[Byte]]
  }

  implicit class ifreqOps(val ptr: Ptr[ifreq]) extends AnyVal {
    def ifr_name: Ptr[CArray[CChar, _16]] = ptr._1
    def ifr_hwaddr: Ptr[socket.sockaddr]  = ptr.cast[Ptr[ifreq_hwaddr]]._2
    def ifr_flags: CShort                 = !(ptr.cast[Ptr[ifreq_flags]])._2
    def ifr_mtu: CInt                     = !(ptr.cast[Ptr[ifreq_mtu]])._2

    def ifr_name_=(v: CArray[CChar, _16]) = !ptr._1 = v
    def ifr_hwaddr_(v: Ptr[socket.sockaddr]) =
      !(ptr.cast[Ptr[ifreq_hwaddr]])._2 = !v
    def ifr_flags_=(v: CShort) = !(ptr.cast[Ptr[ifreq_flags]])._2 = v
    def ifr_mtu_=(v: CInt)     = !(ptr.cast[Ptr[ifreq_mtu]])._2 = v
  }
}
