/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
import nbody.NBodySystem;

object NBody extends benchmarks.Benchmark[Nothing] {
  def loop(innerIterations: Int) {
    val system = new NBodySystem();

    (0 until innerIterations).foreach { i =>
      system.advance(0.01);
    }

    check(system.energy(), innerIterations);
  }

  def check(result: Double, innerIterations: Int): Boolean = {
    if (innerIterations == 250000) {
      return result == -0.1690859889909308;
    }
    if (innerIterations == 1) {
      return result == -0.16907495402506745;
    }

    System.out.println(
      "No verification result for " + innerIterations + " found");
    System.out.println("Result is: " + result);
    false
  }

  override def run(): Nothing =
    throw new RuntimeException("Should never be reached");

  override def check(result: Nothing): Boolean =
    throw new RuntimeException("Should never be reached")
}
