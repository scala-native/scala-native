import scalanative.native._
import scalanative.libc.stdlib._
import scalanative.runtime.GC

object Test {
  def main(args: Array[String]): Unit = {
    import scalanative.runtime
    val ptrBytes = GC.malloc (100).cast[Ptr[Byte]]
    val ptrInt = ptrBytes.cast[Ptr[Int]]
    !ptrInt = 1

    ()
  }
}
