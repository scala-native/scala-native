package scala.scalanative.nio.fs.windows

import java.io.File
import java.net.URI
import java.nio.file.{
  FileSystem,
  Files,
  LinkOption,
  NoSuchFileException,
  Path,
  ProviderMismatchException,
  WatchEvent,
  WatchKey,
  WatchService
}
import java.util.Iterator
import scalanative.annotation.alwaysinline

class WindowsPath private[windows] (
    val pathType: WindowsPath.PathType,
    val root: Option[String],
    val segments: List[String]
)(implicit private val fs: WindowsFileSystem)
    extends Path {
  import WindowsPath._

  private def this(segments: List[String])(implicit fs: WindowsFileSystem) = {
    this(WindowsPath.PathType.Relative, None, segments)
  }

  private def this(path: String)(implicit fs: WindowsFileSystem) = {
    this(path :: Nil)
  }

  private lazy val path: String = {
    val drivePrefix = (pathType, root) match {
      case (PathType.UNC, Some(root)) =>
        root.drop(2).split('\\') match {
          case Array(host, share) => share + "\\"
          case _                  => ""
        }
      case (PathType.Absolute, Some(root))          => root
      case (PathType.DirectoryRelative, Some(root)) => root + "\\"
      case (PathType.DriveRelative, _)              => "\\"
      case _                                        => ""
    }
    drivePrefix + segments.mkString(seperator)
  }

  @alwaysinline
  final private def seperator: String = fs.getSeparator()

  override def getFileSystem(): FileSystem = fs

  override def isAbsolute(): Boolean = pathType == PathType.Absolute

  override def getRoot(): Path =
    if (root.isEmpty) null
    else new WindowsPath(pathType, root, Nil)

  override def getFileName(): Path = {
    if (root.contains(path)) null
    else if (segments.isEmpty) this
    else new WindowsPath(segments.last)
  }

  override def getParent(): Path = {
    val nameCount = getNameCount()
    if (nameCount == 0 || (nameCount == 1 && !isAbsolute()))
      null
    else if (root.isDefined)
      new WindowsPath(pathType, root, segments.init)
    else
      subpath(0, getNameCount() - 1)
  }

  override def getNameCount(): Int = segments.length

  @inline private def getNameString(index: Int): String = {
    val nameCount = getNameCount()
    if (index < 0 || nameCount == 0 || index >= nameCount)
      throw new IllegalArgumentException
    else {
      if (path.isEmpty()) null
      else segments(index)
    }
  }

  override def getName(index: Int): Path = getNameString(index) match {
    case null => this
    case n    => new WindowsPath(n)
  }

  override def subpath(beginIndex: Int, endIndex: Int): Path =
    new WindowsPath(segments.slice(beginIndex, endIndex))

  override def startsWith(other: Path): Boolean =
    if (fs.provider() == other.getFileSystem().provider()) {
      val otherLength = other.getNameCount()
      val thisLength = getNameCount()

      if (otherLength > thisLength) false
      else if (isAbsolute() != other.isAbsolute()) false
      else {
        (0 until otherLength).forall(i => getName(i) == other.getName(i))
      }
    } else {
      false
    }

  override def startsWith(other: String): Boolean =
    startsWith(WindowsPathParser(other))

  override def endsWith(other: Path): Boolean =
    if (fs.provider() == other.getFileSystem().provider()) {
      val otherLength = other.getNameCount()
      val thisLength = getNameCount()
      if (otherLength > thisLength) false
      else if (!other.isAbsolute()) {
        (0 until otherLength).forall(i =>
          getName(thisLength - 1 - i) == other.getName(otherLength - 1 - i)
        )
      } else if (isAbsolute()) {
        this == other
      } else {
        false
      }
    } else {
      false
    }

  override def endsWith(other: String): Boolean =
    endsWith(WindowsPathParser(other))

  private lazy val normalizedPath = WindowsPathParser(normalized(this))

  override def normalize(): Path = normalizedPath

  override def resolve(other: Path): Path = {
    if (other.isAbsolute() || path.isEmpty()) other
    else if (other.toString.isEmpty()) this
    else
      other match {
        case winPath: WindowsPath =>
          new WindowsPath(pathType, root, segments ++ winPath.segments)
        case _ =>
          WindowsPathParser(path.toString + seperator + other.toString)
      }
  }

  override def resolve(other: String): Path =
    resolve(WindowsPathParser(other))

  override def resolveSibling(other: Path): Path = {
    val parent = getParent()
    if (parent == null) other
    else parent.resolve(other)
  }

  override def resolveSibling(other: String): Path =
    resolveSibling(WindowsPathParser(other))

  override def relativize(other: Path): Path = {
    if (isAbsolute() ^ other.isAbsolute()) {
      throw new IllegalArgumentException("'other' is different type of Path")
    } else {
      val normThis = new WindowsPath(WindowsPath.normalized(this))
      if (normThis.toString.isEmpty()) {
        other
      } else if (other.startsWith(normThis)) {
        other.subpath(getNameCount(), other.getNameCount())
      } else if (normThis.getParent() == null) {
        new WindowsPath("../" + other.toString())
      } else {
        val next = normThis.getParent().relativize(other).toString()
        if (next.isEmpty()) new WindowsPath("..")
        else new WindowsPath("../" + next)
      }
    }
  }

  private lazy val absPath =
    if (isAbsolute()) this
    else WindowsPathParser(toFile().getAbsolutePath())

  override def toAbsolutePath(): Path = absPath

  override def toRealPath(options: Array[LinkOption]): Path = {
    if (options.contains(LinkOption.NOFOLLOW_LINKS)) toAbsolutePath()
    else {
      WindowsPathParser(toFile().getCanonicalPath()) match {
        case p if Files.exists(p, Array.empty) => p
        case p => throw new NoSuchFileException(p.path)
      }
    }
  }

  def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[_]]
  ): WatchKey =
    register(watcher, events, Array.empty)

  def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[_]],
      modifiers: Array[WatchEvent.Modifier]
  ): WatchKey =
    throw new ProviderMismatchException

  override def toFile(): File = new File(path)

  private lazy val uri =
    new URI(
      scheme = "file",
      userInfo = null,
      host = null,
      port = -1,
      path = absPath.path,
      query = null,
      fragment = null
    )

  override def toUri(): URI = uri

  override def iterator(): Iterator[Path] =
    new Iterator[Path] {
      private var i: Int = 0
      override def remove(): Unit = throw new UnsupportedOperationException()
      override def hasNext(): Boolean = i < getNameCount()
      override def next(): Path =
        if (hasNext()) {
          val name = getName(i)
          i += 1
          name
        } else {
          throw new NoSuchElementException()
        }
    }

  override def compareTo(other: Path): Int =
    if (fs.provider() == other.getFileSystem().provider()) {
      this.toString().compareTo(other.toString)
    } else {
      throw new ClassCastException()
    }

  override def equals(obj: Any): Boolean =
    obj match {
      case other: WindowsPath =>
        this.fs == other.fs && this.path == other.path
      case _ => false
    }

  override def hashCode(): Int =
    path.##

  override def toString(): String =
    path

}

private[windows] object WindowsPath {
  sealed trait PathType
  object PathType {
    case object Absolute extends PathType
    case object Relative extends PathType
    case object DirectoryRelative extends PathType
    case object DriveRelative extends PathType
    case object UNC extends PathType
  }

  def normalized(path: WindowsPath): String = {
    if (path.path.length < 2) return path.path
    val components = path.segments
      .foldLeft(List.empty[String]) {
        case (acc, "..") =>
          if (acc.isEmpty && path.isAbsolute()) Nil
          else if (acc.isEmpty) List("..")
          else acc.tail
        case (acc, ".") => acc
        case (acc, "")  => acc
        case (acc, seg) => seg :: acc
      }
      .reverse

    path.root.fold(components.mkString(path.seperator)) {
      components.mkString(_, path.seperator, "")
    }
  }

}
