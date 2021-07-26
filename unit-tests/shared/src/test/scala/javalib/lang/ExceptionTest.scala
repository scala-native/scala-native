package javalib.lang

import org.junit.Test
import org.junit.Assert._

class DummyNoStackTraceException extends scala.util.control.NoStackTrace

class ExceptionTest {
  @Test def printStackTrace(): Unit = {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new Exception).printStackTrace(pw)
    val trace = sw.toString
    assertTrue(trace.startsWith("java.lang.Exception"))
    assertTrue(trace.contains("\tat <none>.main(Unknown Source)"))
  }

  @Test def printStackTraceNoStackTraceAvailable(): Unit = {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new DummyNoStackTraceException).printStackTrace(pw)
    val trace = sw.toString
    val expected = Seq(
      "javalib.lang.DummyNoStackTraceException",
      "\t<no stack trace available>"
    ).mkString("\n")
    assertTrue(trace.startsWith(expected))
  }
}
