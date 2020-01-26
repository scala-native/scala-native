package java.io

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scalanative.runtime

import scala.scalanative.cpp.{NullObj}
import scala.scalanative.cpp.ios.{IOStream, FStream, OpenMode}

/** Wraps a cpp file descriptor */
final class FileDescriptor private[java] (private[java] val stream: IOStream,
                                          val readOnly: Boolean = false) {

  def this() = this(new IOStream(NullObj), true)

  def sync(): Unit = stream.sync()

  def valid(): Boolean = stream.valid()

  private[java] def available(): Int = stream.streambuf_in_avail().toInt

  private[java] def ignore(count: Long): Unit = stream.ignore(count)

  private[java] def read(buffer: Array[Byte], offset: Int, count: Int):Int = {
    val buf = buffer.asInstanceOf[runtime.ByteArray].at(offset)
    stream.read(buf, count).toInt
  }

  private[java] def write(buffer: Array[Byte], offset: Int, count: Int):Int = {
    val buf = buffer.asInstanceOf[runtime.ByteArray].at(offset)
    stream.write(buf, count).toInt
  }

  private[java] def close(): Unit = stream.close()
}

object FileDescriptor {
  val in: FileDescriptor  = new FileDescriptor(IOStream.stdin)
  val out: FileDescriptor = new FileDescriptor(IOStream.stdout)
  val err: FileDescriptor = new FileDescriptor(IOStream.stderr)

  private[io] def openReadOnly(file: File): FileDescriptor =
    Zone { implicit z =>
      val stream = FStream.open(file.getPath, OpenMode.in | OpenMode.binary)
      if (!stream.is_open()) {
        throw new FileNotFoundException("No such file " + file.getPath)
      }
      new FileDescriptor(stream, true)
    }
  private[io] def openWriteOnly(file: File, append: Boolean): FileDescriptor = Zone { implicit z =>
      val flags =  OpenMode.out | OpenMode.binary | (if (append) OpenMode.app else OpenMode.trunc)
      val stream = FStream.open(file.getPath, flags)
      if (!stream.is_open())
        throw new FileNotFoundException("Cannot open file " + file.getPath)
      else
        new FileDescriptor(stream)
    }
}
