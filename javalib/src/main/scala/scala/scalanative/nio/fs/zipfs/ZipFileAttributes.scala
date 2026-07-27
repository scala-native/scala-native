package scala.scalanative.nio.fs.zipfs

import java.nio.file.attribute.{BasicFileAttributes, FileTime}

/** Read-only `BasicFileAttributes` for a zip entry or synthetic directory. */
private[zipfs] final class ZipFileAttributes(
    private val _size: Long,
    private val _mtime: FileTime,
    private val _atime: FileTime,
    private val _ctime: FileTime,
    private val _isDirectory: Boolean
) extends BasicFileAttributes {

  override def lastModifiedTime(): FileTime = _mtime
  override def lastAccessTime(): FileTime = _atime
  override def creationTime(): FileTime = _ctime

  override def isRegularFile(): Boolean = !_isDirectory
  override def isDirectory(): Boolean = _isDirectory
  override def isSymbolicLink(): Boolean = false
  override def isOther(): Boolean = false

  override def size(): Long = _size

  // No meaningful identity for a zip entry beyond its name; ZipPath already
  // serves as the canonical identity, so null is fine here.
  override def fileKey(): Object = null
}

private[zipfs] object ZipFileAttributes {
  // Fallback timestamp when ZipEntry has no usable mtime (getTime() == -1
  // and no UT/NTFS FileTime fields). Matches what jdk.zipfs does in this
  // situation.
  val EpochZero: FileTime = FileTime.fromMillis(0L)
}
