package java.io

class FileReader(fd: FileDescriptor)
    extends InputStreamReader(new FileInputStream(fd)) {

  def this(file: File) = this(FileDescriptor.openReadOnly(file))
  def this(fileName: String) = this(new File(fileName))

}
