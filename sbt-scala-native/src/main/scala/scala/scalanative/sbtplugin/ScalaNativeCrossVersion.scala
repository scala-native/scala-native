package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions
import SBTCompat._

import scala.scalanative.build.{TargetArchitecture, x86_64, i386, ARM, ARM64}

object ScalaNativeCrossVersion {
  private final val ReleaseVersion =
    raw"""(\d+)\.(\d+)\.(\d+)""".r

  val currentBinaryVersion = binaryVersion(Versions.current)

  def binaryVersion(full: String): String = full match {
    case ReleaseVersion(major, minor, _) => s"$major.$minor"
    case _                               => full
  }

  def platformVersion(targetArchitecture: TargetArchitecture): String =
    targetArchitecture.toString

  // produces native{version}_{arch}
  def scalaNativeMapped(cross: CrossVersion,
                        targetArchitecture: TargetArchitecture): CrossVersion =
    crossVersionAddPlatformPart(
      crossVersionAddPlatformPart(cross, platformVersion(targetArchitecture)),
      "native" + currentBinaryVersion
    )

  def binary(targetArchitecture: TargetArchitecture): CrossVersion =
    scalaNativeMapped(CrossVersion.binary, targetArchitecture)

  def full(targetArchitecture: TargetArchitecture): CrossVersion =
    scalaNativeMapped(CrossVersion.full, targetArchitecture)
}
