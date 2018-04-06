package scala.scalanative.nio.fs

import scalanative.native._
import scalanative.posix.dirent._
import scalanative.posix.{errno => e}, e._
import scala.collection.mutable.UnrolledBuffer
import scala.reflect.ClassTag

object FileHelpers {
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
}
