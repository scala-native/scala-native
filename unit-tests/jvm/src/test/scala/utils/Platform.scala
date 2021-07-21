package org.scalanative.testsuite.utils

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

  final val isFreeBSD = System.getProperty("os.name").equals("FreeBSD")
}
