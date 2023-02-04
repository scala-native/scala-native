package org.scalanative.testsuite.utils

// See also the scala.scalanative.runtime.Platform package.

import scala.scalanative.buildinfo.ScalaNativeBuildInfo

import scala.scalanative.runtime

object Platform {

  def scalaVersion: String = ScalaNativeBuildInfo.scalaVersion

  final val executingInJVM = false

  final val executingInScalaJS = false

  final val executingInScalaNative = true

  final val hasCompliantArrayIndexOutOfBounds = true

  final val executingInJVMOnJDK8OrLower = false
  final val executingInJVMOnLowerThenJDK11 = false
  final val executingInJVMOnLowerThanJDK15 = false
  final val executingInJVMOnLowerThanJDK17 = false
  final val executingInJVMOnJDK17 = false

  final val hasCompliantAsInstanceOfs = true

  private val osNameProp = System.getProperty("os.name")

  final val isFreeBSD = runtime.Platform.isFreeBSD()
  final val isLinux = runtime.Platform.isLinux()
  final val isMacOs = runtime.Platform.isMac()
  final val isWindows = runtime.Platform.isWindows()

  final val isArm64 = runtime.PlatformExt.isArm64
  final val is32BitPlatform = scala.scalanative.unsafe.is32BitPlatform
  final val asanEnabled = scala.scalanative.meta.LinktimeInfo.asanEnabled

  /* Scala Native has problem sending C signals on Apple arm64 hardware.
   * Hardware reporting in Scala Native is tricky. 'isArm64' reports true
   * when the process is running directly on 'bare metal' but _not_ when
   * the process is (Rosetta 2) translated running on arm64.
   *
   * The bug in question occurs in either case, so report lowest level
   * hardware.
   */

  final val hasArm64SignalQuirk =
    isArm64 || (runtime.Platform.probeMacX8664IsArm64() > 0)

  final val isMultithreadingEnabled =
    scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

}
