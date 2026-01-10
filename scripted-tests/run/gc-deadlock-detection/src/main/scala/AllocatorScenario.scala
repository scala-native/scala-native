import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

/** Scenario: Multiple Blocking Threads (Stress Test)
 *
 *  Creates multiple threads that all block in native code without @blocking.
 *  Tests that GC timeout detection works with multiple stuck threads.
 *
 *  Expected with fix: GC times out, reports all stuck threads, process aborts
 */
object AllocatorScenario {
  def run(): Unit = {
    println("[Test] Running ALLOCATOR scenario (multiple blocking threads)")
    println("[Test] Creating 3 threads blocked in read()")

    val numThreads = 3
    val threads = new Array[Thread](numThreads)
    val threadReady = new Array[AtomicBoolean](numThreads)
    val pipeReadFds = new Array[CInt](numThreads)
    val pipeWriteFds = new Array[CInt](numThreads)

    // Create pipes for each thread
    for (i <- 0 until numThreads) {
      val pipeFds = stackalloc[CInt](2)
      if (PlatformIO.createPipe(pipeFds) == 0) {
        pipeReadFds(i) = pipeFds(0)
        pipeWriteFds(i) = pipeFds(1)
      } else {
        System.err.println(s"ERROR: Failed to create pipe $i")
        System.exit(1)
      }
    }

    // Start blocking threads
    for (i <- 0 until numThreads) {
      threadReady(i) = new AtomicBoolean(false)
      val idx = i
      val readFd = pipeReadFds(i)

      threads(i) = new Thread(
        () => {
          threadReady(idx).set(true)
          println(s"[Thread-$idx] Entering read() WITHOUT @blocking...")

          val buf = stackalloc[Byte](1024)
          val bytesRead = NativeRead.readBlocking(readFd, buf, 1024.toCSize)

          println(s"[Thread-$idx] read() returned: $bytesRead")
        },
        s"blocking-$i"
      )
      threads(i).start()
    }

    // Wait for all threads to be ready
    for (i <- 0 until numThreads) {
      while (!threadReady(i).get()) Thread.sleep(10)
    }
    Thread.sleep(100)

    println()
    println(s"[Main] All $numThreads threads are blocked in read()")
    println("[Main] Triggering GC (should report all stuck threads)...")
    println()

    val startTime = System.currentTimeMillis()
    System.gc()
    val elapsed = System.currentTimeMillis() - startTime

    println(s"[Main] GC completed in ${elapsed}ms")

    // Cleanup
    for (i <- 0 until numThreads) {
      PlatformIO.closePipe(pipeReadFds(i))
      PlatformIO.closePipe(pipeWriteFds(i))
    }

    for (t <- threads) {
      t.join(1000)
    }

    println("[Test] PASS: GC handled multiple stuck threads")
    System.exit(0)
  }
}
