package java.nio.file.attribute

import scalanative.unsigned._
import scalanative.unsafe._

import scalanative.posix._

// Import posix name errno as variable, not class or type.
import scala.scalanative.posix.{errno => posixErrno}, posixErrno.errno

import scalanative.posix.sys.stat

import scala.scalanative.nio.fs.unix.UnixException

final case class PosixUserPrincipal(uid: stat.uid_t)(name: Option[String])
    extends UserPrincipal {
  override def getName() = {
    name getOrElse PosixUserPrincipalLookupService.getUsername(uid)
  }
}

final case class PosixGroupPrincipal(gid: stat.gid_t)(name: Option[String])
    extends GroupPrincipal {
  override def getName(): String = {
    name getOrElse PosixUserPrincipalLookupService.getGroupName(gid)
  }
}

object PosixUserPrincipalLookupService extends UserPrincipalLookupService {
  override def lookupPrincipalByGroupName(group: String): PosixGroupPrincipal =
    Zone.acquire { implicit z =>
      val gid = getGroup(toCString(group)).fold {
        try {
          group.toInt.toUInt
        } catch {
          case _: NumberFormatException =>
            throw new UserPrincipalNotFoundException(group)
        }
      }(_._2)

      PosixGroupPrincipal(gid)(Some(group))
    }

  private[attribute] def getGroupName(gid: stat.gid_t): String = Zone.acquire {
    implicit z =>
      val buf = alloc[grp.group]()

      errno = 0
      val err = grp.getgrgid(gid, buf)

      if (err == 0) {
        fromCString(buf._1)
      } else if (errno == 0) {
        gid.toString
      } else {
        throw UnixException("getgrgid", errno)
      }
  }

  private[attribute] def getUsername(uid: stat.uid_t): String = Zone.acquire {
    implicit z =>
      val buf = alloc[pwd.passwd]()

      errno = 0
      val err = pwd.getpwuid(uid, buf)

      if (err == 0) {
        fromCString(buf._1)
      } else if (errno == 0) {
        uid.toString
      } else {
        throw UnixException("getpwuid", errno)
      }
  }

  private def getGroup(
      name: CString
  )(implicit z: Zone): Option[Ptr[grp.group]] = {
    val buf = alloc[grp.group]()

    errno = 0
    val err = grp.getgrnam(name, buf)

    if (err == 0) {
      Some(buf)
    } else if (errno == 0) {
      None
    } else {
      throw UnixException("getgrnam", errno)
    }
  }

  override def lookupPrincipalByName(name: String): PosixUserPrincipal =
    Zone.acquire { implicit z =>
      val uid = getPasswd(toCString(name)).fold {
        try {
          name.toInt.toUInt
        } catch {
          case _: NumberFormatException =>
            throw new UserPrincipalNotFoundException(name)
        }
      }(_._2)

      PosixUserPrincipal(uid)(Some(name))
    }

  private def getPasswd(
      name: CString
  )(implicit z: Zone): Option[Ptr[pwd.passwd]] = {
    val buf = alloc[pwd.passwd]()

    errno = 0
    val err = pwd.getpwnam(name, buf)

    if (err == 0) {
      Some(buf)
    } else if (errno == 0) {
      None
    } else {
      throw UnixException("getpwnam", errno)
    }
  }
}
