package scala.scalanative.nio.fs.unix

import scala.scalanative.unsafe.{CChar, Ptr, fromCString, stackalloc}
import scala.scalanative.unsigned._
import scala.scalanative.posix.unistd
import scala.scalanative.libc.errno.errno
import scala.collection.immutable.{Map => SMap}
import scala.scalanative.nio.fs.GenericFileSystemProvider
import java.nio.file.attribute._
import java.nio.file.FileSystem

class UnixFileSystemProvider extends GenericFileSystemProvider {

  protected lazy val fs: FileSystem =
    new UnixFileSystem(this, "/", getUserDir())

  protected val knownFileAttributeViews: AttributeViewMapping = {
    def PosixFileAttrView = (p, l) => new PosixFileAttributeViewImpl(p, l)
    SMap(
      classOf[BasicFileAttributeView] -> PosixFileAttrView,
      classOf[PosixFileAttributeView] -> PosixFileAttrView,
      classOf[FileOwnerAttributeView] -> PosixFileAttrView
    )
  }

  private def getUserDir(): String = {
    val buff: Ptr[CChar] = stackalloc[CChar](4096)
    val res = unistd.getcwd(buff, 4095.toUInt)
    if (res == null)
      throw UnixException(
        "Could not determine current working directory",
        errno
      )
    fromCString(res)
  }
}
