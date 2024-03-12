package scala.scalanative.build

import java.util.Locale

/** Utility methods indicating the platform type */
private[scala] object Platform {
  final val isJVM = true

  private lazy val osUsed =
    System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT)

  /** Test for the platform type
   *
   *  @return
   *    true if `Windows`, false otherwise
   */
  lazy val isWindows: Boolean = osUsed.startsWith("windows")

  /** Test for the platform type
   *
   *  @return
   *    true if `Linux`, false otherwise
   */
  lazy val isLinux: Boolean = osUsed.contains("linux")

  /** Test for the platform type
   *
   *  @return
   *    true if `UNIX`, false otherwise
   */
  lazy val isUnix: Boolean = isLinux || osUsed.contains("unix")

  /** Test for the platform type
   *
   *  @return
   *    true if `macOS`, false otherwise
   */
  lazy val isMac: Boolean = osUsed.contains("mac")

  /** Test for the platform type
   *
   *  @return
   *    true if `FreeBSD`, false otherwise
   */
  lazy val isFreeBSD: Boolean = osUsed.contains("freebsd")

  /** Test for the platform type
   *
   *  @return
   *    true if `OpenBSD`, false otherwise
   */
  lazy val isOpenBSD: Boolean = osUsed.contains("openbsd")

  /** Test for the platform type
   *
   *  @return
   *    true if `OpenBSD`, false otherwise
   */
  lazy val isNetBSD: Boolean = osUsed.contains("netbsd")

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
