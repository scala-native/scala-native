package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._

object ScalaNativeCrossVersion {
  val currentBinaryVersion = nir.Versions.currentBinaryVersion

  private[this] def crossVersionAddPlatformPart(
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

  private def scalaNativePrefixFromBinary(binaryVersion: String): String =
    "native" + binaryVersion

  private[scalanative] def scalaNativePrefix: String =
    scalaNativePrefixFromBinary(currentBinaryVersion)

  def scalaNativeMapped(cross: CrossVersion): CrossVersion =
    crossVersionAddPlatformPart(cross, scalaNativePrefix)

  val binary: CrossVersion = scalaNativeMapped(CrossVersion.binary)

  val full: CrossVersion = scalaNativeMapped(CrossVersion.full)
}
