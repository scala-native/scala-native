package scala.scalanative
package sbtplugin

// based Scala.js sbt plugin: ScalaJSCrossVersion

import sbt._
import scala.scalanative.nir.Versions

object ScalaNativeCrossVersion {
  private object FullVersion {
    final val FullVersionRE = """^(\d+)\.(\d+)\.(\d+)(-.*)?$""".r

    private def preRelease(s: String) = Option(s).map(_.stripPrefix("-"))

    def unapply(version: String): Option[(Int, Int, Int, Option[String])] = {
      version match {
        case FullVersionRE(major, minor, patch, preReleaseString) =>
          Some(
            (major.toInt,
             minor.toInt,
             patch.toInt,
             preRelease(preReleaseString)))
        case _ => None
      }
    }
  }

  val currentBinaryVersion = binaryVersion(Versions.current)

  def binaryVersion(full: String): String = full match {
    case FullVersion(0, minor, 0, Some(suffix)) => full
    case FullVersion(0, minor, _, _)            => s"0.$minor"
    case FullVersion(major, 0, 0, Some(suffix)) => s"$major.0-$suffix"
    case FullVersion(major, _, _, _)            => major.toString
  }

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

  def scalaNativeMapped(cross: CrossVersion): CrossVersion =
    crossVersionAddPlatformPart(cross, "native" + currentBinaryVersion)

  val binary: CrossVersion = scalaNativeMapped(CrossVersion.binary)

  val full: CrossVersion = scalaNativeMapped(CrossVersion.full)
}
