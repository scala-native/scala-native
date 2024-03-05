package java.lang

import java.io._
import java.nio.charset.StandardCharsets
import java.util.{Collections, HashMap, Map, Properties, WindowsHelperMethods}
import scala.scalanative.posix.pwdOps._
import scala.scalanative.posix.{pwd, unistd}
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.runtime.{GC, Intrinsics, Platform}
import scala.scalanative.ffi.time
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt.MAX_PATH
import scala.scalanative.windows.UserEnvApi._
import scala.scalanative.windows.WinBaseApi._
import scala.scalanative.windows.ProcessEnvApi._
import scala.scalanative.windows.winnt.AccessToken
import scala.scalanative.windows.WinNlsApi._

final class System private ()

object System {
  import SystemProperties.systemProperties
  import EnvVars.envVars

  def arraycopy(
      src: Object,
      srcPos: scala.Int,
      dest: Object,
      destPos: scala.Int,
      length: scala.Int
  ): Unit =
    scalanative.runtime.Array.copy(src, srcPos, dest, destPos, length)

  def exit(status: Int): Unit =
    Runtime.getRuntime().exit(status)

  def identityHashCode(x: Object): scala.Int =
    java.lang.Long
      .hashCode(Intrinsics.castRawPtrToLong(Intrinsics.castObjectToRawPtr(x)))

  def lineSeparator(): String = {
    if (isWindows) "\r\n"
    else "\n"
  }

  // Custom accessor instead of vars
  def in: InputStream = Streams.in
  def in_=(v: InputStream): Unit = Streams.in = v

  def out: PrintStream = Streams.out
  def out_=(v: PrintStream) = Streams.out = v

  def err: PrintStream = Streams.err
  def err_=(v: PrintStream) = Streams.err = v

  def getProperties(): Properties = systemProperties

  def clearProperty(key: String): String =
    systemProperties.remove(key).asInstanceOf[String]

  def getProperty(key: String): String =
    systemProperties.getProperty(key)

  def getProperty(key: String, default: String): String =
    systemProperties.getProperty(key, default)

  def setProperty(key: String, value: String): String =
    systemProperties.setProperty(key, value).asInstanceOf[String]

  def nanoTime(): scala.Long = time.scalanative_nano_time()
  def currentTimeMillis(): scala.Long = time.scalanative_current_time_millis()

  def getenv(): Map[String, String] = envVars
  def getenv(key: String): String = envVars.get(key.toUpperCase())

  def setIn(in: InputStream): Unit =
    this.in = in

  def setOut(out: PrintStream): Unit =
    this.out = out

  def setErr(err: PrintStream): Unit =
    this.err = err

  def gc(): Unit = GC.collect()
}

// Extract mutable fields to custom object allowing to skip allocations of unused features
private object Streams {
  import FileDescriptor.{in => stdin, out => stdout, err => stderr}
  var in: InputStream = new FileInputStream(stdin)
  var out: PrintStream = new PrintStream(new FileOutputStream(stdout))
  var err: PrintStream = new PrintStream(new FileOutputStream(stderr))
}

private object SystemProperties {
  import System.{lineSeparator, getenv}

  private val systemProperties0 = loadProperties()
  val systemProperties = {
    Platform.setOSProps { (key: CString, value: CString) =>
      systemProperties0.setProperty(fromCString(key), fromCString(value))
      ()
    }
    systemProperties0
  }

