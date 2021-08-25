package scala.scalanative.nio.fs

import java.io.IOException
import java.nio.file.PosixException

import scala.scalanative.unsafe.CInt

object UnixException {
  def apply(file: String, errno: CInt): IOException =
    PosixException(file, errno)
}
