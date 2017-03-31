package scala.scalanative
package posix

import scala.scalanative.native.{CString, CStruct3, extern, name, Ptr}

import stat.gid_t

@extern
object grp {
  type group = CStruct3[CString, // gr_name
                        gid_t, // gr_gid
                        Ptr[CString]] // gr_mem

  @name("scalanative_getgrgid")
  def getgrgid(gid: gid_t): Ptr[group] = extern

  @name("scalanative_getgrnam")
  def getgrnam(name: CString): Ptr[group] = extern
}
