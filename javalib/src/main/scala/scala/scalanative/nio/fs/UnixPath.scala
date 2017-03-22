package scala.scalanative.nio.fs

import java.io.File
import java.net.URI
import java.nio.file.{FileSystem, LinkOption, Path}
import java.util.Iterator

import scala.collection.mutable.UnrolledBuffer

class UnixPath(private val fs: UnixFileSystem, private val rawPath: String)
    extends Path {
  import UnixPath._

  lazy val path: String = normalized(rawPath)

  override def getFileSystem(): FileSystem =
    fs

  override def isAbsolute(): Boolean =
    rawPath.startsWith("/")

  override def getRoot(): Path =
    if (isAbsolute) new UnixPath(fs, "/")
    else null

  override def getFileName(): Path =
    if (path.nonEmpty) new UnixPath(fs, split(path, '/').last)
    else null

  override def getParent(): Path = {
    val nameCount = getNameCount()
    if (nameCount == 0 || (nameCount == 1 && !isAbsolute)) null
    else if (isAbsolute) new UnixPath(fs, "/" + subpath(0, nameCount - 1).toString)
    else subpath(0, nameCount - 1)
  }

  override def getNameCount(): Int =
    if (rawPath.isEmpty) 1
    else split(path, '/').filter(_.nonEmpty).length

  override def getName(index: Int): Path = {
    val nameCount = getNameCount
    if (index < 0 || nameCount == 0 || index >= nameCount)
      throw new IllegalArgumentException
    else {
      if (rawPath.isEmpty) this
      else new UnixPath(fs, split(path, '/').filter(_.nonEmpty)(index))
    }
  }

  override def subpath(beginIndex: Int, endIndex: Int): Path =
    new UnixPath(fs, (beginIndex until endIndex).map(getName).mkString("/"))

  override def startsWith(other: Path): Boolean =
    if (!isAbsolute()) this.toAbsolutePath.startsWith(other)
    else if (fs.provider == other.getFileSystem.provider) {
      val otherLength = other.getNameCount()
      if (otherLength > getNameCount()) false
      else {
        (0 until otherLength).forall(i => getName(i) == other.getName(i))
      }
    } else {
      false
    }

  override def startsWith(other: String): Boolean =
    startsWith(new UnixPath(fs, other))

  override def endsWith(other: Path): Boolean =
    if (fs.provider == other.getFileSystem.provider) {
      val otherLength = other.getNameCount()
      val thisLength  = getNameCount()
      if (otherLength > thisLength) false
      else if (!other.isAbsolute) {
        (0 until otherLength).forall(i =>
          getName(thisLength - 1 - i) == other.getName(otherLength - 1 - i))
      } else if (isAbsolute) {
        this == other
      } else {
        false
      }
    } else {
      false
    }

  override def endsWith(other: String): Boolean =
    endsWith(new UnixPath(fs, other))

  override def normalize(): Path =
    new UnixPath(fs, path)

  override def resolve(other: Path): Path =
    if (other.isAbsolute) other
    else if (other.getNameCount == 0) this
    else new UnixPath(fs, rawPath + "/" + other.toFile().getPath())

  override def resolve(other: String): Path =
    resolve(new UnixPath(fs, other))

  override def resolveSibling(other: Path): Path =
    resolve(other.getParent)

  override def resolveSibling(other: String): Path =
    resolveSibling(new UnixPath(fs, other))

  override def relativize(other: Path): Path = {
    if (other.startsWith(this)) {
      other.subpath(getNameCount, other.getNameCount)
    } else {
      new UnixPath(fs,
                   "../" + getParent().relativize(other).toFile().getPath())
    }
  }

  override def toAbsolutePath(): Path =
    new UnixPath(fs, toFile().getAbsolutePath())

  override def toRealPath(options: LinkOption*): Path = {
    if (options.contains(LinkOption.NOFOLLOW_LINKS)) toAbsolutePath()
    else new UnixPath(fs, toFile().getCanonicalPath())
  }

  override def toFile(): File =
    if (isAbsolute) new File(rawPath)
    else new File(s"${fs.defaultDirectory}/$rawPath")

  override def toUri(): URI =
    new URI(scheme = "file",
            userInfo = null,
            host = null,
            port = -1,
            path = toFile().getAbsolutePath(),
            query = null,
            fragment = null)

  override def iterator(): Iterator[Path] =
    new Iterator[Path] {
      private var i: Int              = 0
      override def remove(): Unit     = throw new UnsupportedOperationException()
      override def hasNext(): Boolean = i < getNameCount()
      override def next(): Path =
        if (hasNext) {
          val name = getName(i)
          i += 1
          name
        } else {
          throw new NoSuchElementException()
        }
    }

  override def compareTo(other: Path): Int =
    if (fs.provider == other.getFileSystem.provider) {
      this.toString.compareTo(other.toString)
    } else {
      throw new ClassCastException()
    }

  override def equals(obj: Any): Boolean =
    obj match {
      case other: UnixPath =>
        this.fs == other.fs && this.path == other.path
      case _ => false
    }

  override def toString(): String =
    rawPath

}

private object UnixPath {
  def normalized(path: String): String = {
    split(path, '/')
      .foldLeft(List.empty[String]) {
        case (acc, "..") => if (acc.isEmpty) List("..") else acc.tail
        case (acc, ".")  => acc
        case (acc, "")   => acc
        case (acc, seg)  => seg :: acc
      }
      .reverse
      .filterNot(_.isEmpty)
      .mkString("/", "/", "")
  }

  // TODO: Remove once `String.split` is supported.
  def split(str: String, atChar: Char): Seq[String] = {
    val buffer = UnrolledBuffer.empty[String]
    var i      = 0
    while (i < str.length) {
      val part = str.drop(i).takeWhile(_ != atChar)
      buffer += part
      i += part.length + 1
    }
    buffer
  }

}
