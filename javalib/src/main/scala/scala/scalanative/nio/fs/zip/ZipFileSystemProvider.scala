package scala.scalanative.nio.fs.zip

import java.io.{ByteArrayInputStream, InputStream, IOException, OutputStream}
import java.net.URI
import java.nio.channels.{ClosedChannelException, FileChannel, SeekableByteChannel}
import java.nio.file._
import java.nio.file.attribute._
import java.nio.file.spi.FileSystemProvider
import java.util.{HashMap, Map => JMap, Set => JSet}
import java.util.concurrent.TimeUnit
import java.util.stream.{Stream, StreamSupport}
import java.util.zip.ZipEntry

/** FileSystemProvider for "jar" scheme URIs.
 *
 *  Creates read-only [[ZipFileSystem]] instances backed by zip/jar archives.
 *  The provider supports the operations needed by [[java.nio.file.Files]]
 *  for reading zip contents: `newInputStream`, `newDirectoryStream`,
 *  `readAttributes`, and `checkAccess`.
 */
class ZipFileSystemProvider extends FileSystemProvider {

  // Track open filesystems by archive path to support getFileSystem(uri)
  private val filesystems =
    new HashMap[String, ZipFileSystem]()

  override def getScheme(): String = "jar"

  override def newFileSystem(uri: URI, env: JMap[String, _]): FileSystem = {
    val archivePath = uriToArchivePath(uri)
    val key = archivePath.toAbsolutePath().toString()
    synchronized {
      if (filesystems.containsKey(key))
        throw new FileSystemAlreadyExistsException(key)
      val fs = new ZipFileSystem(this, archivePath)
      filesystems.put(key, fs)
      fs
    }
  }

  override def newFileSystem(
      path: Path,
      env: JMap[String, _]
  ): FileSystem = {
    // Called by FileSystems.newFileSystem(path, classLoader)
    val name = path.toString()
    if (name.endsWith(".jar") || name.endsWith(".zip")) {
      val key = path.toAbsolutePath().toString()
      synchronized {
        if (filesystems.containsKey(key))
          throw new FileSystemAlreadyExistsException(key)
        val fs = new ZipFileSystem(this, path)
        filesystems.put(key, fs)
        fs
      }
    } else {
      throw new UnsupportedOperationException(
        s"Not a zip/jar file: $path"
      )
    }
  }

  override def getFileSystem(uri: URI): FileSystem = {
    val archivePath = uriToArchivePath(uri)
    val key = archivePath.toAbsolutePath().toString()
    synchronized {
      val fs = filesystems.get(key)
      if (fs == null || !fs.isOpen())
        throw new FileSystemNotFoundException(key)
      fs
    }
  }

  override def getPath(uri: URI): Path = {
    // jar:file:///path/to/archive.jar!/entry/path
    val ssp = uri.getSchemeSpecificPart()
    val bangSlash = ssp.indexOf("!/")
    if (bangSlash < 0) throw new IllegalArgumentException("Missing !/: " + uri)
    val archiveUri = URI.create(ssp.substring(0, bangSlash))
    val entryPath = ssp.substring(bangSlash + 1)
    val fs = getFileSystem(uri).asInstanceOf[ZipFileSystem]
    fs.getPath(entryPath, Array.empty).asInstanceOf[Path]
  }

  // --- Read operations ---

  override def newInputStream(
      path: Path,
      options: Array[OpenOption]
  ): InputStream = {
    val zp = toZipPath(path)
    val fs = zp.fileSystem
    val entryName = zp.toAbsolutePath().normalize().entryName
    val entry = fs.zipFile.getEntry(entryName)
    if (entry == null)
      throw new NoSuchFileException(zp.toString())
    fs.zipFile.getInputStream(entry)
  }

  override def newOutputStream(
      path: Path,
      options: Array[OpenOption]
  ): OutputStream =
    throw new ReadOnlyFileSystemException()

