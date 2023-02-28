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

  /** Test for the target type
   *
   *  @return
   *    true if `msys`, false otherwise
   */
  lazy val isMsys: Boolean = target.endsWith("msys")

  /** Test for the target type
   *
   *  @return
   *    true if `cygnus`, false otherwise
   */
  lazy val isCygwin: Boolean = target.endsWith("cygnus")

  private lazy val target =
    System.getProperty("target.triple", "unknown").toLowerCase(Locale.ROOT)
}
