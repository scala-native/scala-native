package scala.scalanative
package sbtplugin

import sbt._

// https://github.com/scala-js/scala-js/blob/v0.6.20/sbt-plugin/src/main/scala-sbt-1.0/org/scalajs/sbtplugin/SBTCompat.scala

private[sbtplugin] object SBTCompat {

  def crossVersionAddPlatformPart(cross: CrossVersion,
                                  part: String): CrossVersion = {
    cross match {
      case CrossVersion.Disabled =>
        CrossVersion.constant(part)
      case cross: sbt.librarymanagement.Constant =>
        cross.withValue(part + "_" + cross.value)
      case cross: CrossVersion.Binary =>
        cross.withPrefix(part + "_" + cross.prefix)
      case cross: CrossVersion.Full =>
        cross.withPrefix(part + "_" + cross.prefix)
    }
  }
}
