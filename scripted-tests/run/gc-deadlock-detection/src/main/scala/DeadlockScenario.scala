import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

/** Scenario: Deadlock due to Missing @blocking Annotation
 *
 *  When a native call is missing @blocking annotation:
 *    - Thread stays in Managed state during the call
 *    - GC waits forever for thread to reach safepoint
 *    - Without fix: Application hangs FOREVER
 *    - With fix: GC times out after configured interval and aborts
 *
 *  Expected with fix: GC times out, process aborts (non-zero exit)
 */
object DeadlockScenario {
  private val ready = new AtomicBoolean(false)

  def run(): Unit = {
    println("[Test] Running DEADLOCK scenario (missing @blocking)")
    println("[Test] Without fix: will hang forever")
    println("[Test] With fix: should timeout and abort")

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
        println("[Thread] Entering read() WITHOUT @blocking...")
        println("[Thread] Thread stays in Managed state - GC will wait!")
        val buf = stackalloc[Byte](1024)
        val bytesRead = NativeRead.readBlocking(readFd, buf, 1024.toCSize)
        println(s"[Thread] read() returned: $bytesRead")
      },
      "deadlock-thread"
    )

    blockingThread.start()

    while (!ready.get()) Thread.sleep(10)
    Thread.sleep(100)

    println("[Main] Thread is blocked (Managed state), triggering GC...")
    println("[Main] GC will wait for thread to reach safepoint...")

    val startTime = System.currentTimeMillis()
    System.gc()
    val elapsed = System.currentTimeMillis() - startTime

    println(s"[Main] GC completed in ${elapsed}ms")

    PlatformIO.closePipe(writeFd)
    PlatformIO.closePipe(readFd)
    blockingThread.join(2000)

    // If we get here, GC recovered somehow (shouldn't happen with timeout -> abort)
    println("[Test] PASS: GC did not hang indefinitely")
    System.exit(0)
  }
}
