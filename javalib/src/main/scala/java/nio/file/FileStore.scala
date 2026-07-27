package java.nio.file

import java.nio.file.attribute.{FileAttributeView, FileStoreAttributeView}

abstract class FileStore protected () {
  def name(): String
  def `type`(): String
  def isReadOnly(): Boolean
  def getTotalSpace(): Long
  def getUsableSpace(): Long
  def getUnallocatedSpace(): Long
  def getBlockSize(): Long = throw new UnsupportedOperationException()
  def supportsFileAttributeView(tpe: Class[_ <: FileAttributeView]): Boolean
  def supportsFileAttributeView(name: String): Boolean
  def getFileStoreAttributeView[V <: FileStoreAttributeView](tpe: Class[V]): V
  def getAttribute(attribute: String): Object
}
