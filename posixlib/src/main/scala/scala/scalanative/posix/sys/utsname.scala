package scala.scalanative.posix.sys

import scala.scalanative.unsafe._
import scala.scalanative.unsafe.Nat._

@extern
object utsname {
  /* Design notes:
   * 1) The 256 "magic"" number appears to be the macOS macro _SYS_NAMELEN.
   *    Linux uses a much smaller number (65).
   *    _Static_assert() guard code exists in uname.c to ensure that
   *    the size used by the operating system is less than or equal to this.
   *    That prevents new or changed operating systems from writing to
   *    memory where it should: i.e. spraying memory.
   *
   * 2) The allocation of the entire array inside the structure follows
   *    the Open Group 2018 POSIX description. That is, the fields are
   *    actual arrays (CArray) and not the pointers to the beginning of an
   *    array (Ptr[Byte]) one might expect.
   *
   *  3) The CArrays are somewhat difficult to work with in Scala.
   *     The operating system will have placed a null
   *     somewhere in the CArray provided to it. Given that, proper
   *     Scala Strings can be obtained by:
   *         import scala.scalanative.unsafe._
   *         fromCString(u.sysname.at(0).asInstanceOf[CString])
   */

  // If changes are made here, corresponding changes in uname.c may be needed.
  type _256 = Digit3[_2, _5, _6]
  private type str = CArray[Byte, _256]
  type utsname = CStruct5[str, str, str, str, str]

  @name("scalanative_uname")
  @extern def uname(utsname: Ptr[utsname]): CInt = extern
}

object utsnameOps {
  implicit class utsnamePtrOps(val c: Ptr[utsname.utsname]) {
    def sysname = c._1
    def nodename = c._2
    def release = c._3
    def version = c._4
    def machine = c._5
  }
}
