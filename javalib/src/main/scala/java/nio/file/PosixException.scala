package java.nio.file

import java.io.IOException

import scalanative.libc.string
import scalanative.posix.errno._
import scalanative.unsafe.{CInt, fromCString}

object PosixException {
  def apply(file: String, errno: CInt): IOException = errno match {
    case e if e == ENOTDIR => new NotDirectoryException(file)
    case e if e == EACCES  => new AccessDeniedException(file)
    case e if e == ENOENT  => new NoSuchFileException(file)
    case e if e == EEXIST  => new FileAlreadyExistsException(file)
    case e                 => new IOException(fromCString(string.strerror(e)))
  }
}
