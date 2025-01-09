import scala.scalanative.unsafe._

object Main {
  def main(args: Array[String]): Unit = {
    val carr = stackalloc[CArray[Int, Nat.Digit2[Nat._1, Nat._2]]]()
    val obtained = carr.length
    val expected = 12
    assert(obtained == expected, s"expected $expected, got $obtained")
  }
}
