import scalanative.native._
import scalanative.libc.stdlib._

@extern object Foo {
  def malloc(size: CSize): Ptr[Byte] = extern
}

object Test {
  def main(args: Array[String]): Unit = {
    val f = FunctionPtr.fromFunction1(Foo.malloc _)
    f(64)
  }
}
