package java.io

import java.lang as jl
import java.nio.channels.{FileChannelImpl, FileChannel}

import scalanative.unsafe.{Zone, toCString, toCWideStringUTF16LE}

import scalanative.posix.fcntl
import scalanative.posix.sys.stat
import scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.windows
import windows.*
import windows.FileApiExt.*
import windows.HandleApiExt

class RandomAccessFile private (
    file: File,
    fd: FileDescriptor,
    flush: Boolean,
    mode: String
) extends DataOutput
    with DataInput
    with Closeable {
  def this(file: File, mode: String) =
    this(
      file,
      RandomAccessFile.fileDescriptor(file, mode),
      RandomAccessFile.flush(mode),
      mode
    )
  def this(name: String, mode: String) = this(new File(name), mode)

  private lazy val in = new DataInputStream(new FileInputStream(fd))
  private lazy val out = new DataOutputStream(new FileOutputStream(fd))
  private lazy val channel =
    new FileChannelImpl(
      fd,
      Some(file),
      deleteFileOnClose = false,
      openForReading = true,
      openForWriting = mode.contains('w')
    )

  override def close(): Unit =
    channel.close()

  final def getChannel(): FileChannel =
    channel

  def getFD(): FileDescriptor =
    fd

  def getFilePointer(): Long =
    channel.position()

  def length(): Long =
    file.length()

  def read(): Int =
    if (!channel.isOpen()) throw new IOException("Stream Closed")
    else in.read()

  def read(b: Array[Byte]): Int =
    if (!channel.isOpen()) throw new IOException("Stream Closed")
    else in.read(b)

  def read(b: Array[Byte], off: Int, len: Int): Int =
    if (!channel.isOpen()) throw new IOException("Stream Closed")
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
    // DataInputStream#readLine has been deprecated since JDK 1.1
    // so implement RAF#readLine, rather than delegating.
    var pos = getFilePointer()
    var end = length() // standard practice: 1 past last valid byte.
    if (pos >= end) {
      null // JDK 8 specification requires null here.
    } else {
      val builder = new jl.StringBuilder
      var done = false

      while (!done && (pos < end)) {
        val c = readByte().toChar
        pos += 1

        c match {
          case '\n' => done = true

          case '\r' =>
            // If there's a newline after carriage-return, we must eat it too.
            if (pos < end) {
              if (readByte().toChar == '\n') {
                pos += 1
              } else {
                seek(getFilePointer() - 1)
              }
            }
            done = true

          case _ => builder.append(c)
        }
      }

      builder.toString
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
    channel.position(pos)

  def setLength(newLength: Long): Unit =
    channel.truncate(newLength)

  override def skipBytes(n: Int): Int =
    if (n <= 0) 0
    else {
      val currentPosition = getFilePointer()
      val fileLength = length()
      val toSkip =
        if (currentPosition + n > fileLength) fileLength - currentPosition
        else n.toLong
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
    maybeFlush()
  }

  private def maybeFlush(): Unit =
    if (flush) out.flush()
}

private object RandomAccessFile {
  private def fileDescriptor(file: File, _flags: String) = {
    if (_flags == "r" && !file.exists())
      throw new FileNotFoundException(file.getName())

    def invalidFlags() =
      throw new IllegalArgumentException(
        s"""Illegal mode "${_flags}" must be one of "r", "rw", "rws" or "rwd""""
      )

    def unixFileDescriptor() = Zone.acquire { implicit z =>
      import fcntl.*
      import stat.*

      val flags = _flags match {
        case "r"                  => O_RDONLY
        case "rw" | "rws" | "rwd" => O_RDWR | O_CREAT
        case _                    => invalidFlags()
      }
      val mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH

      val fd = open(toCString(file.getPath()), flags, mode)

      if (fd == -1)
        throw new FileNotFoundException(file.getName())

      new FileDescriptor(FileDescriptor.FileHandle(fd), readOnly = false)
    }

    def windowsFileDescriptor() = Zone.acquire { implicit z =>
      import windows.winnt.AccessRights.*
      val (access, dispostion) = _flags match {
        case "r"                  => FILE_GENERIC_READ -> OPEN_EXISTING
        case "rw" | "rws" | "rwd" =>
          (FILE_GENERIC_READ | FILE_GENERIC_WRITE).toUInt -> OPEN_ALWAYS
        case _ => invalidFlags()
      }

      val handle = FileApi.CreateFileW(
        toCWideStringUTF16LE(file.getPath()),
        desiredAccess = access,
        shareMode = FILE_SHARE_READ | FILE_SHARE_WRITE,
        securityAttributes = null,
        creationDisposition = dispostion,
        flagsAndAttributes = FILE_ATTRIBUTE_NORMAL,
        templateFile = null
      )

      if (handle == HandleApiExt.INVALID_HANDLE_VALUE)
        throw new FileNotFoundException(file.getName())

      new FileDescriptor(
        FileDescriptor.FileHandle(handle),
        readOnly = _flags == "r"
      )
    }

    if (isWindows) windowsFileDescriptor()
    else unixFileDescriptor()
  }

  private def flush(mode: String): Boolean =
    mode match {
      case "r" | "rw" => false
      case _          => true
    }
}
