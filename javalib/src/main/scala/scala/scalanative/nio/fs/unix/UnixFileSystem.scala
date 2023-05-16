package scala.scalanative.nio.fs.unix

import java.io.IOException
import java.lang.Iterable
import java.lang.StringBuilder
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
import java.nio.file.attribute.PosixUserPrincipalLookupService
import java.{util => ju}

import scala.scalanative.unsafe._

import scala.scalanative.posix.sys.statvfs

import scalanative.annotation.stub

class UnixFileSystem(
    fsProvider: FileSystemProvider,
    val root: String,
    val defaultDirectory: String
) extends FileSystem {
  private var closed: Boolean = false

  override def provider(): FileSystemProvider = fsProvider
  override def close(): Unit =
    closed = true

  @stub
  override def getFileStores(): Iterable[FileStore] = ???

  override def getUserPrincipalLookupService(): UserPrincipalLookupService =
    PosixUserPrincipalLookupService

  override def getPath(first: String, more: Array[String]): Path = {
    if (more.length == 0) new UnixPath(this, first)
    else {
      val sb = new StringBuilder(first)
      more.foreach { element =>
        if (element.length > 0) {
          if (sb.length() > 0) sb.append('/')
          sb.append(element)
        }
      }
      new UnixPath(this, sb.toString())
    }
  }

  override def getPathMatcher(syntaxAndPattern: String): PathMatcher =
    PathMatcherImpl(syntaxAndPattern)

  override def getRootDirectories(): Iterable[Path] = {
    val list = new ju.LinkedList[Path]()
    list.add(getPath(root, Array.empty))
    list
  }

  override def getSeparator(): String =
    "/"

  override def isOpen(): Boolean =
    closed == false

  override def isReadOnly(): Boolean = Zone { implicit z =>
    val stat = alloc[statvfs.statvfs]()
    val err = statvfs.statvfs(toCString(root), stat)
    if (err != 0) {
      throw new IOException()
    } else {
      val flags = stat._10
      val mask = statvfs.ST_RDONLY
      (flags & mask) == mask
    }
  }

  override def newWatchService(): WatchService =
    throw new UnsupportedOperationException()

  override def supportedFileAttributeViews(): ju.Set[String] = {
    val set = new java.util.HashSet[String]()
    set.add("basic")
    set.add("posix")
    set
  }

}
