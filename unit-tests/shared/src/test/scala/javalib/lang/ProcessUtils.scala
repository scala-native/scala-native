package javalib.lang

import java.lang._

import java.io.InputStream

import scala.io.Source

object ProcessUtils {
  def readInputStream(s: InputStream) = Source.fromInputStream(s).mkString

  val resourceDir =
    s"${System.getProperty("user.dir")}/unit-tests/shared/src/test/resources/process"
}
