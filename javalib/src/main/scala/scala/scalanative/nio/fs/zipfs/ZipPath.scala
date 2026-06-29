package scala.scalanative.nio.fs.zipfs

import java.io.File
import java.net.URI
import java.nio.file._
import java.util.Iterator

import scala.collection.mutable.ListBuffer

/** Immutable, string-oriented path on a [[ZipFileSystem]].
 *
 *  Entry names are UTF-8 `java.lang.String`s, separator is `'/'`. An "absolute"
 *  `ZipPath` starts with `/` and is rooted at the archive root; relative paths
 *  resolve against the file system's default directory `"/"`.
 *
 *  This is a value type — the only method that performs I/O is `toRealPath`
 *  (existence check). `toFile` and `register` throw
 *  [[UnsupportedOperationException]] as paths on a non-default file system
 *  cannot supply them.
 */
final class ZipPath private[zipfs] (
    private val fs: ZipFileSystem,
    private val rawPath: String
) extends Path {

  import ZipPath._

  // Canonical string form: redundant slashes collapsed, trailing slash
  // stripped (except for the root "/").
  private val path: String = normalizeSlashes(rawPath)

  // Offsets table: offsets(i) is the index of the slash that PRECEDES name i,
  // or -1 for the first name in a relative path. offsets(nameCount) is the
  // length of `path`. nameCount = offsets.length - 1.
  private val offsets: Array[Int] = computeOffsets(path)

  private val _isAbsolute: Boolean = path.startsWith("/")

  override def getFileSystem(): FileSystem = fs

  override def isAbsolute(): Boolean = _isAbsolute

  override def getRoot(): Path =
    if (_isAbsolute) fs.rootPath else null

  override def getFileName(): Path = {
    val n = getNameCount()
    if (n == 0) null
    else getName(n - 1)
  }

  override def getParent(): Path = {
    val n = getNameCount()
    if (n == 0) null
    else if (n == 1 && !_isAbsolute) null
    else {
      val end = offsets(n - 1)
      val parentStr =
        if (_isAbsolute && end == 0) "/"
        else if (_isAbsolute) path.substring(0, end)
        else path.substring(0, end)
      new ZipPath(fs, parentStr)
    }
  }

  override def getNameCount(): Int = offsets.length - 1

  private def nameAt(index: Int): String = {
    val n = getNameCount()
    if (index < 0 || index >= n)
      throw new IllegalArgumentException
    val start = offsets(index) + 1
    val end = offsets(index + 1)
    path.substring(start, end)
  }

  override def getName(index: Int): Path =
    new ZipPath(fs, nameAt(index))

  override def subpath(beginIndex: Int, endIndex: Int): Path = {
    val n = getNameCount()
    if (beginIndex < 0 || beginIndex >= n || endIndex <= beginIndex || endIndex > n)
      throw new IllegalArgumentException
    val sb = new java.lang.StringBuilder()
    var i = beginIndex
    while (i < endIndex) {
      if (i > beginIndex) sb.append('/')
      sb.append(nameAt(i))
      i += 1
    }
    new ZipPath(fs, sb.toString)
  }

  override def startsWith(other: Path): Boolean = other match {
    case zp: ZipPath if zp.fs eq fs =>
      if (zp._isAbsolute != _isAbsolute) false
      else if (zp.getNameCount() > getNameCount()) false
      else {
        var i = 0
        val n = zp.getNameCount()
        var ok = true
        while (ok && i < n) {
          if (nameAt(i) != zp.nameAt(i)) ok = false
          i += 1
        }
        ok
      }
    case _ => false
  }

  override def startsWith(other: String): Boolean =
    startsWith(new ZipPath(fs, other))

  override def endsWith(other: Path): Boolean = other match {
    case zp: ZipPath if zp.fs eq fs =>
      val on = zp.getNameCount()
      val tn = getNameCount()
      if (zp._isAbsolute) {
        _isAbsolute && tn == on && {
          var i = 0; var ok = true
          while (ok && i < on) {
            if (nameAt(i) != zp.nameAt(i)) ok = false; i += 1
          }
          ok
        }
      } else if (on > tn) false
      else {
        var i = 0; var ok = true
        while (ok && i < on) {
          if (nameAt(tn - on + i) != zp.nameAt(i)) ok = false
          i += 1
        }
        ok
      }
    case _ => false
  }

  override def endsWith(other: String): Boolean =
    endsWith(new ZipPath(fs, other))

  override def normalize(): Path = {
    val n = getNameCount()
    if (n == 0) return this
    val stack = new ListBuffer[String]()
    var i = 0
    var changed = false
    while (i < n) {
      val seg = nameAt(i)
      seg match {
        case "."  => changed = true
        case ".." =>
          if (stack.nonEmpty && stack.last != "..") {
            stack.remove(stack.length - 1)
            changed = true
          } else if (_isAbsolute) {
            // ".." above root: drop.
            changed = true
          } else {
            stack += ".."
          }
        case other =>
          stack += other
      }
      i += 1
    }
    if (!changed) return this
    val sb = new java.lang.StringBuilder()
    if (_isAbsolute) sb.append('/')
    var first = true
    val it = stack.iterator
    while (it.hasNext) {
      if (!first) sb.append('/')
      sb.append(it.next())
      first = false
    }
    val s = sb.toString
    new ZipPath(fs, if (s.isEmpty && _isAbsolute) "/" else s)
  }

  override def resolve(other: Path): Path = other match {
    case zp: ZipPath =>
      if (zp._isAbsolute) zp
      else if (zp.path.isEmpty) this
      else if (path.isEmpty) zp
      else if (path == "/") new ZipPath(fs, "/" + zp.path)
      else new ZipPath(fs, path + "/" + zp.path)
    case _ =>
      throw new ProviderMismatchException()
  }

  override def resolve(other: String): Path =
    resolve(new ZipPath(fs, other))

  override def resolveSibling(other: Path): Path = {
    val p = getParent()
    if (p == null) other
    else p.resolve(other)
  }

  override def resolveSibling(other: String): Path =
    resolveSibling(new ZipPath(fs, other))

  override def relativize(other: Path): Path = other match {
    case zp: ZipPath =>
      if (zp.fs ne fs) throw new ProviderMismatchException()
      if (_isAbsolute != zp._isAbsolute)
        throw new IllegalArgumentException(
          "'other' is different type of Path"
        )
      if (path == zp.path) new ZipPath(fs, "")
      else if (path.isEmpty) zp
      else {
        val tn = getNameCount()
        val on = zp.getNameCount()
        var common = 0
        val min = math.min(tn, on)
        while (common < min && nameAt(common) == zp.nameAt(common)) common += 1
        val sb = new java.lang.StringBuilder()
        var i = common
        while (i < tn) {
          if (sb.length() > 0) sb.append('/')
          sb.append("..")
          i += 1
        }
        var j = common
        while (j < on) {
          if (sb.length() > 0) sb.append('/')
          sb.append(zp.nameAt(j))
          j += 1
        }
        new ZipPath(fs, sb.toString)
      }
    case _ => throw new ProviderMismatchException()
  }

  override def toUri(): URI = {
    val abs = toAbsolutePath().toString
    // `jar:<archive-uri>!<abs>` where <abs> starts with "/".
    // Use the (scheme, ssp, fragment) constructor so URI-special characters
    // in entry names (space, `#`, `?`, ...) are percent-encoded rather than
    // parsed as URI structure (e.g. `/a#b` would become a fragment with the
    // raw-string constructor).
    new URI("jar", fs.archivePath.toUri().toString + "!" + abs, null)
  }

  override def toAbsolutePath(): Path =
    if (_isAbsolute) this
    else if (path.isEmpty) new ZipPath(fs, "/")
    else new ZipPath(fs, "/" + path)

  override def toRealPath(options: Array[LinkOption]): Path = {
    // ZipFS has no symbolic links, so the real path is the absolute
    // normalized form — provided the entry actually exists.
    val real = toAbsolutePath().normalize().asInstanceOf[ZipPath]
    fs.provider()
      .asInstanceOf[ZipFileSystemProvider]
      .readZipAttributes(real) // throws NoSuchFileException if absent
    real
  }

  override def toFile(): File =
    throw new UnsupportedOperationException(
      "ZipPath is not associated with the default file system"
    )

  override def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[_]],
      modifiers: Array[WatchEvent.Modifier]
  ): WatchKey =
    throw new UnsupportedOperationException("ZipPath.register")

  override def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[_]]
  ): WatchKey =
    throw new UnsupportedOperationException("ZipPath.register")

  override def iterator(): Iterator[Path] =
    new Iterator[Path] {
      private var i = 0
      override def hasNext(): Boolean = i < getNameCount()
      override def next(): Path =
        if (!hasNext()) throw new java.util.NoSuchElementException()
        else { val r = getName(i); i += 1; r }
      override def remove(): Unit = throw new UnsupportedOperationException
    }

  override def compareTo(other: Path): Int = other match {
    case zp: ZipPath if zp.fs.provider() eq fs.provider() =>
      path.compareTo(zp.path)
    case _ =>
      throw new ClassCastException()
  }

  override def equals(obj: Any): Boolean = obj match {
    case zp: ZipPath => (zp.fs eq fs) && zp.path == path
    case _           => false
  }

  override def hashCode(): Int = path.##

  override def toString(): String = path
}

