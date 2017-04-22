package java.lang

import java.io.{InputStream, PrintStream, IOException}
import java.util.{Collections, HashMap, Map, Properties}
import scala.scalanative.native._
import scala.scalanative.posix.unistd
import scala.scalanative.posix.errno.EINTR
import scala.scalanative.runtime.time
import scala.scalanative.runtime.Platform
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

  private def loadProperties() = {
    val sysProps = new Properties()
    sysProps.setProperty("java.version", "1.8")
    sysProps.setProperty("java.vm.specification.version", "1.8")
    sysProps.setProperty("java.vm.specification.vendor", "Oracle Corporation")
    sysProps.setProperty("java.vm.specification.name",
                         "Java Virtual Machine Specification")
    sysProps.setProperty("java.vm.name", "Scala Native")
    sysProps.setProperty("java.specification.version", "1.8")
    sysProps.setProperty("java.specification.vendor", "Oracle Corporation")
    sysProps.setProperty("java.specification.name",
                         "Java Platform API Specification")
    sysProps.setProperty("line.separator", lineSeparator())

    if (Platform.isWindows) {
      sysProps.setProperty("file.separator", "\\")
      sysProps.setProperty("path.separator", ";")
      val userLang    = fromCString(Platform.windowsGetUserLang())
      val userCountry = fromCString(Platform.windowsGetUserCountry())
      sysProps.setProperty("user.language", userLang)
      sysProps.setProperty("user.country", userCountry)

    } else {
      sysProps.setProperty("file.separator", "/")
      sysProps.setProperty("path.separator", ":")
      val userLocale = getenv("LANG")
      if (userLocale != null) {
        val userLang = userLocale.takeWhile(_ != '_')
        // this mess will be updated when Regexes get implemented
        val userCountry = userLocale
          .dropWhile(_ != '_')
          .takeWhile(c => (c != '.') && (c != '@'))
          .drop(1)
        sysProps.setProperty("user.language", userLang)
        sysProps.setProperty("user.country", userCountry)
      }
    }

    sysProps
  }

  private var systemProperties = loadProperties()

  def lineSeparator(): String = {
    if (Platform.isWindows) "\r\n"
    else "\n"
  }

  def getProperties(): Properties = systemProperties

  def clearProperty(key: String): String =
    systemProperties.remove(key).asInstanceOf[String]

  def getProperty(key: String): String =
    systemProperties.getProperty(key)

  def getProperty(key: String, default: String): String =
    systemProperties.getProperty(key, default)

  def setProperty(key: String, value: String): String =
    systemProperties.setProperty(key, value).asInstanceOf[String]

  def nanoTime(): scala.Long          = time.scalanative_nano_time
  def currentTimeMillis(): scala.Long = time.scalanative_current_time_millis

  def getenv(): Map[String, String] = envVars
  def getenv(key: String): String   = envVars.get(key)

  var in: InputStream  = new CStdinStream()
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

  private class CStdinStream() extends java.io.InputStream {
    private def readAndRetry(fd: CInt,
                             buf: Ptr[scala.Byte],
                             count: CSize): CSize = {
      var nread: CSize = -1
      do {
        nread = unistd.read(unistd.STDIN_FILENO, buf, count)
        if (nread == -1 && errno.errno != EINTR)
          throw new IOException("Error on reading stdin")
      } while (nread == -1)
      nread
    }

    def read(): CInt = {
      val buffer = stackalloc[scala.Byte](1)
      var nread  = readAndRetry(unistd.STDIN_FILENO, buffer, 1)

      if (nread != 0) buffer(0).toInt else -1
    }

    override def read(b: Array[scala.Byte], off: Int, len: Int): Int = {
      // Stdin is line buffered, so there is no need to reserve a too big buffer
      val bufsize = len min 128
      val buffer  = stackalloc[scala.Byte](bufsize)
      val nread   = readAndRetry(unistd.STDIN_FILENO, buffer, bufsize)

      if (nread != 0) {
        var i = 0
        while (i < nread) {
          b(off + i) = buffer(i)
          i += 1
        }
        nread.toInt
      } else -1
    }
  }
}
