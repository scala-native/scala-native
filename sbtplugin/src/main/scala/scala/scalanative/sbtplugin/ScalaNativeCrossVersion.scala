package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions

object ScalaNativeCrossVersion {

  val currentBinaryVersion = {
    val ReleaseVersion = raw"""(\d+)\.(\d+)\.(\d+)""".r

    def binaryVersion(full: String): String = full match {
      case ReleaseVersion(major, minor, _) => s"$major.$minor"
      case _                               => full
    }

    binaryVersion(Versions.current)
  }

  def scalaNativeMapped(cross: CrossVersion): CrossVersion = {
    val scalaNativeVersionUnmapped: String => String =
      _ => s"native$currentBinaryVersion"

    val scalaNativeVersionMap: String => String =
      version => s"native${currentBinaryVersion}_$version"

    cross match {
      case CrossVersion.Disabled =>
        CrossVersion.binaryMapped(scalaNativeVersionUnmapped)
      case cross: CrossVersion.Binary =>
        CrossVersion.binaryMapped(
          cross.remapVersion andThen scalaNativeVersionMap)
      case cross: CrossVersion.Full =>
        CrossVersion.fullMapped(
          cross.remapVersion andThen scalaNativeVersionMap)
    }
  }

  val binary: CrossVersion = scalaNativeMapped(CrossVersion.binary)

  val full: CrossVersion = scalaNativeMapped(CrossVersion.full)
}
