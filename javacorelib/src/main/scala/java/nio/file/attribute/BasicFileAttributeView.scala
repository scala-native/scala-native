package java.nio.file.attribute

trait BasicFileAttributeView extends FileAttributeView {
  def name(): String
  def readAttributes(): BasicFileAttributes
  def setTimes(lastModifiedTime: FileTime,
               lastAccessTime: FileTime,
               createTime: FileTime): Unit
}
