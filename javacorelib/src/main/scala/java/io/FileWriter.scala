package java.io

class FileWriter private (fos: FileOutputStream)
    extends OutputStreamWriter(fos) {
  def this(file: File, append: Boolean) =
    this(new FileOutputStream(file, append))
  def this(file: File) =
    this(new FileOutputStream(file))
  def this(fileName: String, append: Boolean) =
    this(new FileOutputStream(fileName, append))
  def this(fileName: String) =
    this(new FileOutputStream(fileName))
  def this(fd: FileDescriptor) =
    this(new FileOutputStream(fd))
}
