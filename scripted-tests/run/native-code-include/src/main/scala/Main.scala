import scalanative.unsafe.*

@extern
object myapi {
  def add3(in: CLongLong): CLongLong = extern
}

object Main {
  import myapi.*
  def main(args: Array[String]): Unit = {
    val res = add3(-3L)
    assert(res == 0L)
    println(s"Add3 to -3 = $res")
  }
}
