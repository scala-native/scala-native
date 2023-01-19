package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.Platform

class DummyNoStackTraceException extends scala.util.control.NoStackTrace

class ExceptionTest {
  @Test def printStackTrace(): Unit = {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new Exception).printStackTrace(pw)
    val trace = sw.toString
    assertTrue(trace.startsWith("java.lang.Exception"))
    if (!Platform.executingInJVM) {
      assertTrue(trace.contains("\tat <none>.main(Unknown Source)"))
    }
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
