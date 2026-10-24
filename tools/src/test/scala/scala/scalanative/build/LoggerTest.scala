package scala.scalanative.build

import scala.collection.mutable.ListBuffer

import org.junit.Assert._
import org.junit.Test

class LoggerTest {
  private def recordingLogger(entries: ListBuffer[(String, String)]): Logger =
    Logger(
      _ => (),
      message => entries += (("debug", message)),
      message => entries += (("info", message)),
      message => entries += (("warn", message)),
      message => entries += (("error", message))
    )

  @Test def compilerWarningsKeepTheirSourceContext(): Unit = {
    val entries = ListBuffer.empty[(String, String)]
    val process = Logger.toCompilerProcessLogger(recordingLogger(entries))
    process.out("compiling")
    process.err("In file included from test.c:1:")
    process.err("test.c:1:1: warning: macro redefined")
    process.err("    1 | #define VALUE 0")
    process.err("""    2 | const char *message = "error: text";""")
    process.err("test.h:1:1: note: previous definition is here")
    process.err("1 warning generated.")
    assertEquals(
      List("info", "info", "warn", "warn", "warn", "warn", "warn"),
      entries.map(_._1).toList
    )
    assertEquals("    1 | #define VALUE 0", entries(3)._2)
  }

  @Test def compilerErrorsResetTheDiagnosticLevel(): Unit = {
    val entries = ListBuffer.empty[(String, String)]
    val process = Logger.toCompilerProcessLogger(recordingLogger(entries))
    process.err("ld: warning: unused search path")
    process.err("test.o: undefined reference to `log'")
    process.err("clang: error: linker command failed")
    process.err("test.c:2:1: warning: unused variable")
    process.err("1 warning and 1 error generated.")
    assertEquals(
      List("warn", "error", "error", "warn", "error"),
      entries.map(_._1).toList
    )
  }

  @Test def unclassifiedStderrRemainsAnError(): Unit = {
    val entries = ListBuffer.empty[(String, String)]
    val logger = recordingLogger(entries)
    Logger.toCompilerProcessLogger(logger).err("unrecognized linker failure")
    Logger.toProcessLogger(logger).err("an application's warning: message")
    assertEquals(List("error", "error"), entries.map(_._1).toList)
  }
}
