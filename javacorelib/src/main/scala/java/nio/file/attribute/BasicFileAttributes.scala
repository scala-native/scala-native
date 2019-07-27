package java.nio.file.attribute

trait BasicFileAttributes {
  def creationTime(): FileTime
  def fileKey(): Object
  def isDirectory(): Boolean
  def isOther(): Boolean
  def isRegularFile(): Boolean
  def isSymbolicLink(): Boolean
  def lastAccessTime(): FileTime
  def lastModifiedTime(): FileTime
  def size(): Long
}
