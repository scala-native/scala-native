package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions
import SBTCompat._

object ScalaNativeCrossVersion {
  private final val ReleaseVersion =
    raw"""(\d+)\.(\d+)\.(\d+)""".r

  val currentBinaryVersion = binaryVersion(Versions.current)

  def binaryVersion(full: String): String = full match {
    case ReleaseVersion(major, _, _) => major
    case _                           => full
  }

  def scalaNativeMapped(cross: CrossVersion): CrossVersion =
    crossVersionAddPlatformPart(cross, "native" + currentBinaryVersion)

  val binary: CrossVersion = scalaNativeMapped(CrossVersion.binary)

  val full: CrossVersion = scalaNativeMapped(CrossVersion.full)
}
