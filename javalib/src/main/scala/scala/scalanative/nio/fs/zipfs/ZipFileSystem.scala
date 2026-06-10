package scala.scalanative.nio.fs.zipfs

import java.nio.file._
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.zip.ZipFile
import java.{util => ju}

/** Read-only file-system view over a single zip/jar archive.
 *
 *  Entry lookup is delegated to `java.util.zip.ZipFile`, which keeps the
 *  archive's central directory in memory. Write support is not implemented
 *  yet: every mount is read-only and mutating operations throw
 *  [[java.nio.file.ReadOnlyFileSystemException]].
 */
class ZipFileSystem private[zipfs] (
    fsProvider: ZipFileSystemProvider,
    val archivePath: Path,
    private[zipfs] val zipFile: ZipFile
) extends FileSystem {

  private val openFlag = new java.util.concurrent.atomic.AtomicBoolean(true)

  private[zipfs] val rootPath: ZipPath = new ZipPath(this, "/")

  override def provider(): FileSystemProvider = fsProvider

  override def isOpen(): Boolean = openFlag.get()

  override def isReadOnly(): Boolean = true

  override def getSeparator(): String = "/"

  override def close(): Unit = {
    if (openFlag.compareAndSet(true, false)) {
      try zipFile.close()
      finally fsProvider.unregister(archivePath, this)
    }
  }

  // For races inside provider.newFileSystem: close without touching the
  // registry (we never put it there).
  private[zipfs] def closeQuietly(): Unit = {
    if (openFlag.compareAndSet(true, false)) zipFile.close()
  }

  // Only real I/O enforces the closed state; path-level methods (getPath,
  // getRootDirectories, getPathMatcher, getFileStores,
  // supportedFileAttributeViews) intentionally keep working after close,
  // matching jdk.zipfs.
  private[zipfs] def ensureOpen(): Unit =
    if (!openFlag.get()) throw new ClosedFileSystemException()

  override def getPath(first: String, more: Array[String]): Path = {
    // Match JDK FileSystem.getPath contract: all components must be non-null.
    if (first == null)
      throw new NullPointerException("first")
    val s =
      if (more == null || more.length == 0) first
      else {
        val sb = new java.lang.StringBuilder()
        sb.append(first)
        var i = 0
        while (i < more.length) {
          val m = more(i)
          if (m == null)
            throw new NullPointerException(s"more[$i]")
          if (m.length > 0) {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/')
              sb.append('/')
            sb.append(m)
          }
          i += 1
        }
        sb.toString
      }
    new ZipPath(this, s)
  }

  override def getRootDirectories(): java.lang.Iterable[Path] = {
    val l = new ju.ArrayList[Path](1)
    l.add(rootPath)
    l
  }

  // Single synthetic store per mount, matching jdk.zipfs which exposes one
  // ZipFileStore per archive.
  private lazy val fileStore: FileStore = new ZipFileStore(this)

  override def getFileStores(): java.lang.Iterable[FileStore] = {
    val l = new ju.ArrayList[FileStore](1)
    l.add(fileStore)
    l
  }

  override def supportedFileAttributeViews(): ju.Set[String] = {
    val s = new ju.HashSet[String]()
    s.add("basic")
    s
  }

  override def getPathMatcher(syntaxAndPattern: String): PathMatcher =
    PathMatcherImpl(syntaxAndPattern)

  override def getUserPrincipalLookupService(): UserPrincipalLookupService =
    throw new UnsupportedOperationException

  override def newWatchService(): WatchService =
    throw new UnsupportedOperationException
}
