package java.lang

import java.io.{InputStream, PrintStream}
import java.util.Properties
import scala.scalanative.native._
import scala.scalanative.runtime.time

final class System private ()

object System {
  def arraycopy(src: Object,
                srcPos: scala.Int,
                dest: Object,
                destPos: scala.Int,
                length: scala.Int): Unit = {
    scalanative.runtime.Array.copy(src, srcPos, dest, destPos, length)
  }

  def exit(status: Int): Unit = {
    Runtime.getRuntime().exit(status)
  }

  def identityHashCode(x: Object): scala.Int =
    x.cast[Word].hashCode

  def getenv(name: String): String                      = ???
  def clearProperty(key: String): String                = ???
  def getProperties(): Properties                       = ???
  def getProperty(key: String): String                  = ???
  def getProperty(key: String, default: String): String = ???
  def setProperty(key: String, value: String): String   = ???

  def nanoTime(): CLong = time.scalanative_nano_time

  var in: InputStream  = _
  var out: PrintStream = new PrintStream(new CFileOutputStream(stdio.stdout))
  var err: PrintStream = new PrintStream(new CFileOutputStream(stdio.stderr))

  private class CFileOutputStream(stream: Ptr[stdio.FILE])
      extends java.io.OutputStream {
    private val buf = stdlib.malloc(1).cast[Ptr[UByte]]
    def write(b: Int): Unit = {
      !buf = b.toUByte
      stdio.fwrite(buf, 1, 1, stream)
    }
  }
}
