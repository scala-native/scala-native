package scala.scalanative.nio.fs

import scalanative.unsigned._
import scalanative.libc._
import scalanative.posix.dirent._
import scalanative.posix.DirentImpl.scalanative_readdirImpl

// Import posix name errno as variable, not class or type.
import scalanative.posix.{errno => posixErrno}, posixErrno._
import scalanative.posix.stdlib
import scalanative.posix.unistd, unistd.access

import scalanative.unsafe._, stdio._
import scalanative.meta.LinktimeInfo.isWindows
import scala.collection.mutable.UnrolledBuffer
import scala.reflect.ClassTag

import java.io.{File, IOException}

import java.{lang => jl}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, PosixException}
import java.{util => ju}

import scala.scalanative.windows._
import scala.scalanative.windows.HandleApiExt.INVALID_HANDLE_VALUE
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.FileApiOps._
import scala.scalanative.windows.ErrorHandlingApi._
import scala.scalanative.windows.winnt.AccessRights._

import java.nio.file.WindowsException
import scala.scalanative.nio.fs.unix.UnixException

object FileHelpers {
  sealed trait FileType
  object FileType {
    case object Normal extends FileType
    case object Directory extends FileType
    case object Link extends FileType

    private[scalanative] def unixFileType(tpe: CInt) =
      if (tpe == DT_LNK) Link
      else if (tpe == DT_DIR) Directory
      else Normal

    private[scalanative] def windowsFileType(attributes: DWord) = {
      def isSet(attr: DWord): Boolean = {
        (attributes & attr) == attr
      }

      if (isSet(FILE_ATTRIBUTE_REPARSE_POINT)) Link
      else if (isSet(FILE_ATTRIBUTE_DIRECTORY)) Directory
      else Normal
    }
  }

  def list[T: ClassTag](
      path: String,
      f: (String, FileType) => T,
      allowEmpty: Boolean = false
  ): Array[T] = Zone.acquire { implicit z =>
    lazy val buffer = UnrolledBuffer.empty[T]

    def collectFile(name: String, fileType: FileType): Unit = {
      // java doesn't list '.' and '..', we filter them out.
      if (name != "." && name != "..") {
        buffer += f(name, fileType)
      }
    }

    def listUnix() = {
      val dir = opendir(toCString(path))

      if (dir == null) {
        if (!allowEmpty) throw UnixException(path, posixErrno.errno)
        null
      } else
        Zone.acquire { implicit z =>
          try {
            var elem = alloc[dirent]()
            var res = 0
            // Avoid deprecated non-POSIX method, use private implementation
            while ({ res = scalanative_readdirImpl(dir, elem); res == 0 }) {
              val name = fromCString(elem._2.at(0))
              val fileType = FileType.unixFileType(elem._3)
              collectFile(name, fileType)
            }

            res match {
              case e if e == EBADF || e == EFAULT || e == EIO =>
                throw UnixException(path, res)
              case _ => buffer.toArray
            }
          } finally closedir(dir)
        }
    }

    def listWindows() = Zone.acquire { implicit z =>
      val searchPath = raw"$path\*"
      if (searchPath.length.toUInt > FileApiExt.MAX_PATH)
        throw new IOException("File name to long")

      val fileData = stackalloc[Win32FindDataW]()
      val searchHandle =
        FindFirstFileW(toCWideStringUTF16LE(searchPath), fileData)
      if (searchHandle == INVALID_HANDLE_VALUE) {
        if (allowEmpty) Array.empty[T]
        else throw WindowsException.onPath(path)
      } else {
        try {
          while ({
            collectFile(
              fromCWideString(fileData.fileName, StandardCharsets.UTF_16LE),
              FileType.windowsFileType(fileData.fileAttributes)
            )
            FileApi.FindNextFileW(searchHandle, fileData)
          }) ()
        } finally {
          FileApi.FindClose(searchHandle)
        }

        GetLastError() match {
          case ErrorCodes.ERROR_NO_MORE_FILES => buffer.toArray
          case err => throw WindowsException.onPath(path)
        }
      }
    }

    if (isWindows) listWindows()
    else listUnix()
  }

