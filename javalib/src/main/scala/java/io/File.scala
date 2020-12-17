package java.io

import java.nio.file.{FileSystems, Path}
import java.net.URI

import scala.annotation.tailrec
import scalanative.annotation.stub
import scalanative.posix.{fcntl, limits, unistd, utime}
import scalanative.posix.sys.stat
import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._, stdlib._, stdio._, string._
import scalanative.nio.fs.FileHelpers
import scalanative.runtime.{DeleteOnExit, Platform}
import unistd._

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
    this(uri.getPath())
    checkURI(uri)
  }

  def compareTo(file: File): Int = {
    if (caseSensitive) getPath().compareTo(file.getPath())
    else getPath().compareToIgnoreCase(file.getPath())
  }

  def canExecute(): Boolean =
    Zone { implicit z => access(toCString(path), unistd.X_OK) == 0 }

  def canRead(): Boolean =
    Zone { implicit z => access(toCString(path), unistd.R_OK) == 0 }

  def canWrite(): Boolean =
    Zone { implicit z => access(toCString(path), unistd.W_OK) == 0 }

  def setExecutable(executable: Boolean): Boolean =
    setExecutable(executable, ownerOnly = true)

  def setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean = {
    import stat._
    val mask = if (!ownerOnly) S_IXUSR | S_IXGRP | S_IXOTH else S_IXUSR
    updatePermissions(mask, executable)
  }

  def setReadable(readable: Boolean): Boolean =
    setReadable(readable, ownerOnly = true)

  def setReadable(readable: Boolean, ownerOnly: Boolean): Boolean = {
    import stat._
    val mask = if (!ownerOnly) S_IRUSR | S_IRGRP | S_IROTH else S_IRUSR
    updatePermissions(mask, readable)
  }

  def setWritable(writable: Boolean): Boolean =
    setWritable(writable, ownerOnly = true)

  def setWritable(writable: Boolean, ownerOnly: Boolean = true): Boolean = {
    import stat._
    val mask = if (!ownerOnly) S_IWUSR | S_IWGRP | S_IWOTH else S_IWUSR
    updatePermissions(mask, writable)
  }

  private def updatePermissions(mask: stat.mode_t, grant: Boolean): Boolean =
    Zone { implicit z =>
      if (grant) {
        stat.chmod(toCString(path), accessMode() | mask) == 0
      } else {
        stat.chmod(toCString(path), accessMode() & (~mask)) == 0
      }
    }

  def exists(): Boolean =
    Zone { implicit z => access(toCString(path), unistd.F_OK) == 0 }

  def toPath(): Path =
    FileSystems.getDefault().getPath(this.getPath(), Array.empty)

  def getPath(): String = path

  def delete(): Boolean =
    if (path.nonEmpty && isDirectory()) {
      deleteDirImpl()
    } else {
      deleteFileImpl()
    }

  private def deleteDirImpl(): Boolean =
    Zone { implicit z => remove(toCString(path)) == 0 }

  private def deleteFileImpl(): Boolean =
    Zone { implicit z => unlink(toCString(path)) == 0 }

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

  def getCanonicalPath(): String =
    Zone { implicit z =>
      if (exists()) {
        fromCString(simplifyExistingPath(toCString(properPath)))
      } else {
        simplifyNonExistingPath(fromCString(resolve(toCString(properPath))))
      }
    }

  /**
   * Finds the canonical path for `path`, using `realpath`.
   * The file must exist, because the result of `realpath` doesn't
   * match that of Java on non-existing file.
   */
  private def simplifyExistingPath(path: CString)(implicit z: Zone): CString = {
    val resolvedName = alloc[Byte](limits.PATH_MAX.toUInt)
    realpath(path, resolvedName)
    resolvedName
  }

  /**
   * Finds the canonical path for `path`.
   */
  private def simplifyNonExistingPath(path: String): String =
    path
      .split(separatorChar)
      .foldLeft(List.empty[String]) {
        case (acc, "..") => if (acc.isEmpty) List("..") else acc.tail
        case (acc, ".")  => acc
        case (acc, "")   => acc
        case (acc, seg)  => seg :: acc
      }
      .reverse
      .filterNot(_.isEmpty())
      .mkString(separator, separator, "")

  @throws(classOf[IOException])
  def getCanonicalFile(): File = new File(getCanonicalPath())

  def getName(): String = {
    val separatorIndex: Int = path.lastIndexOf(separatorChar)
    if (separatorIndex < 0) path
    else path.substring(separatorIndex + 1, path.length())
  }

  def getParent(): String =
    path.split(separatorChar).filterNot(_.isEmpty()) match {
      case Array() if !isAbsolute()  => null
      case Array(_) if !isAbsolute() => null
      case parts if !isAbsolute()    => parts.init.mkString(separator)
      case parts if isAbsolute() =>
        parts.init.mkString(separator, separator, "")
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

  def isDirectory(): Boolean =
    Zone { implicit z => stat.S_ISDIR(accessMode()) != 0 }

  def isFile(): Boolean =
    Zone { implicit z => stat.S_ISREG(accessMode()) != 0 }

  def isHidden(): Boolean =
    getName().startsWith(".")

  def lastModified(): Long =
    Zone { implicit z =>
      val buf = alloc[stat.stat]
      if (stat.stat(toCString(path), buf) == 0) {
        buf._8 * 1000L
      } else {
        0L
      }
    }

  private def accessMode()(implicit z: Zone): stat.mode_t = {
    val buf = alloc[stat.stat]
    if (stat.stat(toCString(path), buf) == 0) {
      buf._13
    } else {
      0.toUInt
    }
  }

  def setLastModified(time: Long): Boolean =
    if (time < 0) {
      throw new IllegalArgumentException("Negative time")
    } else
      Zone { implicit z =>
        val statbuf = alloc[stat.stat]
        if (stat.stat(toCString(path), statbuf) == 0) {
          val timebuf = alloc[utime.utimbuf]
          timebuf._1 = statbuf._8
          timebuf._2 = time / 1000L
          utime.utime(toCString(path), timebuf) == 0
        } else {
          false
        }
      }

  def setReadOnly(): Boolean =
    Zone { implicit z =>
      import stat._
      val mask =
        S_ISUID | S_ISGID | S_ISVTX | S_IRUSR | S_IXUSR | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH
      val newMode = accessMode() & mask
      chmod(toCString(path), newMode) == 0
    }

  def length(): Long =
    Zone { implicit z =>
      val buf = alloc[stat.stat]
      if (stat.stat(toCString(path), buf) == 0) {
        buf._6
      } else {
        0L
      }
    }

  def list(): Array[String] =
    list(FilenameFilter.allPassFilter)

  def list(filter: FilenameFilter): Array[String] =
    if (!isDirectory() || !canRead()) {
      null
    } else
      Zone { implicit z =>
        val elements =
          FileHelpers.list(properPath, (n, _) => n, allowEmpty = true)
        if (elements == null)
          Array.empty[String]
        else
          elements.filter(filter.accept(this, _))
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

  def mkdir(): Boolean =
    Zone { implicit z =>
      val mode = octal("0777")
      stat.mkdir(toCString(path), mode) == 0
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

  def createNewFile(): Boolean =
    FileHelpers.createNewFile(path, throwOnError = true)

  def renameTo(dest: File): Boolean =
    Zone { implicit z =>
      rename(toCString(properPath), toCString(dest.properPath)) == 0
    }

  override def toString(): String = path

  def deleteOnExit(): Unit = DeleteOnExit.addFile(this.getAbsolutePath())

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

  private def getUserDir(): String =
    Zone { implicit z =>
      var buff: CString = alloc[CChar](4096.toUInt)
      var res: CString  = getcwd(buff, 4095.toUInt)
      fromCString(res)
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
        2    // Windows, but starts with C:...
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

      if (path.isEmpty()) userdir
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

  /**
   * Resolve a symbolic link. While the path resolves to an existing path,
   * keep resolving. If an absolute link is found, resolve the parent
   * directories if resolveAbsolute is true.
   */
  // Ported from Apache Harmony
  private def resolveLink(
      path: CString,
      resolveAbsolute: Boolean,
      restart: Boolean = false)(implicit z: Zone): CString = {
    val resolved =
      readLink(path) match {
        // path is not a symlink
        case null =>
          path

        // found an absolute path. continue from there.
        case link if link(0) == separatorChar =>
          resolveLink(link, resolveAbsolute, restart = resolveAbsolute)

        // found a relative path. append to the current path, and continue.
        case link =>
          val linkLength = strlen(link)
          val pathLength = strlen(path)
          val `1UL`      = 1.toULong
          var last       = pathLength - `1UL`
          while (path(last) != separatorChar) last -= `1UL`
          last += `1UL`

          // previous path up to last /, plus result of resolving the link.
          val newPathLength = last + linkLength + `1UL`
          val newPath       = alloc[Byte](newPathLength)
          strncpy(newPath, path, last)
          strncat(newPath, link, linkLength)

          resolveLink(newPath, resolveAbsolute, restart)
      }

    if (restart) resolve(resolved)
    else resolved
  }

  @tailrec private def resolve(path: CString, start: UInt = 0.toUInt)(
      implicit z: Zone): CString = {
    val part: CString = alloc[Byte](limits.PATH_MAX.toUInt)
    val `1U`          = 1.toUInt
    // Find the next separator
    var i = start
    while (i < strlen(path) && path(i) != separatorChar) i += `1U`

    if (i == strlen(path)) resolveLink(path, resolveAbsolute = true)
    else {
      // copy path from start to next separator.
      // and resolve that subpart.
      strncpy(part, path, i + `1U`)

      val resolved = resolveLink(part, resolveAbsolute = true)

      strcpy(part, resolved)
      strcat(part, path + i + `1U`)

      if (strncmp(resolved, path, i + `1U`) == 0) {
        // Nothing changed. Continue from the next segment.
        resolve(part, i + `1U`)
      } else {
        // The path has changed. Start over.
        resolve(part, 0.toUInt)
      }
    }

  }

  /**
   * If `link` is a symlink, follows it and returns the path pointed to.
   * Otherwise, returns `None`.
   */
  private def readLink(link: CString)(implicit z: Zone): CString = {
    val buffer: CString = alloc[Byte](limits.PATH_MAX.toUInt)
    readlink(link, buffer, (limits.PATH_MAX - 1).toUInt) match {
      case -1 =>
        null
      case read =>
        // readlink doesn't null-terminate the result.
        buffer(read) = 0.toByte
        buffer
    }
  }

  val pathSeparatorChar: Char        = if (Platform.isWindows()) ';' else ':'
  val pathSeparator: String          = pathSeparatorChar.toString
  val separatorChar: Char            = if (Platform.isWindows()) '\\' else '/'
  val separator: String              = separatorChar.toString
  private var counter: Int           = 0
  private var counterBase: Int       = 0
  private val caseSensitive: Boolean = !Platform.isWindows()

  def listRoots(): Array[File] =
    if (Platform.isWindows()) ???
    else {
      var array = new Array[File](1)
      array(0) = new File("/")
      return array
    }

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String): File =
    createTempFile(prefix, suffix, null)

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String, directory: File): File =
    FileHelpers.createTempFile(prefix,
                               suffix,
                               directory,
                               minLength = true,
                               throwOnError = true)

  // Ported from Apache Harmony
  private def checkURI(uri: URI): Unit = {
    def throwExc(msg: String): Unit =
      throw new IllegalArgumentException(s"$msg: $uri")
    def compMsg(comp: String): String =
      s"Found $comp component in URI"

    if (!uri.isAbsolute()) {
      throwExc("URI is not absolute")
    } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
      throwExc("URI is not hierarchical")
    } else if (uri.getScheme() == null || !(uri.getScheme() == "file")) {
      throwExc("Expected file scheme in URI")
    } else if (uri.getRawPath() == null || uri.getRawPath().length() == 0) {
      throwExc("Expected non-empty path in URI")
    } else if (uri.getRawAuthority() != null) {
      throwExc(compMsg("authority"))
    } else if (uri.getRawQuery() != null) {
      throwExc(compMsg("query"))
    } else if (uri.getRawFragment() != null) {
      throwExc(compMsg("fragment"))
    }
    // else URI is ok
  }
}
