package java.io

import scalanative.native._, stdlib._, stdio._, string._
import scalanative.posix._

object OSFileSystem extends IFileSystem {

  //438 is the decimal conversion of the octal number 0666, which represents a rw access for
  //all (user, group or owner)
  val RDWRACC = 438

  @throws(classOf[FileNotFoundException])
  def open(fileName: Array[Byte], mode: Int): Long = {
    if (fileName == null) {
      throw new NullPointerException()
    }
    val handler: Long = openImpl(fileName, mode)
    if (handler < 0) {
      try {
        throw new FileNotFoundException(new String(fileName, "UTF-8"));
      } catch {
        case e: java.io.UnsupportedEncodingException =>
          // UTF-8 should always be supported, so throw an assertion
          e.initCause(new FileNotFoundException(new String(fileName)))
          throw new AssertionError(e);
      }
    }
    return handler
  }

  private def openImpl(fileName: Array[Byte], mode: Int): Long = {
    var flags: Int        = 0
    var pathCopy: CString = File.filePathCopy(fileName)
    var accMode: Int = mode match {
      case IFileSystem.O_RDONLY =>
        flags = CFileFlags.openRead
        0
      case IFileSystem.O_WRONLY =>
        flags = CFileFlags.openCreate | CFileFlags.openWrite | CFileFlags.openTruncate
        RDWRACC
      case IFileSystem.O_RDWR =>
        flags = CFileFlags.openRead | CFileFlags.openWrite | CFileFlags.openCreate
        RDWRACC
      case IFileSystem.O_APPEND =>
        flags = CFileFlags.openWrite | CFileFlags.openCreate | CFileFlags.openAppend
        RDWRACC
      case IFileSystem.O_RDWRSYNC =>
        flags = CFileFlags.openRead | CFileFlags.openWrite | CFileFlags.openCreate | CFileFlags.openSync
        RDWRACC
      case _ =>
        throw new IllegalArgumentException(
          "Unknown open mode passed to openImpl")
    }

    return CFile.nativeFileOpen(pathCopy, flags, accMode)

  }

  @throws(classOf[IOException])
  def close(fileDescriptor: Long): Unit = {
    val rc = CFile.fileDescriptorClose(fileDescriptor.toInt)
    if (rc == -1) {
      throw new IOException()
    }
  }

  @throws(classOf[IOException])
  def write(fileDescriptor: Long,
            bytes: Array[Byte],
            offset: Int,
            length: Int): Long = {

    val result = unistd.write(
      fileDescriptor.toInt,
      (bytes.asInstanceOf[scala.scalanative.runtime.ByteArray].at(0) + offset),
      length.toLong)
    if (result < 0) {
      throw new IOException("could not write to file")
    }
    return result
  }

  @throws(classOf[IOException])
  def read(fileDescriptor: Long,
           bytes: Array[Byte],
           offset: Int,
           length: Int): Long = {
    if (bytes == null) {
      throw new NullPointerException()
    }
    val bytesRead: Long = readImpl(fileDescriptor, bytes, offset, length)
    if (bytesRead < -1) {
      throw new IOException()
    }
    return bytesRead
  }

  private def readImpl(fileDescriptor: Long,
                       bytes: Array[Byte],
                       offset: Int,
                       length: Int): Long = {
    if (length == 0) return 0
    val result = unistd.read(
      fileDescriptor.toInt,
      (bytes.asInstanceOf[scala.scalanative.runtime.ByteArray].at(0) + offset),
      length.toLong)
    return if (result == 0) -1 else result
  }

  @throws(classOf[IOException])
  def ttyAvailable(): Long = {
    val nChar: Long = CFile.fileTtyAvalaible().toInt
    if (nChar < 0) {
      throw new IOException()
    }
    return nChar
  }

  @throws(classOf[IOException])
  def ttyRead(bytes: Array[Byte], offset: Int, length: Int): Long = {
    val nChar: Long = unistd.read(
      0 /*STDIN_FILENO*/,
      (bytes.asInstanceOf[scala.scalanative.runtime.ByteArray].at(0) + offset),
      length.toLong)
    if (nChar < 0) {
      throw new IOException()
    }
    return nChar
  }

  @throws(classOf[IOException])
  def seek(fileDescriptor: Long, offset: Long, whence: Int): Long = {
    val pos: Long = seekImpl(fileDescriptor, offset, whence)
    if (pos == -1) {
      throw new IOException()
    }
    return pos
  }

  private def seekImpl(fd: Long, offset: Long, whence: Int): Long = {
    var hywhence = whence match {
      case IFileSystem.SEEK_SET => CFileFlags.seekSet
      case IFileSystem.SEEK_CUR => CFileFlags.seekCur
      case IFileSystem.SEEK_END => CFileFlags.seekEnd
      case _                    => -1
    }
    return CFile.fileSeek(fd.toInt, offset, hywhence)
  }

  @throws(classOf[IOException])
  def available(fileDescriptor: Long): Long = {
    val nChar: Long = availableImpl(fileDescriptor)
    if (nChar < 0) {
      throw new IOException()
    }
    return nChar
  }

  private def availableImpl(fd: Long): Long = {
    val currentPosition: Long = seekImpl(fd, 0, IFileSystem.SEEK_CUR)
    val endPosition: Long     = seekImpl(fd, 0, IFileSystem.SEEK_END)
    seekImpl(fd, currentPosition, IFileSystem.SEEK_SET)
    return endPosition - currentPosition
  }
}
