package java.nio.file

class NoSuchFileException(file: String, other: String, reason: String)
    extends FileSystemException(file, other, reason) {
  def this(file: String) = this(file, null, null)
}
