package scala.scalanative.nio.fs.zip

import java.io.{File, IOException}
import java.lang.{StringBuilder => JStringBuilder}
import java.net.URI
import java.nio.file._
import java.util
import java.util.{Iterator => JIterator, Objects}

/** A Path implementation for entries within a ZipFileSystem.
 *
 *  Zip paths always use "/" as separator. They are either absolute (starting
 *  with "/") or relative. The root path is "/". Directory entries in a zip
 *  archive typically end with "/" but the path itself does not preserve that
 *  trailing slash — it is only used when looking up entries.
 */
final class ZipPath private[zip] (
    private[zip] val fileSystem: ZipFileSystem,
    private val path: String
) extends Path {

  // --- Pre-computed segment offsets, lazily initialized ---

  private lazy val offsets: Array[Int] = computeOffsets()

  private def computeOffsets(): Array[Int] = {
    if (path.isEmpty) return Array.empty
    val result = new java.util.ArrayList[Integer]()
    var start = if (isAbsolute()) 1 else 0
    var i = start
    while (i < path.length) {
      if (path.charAt(i) == '/') {
        if (i > start) result.add(start)
        start = i + 1
      }
      i += 1
    }
    if (start < path.length) result.add(start)
    val arr = new Array[Int](result.size())
    var j = 0
    while (j < arr.length) {
      arr(j) = result.get(j)
      j += 1
    }
    arr
  }

  // --- Path interface ---

  override def getFileSystem(): ZipFileSystem = fileSystem

  override def isAbsolute(): Boolean =
    path.nonEmpty && path.charAt(0) == '/'

  override def getRoot(): ZipPath =
    if (isAbsolute()) new ZipPath(fileSystem, "/") else null

  override def getFileName(): ZipPath =
    if (path.isEmpty) null
    else if (offsets.isEmpty) this
    else {
      val lastOff = offsets(offsets.length - 1)
      val name = path.substring(lastOff)
      new ZipPath(fileSystem, name)
    }

  override def getParent(): ZipPath = {
    if (offsets.length <= 1) {
      if (isAbsolute()) getRoot() else null
    } else {
      val lastOff = offsets(offsets.length - 1)
      // trim trailing slash
      new ZipPath(fileSystem, path.substring(0, lastOff - 1))
    }
  }

  override def getNameCount(): Int = offsets.length

  override def getName(index: Int): ZipPath = {
    if (index < 0 || index >= offsets.length)
      throw new IllegalArgumentException()
    val begin = offsets(index)
    val end =
      if (index + 1 < offsets.length) offsets(index + 1) - 1
      else path.length
    new ZipPath(fileSystem, path.substring(begin, end))
  }

  override def subpath(beginIndex: Int, endIndex: Int): ZipPath = {
    if (beginIndex < 0 || beginIndex >= offsets.length ||
        endIndex <= beginIndex || endIndex > offsets.length)
      throw new IllegalArgumentException()
    val begin = offsets(beginIndex)
    val end =
      if (endIndex < offsets.length) offsets(endIndex) - 1
      else path.length
    new ZipPath(fileSystem, path.substring(begin, end))
  }

  override def startsWith(other: Path): Boolean = {
    val o = checkPath(other)
    if (o.path.isEmpty) return path.isEmpty
    if (isAbsolute() != o.isAbsolute()) return false
    val oCount = o.getNameCount()
    if (oCount > getNameCount()) return false
    var i = 0
    while (i < oCount) {
      if (getName(i).path != o.getName(i).path) return false
      i += 1
    }
    true
  }

  override def startsWith(other: String): Boolean =
    startsWith(new ZipPath(fileSystem, other))

  override def endsWith(other: Path): Boolean = {
    val o = checkPath(other)
    if (o.path.isEmpty) return path.isEmpty
    val oCount = o.getNameCount()
    val thisCount = getNameCount()
    if (oCount > thisCount) return false
    if (o.isAbsolute() && (!isAbsolute() || oCount != thisCount)) return false
    val offset = thisCount - oCount
    var i = 0
    while (i < oCount) {
      if (getName(offset + i).path != o.getName(i).path) return false
      i += 1
    }
    true
  }

  override def endsWith(other: String): Boolean =
    endsWith(new ZipPath(fileSystem, other))

  override def normalize(): ZipPath = {
    val parts = new java.util.ArrayList[String]()
    var i = 0
    while (i < getNameCount()) {
      val seg = getName(i).path
      if (seg == "..") {
        if (!parts.isEmpty()) parts.remove(parts.size() - 1)
      } else if (seg != ".") {
        parts.add(seg)
      }
      i += 1
    }
    val sb = new JStringBuilder()
    if (isAbsolute()) sb.append('/')
    var j = 0
    while (j < parts.size()) {
      if (j > 0) sb.append('/')
      sb.append(parts.get(j))
      j += 1
    }
    val result = sb.toString
    if (result.isEmpty && isAbsolute()) new ZipPath(fileSystem, "/")
    else new ZipPath(fileSystem, result)
  }

  override def resolve(other: Path): ZipPath = {
    val o = checkPath(other)
    if (o.isAbsolute() || path.isEmpty) o
    else if (o.path.isEmpty) this
    else new ZipPath(fileSystem, path + "/" + o.path)
  }

  override def resolve(other: String): ZipPath =
    resolve(new ZipPath(fileSystem, other))

  override def resolveSibling(other: Path): ZipPath = {
    val parent = getParent()
    if (parent == null) checkPath(other)
    else parent.resolve(other)
  }

  override def resolveSibling(other: String): ZipPath =
    resolveSibling(new ZipPath(fileSystem, other))

  override def relativize(other: Path): ZipPath = {
    val o = checkPath(other)
    if (isAbsolute() != o.isAbsolute())
      throw new IllegalArgumentException(
        "Cannot relativize paths with different roots"
      )
    if (path.isEmpty) return o

    val thisNorm = normalize()
    val otherNorm = o.normalize()
    val thisCount = thisNorm.getNameCount()
    val otherCount = otherNorm.getNameCount()

    var common = 0
    val limit = Math.min(thisCount, otherCount)
    while (common < limit &&
           thisNorm.getName(common).path == otherNorm.getName(common).path) {
      common += 1
    }

    val sb = new JStringBuilder()
    var i = common
    while (i < thisCount) {
      if (sb.length() > 0) sb.append('/')
      sb.append("..")
      i += 1
    }
    var j = common
    while (j < otherCount) {
      if (sb.length() > 0) sb.append('/')
      sb.append(otherNorm.getName(j).path)
      j += 1
    }
    new ZipPath(fileSystem, sb.toString)
  }

  override def toUri(): URI = {
    // jar:file:///path/to/archive.jar!/entry/path
    val jarUri = fileSystem.getZipPath().toUri()
    URI.create("jar:" + jarUri + "!" + toAbsolutePath().path)
  }

  override def toAbsolutePath(): ZipPath =
    if (isAbsolute()) this
    else new ZipPath(fileSystem, "/" + path)

  override def toRealPath(options: Array[LinkOption]): ZipPath =
    toAbsolutePath().normalize()

  override def toFile(): File =
    throw new UnsupportedOperationException(
      "Path inside a zip file cannot be converted to a File"
    )

  override def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[_]],
      modifiers: Array[WatchEvent.Modifier]
  ): WatchKey =
    throw new UnsupportedOperationException()

  override def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[_]]
  ): WatchKey =
    throw new UnsupportedOperationException()

  override def iterator(): JIterator[Path] = {
    val paths = new java.util.ArrayList[Path]()
    var i = 0
    while (i < getNameCount()) {
      paths.add(getName(i))
      i += 1
    }
    paths.iterator()
  }

  override def compareTo(other: Path): Int = {
    val o = other.asInstanceOf[ZipPath]
    path.compareTo(o.path)
  }

  override def toString(): String = path

  override def equals(obj: Any): Boolean = obj match {
    case other: ZipPath =>
      fileSystem == other.fileSystem && path == other.path
    case _ => false
  }

  override def hashCode(): Int =
    path.hashCode() * 31 + fileSystem.hashCode()

  // --- Internal helpers ---

  /** Return the entry name to look up in the ZipFile.
   *  Strips leading "/" for absolute paths.
   */
  private[zip] def entryName: String =
    if (path.startsWith("/")) path.substring(1)
    else path

  /** Return the entry name as a directory (with trailing "/"). */
  private[zip] def entryNameAsDir: String = {
    val n = entryName
    if (n.endsWith("/")) n else n + "/"
  }

  private def checkPath(other: Path): ZipPath = other match {
    case zp: ZipPath if zp.fileSystem == fileSystem => zp
    case _ =>
      throw new ProviderMismatchException(
        s"Expected ZipPath from same filesystem, got: $other"
      )
  }
}
