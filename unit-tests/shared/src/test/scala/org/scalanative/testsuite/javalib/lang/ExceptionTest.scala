package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper._

class DummyNoStackTraceException extends scala.util.control.NoStackTrace

class ExceptionTest {
  @Test def printStackTrace(): Unit = {
    assumeNotASAN()
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new Exception).printStackTrace(pw)
    val trace = sw.toString
    assertTrue(trace.startsWith("java.lang.Exception"))

    assumeSupportsStackTraces()
    assertTrue(
      trace.contains(
        "\tat org.scalanative.testsuite.javalib.lang.ExceptionTest"
      )
    )
  }

  @Test def printStackTraceNoStackTraceAvailable(): Unit = {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new DummyNoStackTraceException).printStackTrace(pw)
    val trace = sw.toString
    val expected = Seq(
      "org.scalanative.testsuite.javalib.lang.DummyNoStackTraceException",
      ""
    ).mkString(System.lineSeparator()).trim()
    assertTrue(
      s"expected to start with '$expected', got `$trace`",
      trace.startsWith(expected)
    )
  }
}
