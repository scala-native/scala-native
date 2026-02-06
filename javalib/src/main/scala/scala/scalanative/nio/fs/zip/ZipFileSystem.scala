package scala.scalanative.nio.fs.zip

import java.io.{Closeable, IOException}
import java.lang.Iterable
import java.nio.file._
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.zip.{ZipEntry, ZipFile}
import java.util.{LinkedList, Set => JSet}

/** A read-only FileSystem backed by a zip/jar archive.
 *
 *  Wraps [[java.util.zip.ZipFile]] to expose ZIP entries as paths and streams.
 *  All write operations throw [[UnsupportedOperationException]].
 */
final class ZipFileSystem private[zip] (
    private[zip] val zipProvider: ZipFileSystemProvider,
    private[zip] val archivePath: Path
) extends FileSystem {

  private[zip] val zipFile: ZipFile = new ZipFile(archivePath.toFile())

  @volatile private var closed: Boolean = false

  // Build the entry lookup table and directory tree on construction.
  private[zip] val entryMap: java.util.Map[String, ZipEntry] = {
    val map = new java.util.LinkedHashMap[String, ZipEntry]()
    val en = zipFile.entries()
    while (en.hasMoreElements()) {
      val entry = en.nextElement()
      map.put(entry.getName(), entry)
    }
    map
  }

  // --- FileSystem interface ---

  override def provider(): FileSystemProvider = zipProvider

  override def close(): Unit = {
    if (!closed) {
      closed = true
      zipFile.close()
    }
  }

  override def isOpen(): Boolean = !closed

  override def isReadOnly(): Boolean = true

  override def getSeparator(): String = "/"

  override def getRootDirectories(): Iterable[Path] = {
    val list = new LinkedList[Path]()
    list.add(new ZipPath(this, "/"))
    list
  }

  override def getPath(first: String, more: Array[String]): Path = {
    if (more.length == 0) new ZipPath(this, first)
    else {
      val sb = new java.lang.StringBuilder(first)
      more.foreach { element =>
        if (element.length > 0) {
          if (sb.length() > 0) sb.append('/')
          sb.append(element)
        }
      }
      new ZipPath(this, sb.toString())
    }
  }

  override def getPathMatcher(syntaxAndPattern: String): PathMatcher =
    PathMatcherImpl(syntaxAndPattern)

  override def getUserPrincipalLookupService(): UserPrincipalLookupService =
    throw new UnsupportedOperationException()

  override def newWatchService(): WatchService =
    throw new UnsupportedOperationException()

  override def getFileStores(): Iterable[FileStore] = {
    val list = new LinkedList[FileStore]()
    list
  }

  override def supportedFileAttributeViews(): JSet[String] = {
    val set = new java.util.HashSet[String]()
    set.add("basic")
    set
  }

  // --- Internal helpers ---

  private[zip] def getZipPath(): Path = archivePath

  /** Look up a ZipEntry by entry name (without leading "/"). */
  private[zip] def getEntry(name: String): ZipEntry = {
    if (name.isEmpty || name == "/") return null // root has no entry
    entryMap.get(name)
  }

  /** Check if a path corresponds to a directory in the archive.
   *
   *  A path is a directory if:
   *    - it is the root "/"
   *    - there exists an entry with a trailing "/" matching this path
   *    - there exist entries that are children of this path (implicit
   *      directory)
   */
  private[zip] def isDirectory(path: ZipPath): Boolean = {
    val absPath = path.toAbsolutePath().normalize()
    val name = absPath.entryName
    if (name.isEmpty) return true // root

    // Explicit directory entry
    if (entryMap.containsKey(name + "/")) return true

    // Implicit directory: any entry starts with name + "/"
    val prefix = name + "/"
    val iter = entryMap.keySet().iterator()
    while (iter.hasNext()) {
      if (iter.next().startsWith(prefix)) return true
    }
    false
  }

  /** Check if a path corresponds to a regular file in the archive. */
  private[zip] def isRegularFile(path: ZipPath): Boolean = {
    val absPath = path.toAbsolutePath().normalize()
    val name = absPath.entryName
    if (name.isEmpty) return false // root is a directory
    val entry = entryMap.get(name)
    entry != null && !entry.isDirectory()
  }

  /** Get direct children of a directory path. */
  private[zip] def getChildren(dirPath: ZipPath): java.util.List[ZipPath] = {
    val absPath = dirPath.toAbsolutePath().normalize()
    val prefix =
      if (absPath.entryName.isEmpty) "" // root
      else absPath.entryName + "/"

    val children = new java.util.ArrayList[ZipPath]()
    val seen = new java.util.HashSet[String]()
    val iter = entryMap.keySet().iterator()
    while (iter.hasNext()) {
      val key = iter.next()
      if (key.startsWith(prefix) && key.length > prefix.length) {
        // Determine the direct child name
        val rest = key.substring(prefix.length)
        val slashIdx = rest.indexOf('/')
        val childName =
          if (slashIdx < 0) rest
          else rest.substring(0, slashIdx)

        if (seen.add(childName)) {
          val childPath =
            if (prefix.isEmpty) "/" + childName
            else "/" + prefix + childName
          children.add(new ZipPath(this, childPath))
        }
      }
    }
    children
  }

  private def checkOpen(): Unit =
    if (closed) throw new ClosedFileSystemException()
}
