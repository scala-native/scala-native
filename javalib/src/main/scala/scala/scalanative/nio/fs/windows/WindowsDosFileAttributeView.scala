package scala.scalanative.nio.fs.windows

import java.util.{HashMap => JHashMap}
import java.util.concurrent.TimeUnit
import java.nio.file.{LinkOption, Path}
import java.nio.file.attribute._
import java.lang.{Boolean => JBoolean}
import scalanative.unsigned._
import scalanative.unsafe._
import scala.scalanative.windows._
import scala.scalanative.windows.MinWinBaseApi.{FileTime => WinFileTime, _}
import scala.scalanative.windows.MinWinBaseApiOps.FileTimeOps._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.FileApiOps._
import scala.scalanative.windows.winnt.AccessRights._
import java.nio.file.WindowsException
import java.util.WindowsHelperMethods._

final class WindowsDosFileAttributeView(path: Path, options: Array[LinkOption])
    extends DosFileAttributeView {
  def name(): String = "dos"

  private val followLinks = !options.contains(LinkOption.NOFOLLOW_LINKS)
  private val fileOpeningFlags = {
    FILE_FLAG_BACKUP_SEMANTICS | {
      if (followLinks) 0.toUInt
      else FILE_FLAG_OPEN_REPARSE_POINT
    }
  }

  override def setAttribute(name: String, value: Object): Unit =
    (name, value) match {
      case ("lastModifiedTime", time: FileTime) => setTimes(time, null, null)
      case ("lastAccessTime", time: FileTime)   => setTimes(null, time, null)
      case ("creationTime", time: FileTime)     => setTimes(null, null, time)
      case ("readonly", v: JBoolean)            => setReadOnly(v)
      case ("hidden", v: JBoolean)              => setHidden(v)
      case ("system", v: JBoolean)              => setSystem(v)
      case ("archive", v: JBoolean)             => setArchive(v)
      case _ => super.setAttribute(name, value)
    }

  override def asMap: JHashMap[String, Object] = {
    val map = new JHashMap[String, Object]()
    map.put("lastModifiedTime", attributes.lastModifiedTime())
    map.put("lastAccessTime", attributes.lastAccessTime())
    map.put("creationTime", attributes.creationTime())
    map.put("fileKey", attributes.fileKey())
    map.put("size", Long.box(attributes.size()))
    map.put("isRegularFile", Boolean.box(attributes.isRegularFile()))
    map.put("isDirectory", Boolean.box(attributes.isDirectory()))
    map.put("isSymbolicLink", Boolean.box(attributes.isSymbolicLink()))
    map.put("isOther", Boolean.box(attributes.isOther()))
    map.put("readonly", Boolean.box(attributes.isReadOnly()))
    map.put("hidden", Boolean.box(attributes.isHidden()))
    map.put("system", Boolean.box(attributes.isSystem()))
    map.put("archive", Boolean.box(attributes.isArchive()))
    map
  }

  def setArchive(value: Boolean): Unit =
    setWinAttribute(FILE_ATTRIBUTE_ARCHIVE, value)

  def setHidden(value: Boolean): Unit =
    setWinAttribute(FILE_ATTRIBUTE_HIDDEN, value)

  def setReadOnly(value: Boolean): Unit =
    setWinAttribute(FILE_ATTRIBUTE_READONLY, value)

  def setSystem(value: Boolean): Unit =
    setWinAttribute(FILE_ATTRIBUTE_SYSTEM, value)

  def setTimes(
      lastModifiedTime: FileTime,
      lastAccessTime: FileTime,
      createTime: FileTime
  ): Unit = Zone { implicit z =>
    def setOrNull(ref: Ptr[WinFileTime], value: FileTime): Ptr[WinFileTime] = {
      if (value == null) null
      else {
        !ref = toWindowsFileTime(value)
        ref
      }
    }

    withFileOpen(
      pathAbs,
      access = FILE_GENERIC_WRITE,
      attributes = fileOpeningFlags
    ) { handle =>
      val create, access, write = stackalloc[WinFileTime]()
      if (!SetFileTime(
            handle,
            creationTime = setOrNull(create, createTime),
            lastAccessTime = setOrNull(access, lastAccessTime),
            lastWriteTime = setOrNull(write, lastModifiedTime)
          )) {
        throw WindowsException("Failed to set file times")
      }
    }
  }

  def readAttributes(): DosFileAttributes = attributes

  private lazy val attributes: DosFileAttributes = Zone { implicit z: Zone =>
    val fileInfo = alloc[ByHandleFileInformation]()

    withFileOpen(
      pathAbs,
      access = FILE_READ_ATTRIBUTES,
      attributes = fileOpeningFlags
    ) {
      FileApi.GetFileInformationByHandle(_, fileInfo)
    }

    new DosFileAttributes {
      class DosFileKey(volumeId: DWord, fileIndex: ULargeInteger)

      private val attrs = fileInfo.fileAttributes
      private val createdAt = toFileTime(fileInfo.creationTime)
      private val accessedAt = toFileTime(fileInfo.lastAccessTime)
      private val modifiedAt = toFileTime(fileInfo.lastWriteTime)
      private val fileSize = fileInfo.fileSize
      private val dosFileKey =
        new DosFileKey(
          volumeId = fileInfo.volumeSerialNumber,
          fileIndex = fileInfo.fileIndex
        )

      def creationTime(): FileTime = createdAt
      def lastAccessTime(): FileTime = accessedAt
      def lastModifiedTime(): FileTime = modifiedAt
      def fileKey(): Object = dosFileKey
      def size(): Long = fileSize.toLong

      // to replace with checking reparse tag
      def isSymbolicLink(): Boolean = hasAttrSet(FILE_ATTRIBUTE_REPARSE_POINT)
      def isDirectory(): Boolean = hasAttrSet(FILE_ATTRIBUTE_DIRECTORY)
      def isOther(): Boolean =
        hasAttrSet(FILE_ATTRIBUTE_REPARSE_POINT | FILE_ATTRIBUTE_DEVICE)
      def isRegularFile(): Boolean = {
        !isSymbolicLink() &&
        !isDirectory() &&
        !isOther()
      }

      def isArchive(): Boolean = hasAttrSet(FILE_ATTRIBUTE_ARCHIVE)
      def isHidden(): Boolean = hasAttrSet(FILE_ATTRIBUTE_HIDDEN)
      def isReadOnly(): Boolean = hasAttrSet(FILE_ATTRIBUTE_READONLY)
      def isSystem(): Boolean = hasAttrSet(FILE_ATTRIBUTE_SYSTEM)

      private def hasAttrSet(attr: DWord): Boolean =
        (attrs & attr) != 0.toUInt
    }
  }

  private def setWinAttribute(attribute: DWord, enabled: Boolean): Unit = Zone {
    implicit z =>
      val filename = toCWideStringUTF16LE(pathAbs)
      val previousAttrs = FileApi.GetFileAttributesW(filename)
      def setNewAttrs(): Boolean = {
        val newAttributes =
          if (enabled) previousAttrs | attribute
          else previousAttrs & ~attribute
        FileApi.SetFileAttributesW(filename, newAttributes)
      }

      if (previousAttrs == INVALID_FILE_ATTRIBUTES || !setNewAttrs()) {
        throw WindowsException("Failed to set file attributes")
      }
  }

  private lazy val pathAbs = path.toAbsolutePath().toString

  private def toFileTime(winFileTime: WinFileTime): FileTime = {
    try {
      val withEpochAdjustment = winFileTime.toLong - UnixEpochDifference
      val windowsNanos = Math.multiplyExact(withEpochAdjustment, EpochInterval)
      FileTime.from(windowsNanos, TimeUnit.NANOSECONDS)
    } catch {
      case _: ArithmeticException =>
        val seconds = toUnixEpochMillis(winFileTime)
        FileTime.from(seconds, TimeUnit.MILLISECONDS)
    }
  }

  private def toWindowsFileTime(fileTime: FileTime): WinFileTime = {
    val asWindowsEpoch = fileTime.to(TimeUnit.NANOSECONDS) / EpochInterval
    (asWindowsEpoch + UnixEpochDifference).toULong
  }
}
