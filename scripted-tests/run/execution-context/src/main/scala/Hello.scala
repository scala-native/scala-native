import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Test {
  def main(args: Array[String]): Unit = {
    println("start main")
    Future {
      println("future 1")
      1 + 2
    }.map { x =>
      println("future 2")
      x + 3
    }.map { x =>
      println("future 3")
      x + 4
    }.foreach { res => println("result: " + res) }
    println("end main")
  }
}
