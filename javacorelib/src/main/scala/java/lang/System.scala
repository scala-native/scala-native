package java.lang

import java.io._
import java.util.{Collections, HashMap, Map, Properties}
import scala.scalanative.unsafe._
import scala.scalanative.runtime.{time, Platform, GC, Intrinsics}

final class System private ()

object System {
  def arraycopy(src: Object,
                srcPos: scala.Int,
                dest: Object,
                destPos: scala.Int,
                length: scala.Int): Unit =
    scalanative.runtime.Array.copy(src, srcPos, dest, destPos, length)

  def exit(status: Int): Unit =
    Runtime.getRuntime().exit(status)

  def identityHashCode(x: Object): scala.Int =
    java.lang.Long
      .hashCode(Intrinsics.castRawPtrToLong(Intrinsics.castObjectToRawPtr(x)))

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

    if (Platform.isWindows()) {
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
      locally {
        val buf = stackalloc[pwd.passwd]
        val uid = unistd.getuid()
        val res = pwd.getpwuid(uid, buf)
        if (res == 0 && buf.pw_dir != null) {
          sysProps.setProperty("user.home", fromCString(buf.pw_dir))
        }
      }
      locally {
        val bufSize = 1024
        val buf     = stackalloc[scala.Byte](bufSize)
        val cwd     = unistd.getcwd(buf, bufSize)
        if (cwd != null) {
          sysProps.setProperty("user.dir", fromCString(cwd))
        }
      }
    }

    sysProps
  }

  var in: InputStream  = SystemImpl.std_in()
  var out: PrintStream = new PrintStream(SystemImpl.std_out())
  var err: PrintStream = new PrintStream(SystemImpl.std_err())

  private val systemProperties = loadProperties()
  Platform.setOSProps(new CFuncPtr2[CString, CString, Unit] {
    def apply(key: CString, value: CString): Unit =
      systemProperties.setProperty(fromCString(key), fromCString(value))
  })

  def lineSeparator(): String = {
    if (Platform.isWindows()) "\r\n"
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

  private lazy val envVars: Map[String, String] = SystemImpl.loadAllEnv()
}

private object SystemImpl {
  val envmap = new HashMap[String, String](Platform.getAllEnv(null))
  def loadAllEnv(): Map[String, String] = {
    Platform.getAllEnv(new CFuncPtr2[CString, CString, Unit] {
      def apply(key: CString, value: CString): Unit =
        envmap.put(fromCString(key), fromCString(value))
    })
    Collections.unmodifiableMap(envmap)
  }

  import scala.scalanative.libc.stdio
  import scala.scalanative.runtime

  def std_in(): InputStream = new InputStream {
    def read(): Int = {
      stdio.fgetc(stdio.stdin)
    }
    def read(b: Array[Byte], off: Int, len: Int): Int = {
      val buf = b.asInstanceOf[runtime.ByteArray].at(off)
      stdio.fread(buf, len, 1, stdio.stdin).toInt
    }
  }

  def std_out(): OutputStream = new OutputStream {
    def write(b: Int): Unit = {
      stdio.fputc(b, stdio.stdout)
    }
    def write(b: Array[Byte], off: Int, len: Int): Unit = {
      val buf = b.asInstanceOf[runtime.ByteArray].at(off)
      stdio.fwrite(buf, len, 1, stdio.stdout).toInt
    }
  }

  def std_err(): OutputStream = new OutputStream {
    def write(b: Int): Unit = {
      stdio.fputc(b, stdio.stderr)
    }
    def write(b: Array[Byte], off: Int, len: Int): Unit = {
      val buf = b.asInstanceOf[runtime.ByteArray].at(off)
      stdio.fwrite(buf, len, 1, stdio.stderr)
    }
  }
}
