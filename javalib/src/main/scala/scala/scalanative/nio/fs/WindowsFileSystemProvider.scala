package scala.scalanative.nio.fs

import scala.scalanative.native.{CChar, fromCString, stackalloc}
import scala.scalanative.posix.unistd
import scala.collection.immutable.{Map => SMap}

import java.nio.channels.{
  AsynchronousFileChannel,
  FileChannel,
  SeekableByteChannel
}
import java.nio.file._
import java.nio.file.attribute._
import java.nio.file.spi.FileSystemProvider
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.{Map, Set}

class WindowsFileSystemProvider extends FileSystemProvider {

  private lazy val fs = new WindowsFileSystem(this, "/", getUserDir())

  override def getScheme(): String =
    "file"

  override def newFileSystem(uri: URI, env: Map[String, _]): FileSystem =
    if (uri.getPath != "/" || uri.getPath != "\\") {
      throw new IllegalArgumentException(
        "Path component should be '\\' or '/'")
    } else if (uri.getScheme != "file") {
      throw new IllegalArgumentException("URI does not match this provider.")
    } else {
      throw new FileSystemAlreadyExistsException()
    }

  override def getFileSystem(uri: URI): FileSystem =
    if (uri.getPath != "/" && uri.getPath != "\\") {
      throw new IllegalArgumentException(
        "Path component should be '\\' or '/'")
    } else if (uri.getScheme != "file") {
      throw new IllegalArgumentException("URI does not match this provider.")
    } else {
      fs
    }

  override def getPath(uri: URI): Path =
    if (uri.getScheme != "file") {
      throw new IllegalArgumentException("URI scheme is not \"file\"")
    } else if (!uri.getPath.startsWith("\\") && !uri.getPath.startsWith("/")) {
      throw new IllegalArgumentException("URI is not hierarchical")
    } else {
      fs.getPath(uri.getPath, Array.empty)
    }

  override def newFileSystem(path: Path, env: Map[String, _]): FileSystem =
    newFileSystem(path.toUri, env)

  override def newFileChannel(path: Path,
                              options: Set[_ <: OpenOption],
                              attrs: Array[FileAttribute[_]]): FileChannel =
    FileChannel.open(path, options, attrs)

  override def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]): DirectoryStream[Path] =
    new DirectoryStreamImpl[Path](Files.list(dir), filter)

  override def createDirectory(dir: Path,
                               attrs: Array[FileAttribute[_]]): Unit =
    Files.createDirectory(dir, attrs)

  override def createSymbolicLink(link: Path,
                                  target: Path,
                                  attrs: Array[FileAttribute[_]]): Unit =
    Files.createSymbolicLink(link, target, attrs)

  override def createLink(link: Path, existing: Path): Unit =
    Files.createLink(link, existing)

  override def delete(path: Path): Unit =
    Files.delete(path)

  override def readSymbolicLink(link: Path): Path =
    readSymbolicLink(link)

  override def copy(source: Path,
                    target: Path,
                    options: Array[CopyOption]): Unit =
    Files.copy(source, target, options)

  override def move(source: Path,
                    target: Path,
                    options: Array[CopyOption]): Unit =
    Files.move(source, target, options)

  override def isSameFile(path: Path, path2: Path): Boolean =
    Files.isSameFile(path, path2)

  override def isHidden(path: Path): Boolean =
    Files.isHidden(path)

  override def checkAccess(path: Path, modes: Array[AccessMode]): Unit = {
    val file = path.toFile
    if (modes.contains(AccessMode.READ) && !file.canRead())
      throw new AccessDeniedException(path.toString)
    if (modes.contains(AccessMode.WRITE) && !file.canWrite())
      throw new AccessDeniedException(path.toString)
    if (modes.contains(AccessMode.EXECUTE) && !file.canExecute())
      throw new AccessDeniedException(path.toString)

    ()
  }

  override def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]): V =
    (knownFileAttributeViews.get(tpe) match {
      case None     => null
      case Some(fn) => fn(path, options)
    }).asInstanceOf[V]

  override def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: Array[LinkOption]): A =
    Files.readAttributes(path, tpe, options)

  override def readAttributes(
      path: Path,
      attributes: String,
      options: Array[LinkOption]): Map[String, Object] =
    Files.readAttributes(path, attributes, options)

  override def setAttribute(path: Path,
                            attribute: String,
                            value: Object,
                            options: Array[LinkOption]): Unit =
    Files.setAttribute(path, attribute, value, options)

  private def getUserDir(): String = {
    val buff = stackalloc[CChar](4096)
    val res  = unistd.getcwd(buff, 4095)
    fromCString(res)
  }

  private val knownFileAttributeViews
    : SMap[Class[_ <: FileAttributeView],
           (Path, Array[LinkOption]) => FileAttributeView] =
    SMap(
      classOf[BasicFileAttributeView] -> (
          (p,
           l) => new NativeWindowsFileAttributeView(p, l)),
      classOf[PosixFileAttributeView] -> (
          (p,
           l) => new NativeWindowsFileAttributeView(p, l)),
      classOf[FileOwnerAttributeView] -> (
          (p,
           l) => new NativeWindowsFileAttributeView(p, l))
    )

}
