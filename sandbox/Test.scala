import scalanative.native._

object Test  {
  def main(args: Array[String]): Unit = {
    val p1: Ptr[_] = null
    assert(p1 == null)
    val p2 = stdlib.malloc(42)
    assert(p2 != null)
  }
}
