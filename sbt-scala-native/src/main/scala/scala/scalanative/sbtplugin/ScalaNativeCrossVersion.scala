package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions

object ScalaNativeCrossVersion {
  private final val ReleaseVersion =
    raw"""(\d+)\.(\d+)\.(\d+)""".r

  val currentBinaryVersion = binaryVersion(Versions.current)

  def binaryVersion(full: String): String = full match {
    case ReleaseVersion(major, minor, _) => s"$major.$minor"
    case _                               => full
  }

  private[this] def crossVersionAddPlatformPart(cross: CrossVersion,
                                                part: String): CrossVersion = {
    cross match {
      // .Disabled reference https://github.com/sbt/librarymanagement/pull/316
      case _: sbt.librarymanagement.Disabled =>
        CrossVersion.constant(part)
      case cross: sbt.librarymanagement.Constant =>
        cross.withValue(part + "_" + cross.value)
      case cross: CrossVersion.Binary =>
        cross.withPrefix(part + "_" + cross.prefix)
      case cross: CrossVersion.Full =>
        cross.withPrefix(part + "_" + cross.prefix)
    }
  }

  def scalaNativeMapped(cross: CrossVersion): CrossVersion =
    crossVersionAddPlatformPart(cross, "native" + currentBinaryVersion)

  val binary: CrossVersion = scalaNativeMapped(CrossVersion.binary)

  val full: CrossVersion = scalaNativeMapped(CrossVersion.full)
}
