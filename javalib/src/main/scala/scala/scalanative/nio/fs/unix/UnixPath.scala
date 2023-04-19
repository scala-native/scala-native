package scala.scalanative.nio.fs.unix

import java.io.File
import java.net.URI
import java.nio.file._
import java.util.Iterator

class UnixPath(private val fs: UnixFileSystem, rawPath: String) extends Path {
  import UnixPath._

  private lazy val path: String = removeRedundantSlashes(rawPath)
  private lazy val offsets =
    if (path.isEmpty()) Array(-1, 0)
    else if (path == "/") Array(0)
    else {
      var i = 0
      var count = 1
      while ({
        count += 1
        i = path.indexOf('/', i + 1)
        i != -1
      }) ()
      val result = new Array[Int](count)
      i = if (path.charAt(0) == '/') 0 else -1
      var j = 0
      while ({
        result(j) = i
        i = path.indexOf('/', i + 1)
        j += 1
        i != -1
      }) ()
      result(count - 1) = path.length
      result
    }

  private lazy val _isAbsolute = rawPath.startsWith("/")

  private lazy val root = if (isAbsolute()) new UnixPath(fs, "/") else null

  private lazy val fileName =
    if (path == "/") null
    else if (path.isEmpty()) this
    else new UnixPath(fs, path.split("/").last)

  private lazy val parent = {
    val nameCount = getNameCount()
    if (nameCount == 0 || (nameCount == 1 && !isAbsolute())) null
    else if (isAbsolute())
      new UnixPath(fs, "/" + subpath(0, nameCount - 1).toString)
    else subpath(0, nameCount - 1)
  }

  private lazy val normalizedPath = new UnixPath(fs, normalized(this))

  private lazy val absPath =
    if (path.startsWith("/")) this
    else new UnixPath(fs, toFile().getAbsolutePath())

  private lazy val file =
    if (isAbsolute()) new File(path)
    else new File(s"${fs.defaultDirectory}/$path")

  private lazy val uri =
    new URI(
      scheme = "file",
      userInfo = null,
      host = null,
      port = -1,
      path = toFile().getAbsolutePath(),
      query = null,
      fragment = null
    )

  override def getFileSystem(): FileSystem = fs

  override def isAbsolute(): Boolean = _isAbsolute

  override def getRoot(): Path = root

  override def getFileName(): Path = fileName

  override def getParent(): Path = parent

  override def getNameCount(): Int = offsets.size - 1

  @inline private def getNameString(index: Int): String = {
    val nameCount = getNameCount()
    if (index < 0 || nameCount == 0 || index >= nameCount)
      throw new IllegalArgumentException
    else {
      if (path.isEmpty()) null
      else path.substring(offsets(index) + 1, offsets(index + 1))
    }
  }

  override def getName(index: Int): Path = getNameString(index) match {
    case null => this
    case n    => new UnixPath(fs, n)
  }

  override def subpath(beginIndex: Int, endIndex: Int): Path =
    new UnixPath(fs, (beginIndex until endIndex).map(getName).mkString("/"))

  override def startsWith(other: Path): Boolean =
    if (fs.provider() == other.getFileSystem().provider()) {
      val otherLength = other.getNameCount()
      val thisLength = getNameCount()

      if (otherLength > thisLength) false
      else if (isAbsolute() ^ other.isAbsolute()) false
      else {
        (0 until otherLength).forall(i => getName(i) == other.getName(i))
      }
    } else {
      false
    }

  override def startsWith(other: String): Boolean =
    startsWith(new UnixPath(fs, other))

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
    endsWith(new UnixPath(fs, other))

  override def normalize(): Path = normalizedPath

  override def resolve(other: Path): Path =
    if (other.isAbsolute() || path.isEmpty()) other
    else if (other.toString.isEmpty()) this
    else new UnixPath(fs, path + "/" + other.toString())

  override def resolve(other: String): Path =
    resolve(new UnixPath(fs, other))

  override def resolveSibling(other: Path): Path = {
    val parent = getParent()
    if (parent == null) other
    else parent.resolve(other)
  }

  override def resolveSibling(other: String): Path =
    resolveSibling(new UnixPath(fs, other))

  override def relativize(other: Path): Path = {
    if (isAbsolute() ^ other.isAbsolute()) {
      throw new IllegalArgumentException("'other' is different type of Path")
    } else {
      val normThis = new UnixPath(fs, UnixPath.normalized(this))
      if (normThis.toString.isEmpty()) {
        other
      } else if (other.startsWith(normThis)) {
        other.subpath(getNameCount(), other.getNameCount())
      } else if (normThis.getParent() == null) {
        new UnixPath(fs, "../" + other.toString())
      } else {
        val next = normThis.getParent().relativize(other).toString()
        if (next.isEmpty()) new UnixPath(fs, "..")
        else new UnixPath(fs, "../" + next)
      }
    }
  }

  override def toAbsolutePath(): Path = absPath

  override def toRealPath(options: Array[LinkOption]): Path = {
    if (options.contains(LinkOption.NOFOLLOW_LINKS)) toAbsolutePath()
    else {
      new UnixPath(fs, toFile().getCanonicalPath()) match {
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

  override def toFile(): File = file

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
      case other: UnixPath =>
        this.fs == other.fs && this.path == other.path
      case _ => false
    }

  override def hashCode(): Int =
    path.##

  override def toString(): String =
    path

}

private object UnixPath {
  def normalized(path: UnixPath): String = {
    if (path.path.length < 2) return path.path
    val absolute = path.path.startsWith("/")
    val components =
      (0 until path.offsets.size - 1)
        .map(path.getNameString)
        .foldLeft(List.empty[String]) {
          case (acc, "..") =>
            if (acc.isEmpty && absolute) Nil
            else if (acc.isEmpty) List("..")
            else acc.tail
          case (acc, ".") => acc
          case (acc, "")  => acc
          case (acc, seg) => seg :: acc
        }
        .reverse
    if (absolute) components.mkString("/", "/", "")
    else components.mkString("", "/", "")
  }

  def removeRedundantSlashes(str: String): String =
    if (str.length < 2) str
    else {
      str.indexOf("//") match {
        case -1 =>
          if (str.endsWith("/")) str.substring(0, str.length - 1)
          else str // length > 1
        case idx =>
          val buffer: StringBuffer = new StringBuffer(str)
          var previous = '/'
          var i = idx + 1
          while (i < buffer.length()) {
            val current = buffer.charAt(i)
            if (previous == '/' && current == '/') {
              buffer.deleteCharAt(i)
            } else {
              previous = current
              i += 1
            }
          }
          val result = buffer.toString
          if (result.length > 1 && result.endsWith("/"))
            result.substring(0, result.length - 1)
          else result
      }
    }

}
