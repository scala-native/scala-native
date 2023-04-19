package scala.scalanative.nio.fs.windows

import java.lang.Iterable
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.{
  FileStore,
  FileSystem,
  Path,
  Paths,
  PathMatcher,
  PathMatcherImpl,
  WatchService
}
import java.nio.file.attribute.UserPrincipalLookupService
import java.util.{LinkedList, Set}

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.annotation.stub
import scalanative.windows.FileApi._
import scala.annotation.tailrec

class WindowsFileSystem(fsProvider: WindowsFileSystemProvider)
    extends FileSystem {

  override def provider(): WindowsFileSystemProvider = fsProvider
  override def close(): Unit = throw new UnsupportedOperationException()

  @stub
  override def getFileStores(): Iterable[FileStore] = ???

  override def getPath(first: String, more: Array[String]): Path = {
    if (more.length == 0) WindowsPathParser(first)(this)
    else {
      val sep = getSeparator()
      val sb = new StringBuilder(first)
      more.foreach { element =>
        if (element.length > 0) {
          if (sb.length() > 0) sb.append(sep)
          sb.append(element)
        }
      }
      WindowsPathParser(sb.toString())(this)
    }
  }

  override def getPathMatcher(syntaxAndPattern: String): PathMatcher =
    PathMatcherImpl(syntaxAndPattern)

  override def getRootDirectories(): Iterable[Path] = {
    val list = new LinkedList[Path]()
    val bufferSize = GetLogicalDriveStringsW(0.toUInt, null)
    val buffer: Ptr[CChar16] = stackalloc[CChar16](bufferSize)

    @tailrec
    def readStringsBlock(ptr: Ptr[CChar16]): Unit = {
      val path = fromCWideString(ptr, StandardCharsets.UTF_16LE)
      // GetLogicalDriveStrings returns block of null-terminated strings with
      // additional null-termination character after last string.
      // Empty string means we reached the end.
      if (path.nonEmpty) {
        list.add(Paths.get(path, Array.empty))
        readStringsBlock(ptr + path.length + 1)
      }
    }

    if (GetLogicalDriveStringsW(bufferSize, buffer) > 0.toUInt) {
      readStringsBlock(buffer)
    }
    list
  }
  def getUserPrincipalLookupService(): UserPrincipalLookupService =
    WindowsUserPrincipalLookupService

  override def getSeparator(): String = "\\"

  override def isOpen(): Boolean = true

  override def isReadOnly(): Boolean = false

  override def newWatchService(): WatchService =
    throw new UnsupportedOperationException()

  override def supportedFileAttributeViews(): Set[String] = {
    val set = new java.util.HashSet[String]()
    set.add("basic")
    set.add("owner")
    set.add("dos")
    set.add("acl")
    set
  }

}
