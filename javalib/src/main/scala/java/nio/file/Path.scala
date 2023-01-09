package java.nio.file

import java.lang.Iterable
import java.util.Iterator
import java.io.File
import java.net.URI

trait Path extends Comparable[Path] with Iterable[Path] with Watchable {

  def compareTo(other: Path): Int
  def endsWith(other: Path): Boolean
  def endsWith(other: String): Boolean
  def equals(other: Any): Boolean
  def getFileName(): Path
  def getFileSystem(): FileSystem
  def getName(index: Int): Path
  def getNameCount(): Int
  def getParent(): Path
  def getRoot(): Path
  def hashCode(): Int
  def isAbsolute(): Boolean
  def iterator(): Iterator[Path]
  def normalize(): Path
  def relativize(other: Path): Path
  def resolve(other: Path): Path
  def resolve(other: String): Path
  def resolveSibling(other: Path): Path
  def resolveSibling(other: String): Path
  def startsWith(other: Path): Boolean
  def startsWith(other: String): Boolean
  def subpath(beginIndex: Int, endIndex: Int): Path
  def toAbsolutePath(): Path
  def toFile(): File
  def toRealPath(options: Array[LinkOption]): Path
  def toString(): String
  def toUri(): URI
}

// https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Paths.html
// It is recommended to obtain a Path via the Path.of methods instead of via the get methods
// defined in this class(=java.nio.file.Paths) as this class may be deprecated in a future release.
object Path {
  private lazy val fs = FileSystems.getDefault()
  def of(path: String, paths: Array[String]): Path = fs.getPath(path, paths)
  def of(uri: URI): Path = if (uri.getScheme() == null) {
    throw new IllegalArgumentException("Missing scheme")
  } else if (uri.getScheme().toLowerCase == "file") {
    fs.getPath(uri.getPath(), Array.empty)
  } else {
    throw new FileSystemNotFoundException(
      s"Provider ${uri.getScheme()} is not installed."
    )
  }
}
