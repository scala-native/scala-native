package scala.scalanative
package posix

import scala.scalanative.native.{CInt, CString, CStruct5, extern, name, Ptr}

import stat.{uid_t, gid_t}

@extern
object pwd {

  type passwd = CStruct5[CString, // pw_name
                         uid_t, // pw_uid
                         gid_t, // pw_gid
                         CString, // pw_dir
                         CString] // pw_shell

  @name("scalanative_getpwuid")
  def getpwuid(uid: uid_t, buf: Ptr[passwd]): CInt = extern

  @name("scalanative_getpwnam")
  def getpwnam(name: CString, buf: Ptr[passwd]): CInt = extern
}
