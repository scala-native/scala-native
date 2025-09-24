package java.lang.process

import java.io._

import scala.scalanative.annotation.stub
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.meta.LinktimeInfo.isWindows

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
 *   - read(b, o, l) behavior: "de jure" and "de facto"
 *
 *     The Java 8, and subsequent, "InputStream#read(b, o, l)" documentation
 *     says, in part: "An attempt is made to read as many as len bytes,
 *     but a smaller number may be read."
 *
 *     - Prior to the changes to resolve Issue 4023, Scala Native PipeIO
 *       followed the "as many as len bytes" clause, blocking until
 *       either the len bytes had been read or an End of File or Exception
 *       had been encountered. Compliant but not actual JVM practice.
 *
 *     - Issue 4023 requested that Scala Native PipeIO follow the actual
 *       practice of many JVMs of following the "but a smaller number may
 *       be read." clause. This code will now block until the first byte
 *       becomes available, and then immediately return that byte and
 *       any others which are available at that time. This may and will
 *       result in "short" reads.
 *
 *       JVM actual behavior, with all its JVM versions and implementations
 *       can only be modeled to an approximation. The approximation to resolve
 *       Issue 4023 should be better than the prior approximation.
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
 *
 *     - UnixProcess* and WindowProcess check if the child process has exited
 *       at different places in the code. This can have a major effect on
 *       the timing of events, including the number of bytes available at
 *       an I/O event.
 *
 *       - UnixProcess* checks in the public "waitfor(*)" method.
 *         That method is usually called once by user of PipeIO.
 *
 *       - WindowsProcess checks in the private "checkResult()" method.
 *         That method is usually called once per PipeIO public method call.
 *         This means that it is called at least once per I/O.
 *
 *   - UnixProcessGen2 and WindowsProcess are well exercised.
 *     UnixProcessGen1 is less well exercised and may retain
 *     unfortunate timing interactions.
 */

private[process] final class PipeIO[T](
    val nullStream: T,
    val fdStream: (GenericProcessHandle, FileDescriptor) => T
)

private[process] object PipeIO {
  def apply[T](
      process: GenericProcessHandle,
      childFd: => FileDescriptor,
      redirect: ProcessBuilder.Redirect
  )(implicit ioStream: PipeIO[T]): T = {
    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.PIPE =>
        val fd = childFd // could specify INVALID if PIPE is not needed
        if (fd.valid()) ioStream.fdStream(process, fd) else ioStream.nullStream
      case _ =>
        ioStream.nullStream
    }
  }

  trait Stream extends InputStream {
    def process: GenericProcessHandle
    def drain(): Unit = {}
  }

  class StreamImpl(val process: GenericProcessHandle, is: FileInputStream)
      extends Stream {

    private var src: InputStream = is

    // By convention, caller is synchronized on 'this'.
    private def availableUnSync() = {
      src match {
        case fis: FileInputStream => availableFD()
        case _                    => src.available()
      }
    }

    override def available(): Int = synchronized {
      try
        availableUnSync()
      finally process.checkIfExited()
    }

    override def read(): Int = synchronized {
      try
        src.read()
      finally process.checkIfExited()
    }

    override def read(buf: Array[scala.Byte], offset: Int, len: Int): Int =
      synchronized {

        if (offset < 0 || len < 0 || len > buf.length - offset) {
          val end = offset + len
          throw new IndexOutOfBoundsException(
            s"Range [$offset, $end) out of bounds for length ${buf.length}"
          )
        }

        if (len == 0) 0
        else {
          try {
            val avail = availableUnSync()

            if (avail > 0) {
              val nToRead = Math.min(len, avail)
              src.read(buf, offset, nToRead)
            } else {
              src match {
                case fis: FileInputStream =>
                  val nRead = src.read(buf, offset, 1)

                  if (nRead == -1) -1
                  else {

                    val nToRead =
                      Math.min(len - 1, availableUnSync()) // possibly zero

                    val nSecondRead =
                      if (nToRead == 0) 0
                      else src.read(buf, offset + 1, nToRead)

                    if (nSecondRead == -1) 1
                    else nSecondRead + 1
                  }

                case _ => -1 // EOF
              }
            }
          } finally process.checkIfExited()
        }
      }

    /* Switch horses, or at least InputStreams, "in media res".
     * See Design Note at top of file.
     */
    override def drain(): Unit = synchronized {

      val avail = availableUnSync()

      val newSrc =
        if (avail <= 0) PipeIO.NullInput
        else new ByteArrayInputStream(src.readNBytes(avail))

      // release JVM FileDescriptor and, especially, its OS fd.
      src.close()

      src = newSrc
    }

    private def availableFD(): Int = {
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
    new PipeIO(NullOutput, (_, fd) => new FileOutputStream(fd))

  private object NullInput extends Stream {
    @stub
    override def process: GenericProcessHandle = ???
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
