package scala.scalanative.nio.fs

import scalanative.libc._
import scalanative.posix.dirent._
import scalanative.posix.unistd
import unistd.access
import scalanative.unsafe._
import stdlib._
import stdio._
import scalanative.meta.LinktimeInfo.isWindows
import scala.collection.mutable.UnrolledBuffer
import scala.reflect.ClassTag
import java.io.{File, IOException}
import java.nio.charset.StandardCharsets
import scala.scalanative.windows.{ErrorCodes, WChar}
import scala.scalanative.windows.ErrorHandlingApi._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._
import scala.scalanative.windows._

object FileHelpers {
  private[this] lazy val random = new scala.util.Random()
  final case class Dirent(name: String, tpe: CShort)
  def list[T: ClassTag](
      path: String,
      f: (String, CShort) => T,
      allowEmpty: Boolean = false
  ): Array[T] = Zone { implicit z =>
    val dir = opendir(toCString(path))

    if (dir == null) {
      if (!allowEmpty) throw UnixException(path, errno.errno)
      null
    } else {
      val buffer = UnrolledBuffer.empty[T]
      Zone { implicit z =>
        var elem = alloc[dirent]
        var res = 0
        while ({ res = readdir(dir, elem); res == 0 }) {
          val name = fromCString(elem._2.at(0))

          // java doesn't list '.' and '..', we filter them out.
          if (name != "." && name != "..") {
            buffer += f(name, elem._3)
          }
        }
        closedir(dir)
        if (res == -1)
          buffer.toArray
        else
          throw UnixException(path, res)
      }
    }
  }

  def createNewFile(path: String, throwOnError: Boolean = false): Boolean =
    if (path.isEmpty()) {
      throw new IOException("No such file or directory")
    } else if (exists(path)) {
      false
    } else
      Zone { implicit z =>
        if (isWindows) {
          val handle = CreateFileW(
            toCWideStringUTF16LE(path),
            desiredAccess = FILE_GENERIC_WRITE,
            shareMode = FILE_SHARE_ALL,
            securityAttributes = null,
            creationDisposition = CREATE_NEW,
            flagsAndAttributes = FILE_ATTRIBUTE_NORMAL,
            templateFile = null
          )
          HandleApi.CloseHandle(handle)
          GetLastError() match {
            case ErrorCodes.ERROR_FILE_EXISTS => false
            case _                            => handle != INVALID_HANDLE_VALUE
          }
        } else {
          fopen(toCString(path), c"w") match {
            case null =>
              if (throwOnError) throw UnixException(path, errno.errno)
              else false
            case fd => fclose(fd); exists(path)
          }
        }
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
      do {
        result = genTempFile(prefix, newSuffix, tmpDir)
      } while (!createNewFile(result.toString, throwOnError))
      result
    }

  def exists(path: String): Boolean =
    Zone { implicit z =>
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
    if (isWindows) {
      val buffer = stackalloc[WChar](MAX_PATH)
      GetTempPathW(MAX_PATH, buffer)
      fromCWideString(buffer, StandardCharsets.UTF_16LE)
    } else {
      val dir = getenv(c"TMPDIR")
      if (dir == null) {
        System.getProperty("java.io.tmpdir") match {
          case null => "/tmp"
          case d    => d
        }
      } else {
        fromCString(dir)
      }
    }
  }

  private def genTempFile(
      prefix: String,
      suffix: String,
      directory: String
  ): File = {
    val id = random.nextLong() match {
      case l if l == java.lang.Long.MIN_VALUE => 0
      case l                                  => math.labs(l)
    }
    val fileName = prefix + id + suffix
    new File(directory, fileName)
  }
}
