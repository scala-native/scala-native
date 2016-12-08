package benchmarks

sealed abstract class BenchmarkResult(val name: String, val success: Boolean)

case class BenchmarkCompleted(override val name: String,
                              timesNs: Seq[Long],
                              override val success: Boolean)
    extends BenchmarkResult(name, success)

case class BenchmarkFailed(override val name: String, cause: Throwable)
    extends BenchmarkResult(name, false)

case class BenchmarkDisabled(override val name: String)
    extends BenchmarkResult(name, true)

abstract class Benchmark[T] {
  def run(): T
  def check(t: T): Boolean

  def iterations(): Int = {
    // Run once to estimate how long this benchmark takes
    val nsPerBenchmark = 3e9.toLong
    val timeEstimate   = estimateTime()
    Math.max(1, (nsPerBenchmark / timeEstimate).toInt)
  }

  private class BenchmarkDisabledException extends Exception
  final def disableBenchmark(): Nothing = throw new BenchmarkDisabledException

  final def estimateTime(): Long = {
    val start = System.nanoTime()
    val _     = try { run() } catch { case _: Throwable => () }
    System.nanoTime() - start
  }

  final def loop(iterations: Int): BenchmarkResult =
    try {
      var success: Boolean   = true
      var i: Int             = 0
      val times: Array[Long] = new Array[Long](iterations)

      while (i < iterations) {
        val start  = System.nanoTime()
        val result = run()
        val end    = System.nanoTime()

        success = success && check(result)
        times(i) = end - start
        i = i + 1
      }

      BenchmarkCompleted(this.getClass.getName, times, success)
    } catch {
      case _: BenchmarkDisabledException =>
        BenchmarkDisabled(this.getClass.getName)
      case t: Throwable =>
        BenchmarkFailed(this.getClass.getName, t)
    }
}
