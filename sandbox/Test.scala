import scalanative.native._
import scalanative.libc.stdlib._
import scalanative.runtime.GC

trait T
trait S

class C extends T

object Main {
  def main(args: Array[String]): Unit = {
    val c = new C
    fprintf(stdout, if (c.isInstanceOf[T]) c"C is T\n" else c"C is not T\n")
    fprintf(stdout, if (c.isInstanceOf[S]) c"C is S\n" else c"C is not S\n")
  }
}
