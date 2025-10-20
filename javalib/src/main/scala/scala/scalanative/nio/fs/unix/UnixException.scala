package scala.scalanative.nio.fs.unix

import java.io.IOException
import java.nio.file.PosixException

import scala.scalanative.unsafe.*

object UnixException {
  def apply(file: String, errno: CInt): IOException =
    PosixException(file, errno)
}
