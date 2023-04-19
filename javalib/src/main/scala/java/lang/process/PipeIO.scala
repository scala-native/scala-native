package java.lang.process

import java.io._
import scala.annotation.tailrec
import scala.scalanative.annotation.stub
import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.windows.DWord
import scala.scalanative.windows.NamedPipeApi.PeekNamedPipe
import scala.scalanative.unsigned._
/*
 * Used to set up input/output streams for a child process spawned by
 * a ProcessBuilder using the Redirect info for the stream.
 */
private[lang] final class PipeIO[T](
    val nullStream: T,
    val fdStream: (GenericProcess, FileDescriptor) => T
)

private[lang] object PipeIO {
  def apply[T](
      process: GenericProcess,
      childFd: => FileDescriptor,
      redirect: ProcessBuilder.Redirect
  )(implicit ioStream: PipeIO[T]): T = {
    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.PIPE =>
        ioStream.fdStream(process, childFd)
      case _ =>
        ioStream.nullStream
    }
  }
  trait Stream extends InputStream {
    def process: GenericProcess
    def drain(): Unit = {}
  }
  class StreamImpl(val process: GenericProcess, is: FileInputStream)
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
      @tailrec
      def loop(readBuf: Array[scala.Byte]): Array[Byte] = {
        if (drained) readBuf
        else {
          val toRead = availableFD()
          if (toRead > 0) {
            val size = readBuf.size
            val newBuf = java.util.Arrays.copyOf(readBuf, size + toRead)
            is.read(newBuf, size, toRead)
            loop(newBuf)
          } else readBuf
        }
      }

      val readBuf = loop(Array.emptyByteArray)
      drained = true
      is.close()
      this.in = new ByteArrayInputStream(readBuf)
    }

    private[this] var drained = false
    private def availableFD() = {
      if (isWindows) {
        val availableTotal = stackalloc[DWord]()
        val hasPeaked = PeekNamedPipe(
          pipe = is.getFD().handle,
          buffer = null,
          bufferSize = 0.toUInt,
          bytesRead = null,
          totalBytesAvailable = availableTotal,
          bytesLeftThisMessage = null
        )
        if (hasPeaked) (!availableTotal).toInt
        else 0
      } else {
        val res = stackalloc[CInt]()
        ioctl(
          is.getFD().fd,
          FIONREAD,
          res.asInstanceOf[Ptr[scala.Byte]]
        ) match {
          case -1 => 0
          case _  => !res
        }
      }
    }
  }

  implicit val InputPipeIO: PipeIO[Stream] =
    new PipeIO(NullInput, (p, fd) => new StreamImpl(p, new FileInputStream(fd)))
  implicit val OutputPipeIO: PipeIO[OutputStream] =
    new PipeIO(
      NullOutput,
      (_, fd) => new BufferedOutputStream(new FileOutputStream(fd))
    )

  private object NullInput extends Stream {
    @stub
    override def process: GenericProcess = ???
    override def available(): Int = 0
    override def close(): Unit = {}
    override def read(): Int = 0
    override def read(buf: Array[scala.Byte], offset: Int, len: Int) = -1
  }
  private object NullOutput extends OutputStream {
    override def close(): Unit = {}
    override def write(b: Int): Unit = {}
  }
}
