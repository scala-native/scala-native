package scala.scalanative.nio.fs

import scalanative.native._
import scalanative.libc._
import scalanative.posix.dirent._
import scalanative.posix.{errno => e, fcntl, unistd}, e._, unistd.access
import scalanative.native._, stdlib._, stdio._, string._
import scala.collection.mutable.UnrolledBuffer
import scala.reflect.ClassTag
import java.io.{File, IOException}

object FileHelpers {
  private[this] lazy val random = new scala.util.Random()
  final case class Dirent(name: String, tpe: CShort)
  def list[T: ClassTag](path: String,
                        f: (String, CShort) => T,
                        allowEmpty: Boolean = false): Array[T] = Zone {
    implicit z =>
      val dir = opendir(toCString(path))

      if (dir == null) {
        if (!allowEmpty) throw UnixException(path, errno.errno)
        null
      } else {
        val buffer = UnrolledBuffer.empty[T]
        Zone { implicit z =>
          var elem = alloc[dirent]
          var res  = 0
          while ({ res = readdir(dir, elem); res == 0 }) {
            val name = fromCString(elem._2.asInstanceOf[CString])

            // java doesn't list '.' and '..', we filter them out.
            if (name != "." && name != "..") {
              buffer += f(name, !elem._3)
            }
          }
          closedir(dir)
          res match {
            case e if e == EBADF || e == EFAULT || e == EIO =>
              throw UnixException(path, res)
            case _ => buffer.toArray
          }
        }
      }
  }

  def createNewFile(path: String, throwOnError: Boolean = false): Boolean =
    if (path.isEmpty) {
      throw new IOException("No such file or directory")
    } else if (exists(path)) {
      false
    } else
      Zone { implicit z =>
        fopen(toCString(path), c"w") match {
          case null =>
            if (throwOnError) throw UnixException(path, errno.errno)
            else false
          case fd => fclose(fd); exists(path)
        }
      }

  def createTempFile(prefix: String,
                     suffix: String,
                     dir: File,
                     minLength: Boolean,
                     throwOnError: Boolean = false): File =
    if (prefix == null) throw new NullPointerException
    else if (minLength && prefix.length < 3)
      throw new IllegalArgumentException("Prefix string too short")
    else {
      val tmpDir       = Option(dir).fold(tempDir)(_.toString)
      val newSuffix    = Option(suffix).getOrElse(".tmp")
      var result: File = null
      do {
        result = genTempFile(prefix, newSuffix, tmpDir)
      } while (!createNewFile(result.toString, throwOnError))
      result
    }

  def exists(path: String): Boolean =
    Zone { implicit z =>
      access(toCString(path), fcntl.F_OK) == 0
    }
  private def tempDir(): String = {
    val dir = getenv(c"TMPDIR")
    if (dir == null) {
      System.getProperty("java.io.tmpdir") match {
        case null => "/tmp"
        case d    => d
      }
    } else fromCString(dir)
  }

  private def genTempFile(prefix: String,
                          suffix: String,
                          directory: String): File = {
    val id = random.nextLong() match {
      case l if l == java.lang.Long.MIN_VALUE => 0
      case l                                  => math.labs(l)
    }
    val fileName = prefix + id + suffix
    new File(directory, fileName)
  }
}
