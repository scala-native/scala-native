package scala.scalanative.nio.fs

import java.util.{HashMap, HashSet, Set}
import java.util.concurrent.TimeUnit
import java.nio.file.{LinkOption, Path}
import java.nio.file.attribute._
import java.io.IOException

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._
import scalanative.posix.{errno => e, grp, pwd, unistd, time, utime}, e._
import scalanative.posix.sys.stat

final class NativePosixFileAttributeView(path: Path, options: Array[LinkOption])
    extends PosixFileAttributeView
    with FileOwnerAttributeView {
  private def throwIOException() =
    throw UnixException(path.toString, errno.errno)
  override val name: String = "posix"

  override def setTimes(lastModifiedTime: FileTime,
                        lastAccessTime: FileTime,
                        createTime: FileTime): Unit = Zone { implicit z =>
    val sb = getStat()

    val buf = alloc[utime.utimbuf]
    buf._1 =
      if (lastAccessTime != null) lastAccessTime.to(TimeUnit.SECONDS)
      else sb._7
    buf._2 =
      if (lastModifiedTime != null) lastModifiedTime.to(TimeUnit.SECONDS)
      else sb._8
    // createTime is ignored: No posix-y way to set it.
    if (utime.utime(toCString(path.toString), buf) != 0)
      throwIOException()
  }

  override def setOwner(owner: UserPrincipal): Unit =
    Zone { implicit z =>
      val passwd = getPasswd(toCString(owner.getName))
      if (unistd.chown(toCString(path.toString), passwd._2, -1.toUInt) != 0)
        throwIOException()
    }

  override def setPermissions(perms: Set[PosixFilePermission]): Unit =
    Zone { implicit z =>
      var mask = 0.toUInt
      NativePosixFileAttributeView.permMap.foreach {
        case (flag, value) => if (perms.contains(value)) mask = mask | flag
      }
      if (stat.chmod(toCString(path.toString), mask) != 0) {
        throwIOException()
      }
    }

  override def getOwner(): UserPrincipal = attributes.owner

  override def setGroup(group: GroupPrincipal): Unit =
    Zone { implicit z =>
      val _group = getGroup(toCString(group.getName))
      val err    = unistd.chown(toCString(path.toString), -1.toUInt, _group._2)

      if (err != 0) {
        throwIOException()
      }
    }

  override def readAttributes(): BasicFileAttributes = attributes

  private def attributes =
    new PosixFileAttributes {
      private[this] var st_dev: stat.dev_t         = _
      private[this] var st_rdev: stat.dev_t        = _
      private[this] var st_ino: stat.ino_t         = _
      private[this] var st_uid: stat.uid_t         = _
      private[this] var st_gid: stat.gid_t         = _
      private[this] var st_size: unistd.off_t      = _
      private[this] var st_atime: time.time_t      = _
      private[this] var st_mtime: time.time_t      = _
      private[this] var st_ctime: time.time_t      = _
      private[this] var st_blocks: stat.blkcnt_t   = _
      private[this] var st_blksize: stat.blksize_t = _
      private[this] var st_nlink: stat.nlink_t     = _
      private[this] var st_mode: stat.mode_t       = _

      Zone { implicit z =>
        val buf = getStat()
        st_dev = buf._1
        st_rdev = buf._2
        st_ino = buf._3
        st_uid = buf._4
        st_gid = buf._5
        st_size = buf._6
        st_atime = buf._7
        st_mtime = buf._8
        st_ctime = buf._9
        st_blocks = buf._10
        st_blksize = buf._11
        st_nlink = buf._12
        st_mode = buf._13
      }

      private def filePasswd()(implicit z: Zone) =
        getPasswd(st_uid)

      private def fileGroup()(implicit z: Zone) =
        getGroup(st_gid)

      override def fileKey = st_ino.asInstanceOf[Object]

      override lazy val isDirectory =
        stat.S_ISDIR(st_mode) == 1

      override lazy val isRegularFile =
        stat.S_ISREG(st_mode) == 1

      override lazy val isSymbolicLink =
        stat.S_ISLNK(st_mode) == 1

      override lazy val isOther =
        !isDirectory && !isRegularFile && !isSymbolicLink

      override def lastAccessTime =
        FileTime.from(st_atime, TimeUnit.SECONDS)

      override def lastModifiedTime =
        FileTime.from(st_mtime, TimeUnit.SECONDS)

      override def creationTime =
        FileTime.from(st_ctime, TimeUnit.SECONDS)

      override def group = new GroupPrincipal {
        override val getName =
          Zone { implicit z =>
            fromCString(fileGroup()._1)
          }
      }

      override def owner = new UserPrincipal {
        override val getName =
          Zone { implicit z =>
            fromCString(filePasswd()._1)
          }
      }

      override def permissions = {
        val set = new HashSet[PosixFilePermission]
        NativePosixFileAttributeView.permMap.foreach {
          case (flag, value) =>
            if ((st_mode & flag).toInt != 0) set.add(value)
        }
        set
      }

      override def size = st_size
    }

  override def asMap(): HashMap[String, Object] = {
    val attrs = attributes
    val values =
      List(
        "lastModifiedTime" -> attrs.lastModifiedTime,
        "lastAccessTime"   -> attrs.lastAccessTime,
        "creationTime"     -> attrs.creationTime,
        "size"             -> Long.box(attrs.size),
        "isRegularFile"    -> Boolean.box(attrs.isRegularFile),
        "isDirectory"      -> Boolean.box(attrs.isDirectory),
        "isSymbolicLink"   -> Boolean.box(attrs.isSymbolicLink),
        "isOther"          -> Boolean.box(attrs.isOther),
        "fileKey"          -> attrs.fileKey,
        "permissions"      -> attrs.permissions,
        "group"            -> attrs.group
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

  private def getStat()(implicit z: Zone): Ptr[stat.stat] = {
    val buf = alloc[stat.stat]
    val err =
      if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
        stat.lstat(toCString(path.toString), buf)
      } else {
        stat.stat(toCString(path.toString), buf)
      }

    if (err == 0) buf
    else throwIOException()
  }

  private def getGroup(name: CString)(implicit z: Zone): Ptr[grp.group] = {
    val buf = alloc[grp.group]
    val err = grp.getgrnam(name, buf)

    if (err == 0) buf
    else throwIOException()
  }

  private def getGroup(gid: stat.gid_t)(implicit z: Zone): Ptr[grp.group] = {
    val buf = alloc[grp.group]
    val err = grp.getgrgid(gid, buf)

    if (err == 0) buf
    else throwIOException()
  }

  private def getPasswd(name: CString)(implicit z: Zone): Ptr[pwd.passwd] = {
    val buf = alloc[pwd.passwd]
    val err = pwd.getpwnam(name, buf)

    if (err == 0) buf
    else throwIOException()
  }

  private def getPasswd(uid: stat.uid_t)(implicit z: Zone): Ptr[pwd.passwd] = {
    val buf = alloc[pwd.passwd]
    val err = pwd.getpwuid(uid, buf)

    if (err == 0) buf
    else throwIOException()
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
