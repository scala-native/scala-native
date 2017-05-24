package benchmarks

sealed abstract class BenchmarkResult(val name: String, val success: Boolean)

case class BenchmarkCompleted(override val name: String,
                              iterations: Int,
                              timesNs: Seq[Long],
                              override val success: Boolean)
    extends BenchmarkResult(name, success)

case class BenchmarkFailed(override val name: String, cause: Throwable)
    extends BenchmarkResult(name, false)

case class BenchmarkDisabled(override val name: String)
    extends BenchmarkResult(name, true)

sealed class BenchmarkRunningTime(val iterations: Int)

case object VeryLongRunningTime extends BenchmarkRunningTime(20)
case object LongRunningTime     extends BenchmarkRunningTime(1000)
case object MediumRunningTime   extends BenchmarkRunningTime(10000)
case object ShortRunningTime    extends BenchmarkRunningTime(30000)
case object UnknownRunningTime  extends BenchmarkRunningTime(1)

abstract class Benchmark[T]() {
  def run(): T
  def check(t: T): Boolean

  val runningTime: BenchmarkRunningTime

  def iterations(): Int = runningTime.iterations

  private class BenchmarkDisabledException extends Exception
  final def disableBenchmark(): Nothing = throw new BenchmarkDisabledException

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

      BenchmarkCompleted(this.getClass.getName, iterations, times, success)
    } catch {
      case _: BenchmarkDisabledException =>
        BenchmarkDisabled(this.getClass.getName)
      case t: Throwable =>
        BenchmarkFailed(this.getClass.getName, t)
    }
}
