package java.io

import scala.scalanative.native.{toCString, Zone}
import scala.scalanative.posix.fcntl

class FileReader(fd: FileDescriptor)
    extends InputStreamReader(new FileInputStream(fd)) {

  def this(file: File) = this(FileReader.fileDescriptor(file))
  def this(fileName: String) = this(new File(fileName))

}

object FileReader {
  private def fileDescriptor(file: File): FileDescriptor =
    Zone { implicit z =>
      val fd = fcntl.open(toCString(file.getPath), fcntl.O_RDONLY)
      new FileDescriptor(fd, true)
    }
}
