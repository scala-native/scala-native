import scala.scalanative.meta.LinktimeInfo.isMac

object Hello {
  @noinline def main(args: Array[String]): Unit = f()

  @noinline def f() = g()

  @noinline def g() = error()

  @noinline def error() = {
    val stacktrace = new Error("test").getStackTrace().toList

    val actual = stacktrace.map(_.toString).filter { elem =>
      elem.startsWith("Hello")
    }
    val expectedHello =
      if (isMac) {
        List(
          "Hello$.error(Hello.scala:11)",
          "Hello$.g(Hello.scala:9)",
          "Hello$.f(Hello.scala:7)",
          "Hello$.main(Hello.scala:5)",
          "Hello.main(Hello.scala:5)"
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
}
