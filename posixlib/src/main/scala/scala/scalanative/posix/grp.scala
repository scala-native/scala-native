package scala.scalanative
package posix

import scalanative.unsafe.{CInt, CString, CStruct3, extern, name, Ptr}
import scalanative.posix.sys.stat.gid_t

@extern
object grp {
  type group = CStruct3[CString, // gr_name
                        gid_t, // gr_gid
                        Ptr[CString]] // gr_mem

  def getgrgid(gid: gid_t): Ptr[group] = extern

  def getgrnam(name: CString): Ptr[group] = extern
}
