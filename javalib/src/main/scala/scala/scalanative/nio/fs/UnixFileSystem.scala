package scala.scalanative.nio.fs

import java.io.IOException
import java.lang.Iterable
import java.nio.file.{
  FileStore,
  FileSystem,
  Path,
  PathMatcher,
  PathMatcherImpl,
  WatchService
}
import java.nio.file.spi.FileSystemProvider
import java.nio.file.attribute.UserPrincipalLookupService
import java.util.{LinkedList, Set}

import scala.scalanative.native.{
  CUnsignedLong,
  Ptr,
  sizeof,
  statvfs,
  toCString,
  Zone,
  alloc
}

class UnixFileSystem(override val provider: FileSystemProvider,
                     val root: String,
                     val defaultDirectory: String)
    extends FileSystem {
  private var closed: Boolean = false

  override def close(): Unit =
    closed = true

  override def getFileStores(): Iterable[FileStore] =
    ???

  override def getPath(first: String, more: Array[String]): Path =
    new UnixPath(this, (first +: more).mkString("/"))

  override def getPathMatcher(syntaxAndPattern: String): PathMatcher =
    PathMatcherImpl(syntaxAndPattern)

  override def getRootDirectories(): Iterable[Path] = {
    val list = new LinkedList[Path]()
    list.add(getPath(root, Array.empty))
    list
  }

  override def getSeparator(): String =
    "/"

  override def isOpen(): Boolean =
    closed == false

  override def isReadOnly(): Boolean = Zone { implicit z =>
    val stat = alloc[statvfs.statvfs]
    val err  = statvfs.statvfs(toCString(root), stat)
    if (err != 0) {
      throw new IOException()
    } else {
      val flags = !(stat._10)
      val mask  = statvfs.ST_RDONLY
      (flags & mask) == mask
    }
  }

  override def newWatchService: WatchService =
    throw new UnsupportedOperationException()

  override def supportedFileAttributeViews(): Set[String] = {
    val set = new java.util.HashSet[String]()
    set.add("basic")
    set.add("posix")
    set
  }

}
