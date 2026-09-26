import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

object Main {
  private final val ThreadCount = 512
  private final val JoinTimeoutMillis = 30000L
  private final class Worker(
      ready: CountDownLatch,
      start: CountDownLatch,
      completed: AtomicInteger
  ) extends Thread {
    @volatile var failure: Throwable = null

    override def run(): Unit =
      try {
        ready.countDown()
        start.await()
        completed.incrementAndGet()
      } catch {
        case t: Throwable => failure = t
      }
  }

  def main(args: Array[String]): Unit = {
    val ready = new CountDownLatch(ThreadCount)
    val start = new CountDownLatch(1)
    val completed = new AtomicInteger(0)
    val workers = new Array[Worker](ThreadCount)

    var i = 0
    while (i < ThreadCount) {
      val worker = new Worker(ready, start, completed)
      workers(i) = worker
      worker.start()
      i += 1
    }

    if (!ready.await(30, TimeUnit.SECONDS))
      throw new AssertionError("workers did not start in time")

    i = 0
    while (i < ThreadCount) {
      val worker = workers(i)
      if (!worker.isAlive())
        throw new AssertionError(s"worker $i exited before release")
      if (worker.failure != null)
        throw worker.failure
      i += 1
    }

    System.gc()
    start.countDown()

    i = 0
    while (i < ThreadCount) {
      val worker = workers(i)
      worker.join(JoinTimeoutMillis)
      if (worker.isAlive())
        throw new AssertionError(s"worker $i did not finish in time")
      if (worker.failure != null)
        throw worker.failure
      i += 1
    }

    val count = completed.get()
    if (count != ThreadCount)
      throw new AssertionError(
        s"completed $count workers, expected $ThreadCount"
      )

    println(s"mutator-cursors: OK ($ThreadCount rapid threads)")
  }
}
