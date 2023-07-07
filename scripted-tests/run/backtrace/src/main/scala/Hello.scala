object Hello {
  def main(args: Array[String]): Unit = f()

  def f() = g()

  def g() = error()

  def error() = {
    val stacktrace = new Error("test").getStackTrace().toList

    val stacktraceHello = stacktrace.filter { elem =>
      elem.getFileName() == "Hello.scala"
    }
    val expectedHello = List(
      "Hello$.error(Hello.scala:8)",
      "Hello$.g(Hello.scala:6)",
      "Hello$.f(Hello.scala:4)",
      "Hello$.main(Hello.scala:2)",
      "Hello.main(Hello.scala:2)",
    )
    val actual = stacktraceHello.map(print)
    assert(actual == expectedHello, s"actual:\n${actual.mkString("\n")}")
  }

  def print(elem: StackTraceElement) = {
    val filename = elem.getFileName()
    val line = elem.getLineNumber()
    val method = elem.getMethodName()
    val module = elem.getClassName()
    s"$module.$method($filename:$line)"
  }
}
