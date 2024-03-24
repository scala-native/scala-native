import scala.util.Random
object Test {
  def main(args: Array[String]): Unit = {
    val joinThreads = args.contains("--join")
    val threads = List
      .tabulate(8) { id =>
        new Thread(() => {
          sys.addShutdownHook(println(s"On shutdown:$id"))
          while (true) {
            Thread.sleep(100 + Random.nextInt(1000))
            print(s"$id;")
          }
        })
      }
    threads.foreach(_.start())
    if (joinThreads) threads.foreach(_.join())
  }
}
