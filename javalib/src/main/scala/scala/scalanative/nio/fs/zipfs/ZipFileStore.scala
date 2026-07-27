package scala.scalanative.nio.fs.zipfs

import java.nio.file.FileStore
import java.nio.file.attribute.{
  BasicFileAttributeView, FileAttributeView, FileStoreAttributeView
}

private[zipfs] final class ZipFileStore(fs: ZipFileSystem) extends FileStore {
  override def name(): String = fs.archivePath.toString
  override def `type`(): String = "zipfs"
  override def isReadOnly(): Boolean = fs.isReadOnly()
  override def getTotalSpace(): Long = Long.MaxValue
  override def getUsableSpace(): Long = Long.MaxValue
  override def getUnallocatedSpace(): Long = Long.MaxValue

  override def supportsFileAttributeView(
      tpe: Class[_ <: FileAttributeView]
  ): Boolean = tpe == classOf[BasicFileAttributeView]

  override def supportsFileAttributeView(name: String): Boolean =
    name == "basic"

  override def getFileStoreAttributeView[V <: FileStoreAttributeView](
      tpe: Class[V]
  ): V = null.asInstanceOf[V]

  override def getAttribute(attribute: String): Object =
    throw new UnsupportedOperationException(attribute)
}