  def createNewFile(path: String, throwOnError: Boolean = false): Boolean =
    if (path.isEmpty()) {
      throw new IOException("No such file or directory")
    } else if (exists(path)) {
      false
    } else
      Zone.acquire { implicit z =>
        if (isWindows) {
          val handle = CreateFileW(
            toCWideStringUTF16LE(path),
            desiredAccess = FILE_GENERIC_WRITE,
            shareMode = FILE_SHARE_ALL,
            securityAttributes = null,
            creationDisposition = CREATE_ALWAYS,
            flagsAndAttributes = FILE_ATTRIBUTE_NORMAL,
            templateFile = null
          )
          HandleApi.CloseHandle(handle)
          GetLastError() match {
            case ErrorCodes.ERROR_FILE_EXISTS => false
            case errCode                      =>
              if (handle != INVALID_HANDLE_VALUE) true
              else if (throwOnError)
                throw WindowsException(
                  s"Cannot create new file $path",
                  errorCode = errCode
                )
              else false
          }
        } else {
          fopen(toCString(path), c"w") match {
            case null =>
              if (throwOnError) throw UnixException(path, posixErrno.errno)
              else false
            case fd => fclose(fd); exists(path)
          }
        }
      }

  def createTempDirectoryUnixImpl(
      dir: Path,
      prefix: String
  ): Path = Zone.acquire { implicit z =>
    /* Use os 'mkdtemp()' to come closer to atomically determining
     * a temporary name and creating a directory with that name and
     * its specified default 700 file permissions.  This also the JVM
     * default, so everything is harmonious.
     *
     * Go thru the extra step of using genTempIdent() so that user visible
     * names are similar, but not identical, to JVM practice. The
     * detail oriented or those running scripts may notice that the
     * last (righmost) 6 characters are now alpha-numeric. JVM practice
     * is that they be strictly numeric.  The added safety of atomic action
     * is worth the slight but regretable difference. It is hard being atomic.
     */

    val maxElementLen = 255
    val reservedLen = 6 // leave room for template suffix length

    val sb = new jl.StringBuilder(maxElementLen).append(dir.toString)

    if (sb.charAt(sb.length() - 1) != '/')
      sb.append('/')

    sb.append(genTempIdent(if (prefix == null) "" else prefix, ""))

    val maxUnReservedLen = (maxElementLen - reservedLen)
    if (sb.length() > maxUnReservedLen)
      sb.setLength(maxUnReservedLen)

    sb.repeat('X', reservedLen)

    val template = sb.toString()

    val cDir = stdlib.mkdtemp(toCString(template))

    if (cDir == null) {
      throw PosixException(
        s"Create temporary directory '${template}'",
        posixErrno.errno
      )
    }

    Path.of(fromCString(cDir), Array.empty)
  }

  def createTempFile(
      prefix: String,
      suffix: String,
      dir: File,
      minLength: Boolean,
      throwOnError: Boolean = false
  ): File =
    if (prefix == null) throw new NullPointerException
    else if (minLength && prefix.length < 3)
      throw new IllegalArgumentException("Prefix string too short")
    else {
      val tmpDir = Option(dir).fold(tempDir)(_.toString)
      val newSuffix = Option(suffix).getOrElse(".tmp")
      var result: File = null
      while ({
        result = genTempFile(prefix, newSuffix, tmpDir)
        !createNewFile(result.toString, throwOnError)
      }) ()
      result
    }

  def exists(path: String): Boolean =
    Zone.acquire { implicit z =>
      if (isWindows) {
        import ErrorCodes._
        def canAccessAttributes = // fast-path
          GetFileAttributesW(
            toCWideStringUTF16LE(path)
          ) != INVALID_FILE_ATTRIBUTES
        def errorCodeIndicatesExistence = GetLastError() match {
          case ERROR_FILE_NOT_FOUND | ERROR_PATH_NOT_FOUND |
              ERROR_INVALID_NAME =>
            false
          case _ =>
            true // any other error code indicates that given path might exist
        }
        canAccessAttributes || errorCodeIndicatesExistence
      } else
        access(toCString(path), unistd.F_OK) == 0
    }

  lazy val tempDir: String = {
    val propertyName = "java.io.tmpdir"
    // set at first lookup after program start.
    val dir = System.getProperty(propertyName)
    ju.Objects.requireNonNull(
      dir,
      s"Required Java System property ${propertyName} is not defined."
    )
    dir
  }

  private lazy val random = new scala.util.Random()

  private def genTempIdent(prefix: String, suffix: String): String = {
    val id = random.nextLong() match {
      case l if l == java.lang.Long.MIN_VALUE => 0
      case l                                  => math.llabs(l)
    }

    s"${prefix}${id}${suffix}"
  }

  private def genTempFile(
      prefix: String,
      suffix: String,
      directory: String
  ): File =
    new File(directory, genTempIdent(prefix, suffix))
}
