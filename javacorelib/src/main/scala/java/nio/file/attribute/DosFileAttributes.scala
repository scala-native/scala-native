package java.nio.file.attribute

trait DosFileAttributes extends BasicFileAttributes {
  def isArchive(): Boolean
  def isHidden(): Boolean
  def isReadOnly(): Boolean
  def isSystem(): Boolean
}
