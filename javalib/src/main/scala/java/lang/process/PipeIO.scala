package java.lang.process

import java.io._

import scala.scalanative.annotation.stub
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.DWord
import scala.scalanative.windows.NamedPipeApi.PeekNamedPipe

/* Used to set up input/output streams for a child process spawned by
 * a ProcessBuilder using the Redirect info for the stream.
 */

/* Design Note:
 *
 *   Making the changes required to resolve Issue 4023 was hard;
 *   describing the underlying logic of PipeIO.scala is even harder.
 *   This note is, by some standards, brief. It offers clues and should
 *   not be taken as a proper or sufficient complete design description.
 *   You have the code, what more documentation could you possibly need?
 *
 *   May the clues provided save future maintainers time, effort, and
 *   sheer pain.
 *
 *   Due respect for prior developers demands that one appreciate
 *   "it made sense at the time". Still, one can recognize that the entire
 *   Scala Native implementation of "java.lang.process" offers abundant
 *   opportunities for improvement.
 *
 *   - Unexpected use of synchronized methods
 *
 *     At first reading, the use of synchronized methods in this file
 *     is unexpected if not astonishing.
 *
 *        - NB: Even though the methods are synchronized, that internal detail
 *          is not made public.
 *
 *          The JVM recommendation to use external synchronization if two or
 *          more threads are doing reads or writes to the same pipe input or
 *          output is still operative and essential.
 *
 *     Much of the complexity of PipeIO comes from the need to ensure
 *     that operating system file descriptors (Unix fds, Windows handles)
 *     are eventually closed in the parent process after the child process
 *     exits, even if the PipeIO user never explicitly calls close().
 *
 *       <Skipping lightly over an extended discussion of how much defensive
 *        complexity should be added in order to be robust to bad caller
 *        behavior.>
 *
 *     When a child process exits, the operating system places an End-of-File
 *     (EOF) marker is placed in the child output and error streams and
 *     then closes() that end of the pipe.
 *
 *     It is up to the parent process to close the os fds at its end of
 *     the pipe. When a pipe fd is closed, any data still in the os pipe
 *     is lost.
 *
 *     PipeIO must take steps to keep the data in the pipe available to
 *     its callers before closing its end of the os pipe.  It uses a
 *     series of steps it calls "draining" to accomplish this. When
 *     the parent becomes aware that the child process has exited, the
 *     parent copies all bytes remaining at its input end of the os pipe
 *     to a local buffer and closes its os fd. That releases the resource
 *     and keeps it from becoming unavailable until the parent process
 *     exits.
 *
 *     To prevent data corruption, the "drain()" method in a thread must
 *     wait until no thread is executing a read(*) method, and then
 *     exclude all other thread PipeIO public operations until after it
 *     has copied the in-flight os pipe data and switched the 'src' variable to
 *     read from that saved data. This is accomplished by using
 *     synchronized methods.
 */

private[process] final class PipeIO[T](
    val nullStream: T,
    val fdStream: FileDescriptor => T
)

private[process] object PipeIO {
  def apply[T](
      childFd: => FileDescriptor,
      redirect: ProcessBuilder.Redirect
  )(implicit ioStream: PipeIO[T]): T = {
    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.PIPE =>
        val fd = childFd // could specify INVALID if PIPE is not needed
        if (fd.valid()) ioStream.fdStream(fd) else ioStream.nullStream
      case _ =>
        ioStream.nullStream
    }
  }

  trait Stream extends InputStream {
    def drain(): Unit = {}
  }

  class StreamImpl(is: FileInputStream) extends Stream {

    private var src: InputStream = is

    override def available(): Int =
      synchronized(src.available())

    private def switchToNullInput(): Unit = {
      src.close()
      src = NullInput
    }

    override def read(): Int =
      if (src eq NullInput) -1
      else
        synchronized {
          val res = src.read()
          if (res == -1) switchToNullInput()
          res
        }

    override def read(buf: Array[scala.Byte], offset: Int, len: Int): Int = {
      if (offset < 0 || len < 0 || len > buf.length - offset) {
        val end = offset + len
        throw new IndexOutOfBoundsException(
          s"Range [$offset, $end) out of bounds for length ${buf.length}"
        )
      }

      if (len == 0) 0
      else if (src eq NullInput) -1
      else
        synchronized {
          val res = src.read(buf, offset, len)
          if (res < 0) switchToNullInput()
          res
        }
    }

    /* Switch horses, or at least InputStreams, "in media res".
     * See Design Note at top of file.
     */
    override def drain(): Unit = if (src eq is) synchronized {
      if (src eq is) { // not yet drained
        val avail = is.available()

        src =
          if (avail <= 0) PipeIO.NullInput
          else new ByteArrayInputStream(is.readNBytes(avail))

        // release JVM FileDescriptor and, especially, its OS fd.
        is.close()
      }
    }
  }

  implicit val InputPipeIO: PipeIO[Stream] =
    new PipeIO(NullInput, fd => new StreamImpl(new FileInputStream(fd)))

  implicit val OutputPipeIO: PipeIO[OutputStream] =
    new PipeIO(NullOutput, fd => new FileOutputStream(fd))

  private object NullInput extends Stream {
    override def available(): Int = 0
    override def close(): Unit = {}
    override def read(): Int = -1
    override def read(buf: Array[scala.Byte], offset: Int, len: Int) =
      if (len == 0) 0 else -1
  }

  private object NullOutput extends OutputStream {
    override def close(): Unit = {}
    override def write(b: Int): Unit = {}
  }
}
