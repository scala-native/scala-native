import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

/** Scenario: Correct Behavior with @blocking Annotation
 *
 *  When a native call has @blocking annotation:
 *    - Thread switches to Unmanaged state before the call
 *    - GC can proceed immediately without waiting
 *    - This is the correct, expected behavior
 *
 *  Expected: GC completes in < 1 second
 */
object CorrectScenario {
  private val ready = new AtomicBoolean(false)

  def run(): Unit = {
    println("[Test] Running CORRECT scenario (@blocking annotation present)")
    println("[Test] GC should complete immediately")

    val pipeFds = stackalloc[CInt](2)
    if (PlatformIO.createPipe(pipeFds) != 0) {
      System.err.println("ERROR: Failed to create pipe")
      System.exit(1)
    }
    val readFd = pipeFds(0)
    val writeFd = pipeFds(1)

    val blockingThread = new Thread(
      () => {
        ready.set(true)
        println("[Thread] Entering read() with @blocking...")
        val buf = stackalloc[Byte](1024)
        val bytesRead =
          NativeRead.readBlockingCorrect(readFd, buf, 1024.toCSize)
        println(s"[Thread] read() returned: $bytesRead")
      },
      "blocking-thread"
    )

    blockingThread.start()

    while (!ready.get()) Thread.sleep(10)
    Thread.sleep(100)

    println("[Main] Thread is blocked (Unmanaged state), triggering GC...")
    val startTime = System.currentTimeMillis()
    System.gc()
    val elapsed = System.currentTimeMillis() - startTime

    println(s"[Main] GC completed in ${elapsed}ms")

    PlatformIO.closePipe(writeFd)
    PlatformIO.closePipe(readFd)
    blockingThread.join(2000)

    if (elapsed < 100) {
      println("[Test] PASS: GC completed quickly as expected")
      System.exit(0)
    } else {
      println("[Test] FAIL: GC took too long")
      System.exit(1)
    }
  }
}
