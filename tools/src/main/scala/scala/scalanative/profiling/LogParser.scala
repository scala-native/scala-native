package scala.scalanative
package profiling

import Event._

import java.io.{
  InputStream,
  File,
  FileInputStream,
  BufferedInputStream,
  EOFException
}

/** A class that can parse the logs produced by profiling. */
class LogParser(stream: InputStream) {
  import LogParser._

  private val buffer    = new Array[Byte](1024)
  private var pos       = 0
  private var available = 0

  /** Read the next byte from the stream. */
  private def next(): Byte = {
    if (available == 0) {
      available = stream.read(buffer, 0, buffer.length)
      pos = 0
    }

    if (available <= 0)
      throw new EOFException()

    val result = buffer(pos)
    pos = pos + 1
    available = available - 1
    result
  }

  /** Reads `steps` bytes, shifts the value from the corresponding step. */
  private def readAndShift(steps: Int): Long =
    (0 until steps).foldRight(0L) {
      case (i, acc) =>
        val read = next()
        acc | ((read << (i * 8)) & 0xFF)
    }

  /** reads an Int from the stream. */
  private def nextInt(): Int =
    readAndShift(4).toInt

  /** Reads a pointer from the stream. */
  private def nextPointer(): Long =
    readAndShift(8)

  /** Reads a string from the stream. */
  private def nextString(): String = {
    val count = nextInt()
    val chars = Array.fill(count)(next().toChar)
    new String(chars)
  }

  /** Reads the next event from the stream, if any. Throws an exception at EOF. */
  private def nextEventUnsafe(): Event = {
    val tag = next().toInt
    tag match {
      case TAG_CALL =>
        Call(nextPointer(), nextInt())

      case TAG_LOAD =>
        Load(nextPointer())

      case TAG_STORE =>
        Store(nextPointer())

      case TAG_CLASSALLOC =>
        Classalloc(nextInt(), nextString())

      case TAG_METHOD =>
        Method(nextInt(), nextInt(), nextString())

      case TAG_AS =>
        As(nextInt(), nextInt(), nextPointer())

      case TAG_IS =>
        Is(nextInt(), nextInt(), nextPointer())

      case TAG_BOX =>
        Box(nextInt())

      case TAG_UNBOX =>
        Unbox(nextInt(), nextPointer())
    }
  }

  /** Optionally reads an event from the stream, if any. */
  def nextEvent(): Option[Event] =
    try Some(nextEventUnsafe())
    catch { case _: EOFException => None }

}

object LogParser {

  val TAG_CALL       = 1
  val TAG_LOAD       = 2
  val TAG_STORE      = 3
  val TAG_CLASSALLOC = 4
  val TAG_METHOD     = 5
  val TAG_AS         = 6
  val TAG_IS         = 7
  val TAG_BOX        = 8
  val TAG_UNBOX      = 9

  /** Returns a stream containing all the events in `file`. */
  def apply(file: File): Stream[Event] = {
    val stream = new BufferedInputStream(new FileInputStream(file))
    val parser = new LogParser(stream)

    def read(): Stream[Event] =
      parser.nextEvent() match {
        case Some(event) => event #:: read()
        case None        => Stream.empty
      }

    read()
  }
}
