package java.nio.file.attribute

import java.{util => ju}
import java.util.{HashMap, HashSet, Set}
import java.util.concurrent.TimeUnit
import java.nio.file.{LinkOption, Path, PosixException}
import java.nio.file.attribute._

import scalanative.unsigned._
import scalanative.unsafe._

import scalanative.posix._

// Import posix name errno as variable, not class or type.
import scala.scalanative.posix.{errno => posixErrno}, posixErrno.errno

import scalanative.posix.sys.stat

final class PosixFileAttributeViewImpl(path: Path, options: Array[LinkOption])
    extends PosixFileAttributeView
    with FileOwnerAttributeView {
  private def throwIOException() =
    throw PosixException(path.toString, errno)

  override def name(): String = "posix"

  override def setTimes(
      lastModifiedTime: FileTime,
      lastAccessTime: FileTime,
      createTime: FileTime
  ): Unit = Zone { implicit z =>
    import scala.scalanative.posix.sys.statOps.statOps
    val sb = getStat()

    val buf = alloc[utime.utimbuf]()
    buf._1 =
      if (lastAccessTime != null) lastAccessTime.to(TimeUnit.SECONDS).toSize
      else sb.st_atime
    buf._2 =
      if (lastModifiedTime != null) lastModifiedTime.to(TimeUnit.SECONDS).toSize
      else sb.st_mtime
    // createTime is ignored: No posix-y way to set it.
    if (utime.utime(toCString(path.toString), buf) != 0)
      throwIOException()
  }

  override def setOwner(owner: UserPrincipal): Unit =
    Zone { implicit z =>
      val uid = owner match {
        case u: PosixUserPrincipal => u.uid

        case _ =>
          throw new IllegalArgumentException("unsupported UserPrincipal")
      }
      if (unistd.chown(toCString(path.toString), uid, -1.toUInt) != 0) {
        throwIOException()
      }
    }

  override def setPermissions(perms: Set[PosixFilePermission]): Unit =
    Zone { implicit z =>
      var mask = 0.toUInt
      PosixFileAttributeViewImpl.permMap.foreach {
        case (flag, value) => if (perms.contains(value)) mask = mask | flag
      }
      if (stat.chmod(toCString(path.toString), mask) != 0) {
        throwIOException()
      }
    }

  override def getOwner(): UserPrincipal = attributes.owner()

  override def setGroup(group: GroupPrincipal): Unit =
    Zone { implicit z =>
      val gid = group match {
        case g: PosixGroupPrincipal => g.gid

        case _ =>
          throw new IllegalArgumentException("unsupported GroupPrincipal")
      }

      if (unistd.chown(toCString(path.toString), -1.toUInt, gid) != 0) {
        throwIOException()
      }
    }

  override def readAttributes(): BasicFileAttributes = attributes

  private def attributes =
    new PosixFileAttributes {
      private[this] var st_ino: stat.ino_t = _
      private[this] var st_uid: stat.uid_t = _
      private[this] var st_gid: stat.gid_t = _
      private[this] var st_size: stat.off_t = _
      private[this] var st_atime: time.time_t = _
      private[this] var st_mtime: time.time_t = _
      private[this] var st_mode: stat.mode_t = _

      Zone { implicit z =>
        val buf = getStat()
        import scala.scalanative.posix.sys.statOps.statOps

        // Copy only what is referenced below. Save runtime cycles.
        st_ino = buf.st_ino
        st_uid = buf.st_uid
        st_gid = buf.st_gid
        st_size = buf.st_size
        st_atime = buf.st_atime
        st_mtime = buf.st_mtime
        st_mode = buf.st_mode
      }

      override def fileKey() = st_ino.asInstanceOf[Object]

      private lazy val isDir = stat.S_ISDIR(st_mode) == 1
      override def isDirectory() = isDir

      private lazy val isRegular = stat.S_ISREG(st_mode) == 1
      override def isRegularFile() = isRegular

      private lazy val isSymLink = stat.S_ISLNK(st_mode) == 1
      override def isSymbolicLink() = isSymLink

      override def isOther() =
        !isDirectory() && !isRegularFile() && !isSymbolicLink()

      override def lastAccessTime() =
        FileTime.from(st_atime.toLong, TimeUnit.SECONDS)

      override def lastModifiedTime() =
        FileTime.from(st_mtime.toLong, TimeUnit.SECONDS)

      override def creationTime() = {
        // True file creationTime is not accessible in Posix.
        // st_ctime is last metadata change time, NOT original creation time.
        // See the Java 8 documentation "Interface BasicFileAttributes" section
        // for creationTime(). It allows the use of  last-modified-time
        // as a fallback when the true creationTime is unobtainable.

        FileTime.from(st_mtime.toLong, TimeUnit.SECONDS)
      }

      override def group() = PosixGroupPrincipal(st_gid)(None)

      override def owner() = PosixUserPrincipal(st_uid)(None)

      override def permissions() = {
        val set = new ju.HashSet[PosixFilePermission]
        PosixFileAttributeViewImpl.permMap.foreach {
          case (flag, value) =>
            if ((st_mode & flag).toInt != 0) set.add(value)
        }
        set
      }

      override def size() = st_size.toLong
    }

  override def asMap: ju.HashMap[String, Object] = {
    val attrs = attributes
    val values =
      List(
        "lastModifiedTime" -> attrs.lastModifiedTime(),
        "lastAccessTime" -> attrs.lastAccessTime(),
        "creationTime" -> attrs.creationTime(),
        "size" -> Long.box(attrs.size()),
        "isRegularFile" -> Boolean.box(attrs.isRegularFile()),
        "isDirectory" -> Boolean.box(attrs.isDirectory()),
        "isSymbolicLink" -> Boolean.box(attrs.isSymbolicLink()),
        "isOther" -> Boolean.box(attrs.isOther()),
        "fileKey" -> attrs.fileKey(),
        "permissions" -> attrs.permissions(),
        "group" -> attrs.group()
      )

    val map = new ju.HashMap[String, Object]()
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
    val buf = alloc[stat.stat]()
    val err =
      if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
        stat.lstat(toCString(path.toString), buf)
      } else {
        stat.stat(toCString(path.toString), buf)
      }

    if (err == 0) buf
    else throwIOException()
  }
}

private object PosixFileAttributeViewImpl {
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
