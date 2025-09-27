package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._

object ScalaNativeCrossVersion {

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

  def scalaNativeSuffix(version: String): String =
    "native" + nir.Versions.binaryVersion(version)

  def scalaNativeMapped(cross: CrossVersion, version: String): CrossVersion =
    crossVersionAddPlatformPart(cross, scalaNativeSuffix(version))

  def binary(version: String): CrossVersion =
    scalaNativeMapped(CrossVersion.binary, version)

}
