package java.nio.file

import java.lang.Iterable

import java.io.{
  BufferedReader,
  BufferedWriter,
  FileInputStream,
  FileOutputStream,
  InputStream,
  IOException,
  OutputStream
}

import java.nio.file.attribute._
import java.nio.charset.Charset
import java.nio.channels.SeekableByteChannel

import java.util.function.BiPredicate
import java.util.{List, Map, Set}
import java.util.stream.Stream

object Files {

  private def copy(in: InputStream, out: OutputStream): Long = {
    var written: Long = 0L
    var value: Int    = 0

    while ({ value = in.read; value != -1 }) {
      out.write(value)
      written += 1
    }

    written
  }

  def copy(in: InputStream, target: Path, options: Array[CopyOption]): Long = {
    val replaceExisting =
      if (options.isEmpty) false
      else if (options.length == 1 && options(0) == StandardCopyOption.REPLACE_EXISTING)
        true
      else throw new UnsupportedOperationException()

    val targetFile = target.toFile

    val out =
      if (!targetFile.exists || (targetFile.isFile && replaceExisting)) {
        new FileOutputStream(targetFile, append = false)
      } else if (targetFile.isDirectory && targetFile.list.isEmpty) {
        if (!targetFile.delete()) throw new IOException()
        new FileOutputStream(targetFile, append = false)
      } else {
        throw new FileAlreadyExistsException(targetFile.getAbsolutePath)
      }
    copy(in, out)
  }

  def copy(source: Path, out: OutputStream): Long = {
    val in = new FileInputStream(source.toFile)
    copy(in, out)
  }

  def copy(source: Path, target: Path, options: Array[CopyOption]): Path = {
    val in = new FileInputStream(source.toFile)
    copy(in, target, options)
    target
  }

  def createDirectories(dir: Path, attrs: Array[FileAttribute[_]]): Path =
    ???

  def createDirectory(dir: Path, attrs: Array[FileAttribute[_]]): Path =
    ???

  def createFile(path: Path, attrs: Array[FileAttribute[_]]): Path =
    ???

  def createLink(link: Path, existing: Path): Path =
    ???

  def createSymbolicLink(link: Path,
                         target: Path,
                         attrs: Array[FileAttribute[_]]): Path =
    ???

  def createTempDirectory(dir: Path,
                          prefix: String,
                          attrs: Array[FileAttribute[_]]): Path =
    ???

  def createTempDirectory(prefix: String,
                          attrs: Array[FileAttribute[_]]): Path =
    ???

  def createTempFile(dir: Path,
                     prefix: String,
                     suffix: String,
                     attrs: Array[FileAttribute[_]]): Path =
    ???

  def createTempFile(prefix: String,
                     suffix: String,
                     attrs: Array[FileAttribute[_]]): Path =
    ???

  def delete(path: Path): Unit =
    ???

  def deleteIfExists(path: Path): Boolean =
    ???

  def exists(path: Path, options: Array[LinkOption]): Boolean =
    ???

  def find(start: Path,
           maxDepth: Int,
           matcher: BiPredicate[Path, BasicFileAttributes],
           options: Array[FileVisitOption]): Stream[Path] =
    ???

