package java.nio.file

import java.lang.Iterable

import java.io.{
  BufferedReader,
  BufferedWriter,
  File,
  FileInputStream,
  FileOutputStream,
  InputStream,
  IOException,
  OutputStream
}

import java.nio.file.attribute._
import java.nio.charset.Charset
import java.nio.channels.SeekableByteChannel

import java.util.concurrent.TimeUnit
import java.util.function.BiPredicate
import java.util.{EnumSet, HashSet, List, Map, Set}
import java.util.stream.{Stream, WrappedScalaStream}

import scala.scalanative.native.{CString, fromCString, Ptr, sizeof, toCString}
import scala.scalanative.posix.{limits, stat, unistd}
import scala.scalanative.runtime.GC

import scala.collection.immutable.{Stream => SStream, Set => SSet}

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
      } else if (targetFile.isDirectory && targetFile.list.isEmpty && replaceExisting) {
        if (!targetFile.delete()) throw new IOException()
        new FileOutputStream(targetFile, append = false)
      } else {
        throw new FileAlreadyExistsException(targetFile.getAbsolutePath)
      }

    try copy(in, out)
    finally out.close()
  }

  def copy(source: Path, out: OutputStream): Long = {
    val in = new FileInputStream(source.toFile)
    copy(in, out)
  }

  def copy(source: Path, target: Path, options: Array[CopyOption]): Path = {
    val in = new FileInputStream(source.toFile)
    try copy(in, target, options)
    finally in.close()
    target
  }

  def createDirectories(dir: Path, attrs: Array[FileAttribute[_]]): Path =
    if (exists(dir, Array.empty) && !isDirectory(dir, Array.empty))
      throw new FileAlreadyExistsException(dir.toString)
    else if (exists(dir, Array.empty)) dir
    else {
      val parent = dir.getParent()
      if (parent != null) createDirectories(parent, attrs)
      createDirectory(dir, attrs)
      dir
    }

  def createDirectory(dir: Path, attrs: Array[FileAttribute[_]]): Path =
    if (exists(dir, Array.empty))
      throw new FileAlreadyExistsException(dir.toString)
    else if (dir.toFile().mkdir()) {
      setAttributes(dir, attrs)
      dir
    } else {
      throw new IOException()
    }

  def createFile(path: Path, attrs: Array[FileAttribute[_]]): Path =
    if (exists(path, Array.empty))
      throw new FileAlreadyExistsException(path.toString)
    else if (path.toFile().createNewFile()) {
      setAttributes(path, attrs)
      path
    } else {
      throw new IOException()
    }

  def createLink(link: Path, existing: Path): Path =
    if (exists(link, Array.empty)) {
      throw new FileAlreadyExistsException(link.toString)
    } else if (unistd.link(toCString(existing.toString),
                           toCString(link.toString)) == 0) {
      link
    } else {
      throw new IOException()
    }

  def createSymbolicLink(link: Path,
                         target: Path,
                         attrs: Array[FileAttribute[_]]): Path =
    if (exists(link, Array.empty)) {
      throw new FileAlreadyExistsException(target.toString)
    } else if (unistd.symlink(toCString(target.toString),
                              toCString(link.toString)) == 0) {
      setAttributes(link, attrs)
      link
    } else {
      throw new IOException()
    }

  private def createTempDirectory(dir: File,
                                  prefix: String,
                                  attrs: Array[FileAttribute[_]]): Path = {
    val temp = File.createTempFile(prefix, "", dir)
    if (temp.delete() && temp.mkdir()) {
      val tempPath = temp.toPath()
      setAttributes(tempPath, attrs)
      tempPath
    } else {
      throw new IOException()
    }
  }

  def createTempDirectory(dir: Path,
                          prefix: String,
                          attrs: Array[FileAttribute[_]]): Path =
    createTempDirectory(dir.toFile, prefix, attrs)

  def createTempDirectory(prefix: String,
                          attrs: Array[FileAttribute[_]]): Path =
    createTempDirectory(null: File, prefix, attrs)

  private def createTempFile(dir: File,
                             prefix: String,
                             suffix: String,
                             attrs: Array[FileAttribute[_]]): Path = {
    val temp     = File.createTempFile(prefix, suffix, dir)
    val tempPath = temp.toPath()
    setAttributes(tempPath, attrs)
    tempPath
  }

  def createTempFile(dir: Path,
                     prefix: String,
                     suffix: String,
                     attrs: Array[FileAttribute[_]]): Path =
    createTempFile(dir.toFile(), prefix, suffix, attrs)

  def createTempFile(prefix: String,
                     suffix: String,
                     attrs: Array[FileAttribute[_]]): Path =
    createTempFile(null: File, prefix, suffix, attrs)

  def delete(path: Path): Unit =
    if (!exists(path, Array.empty)) {
      throw new NoSuchFileException(path.toString)
    } else {
      if (path.toFile().delete()) ()
      else throw new IOException()
    }

  def deleteIfExists(path: Path): Boolean =
    try { delete(path); true } catch { case _: NoSuchFileException => false }

  def exists(path: Path, options: Array[LinkOption]): Boolean =
    if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
      path.toFile.exists() || isSymbolicLink(path)
    } else {
      path.toFile.exists()
    }

  def find(start: Path,
           maxDepth: Int,
           matcher: BiPredicate[Path, BasicFileAttributes],
           options: Array[FileVisitOption]): Stream[Path] = {
    val stream = walk(start, maxDepth, 0, options, SSet.empty).filter { p =>
      val attributes = getAttributes(p)
      matcher.test(p, attributes)
    }
    new WrappedScalaStream(stream)
  }

  // def getAttribute(path: Path,
  //                  attribute: String,
  //                  options: Array[LinkOption]): Object =
  //   ???
  //
  // def getFileAttributeView[V <: FileAttributeView](
  //     path: Path,
  //     tpe: Class[V],
  //     options: Array[LinkOption]): V =
  //   ???
  //
  // def getFileStore(path: Path): FileStore =
  //   ???
  //
  // def getLastModifiedTime(path: Path, options: Array[LinkOption]): FileTime =
  //   ???
  //
  // def getOwner(path: Path, options: Array[LinkOption]): UserPrincipal =
  //   ???
  //
  // def getPosixFilePermissions(
  //     path: Path,
  //     options: Array[LinkOption]): Set[PosixFilePermission] =
  //   ???

  def isDirectory(path: Path, options: Array[LinkOption]): Boolean = {
    val notALink =
      if (options.contains(LinkOption.NOFOLLOW_LINKS)) !isSymbolicLink(path)
      else true
    exists(path, options) && notALink && path.toFile().isDirectory()
  }

  // def isExecutable(path: Path): Boolean =
  //   ???
  //
  // def isHidden(path: Path): Boolean =
  //   ???
  //
  // def isReadable(path: Path): Boolean =
  //   ???

  def isRegularFile(path: Path, options: Array[LinkOption]): Boolean = {
    val buf = GC.malloc_atomic(sizeof[stat.stat]).cast[Ptr[stat.stat]]
    val err =
    if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
      stat.lstat(toCString(path.toFile.getPath()), buf)
    } else {
      stat.stat(toCString(path.toFile.getPath()), buf)
    }
    if (err == 0) stat.S_ISREG(!(buf._13)) == 1
    else false
  }

  // def isSameFile(path: Path, path2: Path): Boolean =
  //   ???

  def isSymbolicLink(path: Path): Boolean = {
    val buf = GC.malloc_atomic(sizeof[stat.stat]).cast[Ptr[stat.stat]]
    if (stat.lstat(toCString(path.toFile.getPath()), buf) == 0) {
      stat.S_ISLNK(!(buf._13)) == 1
    } else {
      false
    }
  }

  // def isWritable(path: Path): Boolean =
  //   ???
  //
  // def lines(path: Path): Stream[String] =
  //   ???
  //
  // def lines(path: Path, cs: Charset): Stream[String] =
  //   ???

  private def _list(dir: Path): SStream[Path] =
    dir.toFile().list().toStream.map(dir.resolve)

  def list(dir: Path): Stream[Path] =
    new WrappedScalaStream(_list(dir))

  // def move(source: Path, target: Path, options: Array[CopyOption]): Path =
  //   ???
  //
  // def newBufferedReader(path: Path): BufferedReader =
  //   ???
  //
  // def newBufferedReader(path: Path, cs: Charset): BufferedReader =
  //   ???
  //
  // def newBufferedReader(path: Path,
  //                       cs: Charset,
  //                       options: Array[OpenOption]): BufferedReader =
  //   ???
  //
  // def newBufferedWriter(path: Path,
  //                       cs: Charset,
  //                       options: Array[OpenOption]): BufferedWriter =
  //   ???
  //
  // def newBufferedWriter(path: Path,
  //                       options: Array[OpenOption]): BufferedWriter =
  //   ???
  //
  // def newByteChannel(path: Path,
  //                    options: Array[OpenOption]): SeekableByteChannel =
  //   ???
  //
  // def newByteChannel(path: Path,
  //                    options: Set[_ <: OpenOption],
  //                    attrs: Array[FileAttribute[_]]): SeekableByteChannel =
  //   ???
  //
  // def newDirectoryStream(dir: Path): DirectoryStream[Path] =
  //   ???
  //
  // def newDirectoryStream(
  //     dir: Path,
  //     filter: DirectoryStream.Filter[_ >: Path]): DirectoryStream[Path] =
  //   ???
  //
  // def newDirectoryStream(dir: Path, glob: String): DirectoryStream[Path] =
  //   ???
  //
  // def newInputStream(path: Path, options: Array[OpenOption]): InputStream =
  //   ???
  //
  // def newOutputStream(path: Path, options: Array[OpenOption]): OutputStream =
  //   ???
  //
  // def notExists(path: Path, options: Array[LinkOption]): Boolean =
  //   ???
  //
  // def probeContentType(path: Path): String =
  //   ???
  //
  // def readAllBytes(path: Path): Array[Byte] =
  //   ???
  //
  // def readAllLines(path: Path): List[String] =
  //   ???
  //
  // def readAllLines(path: Path, cs: Charset): List[String] =
  //   ???
  //
  // def readAttributes[A <: BasicFileAttributes](path: Path,
  //                                              tpe: Class[A],
  //                                              options: Array[LinkOption]): A =
  //   ???
  //
  // def readAttributes(path: Path,
  //                    attributes: String,
  //                    options: Array[LinkOption]): Map[String, Object] =
  //   ???

  def readSymbolicLink(link: Path): Path =
    if (!isSymbolicLink(link)) throw new NotLinkException(link.toString)
    else {
      val buf: CString = GC.malloc(limits.PATH_MAX).cast[CString]
      if (unistd.readlink(toCString(link.toString), buf, limits.PATH_MAX) == -1) {
        throw new IOException()
      } else {
        Paths.get(fromCString(buf), Array.empty)
      }
    }

  def setAttribute(path: Path,
                   attribute: String,
                   value: Any,
                   options: Array[LinkOption]): Path =
    ???

  // def setLastModifiedTime(path: Path, time: FileTime): Path =
  //   ???
  //
  // def setOwner(path: Path, owner: UserPrincipal): Path =
  //   ???
  //
  // def setPosixFilePermissions(path: Path,
  //                             perms: Set[PosixFilePermission]): Path =
  //   ???
  //
  // def size(path: Path): Long =
  //   ???
  //
  def walk(start: Path, options: Array[FileVisitOption]): Stream[Path] =
    walk(start, Int.MaxValue, options)

  def walk(start: Path,
           maxDepth: Int,
           options: Array[FileVisitOption]): Stream[Path] =
    new WrappedScalaStream(walk(start, maxDepth, 0, options, Set(start)))

  private def walk(start: Path,
                   maxDepth: Int,
                   currentDepth: Int,
                   options: Array[FileVisitOption],
                   visited: SSet[Path]): SStream[Path] =
    if (!isDirectory(start, Array.empty))
      throw new NotDirectoryException(start.toString)
    else {
      start #:: _list(start).flatMap {
        case p
            if isSymbolicLink(p) && options.contains(
              FileVisitOption.FOLLOW_LINKS) =>
          val newVisited = visited + p
          val target     = readSymbolicLink(p)
          if (newVisited.contains(target))
            throw new FileSystemLoopException(p.toString)
          else walk(p, maxDepth, currentDepth + 1, options, newVisited)
        case p
            if isDirectory(p, Array(LinkOption.NOFOLLOW_LINKS)) && currentDepth < maxDepth =>
          val newVisited =
            if (options.contains(FileVisitOption.FOLLOW_LINKS)) visited + p
            else visited
          walk(p, maxDepth, currentDepth + 1, options, newVisited)
        case p => p #:: SStream.Empty
      }
    }

  def walkFileTree(start: Path, visitor: FileVisitor[_ >: Path]): Path =
    walkFileTree(start,
                 EnumSet.noneOf(classOf[FileVisitOption]),
                 Int.MaxValue,
                 visitor)

  private case object TerminateTraversalException extends Exception
  def walkFileTree(start: Path,
                   options: Set[FileVisitOption],
                   maxDepth: Int,
                   visitor: FileVisitor[_ >: Path]): Path =
    try _walkFileTree(start, options, maxDepth, visitor)
    catch { case TerminateTraversalException => start }

  private def _walkFileTree(start: Path,
                            options: Set[FileVisitOption],
                            maxDepth: Int,
                            visitor: FileVisitor[_ >: Path]): Path = {
    val stream = walk(start,
                      maxDepth,
                      0,
                      options.toArray.asInstanceOf[Array[FileVisitOption]],
                      SSet.empty)
    val dirsToSkip = scala.collection.mutable.Set.empty[Path]
    val openDirs   = scala.collection.mutable.Stack.empty[Path]
    stream.foreach { p =>
      val parent = p.getParent

      if (dirsToSkip.contains(parent)) ()
      else {
        val attributes = getAttributes(p)

        while (openDirs.nonEmpty && !parent.startsWith(openDirs.head)) {
          visitor.postVisitDirectory(openDirs.pop(), null)
        }

        val result =
          if (attributes.isRegularFile) {
            visitor.visitFile(p, attributes)
          } else if (attributes.isDirectory) {
            openDirs.push(p)
            visitor.preVisitDirectory(p, attributes) match {
              case FileVisitResult.SKIP_SUBTREE =>
                openDirs.pop; FileVisitResult.SKIP_SUBTREE
              case other => other
            }
          } else {
            FileVisitResult.CONTINUE
          }

        result match {
          case FileVisitResult.TERMINATE     => throw TerminateTraversalException
          case FileVisitResult.SKIP_SUBTREE  => dirsToSkip += p
          case FileVisitResult.SKIP_SIBLINGS => dirsToSkip += parent
          case FileVisitResult.CONTINUE      => ()
        }
      }

    }
    while (openDirs.nonEmpty) {
      visitor.postVisitDirectory(openDirs.pop(), null)
    }
    start
  }

  // def write(path: Path, bytes: Array[Byte], options: Array[OpenOption]): Path =
  //   ???
  //
  // def write(path: Path,
  //           lines: Iterable[_ <: CharSequence],
  //           cs: Charset,
  //           options: Array[OpenOption]): Path =
  //   ???
  //
  // def write(path: Path,
  //           lines: Iterable[_ <: CharSequence],
  //           options: Array[OpenOption]): Path =
  //   ???

  private def getAttributes(path: Path): BasicFileAttributes =
    new BasicFileAttributes {
      val sb = GC.malloc_atomic(sizeof[stat.stat]).cast[Ptr[stat.stat]]
      stat.stat(toCString(path.toString), sb)

      override val fileKey: Object = (!(sb._3)).asInstanceOf[Object]
      override val isDirectory     = stat.S_ISDIR(!(sb._13)) == 1
      override val isRegularFile   = stat.S_ISREG(!(sb._13)) == 1
      override val isSymbolicLink  = stat.S_ISLNK(!(sb._13)) == 1
      override val isOther         = !isDirectory && !isRegularFile && !isSymbolicLink

      override val lastAccessTime =
        FileTime.from(!(sb._7), TimeUnit.SECONDS)
      override val lastModifiedTime =
        FileTime.from(!(sb._8), TimeUnit.SECONDS)
      override val creationTime =
        FileTime.from(!(sb._9), TimeUnit.SECONDS)

      override val size = !(sb._6)
    }

  private def setAttributes(path: Path, attrs: Array[FileAttribute[_]]): Unit =
    attrs.map(a => (a.name, a.value)).toMap.foreach {
      case (name, value) => setAttribute(path, name, value, Array.empty)
    }

}
