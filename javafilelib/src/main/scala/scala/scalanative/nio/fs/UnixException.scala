package scala.scalanative.nio.fs

import scala.scalanative.unsafe._
import scala.scalanative.posix.errno._
import java.io.IOException
import java.nio.file._
import scalanative.libc.string

object UnixException {
  def apply(file: String, errno: CInt): IOException = errno match {
    case e if e == ENOTDIR => new NotDirectoryException(file)
    case e if e == EACCES  => new AccessDeniedException(file)
    case e if e == ENOENT  => new NoSuchFileException(file)
    case e if e == EEXIST  => new FileAlreadyExistsException(file)
    case e                 => new IOException(fromCString(string.strerror(e)))
  }
}
