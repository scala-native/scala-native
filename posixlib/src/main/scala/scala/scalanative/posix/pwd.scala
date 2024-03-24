package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.posix.sys.types.{uid_t, gid_t}

@extern
@define("__SCALANATIVE_POSIX_PWD")
object pwd {

  type passwd = CStruct5[
    CString, // pw_name
    uid_t, // pw_uid
    gid_t, // pw_gid
    CString, // pw_dir
    CString // pw_shell
  ]

  @name("scalanative_getpwuid")
  def getpwuid(uid: uid_t, buf: Ptr[passwd]): CInt = extern

  @name("scalanative_getpwnam")
  def getpwnam(name: CString, buf: Ptr[passwd]): CInt = extern
}

object pwdOps {
  import pwd._

  implicit class passwdOps(val ptr: Ptr[passwd]) extends AnyVal {
    def pw_name: CString = ptr._1
    def pw_name_=(value: CString): Unit = ptr._1 = value
    def pw_uid: uid_t = ptr._2
    def pw_uid_=(value: uid_t): Unit = ptr._2 = value
    def pw_gid: gid_t = ptr._3
    def pw_gid_=(value: gid_t): Unit = ptr._3 = value
    def pw_dir: CString = ptr._4
    def pw_dir_=(value: CString): Unit = ptr._4 = value
    def pw_shell: CString = ptr._5
    def pw_shell_=(value: CString): Unit = ptr._5 = value
  }

}
