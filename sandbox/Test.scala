import scalanative.native._
import scalanative.libc.stdlib._

@extern
object atexit {
  @name("atexit")
  def apply(f: FunctionPtr0[Unit]): Unit = extern
}

object Test {
  def main(args: Array[String]): Unit = {
    atexit { () => fprintf(stdout, c"first\n"); () }
    atexit { () => fprintf(stdout, c"second\n"); () }
  }
}
