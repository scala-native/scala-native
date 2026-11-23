import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.unsafe._

/** Check that child GC ignores mutators of threads lost during fork. Use _exit
 *  to avoid shutdown hooks collecting while the worker spins.
 */
object ForkScenario {
  def run(): Unit = {
    println("[Test] Running FORK scenario")

    val spinning = new AtomicBoolean(false)
    val worker = new Thread(
      () => {
        spinning.set(true)
        ZombieThread.spinForever()
      },
      "fork-sibling"
    )
    worker.setDaemon(true)
    worker.start()

    while (!spinning.get()) Thread.sleep(10)
    Thread.sleep(50)

    val pid = ForkSupport.fork()
    if (pid == 0) {
      val retained = new Array[Int](32)
      retained(0) = 1
      System.gc()
      ForkSupport.exitNow(if (retained(0) == 1) 0 else 1)
    }

    if (pid < 0) {
      println("[Test] FAIL: fork failed")
      ForkSupport.exitNow(1)
    }

    val waited = stackalloc[CInt]()
    val status = stackalloc[CInt]()
    if (ForkSupport.waitChild(pid, waited, status) != 0) {
      println(
        s"[Test] FAIL: child did not exit 0 (wait=${!waited} status=${!status})"
      )
      ForkSupport.exitNow(1)
    }
    println("[Test] PASS: child collected after fork")
    ForkSupport.exitNow(0)
  }
}

@extern
private object ForkSupport {
  @name("fork_support_fork")
  def fork(): CInt = extern

  @name("fork_support_wait_child")
  def waitChild(pid: CInt, waited: Ptr[CInt], status: Ptr[CInt]): CInt = extern

  @name("fork_support_exit")
  def exitNow(status: CInt): Unit = extern
}
