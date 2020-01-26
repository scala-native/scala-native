package java.nio.file

class FileSystemNotFoundException(msg: String) extends RuntimeException(msg) {
  def this() = this(null)
}
