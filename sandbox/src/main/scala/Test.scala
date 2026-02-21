import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.*

object Test {
  def vthread(body: Runnable): Thread = Thread.startVirtualThread(body)

  def main(args: Array[String]): Unit = {
    val t1 = Thread
      .ofVirtual()
      .start { () =>
        println("started")
        println(1 -> Thread.currentThread())
        Thread.`yield`()
        println(2 -> Thread.currentThread())
        Thread.`yield`()
        println(3 -> Thread.currentThread())
        println("done")
      }
      .join(1000)
    println(t1)
  }

}
