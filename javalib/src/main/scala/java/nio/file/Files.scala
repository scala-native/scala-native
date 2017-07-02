package java.nio.file

import java.lang.Iterable

import java.io.{
  BufferedReader,
  BufferedWriter,
  File,
  FileOutputStream,
  InputStream,
  InputStreamReader,
  IOException,
  OutputStream,
  OutputStreamWriter
}

import java.nio.file.attribute._
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.channels.{FileChannel, SeekableByteChannel}

import java.util.concurrent.TimeUnit
import java.util.function.BiPredicate
import java.util.{
  EnumSet,
  HashMap,
  HashSet,
  Iterator,
  LinkedList,
  List,
  Map,
  Set
}
import java.util.stream.{Stream, WrappedScalaStream}

import scalanative.native._
import scalanative.posix.{limits, unistd}
import scalanative.posix.sys.stat

import scala.collection.immutable.{Map => SMap, Stream => SStream, Set => SSet}

object Files {

  // def getFileStore(path: Path): FileStore
  // def probeContentType(path: Path): String

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
    val in     = newInputStream(source, Array.empty)
    val result = copy(in, out)
    in.close()
    result
  }

  def copy(source: Path, target: Path, options: Array[CopyOption]): Path = {
    val in = newInputStream(source, Array.empty)
    try copy(in, target, options)
    finally in.close()
    target
  }

  private def copy(in: InputStream, out: OutputStream): Long = {
    var written: Long = 0L
    var value: Int    = 0

    while ({ value = in.read; value != -1 }) {
      out.write(value)
      written += 1
    }

    written
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
    Zone { implicit z =>
      if (exists(link, Array.empty)) {
        throw new FileAlreadyExistsException(link.toString)
      } else if (unistd.link(toCString(existing.toString),
                             toCString(link.toString)) == 0) {
        link
      } else {
        throw new IOException()
      }
    }

  def createSymbolicLink(link: Path,
                         target: Path,
                         attrs: Array[FileAttribute[_]]): Path =
    Zone { implicit z =>
      if (exists(link, Array.empty)) {
        throw new FileAlreadyExistsException(target.toString)
      } else if (unistd.symlink(toCString(target.toString),
                                toCString(link.toString)) == 0) {
        setAttributes(link, attrs)
        link
      } else {
        throw new IOException()
      }
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
      val attributes = getFileAttributeView(p,
                                            classOf[BasicFileAttributeView],
                                            Array.empty).readAttributes()
      matcher.test(p, attributes)
    }
    new WrappedScalaStream(stream, None)
  }

  def getAttribute(path: Path,
                   attribute: String,
                   options: Array[LinkOption]): Object = {
    val sepIndex = attribute.indexOf(":")
    val (viewName, attrName) =
      if (sepIndex == -1) ("basic", attribute)
      else
        (attribute.substring(0, sepIndex),
         attribute.substring(sepIndex + 1, attribute.length))
    val viewClass = viewNamesToClasses
      .get(viewName)
      .getOrElse(throw new UnsupportedOperationException())
    val view = getFileAttributeView(path, viewClass, options)
    view.getAttribute(attrName)
  }

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]): V =
    path.getFileSystem().provider().getFileAttributeView(path, tpe, options)

  def getLastModifiedTime(path: Path, options: Array[LinkOption]): FileTime = {
    val realPath = path.toRealPath(options)
    val attributes =
      getFileAttributeView(path, classOf[BasicFileAttributeView], options)
        .readAttributes()
    attributes.lastModifiedTime
  }

  def getOwner(path: Path, options: Array[LinkOption]): UserPrincipal = {
    val view =
      getFileAttributeView(path, classOf[FileOwnerAttributeView], options)
    view.getOwner()
  }

  def getPosixFilePermissions(
      path: Path,
      options: Array[LinkOption]): Set[PosixFilePermission] =
    getAttribute(path, "posix:permissions", options)
      .asInstanceOf[Set[PosixFilePermission]]

  def isDirectory(path: Path, options: Array[LinkOption]): Boolean = {
    val notALink =
      if (options.contains(LinkOption.NOFOLLOW_LINKS)) !isSymbolicLink(path)
      else true
    exists(path, options) && notALink && path.toFile().isDirectory()
  }

  def isExecutable(path: Path): Boolean =
    path.toFile().canExecute()

  def isHidden(path: Path): Boolean =
    path.toFile().isHidden()

  def isReadable(path: Path): Boolean =
    path.toFile().canRead()

  def isRegularFile(path: Path, options: Array[LinkOption]): Boolean =
    Zone { implicit z =>
      val buf = alloc[stat.stat]
      val err =
        if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
          stat.lstat(toCString(path.toFile.getPath()), buf)
        } else {
          stat.stat(toCString(path.toFile.getPath()), buf)
        }
      if (err == 0) stat.S_ISREG(!(buf._13)) == 1
      else false
    }

  def isSameFile(path: Path, path2: Path): Boolean =
    path.toFile().getCanonicalPath() == path2.toFile().getCanonicalPath()

  def isSymbolicLink(path: Path): Boolean =
    Zone { implicit z =>
      val buf = alloc[stat.stat]
      if (stat.lstat(toCString(path.toFile.getPath()), buf) == 0) {
        stat.S_ISLNK(!(buf._13)) == 1
      } else {
        false
      }
    }

  def isWritable(path: Path): Boolean =
    path.toFile().canWrite()

  def lines(path: Path): Stream[String] =
    lines(path, StandardCharsets.UTF_8)

  def lines(path: Path, cs: Charset): Stream[String] =
    newBufferedReader(path, cs).lines()

  private def _list(dir: Path): SStream[Path] =
    dir.toFile().list().toStream.map(dir.resolve)

  def list(dir: Path): Stream[Path] =
    if (!isDirectory(dir, Array.empty)) {
      throw new NotDirectoryException(dir.toString)
    } else {
      new WrappedScalaStream(_list(dir), None)
    }

  def move(source: Path, target: Path, options: Array[CopyOption]): Path = {
    copy(source, target, options)
    delete(source)
    target
  }

  def newBufferedReader(path: Path): BufferedReader =
    newBufferedReader(path, StandardCharsets.UTF_8)

  def newBufferedReader(path: Path, cs: Charset): BufferedReader =
    new BufferedReader(
      new InputStreamReader(newInputStream(path, Array.empty), cs))

  def newBufferedWriter(path: Path,
                        cs: Charset,
                        options: Array[OpenOption]): BufferedWriter = {
    new BufferedWriter(
      new OutputStreamWriter(newOutputStream(path, options), cs))
  }

  def newBufferedWriter(path: Path,
                        options: Array[OpenOption]): BufferedWriter =
    newBufferedWriter(path, StandardCharsets.UTF_8, options)

  def newByteChannel(path: Path,
                     _options: Array[OpenOption]): SeekableByteChannel = {
    val options = new HashSet[OpenOption]()
    _options.foreach(options.add _)
    newByteChannel(path, options, Array.empty)
  }

  def newByteChannel(path: Path,
                     options: Set[_ <: OpenOption],
                     attrs: Array[FileAttribute[_]]): SeekableByteChannel =
    path.getFileSystem().provider().newByteChannel(path, options, attrs)

  def newDirectoryStream(dir: Path): DirectoryStream[Path] = {
    val filter = new DirectoryStream.Filter[Path] {
      override def accept(p: Path): Boolean = true
    }
    newDirectoryStream(dir, filter)
  }

  def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]): DirectoryStream[Path] =
    dir.getFileSystem().provider().newDirectoryStream(dir, filter)

  def newDirectoryStream(dir: Path, glob: String): DirectoryStream[Path] = {
    val filter = new DirectoryStream.Filter[Path] {
      private val matcher =
        FileSystems.getDefault().getPathMatcher("glob:" + glob)
      override def accept(p: Path): Boolean = matcher.matches(p)
    }
    newDirectoryStream(dir, filter)
  }

  def newInputStream(path: Path, options: Array[OpenOption]): InputStream =
    path.getFileSystem().provider().newInputStream(path, options)

  def newOutputStream(path: Path, options: Array[OpenOption]): OutputStream =
    path.getFileSystem().provider().newOutputStream(path, options)

  def notExists(path: Path, options: Array[LinkOption]): Boolean =
    !exists(path, options)

  def readAllBytes(path: Path): Array[Byte] = {
    val bytes  = new Array[Byte](size(path).toInt)
    val buffer = new Array[Byte](4096)
    val input  = newInputStream(path, Array.empty)
    var offset = 0
    var read   = 0
    while ({ read = input.read(buffer); read != -1 }) {
      System.arraycopy(buffer, 0, bytes, offset, read)
      offset += read
    }
    bytes
  }

  def readAllLines(path: Path): List[String] =
    readAllLines(path, StandardCharsets.UTF_8)

  def readAllLines(path: Path, cs: Charset): List[String] = {
    val list   = new LinkedList[String]()
    val reader = newBufferedReader(path, cs)
    val lines  = reader.lines.iterator
    while (lines.hasNext()) {
      list.add(lines.next())
    }
    list
  }

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: Array[LinkOption]): A = {
    val viewClass = attributesClassesToViews
      .get(tpe)
      .getOrElse(throw new UnsupportedOperationException())
    val view = getFileAttributeView(path, viewClass, options)
    view.readAttributes().asInstanceOf[A]
  }

  def readAttributes(path: Path,
                     attributes: String,
                     options: Array[LinkOption]): Map[String, Object] = {
    val parts = attributes.split(":")
    val (viewName, atts) =
      if (parts.length == 1) ("basic", parts(0))
      else (parts(0), parts(1))

    if (atts == "*") {
      val viewClass = viewNamesToClasses
        .get(viewName)
        .getOrElse(throw new UnsupportedOperationException())
      getFileAttributeView(path, viewClass, options).asMap
    } else {
      val attrs = atts.split(",")
      val map   = new HashMap[String, Object]()
      attrs.foreach { att =>
        val value = getAttribute(path, viewName + ":" + att, options)
        if (value != null)
          map.put(att, value)
      }
      map
    }
  }

  def readSymbolicLink(link: Path): Path =
    if (!isSymbolicLink(link)) {
      throw new NotLinkException(link.toString)
    } else
      Zone { implicit z =>
        val buf: CString = alloc[Byte](limits.PATH_MAX)
        if (unistd.readlink(toCString(link.toString), buf, limits.PATH_MAX) == -1) {
          throw new IOException()
        } else {
          Paths.get(fromCString(buf), Array.empty)
        }
      }

  def setAttribute(path: Path,
                   attribute: String,
                   value: AnyRef,
                   options: Array[LinkOption]): Path = {
    val sepIndex = attribute.indexOf(":")
    val (viewName, attrName) =
      if (sepIndex == -1) ("basic", attribute)
      else
        (attribute.substring(0, sepIndex),
         attribute.substring(sepIndex + 1, attribute.length))
    val viewClass = viewNamesToClasses
      .get(viewName)
      .getOrElse(throw new UnsupportedOperationException())
    val view = getFileAttributeView(path, viewClass, options)
    view.setAttribute(attrName, value)
    path
  }

  def setLastModifiedTime(path: Path, time: FileTime): Path = {
    val view =
      getFileAttributeView(path, classOf[BasicFileAttributeView], Array.empty)
    view.setTimes(time, null, null)
    path
  }

  def setOwner(path: Path, owner: UserPrincipal): Path = {
    val view =
      getFileAttributeView(path, classOf[FileOwnerAttributeView], Array.empty)
    view.setOwner(owner)
    path
  }

  def setPosixFilePermissions(path: Path,
                              perms: Set[PosixFilePermission]): Path = {
    val view =
      getFileAttributeView(path, classOf[PosixFileAttributeView], Array.empty)
    view.setPermissions(perms)
    path
  }

  def size(path: Path): Long =
    getAttribute(path, "basic:size", Array.empty).asInstanceOf[Long]

  def walk(start: Path, options: Array[FileVisitOption]): Stream[Path] =
    walk(start, Int.MaxValue, options)

  def walk(start: Path,
           maxDepth: Int,
           options: Array[FileVisitOption]): Stream[Path] =
    new WrappedScalaStream(walk(start, maxDepth, 0, options, Set(start)), None)

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
        val attributes = getFileAttributeView(p,
                                              classOf[BasicFileAttributeView],
                                              Array.empty).readAttributes()
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

  def write(path: Path,
            bytes: Array[Byte],
            _options: Array[OpenOption]): Path = {
    val options =
      if (_options.isEmpty)
        Array[OpenOption](StandardOpenOption.CREATE,
                          StandardOpenOption.TRUNCATE_EXISTING,
                          StandardOpenOption.WRITE)
      else _options

    val out = newOutputStream(path, options)
    out.write(bytes)
    out.close()
    path
  }

  def write(path: Path,
            lines: Iterable[_ <: CharSequence],
            cs: Charset,
            _options: Array[OpenOption]): Path = {
    val options =
      if (_options.isEmpty)
        Array[OpenOption](StandardOpenOption.CREATE,
                          StandardOpenOption.TRUNCATE_EXISTING,
                          StandardOpenOption.WRITE)
      else _options
    val writer = newBufferedWriter(path, cs, options)
    val it     = lines.iterator
    while (it.hasNext()) {
      writer.append(it.next())
      writer.newLine()
    }
    writer.close()
    path
  }

  def write(path: Path,
            lines: Iterable[_ <: CharSequence],
            options: Array[OpenOption]): Path =
    write(path, lines, StandardCharsets.UTF_8, options)

  private def setAttributes(path: Path, attrs: Array[FileAttribute[_]]): Unit =
    attrs.map(a => (a.name, a.value)).toMap.foreach {
      case (name, value: Object) =>
        setAttribute(path, name, value, Array.empty)
    }

  private val attributesClassesToViews: SMap[
    Class[_ <: BasicFileAttributes],
    Class[_ <: BasicFileAttributeView]] =
    SMap(
      classOf[BasicFileAttributes] -> classOf[BasicFileAttributeView],
      classOf[DosFileAttributes]   -> classOf[DosFileAttributeView],
      classOf[PosixFileAttributes] -> classOf[PosixFileAttributeView]
    )

  private val viewNamesToClasses: SMap[String, Class[_ <: FileAttributeView]] =
    SMap(
      "acl"   -> classOf[AclFileAttributeView],
      "basic" -> classOf[BasicFileAttributeView],
      "dos"   -> classOf[DosFileAttributeView],
      "owner" -> classOf[FileOwnerAttributeView],
      "user"  -> classOf[UserDefinedFileAttributeView],
      "posix" -> classOf[PosixFileAttributeView]
    )

}
