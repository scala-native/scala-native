package java.io

import scalanative.native._, stdlib._, stdio._, string._

class FileInputStream private () extends InputStream with Closeable {

  var fd: FileDescriptor = null

  private var fileSystem: IFileSystem = OSFileSystem

  @throws(classOf[FileNotFoundException])
  def this(file: File) = {
    this()
    var filePath: String = if (null == file) null else file.getPath()
    if (file == null) { // luni.4D=Argument must not be null
      throw new NullPointerException("Argument must not be null")
    }

    fd = new FileDescriptor()
    fd.readOnly = true
    fd.descriptor = fileSystem.open(file.setProperPath(), IFileSystem.O_RDONLY)
  }

  def this(fd: FileDescriptor) = {
    this()
    if (fd == null) {
      throw new NullPointerException()
    }
    this.fd = fd
  }

  @throws(classOf[FileNotFoundException])
  def this(fileName: String) = {
    this(if (null == fileName) null else new File(fileName))
  }

  @throws(classOf[IOException])
  override def read(): Int = {
    var readed: Array[Byte] = new Array[Byte](1)
    val result: Int         = read(readed, 0, 1)
    return if (result == -1) -1 else readed(0) & 0xff
  }

  @throws(classOf[IOException])
  override def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if (count > buffer.length - offset || count < 0 || offset < 0) {
      throw new IndexOutOfBoundsException()
    }
    if (0 == count) {
      return 0
    }
    openCheck()
    synchronized {
      // stdin requires special handling
      if (fd == FileDescriptor.in) {
        return fileSystem.ttyRead(buffer, offset, count).toInt
      }
      return fileSystem.read(fd.descriptor, buffer, offset, count).toInt
    }
  }

  @throws(classOf[IOException])
  override def read(buffer: Array[Byte]): Int = read(buffer, 0, buffer.length)

  @throws(classOf[IOException])
  override def available(): Int = {
    openCheck()
    synchronized {
      // stdin requires special handling
      if (fd == FileDescriptor.in) {
        return fileSystem.ttyAvailable().toInt
      }
      return fileSystem.available(fd.descriptor).toInt
    }
  }

  @throws(classOf[IOException])
  override def close(): Unit = {
    if (fd == null) {
      // if fd is null, then the underlying file is not opened, so nothing
      // to close
      return
    }

    synchronized {
      if (fd.descriptor >= 0) {
        fileSystem.close(fd.descriptor)
        fd.descriptor = -1
      }
    }
  }

  @throws(classOf[IOException])
  override protected def finalize() = close()

  @throws(classOf[IOException])
  def getFD(): FileDescriptor = fd

  @throws(classOf[IOException])
  override def skip(count: Long): Long = {
    openCheck()

    if (count == 0) {
      return 0
    }
    if (count < 0) {
      // error message : luni.AC
      throw new IOException("Number of bytes to skip cannot be negative")
    }

    // stdin requires special handling
    if (fd == FileDescriptor.in) {
      // Read and discard count bytes in 8k chunks
      var skipped: Long = 0
      var numRead: Long = 0
      var chunk: Int    = if (count < 8192) count.toInt else 8192
      var buffer        = new Array[Byte](chunk)
      var i: Long       = (count / chunk).toLong
      //in harmony it was i >= 0, however it is boggus, don't ask my why.
      while (i > 0) {
        numRead = fileSystem.ttyRead(buffer, 0, chunk)
        skipped += numRead
        if (numRead < chunk) {
          return skipped
        }
        i -= 1
      }
      return skipped
    }

    synchronized {
      val currentPosition: Long =
        fileSystem.seek(fd.descriptor, 0L, IFileSystem.SEEK_CUR)
      val newPosition: Long = fileSystem
        .seek(fd.descriptor, currentPosition + count, IFileSystem.SEEK_SET)
      return newPosition - currentPosition
    }
  }

  @throws(classOf[IOException])
  private def openCheck(): Unit = synchronized {
    if (fd.descriptor < 0) {
      throw new IOException()
    }
  }
}
