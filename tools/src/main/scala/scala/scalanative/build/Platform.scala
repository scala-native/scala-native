package scala.scalanative.build

import java.util.Locale

object Platform {
  private lazy val osUsed =
    System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT)

  lazy val isWindows: Boolean = osUsed.startsWith("windows")
  lazy val isLinux: Boolean = osUsed.contains("linux")
  lazy val isUnix: Boolean = isLinux || osUsed.contains("unix")
  lazy val isMac: Boolean = osUsed.contains("mac")
  lazy val isFreeBSD: Boolean = osUsed.contains("freebsd")
}
