package org.scalanative.testsuite.utils

// See also the scala.scalanative.runtime.Platform package.

import scala.scalanative.buildinfo.ScalaNativeBuildInfo

object Platform {

  def scalaVersion: String = ScalaNativeBuildInfo.scalaVersion

  final val executingInJVM = false

  final val executingInScalaJS = false

  final val executingInScalaNative = true

  final val hasCompliantArrayIndexOutOfBounds = true

  final val executingInJVMOnJDK8OrLower = false

  final val hasCompliantAsInstanceOfs = true

  private val osNameProp = System.getProperty("os.name")
  final val isFreeBSD = osNameProp.equals("FreeBSD")
  final val isWindows = osNameProp.toLowerCase.startsWith("windows")
  final val isMacOs = osNameProp.toLowerCase.contains("mac")

  final val isArm64 = scalanative.runtime.PlatformExt.isArm64
}
