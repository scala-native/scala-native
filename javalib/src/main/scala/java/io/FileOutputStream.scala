package java.io

class FileOutputStream(fd: FileDescriptor)
    extends OutputStream
    with Closeable {

  private val fileSystem: IFileSystem = OSFileSystem

  @throws(classOf[FileNotFoundException])
  def this(file: File) = {
    this(new FileDescriptor())
    fd.descriptor = fileSystem.open(file.setProperPath(), IFileSystem.O_WRONLY)
  }

  @throws(classOf[FileNotFoundException])
  def this(file: File, append: Boolean) = {
    this(new FileDescriptor())
    fd.descriptor = fileSystem.open(
      file.setProperPath(),
      if (append) IFileSystem.O_APPEND else IFileSystem.O_WRONLY)
  }

  @throws(classOf[FileNotFoundException])
  def this(filename: String) = {
    this(new FileDescriptor)
    fd.descriptor =
      fileSystem.open(new File(filename).setProperPath(), IFileSystem.O_WRONLY)
  }

  @throws(classOf[FileNotFoundException])
  def this(filename: String, append: Boolean) = {
    this(new FileDescriptor())
    fd.descriptor = fileSystem.open(
      new File(filename).setProperPath(),
      if (append) IFileSystem.O_APPEND else IFileSystem.O_WRONLY)
  }

  @throws(classOf[IOException])
  override def close(): Unit = if (fd != null) {
    // if fd is null, then the underlying file is not opened, so nothing
    // to close

    synchronized {
      if (fd.descriptor >= 0) {
        fileSystem.close(fd.descriptor)
        fd.descriptor = -1
      }
    }
  }

  override protected def finalize(): Unit = {
    close()
  }

  @throws(classOf[IOException])
  final def getFD(): FileDescriptor = fd

  @throws(classOf[IOException])
  override def write(buffer: Array[Byte]) = write(buffer, 0, buffer.length)

  @throws(classOf[IOException])
  override def write(buffer: Array[Byte], offset: Int, count: Int) = {
    if (buffer == null) {
      throw new NullPointerException()
    }
    if (count < 0 || offset < 0 || offset > buffer.length
        || count > buffer.length - offset) {
      throw new IndexOutOfBoundsException()
    }

    if (count != 0) {
      openCheck()
      fileSystem.write(fd.descriptor, buffer, offset, count)
    }
  }

  @throws(classOf[IOException])
  override def write(oneByte: Int): Unit = {
    openCheck()
    val byteArray: Array[Byte] = new Array[Byte](1)
    byteArray(0) = oneByte.toByte
    fileSystem.write(fd.descriptor, byteArray, 0, 1);
  }

  @throws(classOf[IOException])
  private def openCheck() = synchronized {
    if (fd.descriptor < 0) {
      throw new IOException()
    }
  }
}
