package scala.scalanative.nio.fs.attribute

import java.nio.file.attribute._

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._
import scalanative.posix.{errno => e, grp, pwd, unistd, time, utime}, e._
import scalanative.posix.sys.stat
import scala.scalanative.nio.fs.UnixException

final case class NativeUserPrincipal(uid: stat.uid_t)(name: Option[String])
    extends UserPrincipal {
  override def getName = {
    name getOrElse NativeUserPrincipalLookupService.getUsername(uid)
  }
}

final case class NativeGroupPrincipal(gid: stat.gid_t)(name: Option[String])
    extends GroupPrincipal {
  override def getName: String = {
    name getOrElse NativeUserPrincipalLookupService.getGroupName(gid)
  }
}

object NativeUserPrincipalLookupService extends UserPrincipalLookupService {
  override def lookupPrincipalByGroupName(group: String): NativeGroupPrincipal =
    Zone { implicit z =>
      val gid = getGroup(toCString(group)).fold {
        throw new UserPrincipalNotFoundException(group)
      }(_._2)

      NativeGroupPrincipal(gid)(Some(group))
    }

  private[attribute] def getGroupName(gid: stat.gid_t): String = Zone {
    implicit z =>
      val buf = alloc[grp.group]

      errno.errno = 0
      val err = grp.getgrgid(gid, buf)

      if (err == 0) {
        fromCString(buf._1)
      } else if (errno.errno == 0) {
        gid.toString
      } else {
        throw UnixException("getgrgid", errno.errno)
      }
  }

  private[attribute] def getUsername(uid: stat.uid_t): String = Zone {
    implicit z =>
      val buf = alloc[pwd.passwd]

      errno.errno = 0
      val err = pwd.getpwuid(uid, buf)

      if (err == 0) {
        fromCString(buf._1)
      } else if (errno.errno == 0) {
        uid.toString
      } else {
        throw UnixException("getpwuid", errno.errno)
      }
  }

  private def getGroup(name: CString)(
      implicit z: Zone): Option[Ptr[grp.group]] = {
    val buf = alloc[grp.group]

    errno.errno = 0
    val err = grp.getgrnam(name, buf)

    if (err == 0) {
      Some(buf)
    } else if (errno.errno == 0) {
      None
    } else {
      throw UnixException("getgrnam", errno.errno)
    }
  }

  override def lookupPrincipalByName(name: String): NativeUserPrincipal = Zone {
    implicit z =>
      val uid = getPasswd(toCString(name)).fold {
        throw new UserPrincipalNotFoundException(name)
      }(_._2)

      NativeUserPrincipal(uid)(Some(name))
  }

  private def getPasswd(name: CString)(
      implicit z: Zone): Option[Ptr[pwd.passwd]] = {
    val buf = alloc[pwd.passwd]

    errno.errno = 0
    val err = pwd.getpwnam(name, buf)

    if (err == 0) {
      Some(buf)
    } else if (errno.errno == 0) {
      None
    } else {
      throw UnixException("getpwnam", errno.errno)
    }
  }
}
