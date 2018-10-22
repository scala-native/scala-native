package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions
import scala.scalanative.build.Bits
import SBTCompat._

object ScalaNativeCrossVersion {
  private final val ReleaseVersion =
    raw"""(\d+)\.(\d+)\.(\d+)""".r

  val currentBinaryVersion = binaryVersion(Versions.current)

  def binaryVersion(full: String): String = full match {
    case ReleaseVersion(major, minor, _) => s"$major.$minor"
    case _                               => full
  }

  def scalaNativeMapped(cross: CrossVersion, bits: Bits): CrossVersion =
    crossVersionAddPlatformPart(
      crossVersionAddPlatformPart(cross, bits.toString),
      "native" + currentBinaryVersion
    )

  def binary(bits: Bits): CrossVersion =
    scalaNativeMapped(CrossVersion.binary, bits)

  def full(bits: Bits): CrossVersion =
    scalaNativeMapped(CrossVersion.full, bits)
}
