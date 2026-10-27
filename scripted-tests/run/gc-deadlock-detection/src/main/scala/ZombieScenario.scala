import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._

/** Scenario: Zombie Thread
 *
 *  A "zombie" thread is one that:
 *    1. Was registered with the GC (MutatorThread_init called)
 *    2. Terminated WITHOUT calling MutatorThread_delete
 *    3. Still appears in the GC's mutator threads list
 *
 *  GC must not wait for that thread to reach a safepoint. Stop-the-world reaps
 *  a Managed mutator whose pthread has terminated (ESRCH) and then finishes the
 *  collection.
 */
object ZombieScenario {
  def run(): Unit = {
    assert(
      !scala.scalanative.meta.LinktimeInfo.isWindows,
      "ZombieScenario is only supported on POSIX platforms"
    )
    println("[Test] Running ZOMBIE scenario")
    println("[Test] Creating a thread that exits without GC cleanup...")

    val zombieCreated = new AtomicBoolean(false)

    val zombieThread = new Thread(
      () => {
        println(
          s"[Zombie Thread] Started (tid=${Thread.currentThread().getId})"
        )
        println("[Zombie Thread] This thread is registered with GC")

        zombieCreated.set(true)
        Thread.sleep(100)

        println("[Zombie Thread] Exiting WITHOUT cleanup...")
        println("[Zombie Thread] MutatorThread_delete will NOT be called!")

        // Exit without GC cleanup - creates a zombie
        ZombieThread.exitNoCleanup()

        // Should never reach here
        println("[Zombie Thread] ERROR: This should never print!")
      },
      "zombie-thread"
    )

    zombieThread.start()

    while (!zombieCreated.get()) Thread.sleep(10)

    // Wait for thread to actually exit
    println("[Main] Waiting for thread to become zombie...")
    Thread.sleep(500)

    // The thread is gone but still Managed in the mutator list.
    println("[Main] Triggering GC...")
    val startTime = System.currentTimeMillis()
    System.gc()
    val elapsed = System.currentTimeMillis() - startTime

    println(s"[Main] GC completed in ${elapsed}ms")

    zombieThread.join(1000)

    println("[Test] PASS: GC handled zombie thread")
    System.exit(0)
  }
}
