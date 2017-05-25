package java.io

import scala.scalanative.native.{toCString, Zone}
import scala.scalanative.posix.fcntl

class FileWriter(fd: FileDescriptor)
    extends OutputStreamWriter(new FileOutputStream(fd)) {
  def this(file: File, append: Boolean) =
    this(FileWriter.fileDescriptor(file, append))
  def this(file: File) = this(file, false)
  def this(fileName: String, append: Boolean) =
    this(new File(fileName), append)
  def this(fileName: String) = this(new File(fileName))
}

object FileWriter {
  private def fileDescriptor(file: File, append: Boolean) =
    Zone { implicit z =>
      val mode =
        if (append) fcntl.O_WRONLY | fcntl.O_APPEND else fcntl.O_WRONLY
      val fd = fcntl.open(toCString(file.getPath), mode)
      new FileDescriptor(fd)
    }
}
