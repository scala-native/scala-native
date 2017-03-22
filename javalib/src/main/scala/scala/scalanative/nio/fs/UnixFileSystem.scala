package scala.scalanative.nio.fs

import java.io.IOException
import java.lang.Iterable
import java.nio.file.{FileStore, FileSystem, Path, PathMatcher, WatchService}
import java.nio.file.spi.FileSystemProvider
import java.nio.file.attribute.UserPrincipalLookupService
import java.util.{ArrayList, Set}

import scala.scalanative.native.{CUnsignedLong, statvfs, toCString}

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
    ???

  override def getRootDirectories(): Iterable[Path] = {
    val list = new ArrayList[Path]()
    list.add(getPath(root, Array.empty))
    list
  }

  override def getSeparator(): String =
    "/"

  override def getUserPrincipalLookupService(): UserPrincipalLookupService =
    ???

  override def isOpen(): Boolean =
    closed == false

  override def isReadOnly(): Boolean = {
    val stat = statvfs.statvfs(toCString(root))
    if (stat == null) throw new IOException()
    else {
      val flags = !(stat._10)
      val mask  = statvfs.ST_RDONLY
      (flags & mask) == mask
    }
  }

  override def newWatchService: WatchService =
    ???

  // TODO:
  override def supportedFileAttributeViews(): Set[String] =
    new java.util.HashSet[String]()

}
