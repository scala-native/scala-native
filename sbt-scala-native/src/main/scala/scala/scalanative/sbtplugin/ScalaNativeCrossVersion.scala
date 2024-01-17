package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions

object ScalaNativeCrossVersion {
  val currentBinaryVersion = Versions.currentBinaryVersion

  private def crossVersionAddPlatformPart(
      cross: CrossVersion,
      part: String
  ): CrossVersion = {
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
    crossVersionAddPlatformPart(cross, "native" + Versions.currentBinaryVersion)

  val binary: CrossVersion = scalaNativeMapped(CrossVersion.binary)

  val full: CrossVersion = scalaNativeMapped(CrossVersion.full)
}