  def getAttribute(path: Path,
                   attribute: String,
                   options: Array[LinkOption]): Object =
    ???

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]): V =
    ???

  def getFileStore(path: Path): FileStore =
    ???

  def getLastModifiedTime(path: Path, options: Array[LinkOption]): FileTime =
    ???

  def getOwner(path: Path, options: Array[LinkOption]): UserPrincipal =
    ???

  def getPosixFilePermissions(
      path: Path,
      options: Array[LinkOption]): Set[PosixFilePermission] =
    ???

  def isDirectory(path: Path, options: Array[LinkOption]): Boolean =
    ???

  def isExecutable(path: Path): Boolean =
    ???

  def isHidden(path: Path): Boolean =
    ???

  def isReadable(path: Path): Boolean =
    ???

  def isRegularFile(path: Path, options: Array[LinkOption]): Boolean =
    ???

  def isSameFile(path: Path, path2: Path): Boolean =
    ???

  def isSymbolicLink(path: Path): Boolean =
    ???

  def isWritable(path: Path): Boolean =
    ???

  def lines(path: Path): Stream[String] =
    ???

  def lines(path: Path, cs: Charset): Stream[String] =
    ???

  def list(dir: Path): Stream[Path] =
    ???

  def move(source: Path, target: Path, options: Array[CopyOption]): Path =
    ???

  def newBufferedReader(path: Path): BufferedReader =
    ???

  def newBufferedReader(path: Path, cs: Charset): BufferedReader =
    ???

  def newBufferedReader(path: Path,
                        cs: Charset,
                        options: Array[OpenOption]): BufferedReader =
    ???

  def newBufferedWriter(path: Path,
                        cs: Charset,
                        options: Array[OpenOption]): BufferedWriter =
    ???

  def newBufferedWriter(path: Path,
                        options: Array[OpenOption]): BufferedWriter =
    ???

  def newByteChannel(path: Path,
                     options: Array[OpenOption]): SeekableByteChannel =
    ???

  def newByteChannel(path: Path,
                     options: Set[_ <: OpenOption],
                     attrs: Array[FileAttribute[_]]): SeekableByteChannel =
    ???

  def newDirectoryStream(dir: Path): DirectoryStream[Path] =
    ???

  def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]): DirectoryStream[Path] =
    ???

  def newDirectoryStream(dir: Path, glob: String): DirectoryStream[Path] =
    ???

  def newInputStream(path: Path, options: Array[OpenOption]): InputStream =
    ???

  def newOutputStream(path: Path, options: Array[OpenOption]): OutputStream =
    ???

  def notExists(path: Path, options: Array[LinkOption]): Boolean =
    ???

  def probeContentType(path: Path): String =
    ???

  def readAllBytes(path: Path): Array[Byte] =
    ???

  def readAllLines(path: Path): List[String] =
    ???

  def readAllLines(path: Path, cs: Charset): List[String] =
    ???

  def readAttributes[A <: BasicFileAttributes](path: Path,
                                               tpe: Class[A],
                                               options: Array[LinkOption]): A =
    ???

  def readAttributes(path: Path,
                     attributes: String,
                     options: Array[LinkOption]): Map[String, Object] =
    ???

  def readSymbolicLink(link: Path): Path =
    ???

  def setAttribute(path: Path,
                   attribute: String,
                   value: Object,
                   options: Array[LinkOption]): Path =
    ???

  def setLastModifiedTime(path: Path, time: FileTime): Path =
    ???

  def setOwner(path: Path, owner: UserPrincipal): Path =
    ???

  def setPosixFilePermissions(path: Path,
                              perms: Set[PosixFilePermission]): Path =
    ???

  def size(path: Path): Long =
    ???

  def walk(start: Path, options: Array[FileVisitOption]): Stream[Path] =
    ???

  def walk(start: Path,
           maxDepth: Int,
           options: Array[FileVisitOption]): Stream[Path] =
    ???

  def walkFileTree(start: Path, visitor: FileVisitor[_ >: Path]): Path =
    ???

  def walkFileTree(start: Path,
                   options: Set[FileVisitOption],
                   maxDepth: Int,
                   visitor: FileVisitor[_ >: Path]): Path =
    ???

  def write(path: Path, bytes: Array[Byte], options: Array[OpenOption]): Path =
    ???

  def write(path: Path,
            lines: Iterable[_ <: CharSequence],
            cs: Charset,
            options: Array[OpenOption]): Path =
    ???

  def write(path: Path,
            lines: Iterable[_ <: CharSequence],
            options: Array[OpenOption]): Path =
    ???

}