  override def newByteChannel(
      path: Path,
      options: JSet[_ <: OpenOption],
      attrs: Array[FileAttribute[_]]
  ): SeekableByteChannel = {
    // For read-only access, provide a SeekableByteChannel from the entry bytes
    val zp = toZipPath(path)
    val is = newInputStream(zp, Array.empty)
    val bytes =
      try {
        val buf = new java.io.ByteArrayOutputStream()
        val tmp = new Array[Byte](8192)
        var n = 0
        while ({ n = is.read(tmp); n >= 0 }) buf.write(tmp, 0, n)
        buf.toByteArray()
      } finally is.close()

    val buffer = java.nio.ByteBuffer.wrap(bytes)
    new SeekableByteChannel {
      private var open = true
      private var pos = 0L

      override def read(dst: java.nio.ByteBuffer): Int = {
        if (!open) throw new ClosedChannelException()
        val remaining = bytes.length - pos.toInt
        if (remaining <= 0) -1
        else {
          val toRead = Math.min(remaining, dst.remaining())
          dst.put(bytes, pos.toInt, toRead)
          pos += toRead
          toRead
        }
      }

      override def write(src: java.nio.ByteBuffer): Int =
        throw new ReadOnlyFileSystemException()

      override def position(): Long = pos
      override def position(newPosition: Long): SeekableByteChannel = {
        pos = newPosition; this
      }
      override def size(): Long = bytes.length.toLong
      override def truncate(size: Long): SeekableByteChannel =
        throw new ReadOnlyFileSystemException()
      override def isOpen(): Boolean = open
      override def close(): Unit = open = false
    }
  }

  override def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path] = {
    val zp = toZipPath(dir)
    val fs = zp.fileSystem
    if (!fs.isDirectory(zp))
      throw new NotDirectoryException(zp.toString())

    val children = fs.getChildren(zp)
    new DirectoryStream[Path] {
      private var iteratorCalled = false
      private var closed = false

      override def iterator(): java.util.Iterator[Path] = {
        if (closed || iteratorCalled)
          throw new IllegalStateException("Iterator already obtained")
        iteratorCalled = true

        // Pre-filter
        val filtered = new java.util.ArrayList[Path]()
        val it = children.iterator()
        while (it.hasNext()) {
          val child = it.next()
          if (filter.accept(child)) filtered.add(child)
        }
        filtered.iterator()
      }

      override def close(): Unit = closed = true
    }
  }

  // --- Attribute operations ---

  override def createDirectory(
      dir: Path,
      attrs: Array[FileAttribute[_]]
  ): Unit =
    throw new ReadOnlyFileSystemException()

  override def delete(path: Path): Unit =
    throw new ReadOnlyFileSystemException()

  override def copy(
      source: Path,
      target: Path,
      options: Array[CopyOption]
  ): Unit =
    throw new ReadOnlyFileSystemException()

  override def move(
      source: Path,
      target: Path,
      options: Array[CopyOption]
  ): Unit =
    throw new ReadOnlyFileSystemException()

  override def isSameFile(path: Path, path2: Path): Boolean = {
    val zp1 = toZipPath(path)
    val zp2 = toZipPath(path2)
    zp1.fileSystem == zp2.fileSystem &&
    zp1.toAbsolutePath().normalize().toString() ==
      zp2.toAbsolutePath().normalize().toString()
  }

  override def isHidden(path: Path): Boolean = false

  override def checkAccess(path: Path, modes: Array[AccessMode]): Unit = {
    val zp = toZipPath(path)
    val fs = zp.fileSystem
    // Check write/execute access immediately
    modes.foreach { mode =>
      if (mode == AccessMode.WRITE || mode == AccessMode.EXECUTE)
        throw new AccessDeniedException(zp.toString())
    }
    // Check existence
    val absPath = zp.toAbsolutePath().normalize()
    val name = absPath.entryName
    if (name.isEmpty) return // root always exists
    if (fs.getEntry(name) == null && fs.getEntry(name + "/") == null) {
      if (!fs.isDirectory(zp))
        throw new NoSuchFileException(zp.toString())
    }
  }

  override def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]
  ): V = {
    if (tpe == classOf[BasicFileAttributeView] ||
        tpe.getName() == "java.nio.file.attribute.BasicFileAttributeView") {
      new ZipBasicFileAttributeView(toZipPath(path)).asInstanceOf[V]
    } else {
      null.asInstanceOf[V]
    }
  }

  override def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: Array[LinkOption]
  ): A = {
    if (tpe == classOf[BasicFileAttributes] ||
        tpe.getName() == "java.nio.file.attribute.BasicFileAttributes") {
      new ZipBasicFileAttributeView(toZipPath(path))
        .readAttributes()
        .asInstanceOf[A]
    } else {
      throw new UnsupportedOperationException(
        s"Unsupported attribute type: ${tpe.getName()}"
      )
    }
  }

  override def readAttributes(
      path: Path,
      attributes: String,
      options: Array[LinkOption]
  ): JMap[String, Object] = {
    val view = new ZipBasicFileAttributeView(toZipPath(path))
    view.asMap
  }

  override def setAttribute(
      path: Path,
      attribute: String,
      value: Object,
      options: Array[LinkOption]
  ): Unit =
    throw new ReadOnlyFileSystemException()

  // --- Internal ---

  /** Remove a filesystem from the tracked map when it closes. */
  private[zip] def removeFileSystem(fs: ZipFileSystem): Unit = {
    val key = fs.archivePath.toAbsolutePath().toString()
    synchronized {
      filesystems.remove(key)
    }
  }

  private def toZipPath(path: Path): ZipPath = path match {
    case zp: ZipPath => zp
    case _ =>
      throw new ProviderMismatchException(
        s"Expected ZipPath, got: ${path.getClass.getName}"
      )
  }

  private def uriToArchivePath(uri: URI): Path = {
    val ssp = uri.getSchemeSpecificPart()
    // jar:file:///path/to/archive.jar or jar:file:///path/to/archive.jar!/entry
    val archivePart =
      if (ssp.contains("!/")) ssp.substring(0, ssp.indexOf("!/"))
      else ssp
    Paths.get(URI.create(archivePart))
  }
}

