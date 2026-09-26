package java.io

class FileReader private[java] (fd: FileDescriptor, file: Option[File])
    extends InputStreamReader(new FileInputStream(fd, file)) {

  def this(fd: FileDescriptor) = this(fd, None)
  def this(file: File) = this(FileDescriptor.openReadOnly(file), Some(file))
  def this(fileName: String) = this(new File(fileName))

}
