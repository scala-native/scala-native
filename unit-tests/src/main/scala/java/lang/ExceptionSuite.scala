package java.lang

class DummyNoStackTraceException extends scala.util.control.NoStackTrace

object ExceptionSuite extends tests.Suite {
  test("printStackTrace") {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new Exception).printStackTrace(pw)
    val trace = sw.toString
    assert(trace.startsWith("java.lang.Exception"))
    assert(trace.contains("\tat tests.Main$.main(Unknown Source)"))
    assert(trace.contains("\tat <none>.main(Unknown Source)"))
  }

  test("printStackTrace <no stack trace available>") {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new DummyNoStackTraceException).printStackTrace(pw)
    val trace = sw.toString
    val expected = Seq(
      "java.lang.DummyNoStackTraceException",
      "\t<no stack trace available>"
    ).mkString("\n")
    assert(trace.startsWith(expected))
  }
}
