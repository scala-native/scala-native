package java.lang

class DummyNoStackTraceException extends scala.util.control.NoStackTrace

object ExceptionSuite extends tests.Suite {
  test("printStackTrace") {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new Exception).printStackTrace(pw)

    val segments = Seq(
      "java.lang.Exception",
      "\tat java.lang.ExceptionSuite$$anonfun$1::apply$mcV$sp_unit",
      "\tat tests.Suite$$anonfun$test$1::apply$mcZ$sp_bool",
      "\tat tests.Suite$$anonfun$run$1::apply_class.tests.Test_unit",
      "\tat tests.Suite$$anonfun$run$1::apply_class.java.lang.Object_class.java.lang.Object",
      "\tat tests.Suite::run_bool",
      "\tat main"
    )

    val stackTrace = sw.toString

    assert(segments.forall(stackTrace.contains))
  }

  test("printStackTrace <no stack trace available>") {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new DummyNoStackTraceException).printStackTrace(pw)
    val expected = Seq(
      "java.lang.DummyNoStackTraceException",
      "\t<no stack trace available>"
    ).mkString("\n")
    assert(sw.toString.startsWith(expected))
  }
}
