object Test {
  def main(args: Array[String]): Unit = {
    import scala.concurrent.Future
    import scala.concurrent.ExecutionContext.Implicits.global
    Future(1 + 2).map(_ + 3).map(_ + 4).foreach(println)
  }
}
