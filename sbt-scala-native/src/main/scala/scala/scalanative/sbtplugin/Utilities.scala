package scala.scalanative
package sbtplugin

import java.lang.System.{lineSeparator => nl}

import scala.language.postfixOps

import sbt._

import scalanative.sbtplugin.SBTCompat.{Process, ProcessLogger, _}

object Utilities {

  object SilentLogger extends ProcessLogger {
    def info(s: => String): Unit  = ()
    def error(s: => String): Unit = ()
    def buffer[T](f: => T): T     = f
  }

  implicit class RichFile(file: File) {
    def abs: String = file.getAbsolutePath
  }

  implicit class RichLogger(logger: Logger) {
    def time[T](msg: String)(f: => T): T = {
      import java.lang.System.nanoTime
      val start = nanoTime()
      val res   = f
      val end   = nanoTime()
      logger.info(s"$msg (${(end - start) / 1000000} ms)")
      res
    }

    def toLogger: build.Logger =
      build.Logger(logger.debug(_),
                   logger.info(_),
                   logger.warn(_),
                   logger.error(_))

    def running(command: Seq[String]): Unit =
      logger.debug("running" + nl + command.mkString(nl + "\t"))
  }
}
