package test

object Test {
  def main(args: Array[String]): Unit = {
    val x: Any = 1
    x.asInstanceOf[Int]
    ()
  }
}
