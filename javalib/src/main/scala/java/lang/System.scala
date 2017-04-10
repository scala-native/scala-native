package java.lang

import java.io.{InputStream, PrintStream}
import java.util.{Collections, HashMap, Map, Properties}
import scala.scalanative.native._
import scala.scalanative.posix._
import scala.scalanative.runtime.time
import scala.scalanative.runtime.GC

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

  def clearProperty(key: String): String                = ???
  def getProperties(): Properties                       = ???
  def getProperty(key: String): String                  = ???
  def getProperty(key: String, default: String): String = ???
  def setProperty(key: String, value: String): String   = ???

  def nanoTime(): scala.Long          = time.scalanative_nano_time
  def currentTimeMillis(): scala.Long = time.scalanative_current_time_millis

  def getenv(): Map[String, String] = envVars
  def getenv(key: String): String   = envVars.get(key)

  var in: InputStream  = _
  var out: PrintStream = new PrintStream(new CFileOutputStream(stdio.stdout))
  var err: PrintStream = new PrintStream(new CFileOutputStream(stdio.stderr))

  def gc(): Unit = GC.collect()

  private lazy val envVars: Map[String, String] = {
    // workaround since `while(ptr(0) != null)` causes segfault
    def isDefined(ptr: Ptr[CString]): Boolean = {
      val s: CString = ptr(0)
      s != null
    }

    // Count to preallocate the map
    var size    = 0
    var sizePtr = unistd.environ
    while (isDefined(sizePtr)) {
      size += 1
      sizePtr += 1
    }

    val map               = new java.util.HashMap[String, String](size)
    var ptr: Ptr[CString] = unistd.environ
    while (isDefined(ptr)) {
      val variable = fromCString(ptr(0))
      val name     = variable.takeWhile(_ != '=')
      val value =
        if (name.length < variable.length)
          variable.substring(name.length + 1, variable.length)
        else
          ""
      map.put(name, value)
      ptr = ptr + 1
    }

    Collections.unmodifiableMap(map)
  }

  private class CFileOutputStream(stream: Ptr[stdio.FILE])
      extends java.io.OutputStream {
    private val buf = stdlib.malloc(1)
    def write(b: Int): Unit = {
      !buf = b.toUByte.toByte
      stdio.fwrite(buf, 1, 1, stream)
    }
  }
}
