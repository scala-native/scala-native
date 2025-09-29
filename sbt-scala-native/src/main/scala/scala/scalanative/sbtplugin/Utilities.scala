package scala.scalanative
package sbtplugin

import sbt._

import java.lang.System.{lineSeparator => nl}

import scala.language.postfixOps

object Utilities {
  implicit class RichLogger(logger: Logger) {
    def toLogger: build.Logger =
      build.Logger(
        logger.trace(_),
        logger.debug(_),
        logger.info(_),
        logger.warn(_),
        logger.error(_)
      )

    def running(command: Seq[String]): Unit =
      logger.debug("Running" + nl + command.mkString(nl + "\t"))
  }
}
