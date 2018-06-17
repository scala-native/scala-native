package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions
import SBTCompat._

import scala.scalanative.build.Bits

object ScalaNativeCrossVersion {
  private final val ReleaseVersion =
    raw"""(\d+)\.(\d+)\.(\d+)""".r

  val currentBinaryVersion = binaryVersion(Versions.current)

  def binaryVersion(full: String): String = full match {
    case ReleaseVersion(major, minor, _) => s"$major.$minor"
    case _                               => full
  }

  def platformVersion(targetBits: Bits): String =
    targetBits.toString

  // produces native{version}_{bits}
  def scalaNativeMapped(cross: CrossVersion, targetBits: Bits): CrossVersion =
    crossVersionAddPlatformPart(
      crossVersionAddPlatformPart(cross, platformVersion(targetBits)),
      "native" + currentBinaryVersion
    )

  def binary(targetBits: Bits): CrossVersion =
    scalaNativeMapped(CrossVersion.binary, targetBits)

  def full(targetBits: Bits): CrossVersion =
    scalaNativeMapped(CrossVersion.full, targetBits)
}
