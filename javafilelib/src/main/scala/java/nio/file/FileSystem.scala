package java.nio.file

import java.io.Closeable
import java.lang.Iterable
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.Set

abstract class FileSystem extends Closeable {
  // def getUserPrincipalLookupService(): UserPrincipalLookupService

  def close(): Unit
  def getFileStores(): Iterable[FileStore]
  def getPath(first: String, more: Array[String]): Path
  def getPathMatcher(syntaxAndPattern: String): PathMatcher
  def getRootDirectories(): Iterable[Path]
  def getSeparator(): String
  def isOpen(): Boolean
  def isReadOnly(): Boolean
  def newWatchService(): WatchService
  def provider(): FileSystemProvider
  def supportedFileAttributeViews(): Set[String]
}
