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

    private var src: InputStream = is

    // By convention, caller is synchronized on 'src'.
    private def availableUnSync() = {
      src match {
        case fis: FileInputStream => availableFD()
        case _                    => src.available()
      }
    }

    override def available(): Int = src.synchronized {
      try
        availableUnSync()
      finally process.checkResult()
    }

    override def read(): Int = src.synchronized {
      try
        src.read()
      finally process.checkResult()
    }

    override def read(b: Array[Byte]): Int = this.read(b, 0, b.length)

    override def read(buf: Array[scala.Byte], offset: Int, len: Int): Int =
      src.synchronized {
        /* The read(buf, offset, len) within this method will always check
         * arguments, so save a few cycles and do not do check here.
         */

        try {
          val avail = availableUnSync()

          if (avail > 0) {
            src.read(buf, offset, avail)
          } else {
            src match {
              case fis: FileInputStream =>
                val nRead = src.read(buf, offset, 1)

                if (nRead == -1) -1
                else {
                  val a = availableUnSync()

                  if (a == 0) nRead
                  else {
                    val newOffset = offset + 1
                    val availableBuffer = buf.length - newOffset
                    if (availableBuffer <= 0) nRead
                    else {
                      val nToRead = Math.min(availableBuffer, a)
                      val nr =
                        if (nToRead == 0) 0
                        else src.read(buf, newOffset, nToRead)
                      if (nr <= 0) nRead else nRead + nr
                    }
                  }
                }

              case _ => -1 // EOF

            }
          }
        } finally process.checkResult()
      }

    /* Switch horses, or at least InputStreams in media res.
     * See Design Note at top of file.
     */
    override def drain(): Unit = src.synchronized {

      val newSrc = new ByteArrayInputStream(src.readNBytes(availableUnSync()))

      src.close() // release JVM FileDescriptor and, especially, its OS fd.

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
