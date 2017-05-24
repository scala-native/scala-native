package scala.scalanative.nio.fs

import java.io.File
import java.net.URI
import java.nio.file.{FileSystem, LinkOption, Path, WatchEvent, WatchKey}
import java.util.Iterator

import scala.collection.mutable.UnrolledBuffer

class UnixPath(private val fs: UnixFileSystem, private val rawPath: String)
    extends Path {
  import UnixPath._

  lazy val path: String = removeRedundantSlashes(rawPath)

  override def getFileSystem(): FileSystem =
    fs

  override def isAbsolute(): Boolean =
    rawPath.startsWith("/")

  override def getRoot(): Path =
    if (isAbsolute) new UnixPath(fs, "/")
    else null

  override def getFileName(): Path =
    if (path == "/") null
    else if (path.isEmpty) this
    else new UnixPath(fs, path.split("/").last)

  override def getParent(): Path = {
    val nameCount = getNameCount()
    if (nameCount == 0 || (nameCount == 1 && !isAbsolute)) null
    else if (isAbsolute)
      new UnixPath(fs, "/" + subpath(0, nameCount - 1).toString)
    else subpath(0, nameCount - 1)
  }

  override def getNameCount(): Int =
    if (rawPath.isEmpty) 1
    else path.split("/").filter(_.nonEmpty).length

  override def getName(index: Int): Path = {
    val nameCount = getNameCount
    if (index < 0 || nameCount == 0 || index >= nameCount)
      throw new IllegalArgumentException
    else {
      if (rawPath.isEmpty) this
      else new UnixPath(fs, path.split("/").filter(_.nonEmpty)(index))
    }
  }

  override def subpath(beginIndex: Int, endIndex: Int): Path =
    new UnixPath(fs, (beginIndex until endIndex).map(getName).mkString("/"))

  override def startsWith(other: Path): Boolean =
    if (fs.provider == other.getFileSystem.provider) {
      val otherLength = other.getNameCount()
      val thisLength  = getNameCount()

      if (otherLength > thisLength) false
      else if (isAbsolute ^ other.isAbsolute) false
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
    new UnixPath(fs, normalized(path))

  override def resolve(other: Path): Path =
    if (other.isAbsolute || path.isEmpty) other
    else if (other.toString.isEmpty) this
    else new UnixPath(fs, rawPath + "/" + other.toString())

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
    if (isAbsolute ^ other.isAbsolute) {
      throw new IllegalArgumentException("'other' is different type of Path")
    } else if (path.isEmpty) {
      other
    } else if (other.startsWith(this)) {
      other.subpath(getNameCount, other.getNameCount)
    } else if (getParent() == null) {
      new UnixPath(fs, "../" + other.toString())
    } else {
      val next = getParent().relativize(other).toString()
      if (next.isEmpty) new UnixPath(fs, "..")
      else new UnixPath(fs, "../" + next)
    }
  }

  override def toAbsolutePath(): Path =
    new UnixPath(fs, toFile().getAbsolutePath())

  override def toRealPath(options: Array[LinkOption]): Path = {
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
        this.fs == other.fs && this.rawPath == other.rawPath
      case _ => false
    }

  override def hashCode(): Int =
    rawPath.##

  override def toString(): String =
    rawPath

}

private object UnixPath {
  def normalized(path: String): String = {
    val absolute = path.startsWith("/")
    val components =
      path
        .split("/")
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
      val buffer   = new StringBuffer(str)
      var previous = buffer.charAt(0)
      var i        = 1
      while (i < buffer.length) {
        val current = buffer.charAt(i)
        if (previous == '/' && current == '/') {
          buffer.deleteCharAt(i)
        } else {
          previous = current
          i += 1
        }
      }
      val result = buffer.toString
      if (result.length > 1 && result.endsWith("/")) result.init
      else result
    }

}
