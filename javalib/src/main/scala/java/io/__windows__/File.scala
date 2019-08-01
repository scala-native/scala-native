package java.io

import java.nio.file.{FileSystems, Path}
import java.net.URI

import scala.annotation.tailrec
import scalanative.annotation.stub
import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._, stdlib._, stdio._, string._
//import scalanative.nio.fs.FileHelpers
import scalanative.runtime.{DeleteOnExit, Platform}

class File(_path: String) extends Serializable with Comparable[File] {
  import File._

  if (_path == null) throw new NullPointerException()
  private val path: String           = fixSlashes(_path)
  private[io] val properPath: String = File.properPath(path)
  private[io] val properPathBytes: Array[Byte] =
    File.properPath(path).getBytes("UTF-8")

  def this(parent: String, child: String) =
    this(
      Option(parent).map(p => p + File.separatorChar + child).getOrElse(child))

  def this(parent: File, child: String) =
    this(Option(parent).map(_.path).orNull, child)

  def this(uri: URI) = {
    this(uri.getPath)
    checkURI(uri)
  }

  def compareTo(file: File): Int = {
    if (caseSensitive) getPath().compareTo(file.getPath())
    else getPath().compareToIgnoreCase(file.getPath())
  }

  def canExecute(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def canRead(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def canWrite(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def setExecutable(executable: Boolean): Boolean =
    setExecutable(executable, ownerOnly = true)

  def setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def setReadable(readable: Boolean): Boolean =
    setReadable(readable, ownerOnly = true)

  def setReadable(readable: Boolean, ownerOnly: Boolean): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def setWritable(writable: Boolean): Boolean =
    setWritable(writable, ownerOnly = true)

  def setWritable(writable: Boolean, ownerOnly: Boolean = true): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def exists(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def toPath(): Path =
    FileSystems.getDefault().getPath(this.getPath(), Array.empty)

  def getPath(): String = path

  def delete(): Boolean =
    if (path.nonEmpty && isDirectory()) {
      deleteDirImpl()
    } else {
      deleteFileImpl()
    }

  private def deleteDirImpl(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  private def deleteFileImpl(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  override def equals(that: Any): Boolean =
    that match {
      case that: File if caseSensitive =>
        this.path == that.path
      case that: File =>
        this.path.toLowerCase == that.path.toLowerCase
      case _ =>
        false
    }

  def getAbsolutePath(): String = properPath

  def getAbsoluteFile(): File = new File(this.getAbsolutePath())

  def getCanonicalPath(): String = {
    throw new IOException("Not implemented")
    ""
  }

  @throws(classOf[IOException])
  def getCanonicalFile(): File = new File(getCanonicalPath())

  def getName(): String = {
    val separatorIndex: Int = path.lastIndexOf(separatorChar)
    if (separatorIndex < 0) path
    else path.substring(separatorIndex + 1, path.length())
  }

  def getParent(): String =
    path.split(separatorChar).filterNot(_.isEmpty) match {
      case Array() if !isAbsolute  => null
      case Array(_) if !isAbsolute => null
      case parts if !isAbsolute    => parts.init.mkString(separator)
      case parts if isAbsolute     => parts.init.mkString(separator, separator, "")
    }

  def getParentFile(): File = {
    val parent = getParent()
    if (parent == null) null
    else new File(parent)
  }

  override def hashCode(): Int =
    if (caseSensitive) path.hashCode ^ 1234321
    else path.toLowerCase.hashCode ^ 1234321

  def isAbsolute(): Boolean =
    File.isAbsolute(path)

  def isDirectory(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def isFile(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def isHidden(): Boolean =
    getName().startsWith(".")

  def lastModified(): Long = {
    throw new IOException("Not implemented")
    0L
  }

  def setLastModified(time: Long): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def setReadOnly(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def length(): Long = {
    throw new IOException("Not implemented")
    0L
  }

  def list(): Array[String] =
    list(FilenameFilter.allPassFilter)

  def list(filter: FilenameFilter): Array[String] = {
    throw new IOException("Not implemented")
    null
  }

  def listFiles(): Array[File] =
    listFiles(FilenameFilter.allPassFilter)

  def listFiles(filter: FilenameFilter): Array[File] =
    list(filter).map(new File(this, _))

  def listFiles(filter: FileFilter): Array[File] = {
    val filenameFilter =
      new FilenameFilter {
        override def accept(dir: File, name: String): Boolean =
          filter.accept(new File(dir, name))
      }
    listFiles(filenameFilter)
  }

  def mkdir(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def mkdirs(): Boolean =
    if (exists()) {
      false
    } else if (mkdir()) {
      true
    } else {
      val parent = getParentFile()
      if (parent == null) {
        false
      } else {
        parent.mkdirs() && mkdir()
      }
    }

  def createNewFile(): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  def renameTo(dest: File): Boolean = {
    throw new IOException("Not implemented")
    false
  }

  override def toString(): String = path

  def deleteOnExit(): Unit = DeleteOnExit.addFile(this.getAbsolutePath)

  @stub
  def toURL(): java.net.URL = ???

  // Ported from Apache Harmony
  def toURI(): URI = {
    val path = getAbsolutePath()
    if (!path.startsWith("/")) {
      // start with sep.
      new URI(
        "file",
        null,
        new StringBuilder(path.length + 1).append('/').append(path).toString,
        null,
        null)
    } else if (path.startsWith("//")) {
      // UNC path
      new URI("file", "", path, null)
    } else {
      new URI("file", null, path, null, null)
    }
  }
}

object File {

  private val random = new java.util.Random()

  private def octal(v: String): UInt =
    Integer.parseInt(v, 8).toUInt

  private def getUserDir(): String = {
    throw new IOException("Not implemented")
    ""
  }

  /** The purpose of this method is to take a path and fix the slashes up. This
   *  includes changing them all to the current platforms fileSeparator and
   *  removing duplicates.
   */
  // Ported from Apache Harmony
  private def fixSlashes(path: String): String = {
    val length    = path.length
    var newLength = 0

    var uncIndex =
      if (separatorChar == '/') 0 // UNIX world
      else if (length > 2 && path.charAt(1) == ':')
        2 // Windows, but starts with C:...
      else 1 // Possible UNC path name

    var foundSlash = false
    val newPath    = path.toCharArray()
    var i          = 0
    while (i < length) {
      val currentChar = newPath(i)

      if ((separatorChar == '\\' && currentChar == '\\') || currentChar == '/') {
        // UNC Name requires 2 leading slashes
        if ((foundSlash && i == uncIndex) || !foundSlash) {
          newPath(newLength) = separatorChar
          newLength += 1
          foundSlash = true
        }
      } else {
        // check for leading slashes before a drive
        if (currentChar == ':'
            && uncIndex > 0
            && (newLength == 2 || (newLength == 3 && newPath(1) == separatorChar))
            && newPath(0) == separatorChar) {
          newPath(0) = newPath(newLength - 1)
          newLength = 1
          // allow trailing slash after drive letter
          uncIndex = 2
        }
        newPath(newLength) = currentChar
        newLength += 1
        foundSlash = false
      }

      i += 1
    }

    if (foundSlash && (newLength > (uncIndex + 1) || (newLength == 2 && newPath(
          0) != separatorChar))) {
      newLength -= 1
    }

    new String(newPath, 0, newLength)
  }

  /**
   * Returns a string representing the proper path of this file. If this file
   * path is absolute, the user.dir property is not prepended, otherwise it
   * is.
   */
  // Ported from Apache Harmony
  private def properPath(path: String): String = {
    if (isAbsolute(path)) path
    else {
      val userdir =
        Option(getUserDir())
          .getOrElse(
            throw new IOException(
              "getcwd() error in trying to get user directory."))

      if (path.isEmpty) userdir
      else if (userdir.endsWith(separator)) userdir + path
      else userdir + separator + path
    }
  }

  def isAbsolute(path: String): Boolean =
    if (separatorChar == '\\') { // Windows. Must start with `\\` or `X:(\|/)`
      (path.length > 1 && path.startsWith(separator + separator)) ||
      (path.length > 2 && path(0).isLetter && path(1) == ':' && (path(2) == '/' || path(
        2) == '\\'))
    } else {
      path.length > 0 && path.startsWith(separator)
    }

  val pathSeparatorChar: Char        = if (Platform.isWindows) ';' else ':'
  val pathSeparator: String          = pathSeparatorChar.toString
  val separatorChar: Char            = if (Platform.isWindows) '\\' else '/'
  val separator: String              = separatorChar.toString
  private var counter: Int           = 0
  private var counterBase: Int       = 0
  private val caseSensitive: Boolean = !Platform.isWindows

  def listRoots(): Array[File] =
    if (Platform.isWindows) ???
    else {
      var array = new Array[File](1)
      array(0) = new File("/")
      return array
    }

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String): File =
    createTempFile(prefix, suffix, null)

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String, directory: File): File = {
    throw new IOException("Not implemented")
    null
  }

  // Ported from Apache Harmony
  private def checkURI(uri: URI): Unit = {
    def throwExc(msg: String): Unit =
      throw new IllegalArgumentException(s"$msg: $uri")
    def compMsg(comp: String): String =
      s"Found $comp component in URI"

    if (!uri.isAbsolute) {
      throwExc("URI is not absolute")
    } else if (!uri.getRawSchemeSpecificPart.startsWith("/")) {
      throwExc("URI is not hierarchical")
    } else if (uri.getScheme == null || !(uri.getScheme == "file")) {
      throwExc("Expected file scheme in URI")
    } else if (uri.getRawPath == null || uri.getRawPath.length == 0) {
      throwExc("Expected non-empty path in URI")
    } else if (uri.getRawAuthority != null) {
      throwExc(compMsg("authority"))
    } else if (uri.getRawQuery != null) {
      throwExc(compMsg("query"))
    } else if (uri.getRawFragment != null) {
      throwExc(compMsg("fragment"))
    }
    // else URI is ok
  }
}
