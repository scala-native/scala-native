package scala.scalanative.nio.fs

import java.nio.file.{LinkOption, Path}
import java.nio.file.attribute._
import java.time.Instant, Instant._
import java.util.{HashMap, HashSet, Set}

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._

import scalanative.posix.{fcntl, grp, pwd, time, unistd}
import scalanative.posix.{errno => posixErrno}, posixErrno._
import scalanative.posix.sys.stat, stat._
import scalanative.posix.sys.statOps._
import scalanative.posix.timeOps._

final class NativePosixFileAttributeView(path: Path, options: Array[LinkOption])
    extends PosixFileAttributeView
    with FileOwnerAttributeView {

  private def throwIOException() = {
    // Exception is correct but message may differ from that given by JVM.
    if (errno.errno == posixErrno.EPERM) {
      throw new SecurityException("Operation not permitted")
    } else {
      throw UnixException(path.toString, errno.errno)
    }
  }

  override val name: String = "posix"

  private def fillTimespec(from: FileTime, to: Ptr[timespec]) {

    if (from == null) {
      to.tv_nsec = stat.UTIME_OMIT
    } else {
      // There ways to do this which are faster at runtime, but keep time
      // math centralized in Instant. Easier to get one place correct.
      // Have to leave some opportunities for improvement to the next
      // generation.
      val instant = from.toInstant
      to.tv_sec = instant.getEpochSecond()
      to.tv_nsec = instant.getNano()
    }
  }

  override def setTimes(lastModifiedTime: FileTime,
                        lastAccessTime: FileTime,
                        createTime: FileTime): Unit = Zone { implicit z =>
    val times = alloc[timespec](2)

    fillTimespec(lastAccessTime, times)
    fillTimespec(lastModifiedTime, times + 1)

    // createTime is ignored: No posix-y way to set it.

    errno.errno = 0

    val status =
      stat.utimensat(fcntl.AT_FDCWD, toCString(path.toString), times, 0)
    if (status != 0) {
      throwIOException()
    }
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
      private[this] var st_dev: dev_t         = _
      private[this] var st_rdev: dev_t        = _
      private[this] var st_ino: ino_t         = _
      private[this] var st_uid: uid_t         = _
      private[this] var st_gid: gid_t         = _
      private[this] var st_size: off_t        = _
      private[this] var st_atim: timespec     = _
      private[this] var st_mtim: timespec     = _
      private[this] var st_ctim: timespec     = _
      private[this] var st_blocks: blkcnt_t   = _
      private[this] var st_blksize: blksize_t = _
      private[this] var st_nlink: nlink_t     = _
      private[this] var st_mode: mode_t       = _

      Zone { implicit z =>
        val buf = getStat()
        st_dev = buf.st_dev
        st_rdev = buf.st_rdev
        st_ino = buf.st_ino
        st_uid = buf.st_uid
        st_gid = buf.st_gid
        st_size = buf.st_size
        st_atim = buf.st_atim
        st_mtim = buf.st_mtim
        st_ctim = buf.st_ctim
        st_blocks = buf.st_blocks
        st_blksize = buf.st_blksize
        st_nlink = buf.st_nlink
        st_mode = buf.st_mode
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
        FileTime.from(new Instant(st_atim._1, st_atim._2.toInt))

      override def lastModifiedTime =
        FileTime.from(new Instant(st_mtim._1, st_mtim._2.toInt))

      override def creationTime =
        FileTime.from(new Instant(st_ctim._1, st_ctim._2.toInt))

      override def group = new GroupPrincipal {
        override val getName =
          Zone { implicit z => fromCString(fileGroup()._1) }
      }

      override def owner = new UserPrincipal {
        override val getName =
          Zone { implicit z => fromCString(filePasswd()._1) }
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
    errno.errno = 0
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
