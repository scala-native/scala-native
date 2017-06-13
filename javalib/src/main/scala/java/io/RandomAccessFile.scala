package java.io

import scalanative.native.{stdio, toCString, Zone}
import scalanative.posix.{fcntl, unistd}
import scalanative.posix.sys.stat

class RandomAccessFile private (file: File,
                                fd: FileDescriptor,
                                flush: Boolean,
                                mode: String)
    extends DataOutput
    with DataInput
    with Closeable {
  def this(file: File, mode: String) =
    this(file,
         RandomAccessFile.fileDescriptor(file, mode),
         RandomAccessFile.flush(mode),
         mode)
  def this(name: String, mode: String) = this(new File(name), mode)

  private var closed: Boolean = false
  private lazy val in         = new DataInputStream(new FileInputStream(fd))
  private lazy val out        = new DataOutputStream(new FileOutputStream(fd))

  override def close(): Unit = {
    closed = true
    fcntl.close(fd.fd)
  }

  // final def getChannel(): FileChannel

  def getFD(): FileDescriptor =
    fd

  def getFilePointer(): Long =
    unistd.lseek(fd.fd, 0, stdio.SEEK_CUR).toLong

  def length(): Long =
    file.length()

  def read(): Int =
    if (closed) throw new IOException("Stream Closed")
    else in.read()

  def read(b: Array[Byte]): Int =
    if (closed) throw new IOException("Stream Closed")
    else in.read(b)

  def read(b: Array[Byte], off: Int, len: Int): Int =
    if (closed) throw new IOException("Stream Closed")
    else in.read(b, off, len)

  override final def readBoolean(): Boolean =
    in.readBoolean()

  override final def readByte(): Byte =
    in.readByte()

  override final def readChar(): Char =
    in.readChar()

  override final def readDouble(): Double =
    in.readDouble()

  override final def readFloat(): Float =
    in.readFloat()

  override final def readFully(b: Array[Byte]): Unit =
    readFully(b, 0, b.length)

  override final def readFully(b: Array[Byte], off: Int, len: Int): Unit =
    in.readFully(b, off, len)

  override final def readInt(): Int =
    in.readInt()

  override final def readLine(): String = {
    if (getFilePointer == length) null
    else {
      val builder = new StringBuilder
      var c       = '0'
      do {
        c = readChar()
        builder.append(c)
      } while (c != '\n' && c != '\r')

      // If there's a newline after carriage-return, we must eat it too.
      if (c == '\r' && readChar() != '\n') {
        seek(getFilePointer - 1)
      }
      builder.toString.init
    }
  }

  override final def readLong(): Long =
    in.readLong()

  override final def readShort(): Short =
    in.readShort()

  override final def readUnsignedByte(): Int =
    in.readUnsignedByte()

  override final def readUnsignedShort(): Int =
    in.readUnsignedShort()

  override final def readUTF(): String =
    in.readUTF()

  def seek(pos: Long): Unit =
    unistd.lseek(fd.fd, pos, stdio.SEEK_SET)

  def setLength(newLength: Long): Unit =
    if (!mode.contains("w")) {
      throw new IOException("Invalid argument")
    } else {
      val currentPosition = getFilePointer()
      if (unistd.ftruncate(fd.fd, newLength) != 0) {
        throw new IOException()
      }
      if (currentPosition > newLength) seek(newLength)
    }

  override def skipBytes(n: Int): Int =
    if (n <= 0) 0
    else {
      val currentPosition = getFilePointer
      val fileLength      = length()
      val toSkip =
        if (currentPosition + n > fileLength) fileLength - currentPosition
        else n
      seek(toSkip)
      toSkip.toInt
    }

  override def write(b: Array[Byte]): Unit = {
    out.write(b)
    maybeFlush()
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    out.write(b, off, len)
    maybeFlush()
  }

  override def write(b: Int): Unit = {
    out.write(b)
    maybeFlush()
  }

  override final def writeBoolean(v: Boolean): Unit = {
    out.writeBoolean(v)
    maybeFlush()
  }

  override final def writeByte(v: Int): Unit = {
    out.writeByte(v)
    maybeFlush()
  }

  override final def writeBytes(s: String): Unit = {
    out.writeBytes(s)
    maybeFlush()
  }

  override final def writeChar(v: Int): Unit = {
    out.writeChar(v)
    maybeFlush()
  }

  override final def writeChars(s: String): Unit = {
    out.writeChars(s)
    maybeFlush()
  }

  override final def writeDouble(v: Double): Unit = {
    out.writeDouble(v)
    maybeFlush()
  }

  override final def writeFloat(v: Float): Unit = {
    out.writeFloat(v)
    maybeFlush()
  }

  override final def writeInt(v: Int): Unit = {
    out.writeInt(v)
    maybeFlush()
  }

  override final def writeLong(v: Long): Unit = {
    out.writeLong(v)
    maybeFlush()
  }

  override final def writeShort(v: Int): Unit = {
    out.writeShort(v)
    maybeFlush()
  }

  override final def writeUTF(str: String): Unit = {
    out.writeUTF(str)
    maybeFlush
  }

  private def maybeFlush(): Unit =
    if (flush) out.flush()
}

private object RandomAccessFile {
  private def fileDescriptor(file: File, _flags: String) =
    Zone { implicit z =>
      import fcntl._
      import stat._
      if (_flags == "r" && !file.exists)
        throw new FileNotFoundException(file.getName)
      val flags = _flags match {
        case "r"                  => O_RDONLY
        case "rw" | "rws" | "rwd" => O_RDWR | O_CREAT
        case _ =>
          throw new IllegalArgumentException(
            s"""Illegal mode "${_flags}" must be one of "r", "rw", "rws" or "rwd"""")
      }
      val mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH
      val fd   = open(toCString(file.getPath), flags, mode)
      new FileDescriptor(fd)
    }

  private def flush(mode: String): Boolean =
    mode match {
      case "r" | "rw" => false
      case _          => true
    }
}
