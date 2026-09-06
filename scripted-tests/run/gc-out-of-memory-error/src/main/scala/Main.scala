import java.util.concurrent.{CountDownLatch, TimeUnit}

object Main {
  private final val Slots = 128
  private final val AllocationSize = 64 * 1024
  private final val FallbackThreads = 4

  private final class FallbackWorker(
      ready: CountDownLatch,
      start: CountDownLatch,
      done: CountDownLatch
  ) extends Thread {
    @volatile var error: OutOfMemoryError = null

    override def run(): Unit = {
      ready.countDown()
      start.await()
      try new Array[Byte](AllocationSize)
      catch { case ex: OutOfMemoryError => error = ex }
      finally done.countDown()
    }
  }

  private def allocateUntilOom(retained: Array[AnyRef]): OutOfMemoryError = {
    try {
      var index = 0
      while (true) {
        retained(index) = new Array[Byte](AllocationSize)
        index += 1
      }
      throw new AssertionError("unreachable")
    } catch {
      case error: OutOfMemoryError => error
    }
  }

  private def clear(retained: Array[AnyRef]): Unit = {
    var index = 0
    while (index < retained.length) {
      retained(index) = null
      index += 1
    }
    System.gc()
  }

  def main(args: Array[String]): Unit = {
    val retained = new Array[AnyRef](Slots)

    val withTrace = allocateUntilOom(retained)
    clear(retained)
    assert(
      withTrace.getStackTrace().exists(_.getMethodName() == "allocateUntilOom"),
      "first OutOfMemoryError did not retain its allocation stack"
    )

    val withTraceAgain = allocateUntilOom(retained)
    assert(
      withTraceAgain
        .getStackTrace()
        .exists(_.getMethodName() == "allocateUntilOom"),
      "refilled reserve did not retain the allocation stack"
    )

    val ready = new CountDownLatch(FallbackThreads)
    val start = new CountDownLatch(1)
    val done = new CountDownLatch(FallbackThreads)
    val workers = new Array[FallbackWorker](FallbackThreads)
    var index = 0
    while (index < FallbackThreads) {
      val worker = new FallbackWorker(ready, start, done)
      workers(index) = worker
      worker.start()
      index += 1
    }
    assert(ready.await(30, TimeUnit.SECONDS), "fallback workers did not start")

    val fallback = allocateUntilOom(retained)
    assert(
      fallback.getStackTrace().isEmpty,
      "fallback OutOfMemoryError unexpectedly contains a stack trace"
    )

    start.countDown()
    assert(done.await(30, TimeUnit.SECONDS), "fallback workers did not finish")
    index = 0
    while (index < FallbackThreads) {
      workers(index).join()
      assert(
        workers(index).error eq fallback,
        "concurrent OOM did not use the shared fallback"
      )
      index += 1
    }

    println("gc-out-of-memory-error: OK")
  }
}
