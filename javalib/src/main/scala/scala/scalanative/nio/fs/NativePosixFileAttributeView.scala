package scala.scalanative.nio.fs

import java.util.{HashMap, HashSet, Set}
import java.util.concurrent.TimeUnit
import java.nio.file.{LinkOption, Path}
import java.nio.file.attribute._
import java.io.IOException

import scala.scalanative.native._
import scala.scalanative.posix.{grp, pwd, stat, unistd, utime}
import scala.scalanative.runtime.GC

final class NativePosixFileAttributeView(path: Path,
                                         options: Array[LinkOption])
    extends PosixFileAttributeView
    with FileOwnerAttributeView {

  override val name: String = "posix"

  override def setTimes(lastModifiedTime: FileTime,
                        lastAccessTime: FileTime,
                        createTime: FileTime): Unit = {
    val sb = getStat()

    val buf = GC.malloc(sizeof[utime.utimbuf]).cast[Ptr[utime.utimbuf]]
    !(buf._1) =
      if (lastAccessTime != null) lastAccessTime.to(TimeUnit.SECONDS)
      else !(sb._7)
    !(buf._2) =
      if (lastModifiedTime != null) lastModifiedTime.to(TimeUnit.SECONDS)
      else !(sb._8)
    // createTime is ignored: No posix-y way to set it.
    if (utime.utime(toCString(path.toString), buf) != 0)
      throw new IOException()
  }

  override def setOwner(owner: UserPrincipal): Unit = {
    val passwd = getPasswd(toCString(owner.getName))
    if (unistd.chown(toCString(path.toString), !(passwd._2), -1.toUInt) != 0)
      throw new IOException()
  }

  override def setPermissions(perms: Set[PosixFilePermission]): Unit = {
    var mask = 0.toUInt
    NativePosixFileAttributeView.permMap.foreach {
      case (flag, value) => if (perms.contains(value)) mask = mask | flag
    }
    if (stat.chmod(toCString(path.toString), mask) != 0)
      throw new IOException()
  }

  override def getOwner(): UserPrincipal =
    attributes.owner

  override def setGroup(group: GroupPrincipal): Unit = {
    val _group = getGroup(toCString(group.getName))
    val err    = unistd.chown(toCString(path.toString), -1.toUInt, !(_group._2))

    if (err != 0) {
      throw new IOException()
    }
  }

  override def readAttributes(): BasicFileAttributes =
    attributes

  private lazy val attributes =
    new PosixFileAttributes {
      private val sb       = getStat()
      private val mode     = !(sb._13)
      private val _passwd  = getPasswd(!(sb._4))
      private val _group   = getGroup(!(sb._5))
      override val fileKey = (!(sb._3)).asInstanceOf[Object]

      override val isDirectory    = stat.S_ISDIR(mode) == 1
      override val isRegularFile  = stat.S_ISREG(mode) == 1
      override val isSymbolicLink = stat.S_ISLNK(mode) == 1
      override val isOther        = !isDirectory && !isRegularFile && !isSymbolicLink

      override val lastAccessTime =
        FileTime.from(!(sb._7), TimeUnit.SECONDS)
      override val lastModifiedTime =
        FileTime.from(!(sb._8), TimeUnit.SECONDS)
      override val creationTime =
        FileTime.from(!(sb._9), TimeUnit.SECONDS)

      override val group = new GroupPrincipal {
        override val getName = fromCString(!(_group._1))
      }
      override val owner = new UserPrincipal {
        override val getName = fromCString(!(_passwd._1))
      }
      override val permissions = {
        val set = new HashSet[PosixFilePermission]
        NativePosixFileAttributeView.permMap.foreach {
          case (flag, value) => if ((mode & flag).toInt != 0) set.add(value)
        }
        set
      }

      override val size = !(sb._6)
    }

  override def asMap(): HashMap[String, Object] = {
    val values =
      List(
        "lastModifiedTime" -> attributes.lastModifiedTime,
        "lastAccessTime"   -> attributes.lastAccessTime,
        "creationTime"     -> attributes.creationTime,
        "size"             -> Long.box(attributes.size),
        "isRegularFile"    -> Boolean.box(attributes.isRegularFile),
        "isDirectory"      -> Boolean.box(attributes.isDirectory),
        "isSymbolicLink"   -> Boolean.box(attributes.isSymbolicLink),
        "isOther"          -> Boolean.box(attributes.isOther),
        "fileKey"          -> attributes.fileKey,
        "permissions"      -> attributes.permissions,
        "group"            -> attributes.group
      )

    val map = new HashMap[String, Object]()
    values.foreach { case (k, v) => map.put(k, v) }
    map
  }

  override def setAttribute(name: String, value: Object): Unit =
    (name, value) match {
      case ("lastModifiedTime", time: FileTime) =>
        setTimes(time, null, null)
      case ("lastAccessTime", time: FileTime) =>
        setTimes(null, time, null)
      case ("creationTime", time: FileTime) =>
        setTimes(null, null, time)
      case ("permissions", permissions: Set[PosixFilePermission @unchecked]) =>
        setPermissions(permissions)
      case ("group", group: GroupPrincipal) =>
        setGroup(group)
      case _ =>
        super.setAttribute(name, value)
    }

  private def getStat(): Ptr[stat.stat] = {
    val buf = GC.malloc_atomic(sizeof[stat.stat]).cast[Ptr[stat.stat]]
    val err =
      if (options.contains(LinkOption.NOFOLLOW_LINKS))
        stat.lstat(toCString(path.toString), buf)
      else stat.stat(toCString(path.toString), buf)

    if (err == 0) buf
    else throw new IOException()
  }

  private def getGroup(name: CString): Ptr[grp.group] = {
    val buf = GC.malloc_atomic(sizeof[grp.group]).cast[Ptr[grp.group]]
    val err = grp.getgrnam(name, buf)

    if (err == 0) buf
    else throw new IOException()
  }

  private def getGroup(gid: stat.gid_t): Ptr[grp.group] = {
    val buf = GC.malloc_atomic(sizeof[grp.group]).cast[Ptr[grp.group]]
    val err = grp.getgrgid(gid, buf)

    if (err == 0) buf
    else throw new IOException()
  }

  private def getPasswd(name: CString): Ptr[pwd.passwd] = {
    val buf = GC.malloc_atomic(sizeof[pwd.passwd]).cast[Ptr[pwd.passwd]]
    val err = pwd.getpwnam(name, buf)

    if (err == 0) buf
    else throw new IOException()
  }

  private def getPasswd(uid: stat.uid_t): Ptr[pwd.passwd] = {
    val buf = GC.malloc_atomic(sizeof[pwd.passwd]).cast[Ptr[pwd.passwd]]
    val err = pwd.getpwuid(uid, buf)

    if (err == 0) buf
    else throw new IOException()
  }

}
private object NativePosixFileAttributeView {
  val permMap =
    List(
      (stat.S_IRUSR, PosixFilePermission.OWNER_READ),
      (stat.S_IWUSR, PosixFilePermission.OWNER_WRITE),
      (stat.S_IXUSR, PosixFilePermission.OWNER_EXECUTE),
      (stat.S_IRGRP, PosixFilePermission.GROUP_READ),
      (stat.S_IWGRP, PosixFilePermission.GROUP_WRITE),
      (stat.S_IXGRP, PosixFilePermission.GROUP_EXECUTE),
      (stat.S_IROTH, PosixFilePermission.OTHERS_READ),
      (stat.S_IWOTH, PosixFilePermission.OTHERS_WRITE),
      (stat.S_IXOTH, PosixFilePermission.OTHERS_EXECUTE)
    )
}
