package java
package lang

import java.io._
import scala.scalanative.native._, signal._
import scala.scalanative.posix.sys.ioctl._

/*
 * Used to set up input/output streams for a child process spawned by
 * a ProcessBuilder using the Redirect info for the stream.
 */
private[lang] final class PipeIO[T](val nullStream: T,
                                    val fdStream: FileDescriptor => T)
private[lang] object PipeIO {
  def apply[T](
      childFd: Int,
      redirect: ProcessBuilder.Redirect
  )(implicit ioStream: PipeIO[T]): T = {
    redirect.`type` match {
      case ProcessBuilder.Redirect.Type.PIPE =>
        ioStream.fdStream(new FileDescriptor(childFd))
      case _ =>
        ioStream.nullStream
    }
  }
  trait Stream extends InputStream {
    def drain(): Unit = {}
  }
  class StreamImpl(is: FileInputStream)
      extends BufferedInputStream(is)
      with Stream {
    override def available() = UnixProcess.locked { _ =>
      super.available() match {
        // Check the FileInputStream in case the BufferedInputStream hasn't been filled yet.
        case 0 =>
          in.available() match {
            case 0 if !drained => availableFD()
            case a             => a
          }
        case a => a
      }
    }
    override def read(): Int = UnixProcess.locked { _ =>
      super.read()
    }
    override def read(buf: Array[scala.Byte], offset: Int, len: Int) =
      UnixProcess.locked { _ =>
        super.read(buf, offset, len)
      }
    override def drain() = {
      // No lock needed because it's called from the interrupt handler while
      // the monitor thread is holding the lock.
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
      ioctl(is.getFD.fd, FIONREAD, res.cast[Ptr[scala.Byte]]) match {
        case -1 => 0
        case _  => !res
      }
    }
  }

  implicit val InputPipeIO: PipeIO[Stream] =
    new PipeIO(NullInput, fd => new StreamImpl(new FileInputStream(fd)))
  implicit val outputPipeIO: PipeIO[OutputStream] =
    new PipeIO(NullOutput,
               fd => new BufferedOutputStream(new FileOutputStream(fd)))

  private final object NullInput extends Stream {
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
