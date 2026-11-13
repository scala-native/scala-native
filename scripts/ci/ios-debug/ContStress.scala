// Temporary reproducer of ContinuationsTest.stressSuspendResumeAcrossPlatformThreads
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, LinkedBlockingQueue, TimeUnit}

import scala.scalanative.runtime.Continuations._

object Test {
  def main(args: Array[String]): Unit = {
    val fibers = 50
    val iterations = 1000
    val workers = math.max(4, Runtime.getRuntime().availableProcessors())
    println(s"workers=$workers")

    val ready = new LinkedBlockingQueue[() => Unit]()
    val finished = new CountDownLatch(fibers)
    val resumedSteps = new AtomicInteger(0)

    var fiberId = 0
    while (fiberId < fibers) {
      boundary[Unit] {
        var step = 0
        while (step < iterations) {
          suspend[Unit] { resume => ready.put(resume) }
          resumedSteps.incrementAndGet()
          step += 1
        }
        finished.countDown()
      }
      fiberId += 1
    }
    println("fibers created")

    def workerLoop(idx: Int): Unit = {
      var keepRunning = true
      var n = 0
      while (keepRunning) {
        val resume = ready.poll(100, TimeUnit.MILLISECONDS)
        if (resume != null) {
          if (n < 3) System.err.println(s"worker $idx resuming #$n")
          resume()
          if (n < 3) System.err.println(s"worker $idx resumed #$n")
          n += 1
          Thread.`yield`()
        } else if (finished.getCount() == 0 && ready.isEmpty())
          keepRunning = false
      }
    }

    (0 until workers).foreach { idx =>
      new Thread(() => workerLoop(idx), s"cont-stress-worker-$idx").start()
    }
    val ok = finished.await(30, TimeUnit.SECONDS)
    println(s"finished=$ok resumed=${resumedSteps.get()} queue=${ready.size()}")
    if (!ok) {
      println("HUNG")
      Thread.sleep(120000)
      System.exit(1)
    }
  }
}
