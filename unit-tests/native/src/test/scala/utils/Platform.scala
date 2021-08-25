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

  final val isFreeBSD = System.getProperty("os.name").equals("FreeBSD")
}
