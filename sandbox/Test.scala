import scala.util.Random
object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    class X(x: String)
    0.until(1e6.toInt).foreach{ _ =>
      val x = new X(Random.alphanumeric.take(1e5.toInt).mkString)
      println(x)
      scala.scalanative.runtime.GC.collect()
    }
  }
}
