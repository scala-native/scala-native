/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody

class NbodyBenchmark extends benchmarks.Benchmark[(Int, Double)] {

  private val sizes = List(250000, 1)
  private var i     = 0

  override def run(): (Int, Double) = {
    disableBenchmark()
    val size = sizes(i % sizes.length)
    i = i + 1
    (size, run(size))
  }

  override def check(t: (Int, Double)): Boolean =
    check(t._2, t._1)

  def run(innerIterations: Int): Double = {
    val system = new NBodySystem();

    (0 until innerIterations).foreach { i =>
      system.advance(0.01);
    }

    system.energy()
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
}
