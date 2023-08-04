import scala.scalanative.meta.LinktimeInfo.isMac

object Hello {
  def main(args: Array[String]): Unit = f()

  def f() = g()

  def g() = error()

  def error() = {
    val stacktrace = new Error("test").getStackTrace().toList

    val actual = stacktrace.map(print).filter { elem =>
      elem.startsWith("Hello")
    }
    val expectedHello =
      if (isMac) {
        List(
          "Hello$.error(Hello.scala:10)",
          "Hello$.g(Hello.scala:8)",
          "Hello$.f(Hello.scala:6)",
          "Hello$.main(Hello.scala:4)",
          "Hello.main(Hello.scala:4)"
        )
      } else {
        List(
          "Hello$.error(Unknown Source)",
          "Hello$.g(Unknown Source)",
          "Hello$.f(Unknown Source)",
          "Hello$.main(Unknown Source)",
          "Hello.main(Unknown Source)"
        )
      }
    assert(actual == expectedHello, s"actual:\n${actual.mkString("\n")}")
  }

  def print(elem: StackTraceElement) = {
    val filename = elem.getFileName()
    val line = elem.getLineNumber()
    val method = elem.getMethodName()
    val module = elem.getClassName()
    val fileline =
      if (filename != null) s"($filename:$line)" else "Unknown Source"
    s"$module.$method($filename:$line)"
  }
}
