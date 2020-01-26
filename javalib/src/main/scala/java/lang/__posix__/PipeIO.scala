package java
package lang

import java.io._
import scala.scalanative.annotation.stub
import scala.scalanative.unsafe._
import scala.scalanative.libc._, signal._
import scala.scalanative.posix.sys.ioctl._

/*
 * Used to set up input/output streams for a child process spawned by
 * a ProcessBuilder using the Redirect info for the stream.
 */
private[lang] final class PipeIO[T](
    val nullStream: T,
    val fdStream: (UnixProcess, FileDescriptor) => T)
private[lang] object PipeIO {
  def apply[T](
      process: UnixProcess,
      childFd: Int,
      redirect: ProcessBuilder.Redirect
  )(implicit ioStream: PipeIO[T]): T = {
    redirect.`type` match {
      case ProcessBuilder.Redirect.Type.PIPE =>
        ioStream.fdStream(process, new FileDescriptor(childFd))
      case _ =>
        ioStream.nullStream
    }
  }
  trait Stream extends InputStream {
    def process: UnixProcess
    def drain(): Unit = {}
  }
  class StreamImpl(val process: UnixProcess, is: FileInputStream)
      extends BufferedInputStream(is)
      with Stream {
    override def available() = {
      val res = super.available() match {
        // Check the FileInputStream in case the BufferedInputStream hasn't been filled yet.
        case 0 =>
          in.available() match {
            case 0 if !drained => availableFD()
            case a             => a
          }
        case a => a
      }
      process.checkResult()
      res
    }
    override def read(): Int = {
      val res = super.read()
      process.checkResult()
      res
    }
    override def read(buf: Array[scala.Byte], offset: Int, len: Int) = {
      val res = super.read(buf, offset, len)
      process.checkResult()
      res
    }
    override def drain() = {
      var toRead                     = 0
      var readBuf: Array[scala.Byte] = Array()
      while ({
        toRead = availableFD
        toRead > 0
      }) {
        val size = if (readBuf == null) 0 else readBuf.size
        readBuf =
          if (readBuf == null) new Array[scala.Byte](toRead)
          else {
            readBuf = java.util.Arrays.copyOf(readBuf, size + toRead)
            readBuf
          }
        val bytesRead = is.read(readBuf, size, toRead)
      }
      is.close()
      this.in = new ByteArrayInputStream(readBuf)
    }

    private[this] var drained = false
    private def availableFD() = {
      val res = stackalloc[CInt]
      ioctl(is.getFD.fd, FIONREAD, res.asInstanceOf[Ptr[scala.Byte]]) match {
        case -1 => 0
        case _  => !res
      }
    }
  }

  implicit val InputPipeIO: PipeIO[Stream] =
    new PipeIO(NullInput, (p, fd) => new StreamImpl(p, new FileInputStream(fd)))
  implicit val outputPipeIO: PipeIO[OutputStream] =
    new PipeIO(NullOutput,
               (p, fd) => new BufferedOutputStream(new FileOutputStream(fd)))

  private final object NullInput extends Stream {
    @stub
    override def process: UnixProcess                                = ???
    override def available(): Int                                    = 0
    override def close(): Unit                                       = {}
    override def read(): Int                                         = 0
    override def read(buf: Array[scala.Byte], offset: Int, len: Int) = -1
  }
  private final object NullOutput extends OutputStream {
    override def close(): Unit       = {}
    override def write(b: Int): Unit = {}
  }
}
