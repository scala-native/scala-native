package org.scalanative.testsuite.utils

import java.util.Locale
// Ported from Scala.js

object Platform {

  final val executingInJVM = true

  final val executingInScalaJS = false

  final val executingInScalaNative = false

  final val hasCompliantArrayIndexOutOfBounds = true

  final val executingInJVMOnJDK8OrLower = jdkVersion <= 8

  private lazy val jdkVersion = {
    val v = System.getProperty("java.version")
    if (v.startsWith("1.")) Integer.parseInt(v.drop(2).takeWhile(_.isDigit))
    else Integer.parseInt(v.takeWhile(_.isDigit))
  }

  final val hasCompliantAsInstanceOfs = true

  private val osNameProp = System.getProperty("os.name")
  final val isFreeBSD = osNameProp.equals("FreeBSD")
  final val isWindows = osNameProp.toLowerCase.startsWith("windows")
  final val isMacOs = osNameProp.toLowerCase.contains("mac")

  private val osArch = System.getProperty("os.arch").toLowerCase(Locale.ROOT)
  final val isArm64 = {
    osArch == "arm64" || osArch == "aarch64"
  }
}
