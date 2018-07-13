package java.lang

import java.io._
import java.util.{Collections, HashMap, Map, Properties}
import scala.scalanative.native._
import scala.scalanative.posix.unistd
import scala.scalanative.posix.sys.utsname._
import scala.scalanative.posix.sys.uname._
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
      sysProps.setProperty("user.home", getenv("HOME"))
      val buf = stackalloc[scala.Byte](1024)
      unistd.getcwd(buf, 1024) match {
        case null =>
        case b    => sysProps.setProperty("user.dir", fromCString(b))
      }
    }

    sysProps
  }

  var in: InputStream =
    new FileInputStream(FileDescriptor.in)
  var out: PrintStream =
    new PrintStream(new FileOutputStream(FileDescriptor.out))
  var err: PrintStream =
    new PrintStream(new FileOutputStream(FileDescriptor.err))

  private val systemProperties = loadProperties()
  Platform.setOSProps(
    CFunctionPtr.fromFunction2((key: CString, value: CString) => {
      systemProperties.setProperty(fromCString(key), fromCString(value));
    }))

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

  def setIn(in: InputStream): Unit =
    this.in = in

  def setOut(out: PrintStream): Unit =
    this.out = out

  def setErr(err: PrintStream): Unit =
    this.err = err

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

    val map               = new HashMap[String, String](size)
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
}
