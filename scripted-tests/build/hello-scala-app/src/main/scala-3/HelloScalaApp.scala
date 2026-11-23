// In Scala 3 scala.App is no longer supported
// test @main() annotation instead

object HelloScalaApp:
  @main()
  def myMainFunction(arg1: String, arg2: String, arg3: String): Unit =
    assert(arg1.equals("hello"))
    assert(arg2.equals("scala"))
    assert(arg3.equals("app"))
