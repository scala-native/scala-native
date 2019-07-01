import scalanative.unsafe._

@extern
object stdlib {
  @name("scalanative_llabs")
  def llabs(in: Ptr[CLongLong]): CLongLong = extern
}

object Main {
  import stdlib._
  def main(args: Array[String]): Unit = {
    val ptr = stackalloc[CLongLong]
    !ptr = -34L
    val abs = llabs(ptr)
    assert(abs == 34L)
    println(s"Abs val of -34 = $abs")
  }
}
