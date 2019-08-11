import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Test {
  def main(args: Array[String]): Unit = {
    Console.err.println("start main")
    Future {
      Console.err.println("future 1")
      1 + 2
    }.map { x =>
        Console.err.println("future 2")
        x + 3
      }
      .map { x =>
        Console.err.println("future 3")
        x + 4
      }
      .foreach { res =>
        println("result: " + res)
      }
    Console.err.println("end main")
  }
}
