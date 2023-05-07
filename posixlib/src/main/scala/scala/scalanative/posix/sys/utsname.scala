package scala.scalanative.posix.sys

import scala.scalanative.unsafe._
import scala.scalanative.unsafe.Nat._

@extern
object utsname {
  type _256 = Digit3[_2, _5, _6]
  private type str = CArray[Byte, _256]
  type utsname = CStruct5[str, str, str, str, str]
  @extern def uname(utsname: Ptr[utsname]): CInt = extern
}

object uname {
  implicit class utsnameOps(val c: Ptr[utsname.utsname]) {
    def sysname = c._1
    def nodename = c._2
    def release = c._3
    def version = c._4
    def machine = c._5
  }
}
