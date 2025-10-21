package scala.scalanative.nio.fs.windows

import java.nio.file.FileSystem
import java.nio.file.attribute._

import scala.collection.immutable.{Map => SMap}

import scala.scalanative.nio.fs.GenericFileSystemProvider

class WindowsFileSystemProvider extends GenericFileSystemProvider {

  protected lazy val fs: FileSystem =
    new WindowsFileSystem(this)

  protected val knownFileAttributeViews: AttributeViewMapping = {
    def Dos = (p, l) => new WindowsDosFileAttributeView(p, l)
    def Acl = (p, l) => new WindowsAclFileAttributeView(p, l)

    SMap(
      classOf[BasicFileAttributeView] -> Dos,
      classOf[DosFileAttributeView] -> Dos,
      classOf[AclFileAttributeView] -> Acl,
      classOf[FileOwnerAttributeView] -> Acl
    )
  }

}
