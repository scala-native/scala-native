object Deadlocking {
  val init = {
    println(s"start init by ${Thread.currentThread()}")
    Thread.sleep(1000)
    throw new RuntimeException()
    println("init done")
    42
  }
}

object Test {
  def main(args: Array[String]): Unit = {
    List.tabulate(8) { n =>
        val t = new Thread(() => {
          println(s"start thread $n")
          println(s"thread $n, result=${Deadlocking.init}")
          println(s"thread done $n")
        })
        t.setName(s"thread-$n")
        t
      }
      .tapEach(_.start())
      .foreach(_.join())
  }
}