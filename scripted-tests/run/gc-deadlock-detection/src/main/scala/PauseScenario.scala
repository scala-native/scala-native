import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._

/** Scenario: Deadlock using pause() Syscall
 *
 *  pause() blocks until a signal is delivered. Without @blocking, this creates
 *  the same deadlock as the read() scenario.
 *
 *  Expected with fix: GC times out, process aborts (non-zero exit)
 */
object PauseScenario {
  private val ready = new AtomicBoolean(false)

  def run(): Unit = {
    println("[Test] Running PAUSE scenario (missing @blocking)")
    println("[Test] pause() blocks until signal delivery")
    println("[Test] Without fix: will hang forever")
    println("[Test] With fix: should timeout and abort")

    val blockingThread = new Thread(
      () => {
        ready.set(true)
        println("[Thread] Calling pause() WITHOUT @blocking...")
        val result = NativeBlocking.pause()
        println(s"[Thread] pause() returned: $result")
      },
      "pause-thread"
    )

    blockingThread.start()

    while (!ready.get()) Thread.sleep(10)
    Thread.sleep(100)

    println("[Main] Thread is blocked in pause(), triggering GC...")

    val startTime = System.currentTimeMillis()
    System.gc()
    val elapsed = System.currentTimeMillis() - startTime

    println(s"[Main] GC completed in ${elapsed}ms")
    blockingThread.join(2000)

    println("[Test] PASS: GC did not hang indefinitely")
    System.exit(0)
  }
}
