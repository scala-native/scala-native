package java.nio.channels

import java.io.IFileSystem

object FileChannelFactory {
  def getFileChannel(stream: Object, fd: Long, mode: Int) = mode match {
    //case IFileSystem.O_RDONLY => new ReadOnlyFileChannel(stream, fd);
    case IFileSystem.O_WRONLY => new WriteOnlyFileChannel(stream, fd);
    //case IFileSystem.O_RDWR => new ReadWriteFileChannel(stream, fd);
    //case IFileSystem.O_RDWRSYNC => new ReadWriteFileChannel(stream, fd);
    case IFileSystem.O_APPEND => new WriteOnlyFileChannel(stream, fd, true);
    case _                    => new RuntimeException("Unknown file channel type : " + mode)
  }
}
