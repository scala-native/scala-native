import scala.scalanative.unsafe._

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
 *    - "allocator": Multiple blocking threads stress test
 */
object Main {
  def main(args: Array[String]): Unit = {
    val mode = if (args.length > 0) args(0) else "help"

    mode match {
      case "correct"   => CorrectScenario.run()
      case "deadlock"  => DeadlockScenario.run()
      case "zombie"    => ZombieScenario.run()
      case "allocator" => AllocatorScenario.run()
      case _           =>
        println("Usage: <binary> <mode>")
        println()
        println("Modes:")
        println("  correct   - Correct behavior with @blocking annotation")
        println("  deadlock  - Deadlock scenario (missing @blocking)")
        println("  zombie    - Zombie thread scenario (POSIX only)")
        println("  allocator - Multiple blocking threads stress test")
        System.exit(1)
    }
  }
}
