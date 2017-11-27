package scala.scalanative
package sbtplugin

import sbt._, Keys._
import scala.sys.process.{ProcessBuilder, ProcessLogger}
import scala.language.implicitConversions

// Ported from Scala.js
// https://github.com/scala-js/scala-js/blob/v0.6.20/sbt-plugin/src/main/scala-sbt-0.13/org/scalajs/sbtplugin/SBTCompat.scala#L1

private[sbtplugin] object SBTCompat {
  def crossVersionAddPlatformPart(cross: CrossVersion,
                                  part: String): CrossVersion = {
    cross match {
      case CrossVersion.Disabled =>
        CrossVersion.binaryMapped(_ => part)
      case cross: CrossVersion.Binary =>
        CrossVersion.binaryMapped(cross.remapVersion.andThen(part + "_" + _))
      case cross: CrossVersion.Full =>
        CrossVersion.fullMapped(cross.remapVersion.andThen(part + "_" + _))
    }
  }

  implicit class RichProcessBuilder(pb: ProcessBuilder) {
    def lineStream_!(logger: ProcessLogger) = pb.lines_!(logger)
    def lineStream_! : Stream[String]       = pb.lines_!
  }

  implicit def promoteProcessLogger(logger: sbt.Logger): ProcessLogger =
    ProcessLogger(msg => logger.info(msg), msg => logger.error(msg))
}
