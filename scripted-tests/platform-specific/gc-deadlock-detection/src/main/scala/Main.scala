import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

/** GC Deadlock Detection Test - Main Entry Point
 *
 *  This test verifies that GC synchronization handles various stuck thread
 *  scenarios. Each scenario is defined in a separate file but invoked through
 *  command-line arguments.
 *
 *  Test modes:
 *    - "correct": @blocking annotation present - GC completes immediately
 *      (baseline)
 *    - "deadlock": Missing @blocking - tests timeout detection
 *    - "zombie": Thread exits without GC cleanup (POSIX only)
 *    - "fork": Child collects after fork with other mutators (POSIX only)
 *    - "allocator": Multiple blocking threads stress test
 */
object Main {
  def main(args: Array[String]): Unit = {
    val mode = if (args.length > 0) args(0) else "help"
    disableCoreDumps()

    mode match {
      case "correct"   => CorrectScenario.run()
      case "deadlock"  => DeadlockScenario.run()
      case "zombie"    => ZombieScenario.run()
      case "fork"      => ForkScenario.run()
      case "allocator" => AllocatorScenario.run()
      case _           =>
        println("Usage: <binary> <mode>")
        println()
        println("Modes:")
        println("  correct   - Correct behavior with @blocking annotation")
        println("  deadlock  - Deadlock scenario (missing @blocking)")
        println("  zombie    - Zombie thread scenario (POSIX only)")
        println("  fork      - GC in a forked child (POSIX only)")
        println("  allocator - Multiple blocking threads stress test")
        System.exit(1)
    }
  }

  /** Scenarios end in an intentional abort(). Piped core handlers (e.g.
   *  systemd-coredump on CI runners) ignore RLIMIT_CORE=0 and dump the whole
   *  reserved GC heap, which can outlast the test timeout. A limit of 1 makes
   *  the kernel skip the dump for both piped and file core patterns.
   */
  private def disableCoreDumps(): Unit =
    if (!LinktimeInfo.isWindows) {
      import scala.scalanative.posix.sys.resource._
      import scala.scalanative.posix.sys.resourceOps._
      val limit = stackalloc[rlimit]()
      if (getrlimit(RLIMIT_CORE, limit) == 0) {
        limit.rlim_cur = 1.toUSize
        setrlimit(RLIMIT_CORE, limit)
      }
    }
}
