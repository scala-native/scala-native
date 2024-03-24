package scala.scalanative
package posix
package net

import scalanative.unsafe._

/** POSIX if.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */
@extern
@define("__SCALANATIVE_POSIX_NET_IF")
object `if` {

  type if_nameindex = CStruct2[
    CUnsignedInt, // if_index
    CString // if_name
  ]

  // Symbolic constants

  @name("scalanative_if_namesize")
  def IF_NAMESIZE: CInt = extern

  // Methods

  def if_freenameindex(ptr: Ptr[if_nameindex]): Unit = extern
  def if_indextoname(ifindex: CUnsignedInt, ifname: Ptr[Byte]): CString =
    extern
  def if_nameindex(): Ptr[if_nameindex] = extern
  def if_nametoindex(ifname: CString): CUnsignedInt = extern
}

object ifOps {
  import `if`.if_nameindex

  implicit class ifOps(private val ptr: Ptr[if_nameindex]) extends AnyVal {
    def if_index: CUnsignedInt = ptr._1
    def if_name: CString = ptr._2
    // These are used as read-only fields, so no Ops here to set them.
  }
}
