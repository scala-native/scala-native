import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.posix.sys.wait._
import scala.scalanative.posix.unistd.{_exit, fork}
import scala.scalanative.unsafe._

/** Check that child GC ignores mutators of threads lost during fork.
 *  Use _exit to avoid shutdown hooks collecting while the worker spins.
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

    val pid = fork()
    if (pid == 0) {
      val retained = new Array[Int](32)
      retained(0) = 1
      System.gc()
      _exit(if (retained(0) == 1) 0 else 1)
    }

    if (pid < 0) {
      println("[Test] FAIL: fork failed")
      _exit(1)
    }

    val status = stackalloc[CInt]()
    !status = 0
    val waited = waitpid(pid, status, 0)
    if (waited != pid || !WIFEXITED(!status) || WEXITSTATUS(!status) != 0) {
      println(
        s"[Test] FAIL: child did not exit 0 (wait=$waited status=${!status})"
      )
      _exit(1)
    }
    println("[Test] PASS: child collected after fork")
    _exit(0)
  }
}