// --- Attribute views ---

private class ZipBasicFileAttributeView(path: ZipPath)
    extends BasicFileAttributeView {

  override def name(): String = "basic"

  override def readAttributes(): BasicFileAttributes = {
    val fs = path.fileSystem
    val absPath = path.toAbsolutePath().normalize()
    val entryName = absPath.entryName

    if (entryName.isEmpty) {
      // Root directory
      new ZipEntryAttributes(null, isDir = true)
    } else {
      val entry = fs.getEntry(entryName)
      if (entry != null) {
        new ZipEntryAttributes(entry, isDir = entry.isDirectory())
      } else {
        // Try as directory
        val dirEntry = fs.getEntry(entryName + "/")
        if (dirEntry != null) {
          new ZipEntryAttributes(dirEntry, isDir = true)
        } else if (fs.isDirectory(absPath)) {
          // Implicit directory
          new ZipEntryAttributes(null, isDir = true)
        } else {
          throw new NoSuchFileException(path.toString())
        }
      }
    }
  }

  override def setTimes(
      lastModifiedTime: FileTime,
      lastAccessTime: FileTime,
      createTime: FileTime
  ): Unit =
    throw new ReadOnlyFileSystemException()

  override def asMap: JMap[String, Object] = {
    val attrs = readAttributes()
    val map = new HashMap[String, Object]()
    map.put("size", java.lang.Long.valueOf(attrs.size()))
    map.put("isDirectory", java.lang.Boolean.valueOf(attrs.isDirectory()))
    map.put("isRegularFile", java.lang.Boolean.valueOf(attrs.isRegularFile()))
    map.put("isSymbolicLink", java.lang.Boolean.valueOf(attrs.isSymbolicLink()))
    map.put("isOther", java.lang.Boolean.valueOf(attrs.isOther()))
    map.put("lastModifiedTime", attrs.lastModifiedTime())
    map.put("lastAccessTime", attrs.lastAccessTime())
    map.put("creationTime", attrs.creationTime())
    map.put("fileKey", attrs.fileKey())
    map
  }

  override def getAttribute(name: String): Object = asMap.get(name)
}

private class ZipEntryAttributes(
    entry: ZipEntry,
    isDir: Boolean
) extends BasicFileAttributes {

  override def lastModifiedTime(): FileTime =
    if (entry != null && entry.getTime() >= 0)
      FileTime.fromMillis(entry.getTime())
    else FileTime.fromMillis(0L)

  override def lastAccessTime(): FileTime = lastModifiedTime()

  override def creationTime(): FileTime = lastModifiedTime()

  override def isRegularFile(): Boolean = !isDir

  override def isDirectory(): Boolean = isDir

  override def isSymbolicLink(): Boolean = false

  override def isOther(): Boolean = false

  override def size(): Long =
    if (entry != null) entry.getSize()
    else 0L

  override def fileKey(): Object = null
}
