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
 *   TBS
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
      extends Stream {

    private var drained = false
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
      finally process.checkResult()
    }

    override def read(): Int = synchronized {
      try
        src.read()
      finally process.checkResult()
    }

// Debug - Begin
    import java.util.Arrays
    import java.nio.charset.StandardCharsets
// Debug - End

    override def read(buf: Array[scala.Byte], offset: Int, len: Int): Int =
      synchronized {

        printf(
          s"\n\nLeeT: PipeIO#read(b,o,l), len: ${len} drained: ${drained}\n\n"
        )

        if (offset < 0 || len < 0 || len > buf.length - offset) {
          val prefix =
            s"Range [${offset}, ${offset} + ${len})"
          val suffix =
            s" out of bounds for length ${buf.length}"
          throw new IndexOutOfBoundsException(s"${prefix}${suffix}")
        }

        if (len == 0) 0
        else {
          try {
            val avail = availableUnSync()

            printf(s"\n\nLeeT: read(b,o,l), top avail: ${avail}\n\n")

            if (avail > 0) {
              val nToRead = Math.min(len, avail)
              printf(s"\n\nLeeT: read(b,o,l), reading nToRead: ${nToRead}\n")
              val nRead = src.read(buf, offset, nToRead)
              val extracted = Arrays.copyOfRange(buf, offset, nRead)
              val result = new String(extracted, StandardCharsets.UTF_8).trim()
              printf(
                s"LeeT: read(b,o,l), nRead: ${nRead} bytes: |${extracted}|\n\n"
              )
              nRead
            } else {
              src match {
                case fis: FileInputStream =>
                  printf(s"LeeT: read(b,o,l), reading 1 byte\n")
                  val nRead = src.read(buf, offset, 1)

                  if (nRead == -1) {
                    printf(s"LeeT: read(b,o,l), 1 byte result: EOF\n\n")
                    -1
                  } else {

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

          } finally process.checkResult()
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
      drained = true
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
