package java.lang

import java.io._
import java.nio.charset.StandardCharsets
import java.util.{Collections, HashMap, Map, Properties, WindowsHelperMethods}
import scala.scalanative.posix.pwdOps._
import scala.scalanative.posix.{pwd, unistd}
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.runtime.{GC, Intrinsics, Platform, time}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt.MAX_PATH
import scala.scalanative.windows.UserEnvApi._
import scala.scalanative.windows.WinBaseApi._
import scala.scalanative.windows.ProcessEnvApi._
import scala.scalanative.windows.winnt.AccessToken

final class System private ()

object System {
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
    sysProps.setProperty("line.separator", lineSeparator())
    getCurrentDirectory().map(sysProps.setProperty("user.dir", _))
    getUserHomeDirectory().map(sysProps.setProperty("user.home", _))

    if (isWindows) {
      sysProps.setProperty("file.separator", "\\")
      sysProps.setProperty("path.separator", ";")
      sysProps.setProperty(
        "java.io.tmpdir", {
          val buffer = stackalloc[scala.Byte](MAX_PATH)
          GetTempPathA(MAX_PATH, buffer)
          fromCString(buffer)
        }
      )

      val userLang = fromCString(Platform.windowsGetUserLang())
      val userCountry = fromCString(Platform.windowsGetUserCountry())
      sysProps.setProperty("user.language", userLang)
      sysProps.setProperty("user.country", userCountry)

    } else {
      sysProps.setProperty("file.separator", "/")
      sysProps.setProperty("path.separator", ":")
      sysProps.setProperty("java.io.tmpdir", "/tmp")
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

  var in: InputStream =
    new FileInputStream(FileDescriptor.in)
  var out: PrintStream =
    new PrintStream(new FileOutputStream(FileDescriptor.out))
  var err: PrintStream =
    new PrintStream(new FileOutputStream(FileDescriptor.err))

  private val systemProperties = loadProperties()
  Platform.setOSProps { (key: CString, value: CString) =>
    val _ = systemProperties.setProperty(fromCString(key), fromCString(value))
  }

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

  def nanoTime(): scala.Long = time.scalanative_nano_time
  def currentTimeMillis(): scala.Long = time.scalanative_current_time_millis

  def getenv(): Map[String, String] = envVars
  def getenv(key: String): String = envVars.get(key)

  def setIn(in: InputStream): Unit =
    this.in = in

  def setOut(out: PrintStream): Unit =
    this.out = out

  def setErr(err: PrintStream): Unit =
    this.err = err

  def gc(): Unit = GC.collect()

  private def getCurrentDirectory(): Option[String] = {
    val bufSize = 1024.toUInt
    if (isWindows) {
      val buf = stackalloc[CChar16](bufSize)
      if (GetCurrentDirectoryW(bufSize, buf) != 0.toUInt)
        Some(fromCWideString(buf, StandardCharsets.UTF_16LE))
      else None
    } else {
      val buf = stackalloc[scala.Byte](bufSize)
      val cwd = unistd.getcwd(buf, bufSize)
      Option(cwd).map(fromCString(_))
    }
  }

  private def getUserHomeDirectory(): Option[String] = {
    if (isWindows) {
      WindowsHelperMethods.withUserToken(AccessToken.TOKEN_QUERY) { token =>
        val bufSize = stackalloc[UInt]
        !bufSize = 256.toUInt
        val buf = stackalloc[CChar16](!bufSize)
        if (GetUserProfileDirectoryW(token, buf, bufSize))
          Some(fromCWideString(buf, StandardCharsets.UTF_16LE))
        else None
      }
    } else {
      val buf = stackalloc[pwd.passwd]
      val uid = unistd.getuid()
      val res = pwd.getpwuid(uid, buf)
      if (res == 0 && buf.pw_dir != null)
        Some(fromCString(buf.pw_dir))
      else None
    }
  }

  private lazy val envVars: Map[String, String] = {
    def getEnvsUnix() = {
      // workaround since `while(ptr(0) != null)` causes segfault
      def isDefined(ptr: Ptr[CString]): Boolean = {
        val s: CString = ptr(0)
        s != null
      }

      // Count to preallocate the map
      var size = 0
      var sizePtr = unistd.environ
      while (isDefined(sizePtr)) {
        size += 1
        sizePtr += 1
      }

      val map = new HashMap[String, String](10)
      var ptr: Ptr[CString] = unistd.environ
      while (isDefined(ptr)) {
        val variable = fromCString(ptr(0))
        val name = variable.takeWhile(_ != '=')
        val value =
          if (name.length < variable.length)
            variable.substring(name.length + 1, variable.length)
          else
            ""
        map.put(name, value)
        ptr = ptr + 1
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
