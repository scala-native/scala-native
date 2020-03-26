package scala.scalanative
package posix

import scalanative.unsafe.{CInt, CString, CStruct5, extern, name, Ptr}
import scalanative.posix.sys.stat.{uid_t, gid_t}

@extern
object pwd {

  type passwd = CStruct5[CString, // pw_name
                         uid_t, // pw_uid
                         gid_t, // pw_gid
                         CString, // pw_dir
                         CString] // pw_shell

  def getpwuid(uid: uid_t): Ptr[passwd] = extern

  def getpwnam(name: CString): Ptr[passwd] = extern
}
