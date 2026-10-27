import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.posix.sys.wait._
import scala.scalanative.posix.unistd.{_exit, fork}
import scala.scalanative.unsafe._

/** Test: GC in a child of fork() in a multithreaded process.
 *
 *  A second mutator spins in native code, so it stays Managed and never reaches
 *  a safepoint. fork() copies that mutator but not its OS thread, so without
 *  the fork handler the child's stop-the-world waits forever.
 *
 *  The child reports success through its exit status only. Both processes leave
 *  with _exit: shutdown hooks would allocate and the spinning thread would
 *  block the collection they trigger.
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