  private def loadProperties() = {
    val sysProps = new Properties()
    sysProps.setProperty("java.version", "1.8")
    sysProps.setProperty("java.vm.specification.version", "1.8")
    sysProps.setProperty("java.vm.specification.vendor", "Oracle Corporation")
    sysProps.setProperty(
      "java.vm.specification.name",
      "Java Virtual Machine Specification"
    )
    sysProps.setProperty("java.vm.name", "Scala Native")
    sysProps.setProperty("java.specification.version", "1.8")
    sysProps.setProperty("java.specification.vendor", "Oracle Corporation")
    sysProps.setProperty(
      "java.specification.name",
      "Java Platform API Specification"
    )
    sysProps.setProperty("line.separator", System.lineSeparator())
    getCurrentDirectory().foreach(sysProps.setProperty("user.dir", _))
    getUserHomeDirectory().foreach(sysProps.setProperty("user.home", _))
    getUserCountry().foreach(sysProps.setProperty("user.country", _))
    getUserLanguage().foreach(sysProps.setProperty("user.language", _))

    if (isWindows) {
      sysProps.setProperty("file.separator", "\\")
      sysProps.setProperty("path.separator", ";")
      sysProps.setProperty(
        "java.io.tmpdir", {
          val buffer: Ptr[scala.Byte] = stackalloc[scala.Byte](MAX_PATH)
          GetTempPathA(MAX_PATH, buffer)
          fromCString(buffer)
        }
      )
    } else {
      sysProps.setProperty("file.separator", "/")
      sysProps.setProperty("path.separator", ":")
      // MacOS uses TMPDIR to specify tmp directory, other formats are also used in the Unix system
      def env(name: String): Option[String] = Option(getenv(name))
      val tmpDirectory = env("TMPDIR")
        .orElse(env("TEMPDIR"))
        .orElse(env("TMP"))
        .orElse(env("TEMP"))
        .getOrElse("/tmp")
      sysProps.setProperty("java.io.tmpdir", tmpDirectory)
    }

    sysProps
  }
  private def getCurrentDirectory(): Option[String] = {
    val bufSize = 1024.toUInt
    if (isWindows) {
      val buf: Ptr[CChar16] = stackalloc[CChar16](bufSize)
      if (GetCurrentDirectoryW(bufSize, buf) != 0)
        Some(fromCWideString(buf, StandardCharsets.UTF_16LE))
      else None
    } else {
      val buf: Ptr[scala.Byte] = stackalloc[scala.Byte](bufSize)
      val cwd = unistd.getcwd(buf, bufSize)
      Option(cwd).map(fromCString(_))
    }
  }

  private def getUserHomeDirectory(): Option[String] = {
    if (isWindows) {
      WindowsHelperMethods.withUserToken(AccessToken.TOKEN_QUERY) { token =>
        val bufSize = stackalloc[UInt]()
        !bufSize = 256.toUInt
        val buf: Ptr[CChar16] = stackalloc[CChar16](!bufSize)
        if (GetUserProfileDirectoryW(token, buf, bufSize))
          Some(fromCWideString(buf, StandardCharsets.UTF_16LE))
        else None
      }
    } else {
      val buf = stackalloc[pwd.passwd]()
      val uid = unistd.getuid()
      val res = pwd.getpwuid(uid, buf)
      if (res == 0 && buf.pw_dir != null)
        Some(fromCString(buf.pw_dir))
      else None
    }
  }

  private def getUserLocaleInfo(
      infoCode: LCType,
      bufSize: UInt
  ): Option[String] = {
    val buf: Ptr[CChar16] = stackalloc[CChar16](bufSize)
    GetLocaleInfoEx(
      LOCALE_NAME_USER_DEFAULT,
      infoCode,
      buf,
      bufSize
    ) match {
      case 0 => None
      case _ => Some(fromCWideString(buf, StandardCharsets.UTF_16))
    }
  }

  private def getUserLanguage(): Option[String] = {
    if (isWindows) {
      getUserLocaleInfo(LOCALE_SISO639LANGNAME2, bufSize = 9.toUInt)
    } else {
      Option(getenv("LANG")).map(_.takeWhile(_ != '_'))
    }
  }

  private def getUserCountry(): Option[String] = {
    if (isWindows) {
      getUserLocaleInfo(LOCALE_SISO3166CTRYNAME2, bufSize = 9.toUInt)
    } else {
      Option(getenv("LANG")).map(
        _.dropWhile(_ != '_')
          .takeWhile(c => (c != '.') && (c != '@'))
          .drop(1)
      )
    }
  }
}

private object EnvVars {
  val envVars: Map[String, String] = {
    def getEnvsUnix() = {
      val map = new HashMap[String, String]()
      val ptr: Ptr[CString] = unistd.environ
      var i = 0
      while (ptr(i) != null) {
        val variable = fromCString(ptr(i))
        val name = variable.takeWhile(_ != '=')
        val value =
          if (name.length < variable.length)
            variable.substring(name.length + 1, variable.length)
          else
            ""
        map.put(name, value)
        i += 1
      }
      map
    }

    def getEnvsWindows(): Map[String, String] = {
      val envsMap = new HashMap[String, String]()
      val envBlockHead = GetEnvironmentStringsW()

      var blockPtr = envBlockHead
      var env: String = null

      while ({
        env = fromCWideString(blockPtr, StandardCharsets.UTF_16LE)
        env != null && env.nonEmpty
      }) {
        blockPtr += env.size + 1
        /// Some Windows internal variables start with =
        val eqIdx = env.indexOf('=', 1)
        // Env variables in Windows are case insenstive - normalize them
        val name = env.substring(0, eqIdx).toUpperCase()
        val value = env.substring(eqIdx + 1)
        envsMap.put(name, value)
      }
      FreeEnvironmentStringsW(envBlockHead)
      envsMap
    }

    Collections.unmodifiableMap {
      if (isWindows) getEnvsWindows()
      else getEnvsUnix()
    }
  }
}
