package scala.scalanative.junit

import sbt.testing._

import scala.collection.mutable.ListBuffer

import org.junit.Assert._
import org.junit.Test

class ReporterEmptyStackTraceAssertions {
  private def report(error: Throwable): (List[String], List[Event]) = {
    val messages = ListBuffer.empty[String]
    val events = ListBuffer.empty[Event]
    val logger = new Logger {
      def ansiCodesSupported(): Boolean = false
      def info(message: String): Unit = ()
      def warn(message: String): Unit = ()
      def debug(message: String): Unit = ()
      def error(message: String): Unit = { messages += message; () }
      def trace(error: Throwable): Unit = throw error
    }
    val handler = new EventHandler {
      def handle(event: Event): Unit = { events += event; () }
    }
    val settings =
      new RunSettings(false, false, RunSettings.Verbosity.Terse, true, true)
    val fingerprint =
      new com.novocode.junit.JUnitFramework().fingerprints().head
    val task =
      new TaskDef(getClass.getName, fingerprint, true, Array.empty[Selector])
    new Reporter(handler, Array(logger), settings, task)
      .reportErrors("Test ", Some("failure"), 0.0, List(error))
    (messages.toList, events.toList)
  }

  @Test def reportsAnExceptionWithoutFrames(): Unit = {
    val error = new RuntimeException("original failure")
    error.setStackTrace(Array.empty[StackTraceElement])
    val (messages, events) = report(error)
    assertTrue(messages.exists(_.contains("original failure")))
    assertEquals(1, events.size)
    assertEquals(Status.Failure, events.head.status())
    assertSame(error, events.head.throwable().get())
  }

  @Test def reportsACauseWhenAllFramesAreFiltered(): Unit = {
    val cause = new RuntimeException("empty cause")
    cause.setStackTrace(Array.empty[StackTraceElement])
    val error = new RuntimeException("original failure", cause)
    error.setStackTrace(
      Array(new StackTraceElement("org.junit.Assert", "fail", "Assert.java", 1))
    )
    val (messages, events) = report(error)
    assertTrue(messages.exists(_.contains("original failure")))
    assertTrue(
      messages.exists(
        _.contains("Caused by: java.lang.RuntimeException: empty cause")
      )
    )
    assertEquals(1, events.size)
    assertSame(error, events.head.throwable().get())
  }
}
