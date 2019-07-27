package java.nio.file.attribute

trait DosFileAttributeView extends BasicFileAttributeView {
  def readAttributes(): DosFileAttributes
  def setArchive(value: Boolean): Unit
  def setHidden(value: Boolean): Unit
  def setReadOnly(value: Boolean): Unit
  def setSystem(value: Boolean): Unit
}
