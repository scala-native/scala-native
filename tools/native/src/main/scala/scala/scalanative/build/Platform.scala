package scala.scalanative.build

import scala.scalanative.meta.LinktimeInfo

/** Utility methods indicating the platform type */
private[scala] object Platform {
  final val isJVM = false

  /** Test for the platform type
   *
   *  @return
   *    true if `Windows`, false otherwise
   */
  lazy val isWindows: Boolean = LinktimeInfo.isWindows

  /** Test for the platform type
   *
   *  @return
   *    true if `Linux`, false otherwise
   */
  lazy val isLinux: Boolean = LinktimeInfo.isLinux

  /** Test for the platform type
   *
   *  @return
   *    true if `UNIX`, false otherwise
   */
  lazy val isUnix: Boolean =
    LinktimeInfo.isLinux || LinktimeInfo.target.os.contains("unix")

  /** Test for the platform type
   *
   *  @return
   *    true if `macOS`, false otherwise
   */
  lazy val isMac: Boolean = LinktimeInfo.isMac

  /** Test for the platform type
   *
   *  @return
   *    true if `FreeBSD`, false otherwise
   */
  lazy val isFreeBSD: Boolean = LinktimeInfo.isFreeBSD

  /** Test for the platform type
   *
   *  @return
   *    true if `OpenBSD`, false otherwise
   */
  lazy val isOpenBSD: Boolean = LinktimeInfo.isOpenBSD

  /** Test for the platform type
   *
   *  @return
   *    true if `NetBSD`, false otherwise
   */
  lazy val isNetBSD: Boolean = LinktimeInfo.isNetBSD

  /** Test for the target type
   *
   *  @return
   *    true if `msys`, false otherwise
   */
  lazy val isMsys: Boolean = LinktimeInfo.target.os.contains("msys")

  /** Test for the target type
   *
   *  @return
   *    true if `cygnus`, false otherwise
   */
  lazy val isCygwin: Boolean = LinktimeInfo.target.os.contains("cygnus")

}
