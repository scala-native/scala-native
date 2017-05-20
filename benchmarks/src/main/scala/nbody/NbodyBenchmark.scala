/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody

import benchmarks.{BenchmarkRunningTime, VeryLongRunningTime}

class NbodyBenchmark extends benchmarks.Benchmark[Double] {

  override val runningTime: BenchmarkRunningTime = VeryLongRunningTime

  override def run(): Double = {
    val system = new NBodySystem()

    var i = 0
    while (i < 250000) {
      system.advance(0.01)
      i += 1
    }

    system.energy()
  }

  override def check(result: Double): Boolean =
    result == -0.1690859889909308
}
