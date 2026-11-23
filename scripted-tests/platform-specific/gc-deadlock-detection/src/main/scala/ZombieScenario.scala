import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._

/** Check that GC reaps a thread that exited without removing its mutator. */
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
