import scalanative.native._, stdlib._, stdio._

object Test {
  def main(args: Array[String]): Unit = {
    atexit { () => fprintf(stdout, c"first\n"); () }
    atexit { () => fprintf(stdout, c"second\n"); () }
  }
}