private object ZipPath {

  // Collapse runs of '/' into one and strip a trailing '/' unless the
  // result would be empty (we want to preserve "/" as the root and "" as
  // the empty relative path).
  def normalizeSlashes(raw: String): String = {
    if (raw.isEmpty) return raw
    val n = raw.length
    val sb = new java.lang.StringBuilder(n)
    var prevSlash = false
    var i = 0
    while (i < n) {
      val c = raw.charAt(i)
      if (c == '/') {
        if (!prevSlash) sb.append('/')
        prevSlash = true
      } else {
        sb.append(c)
        prevSlash = false
      }
      i += 1
    }
    val len = sb.length()
    if (len > 1 && sb.charAt(len - 1) == '/') sb.setLength(len - 1)
    sb.toString
  }

  def computeOffsets(path: String): Array[Int] = {
    // Match JDK convention: an empty relative path has a single empty name
    // (nameCount=1), the root "/" has zero names (nameCount=0).
    if (path.isEmpty) return Array(-1, 0)
    if (path == "/") return Array(0)
    val absolute = path.charAt(0) == '/'
    // Count names: number of '/' inside (path normalized so no doubles,
    // no trailing /) determines segments.
    val n = path.length
    var slashes = 0
    var i = 0
    while (i < n) {
      if (path.charAt(i) == '/') slashes += 1
      i += 1
    }
    val nameCount = if (absolute) slashes else slashes + 1
    val out = new Array[Int](nameCount + 1)
    out(0) = if (absolute) 0 else -1
    var idx = 1
    i = if (absolute) 1 else 0
    while (i < n) {
      if (path.charAt(i) == '/') {
        out(idx) = i
        idx += 1
      }
      i += 1
    }
    out(idx) = n
    out
  }
}
