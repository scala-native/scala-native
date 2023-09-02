package java.nio.file

class FileSystemAlreadyExistsException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(null, cause)
  def this() = this(null, null)
}
