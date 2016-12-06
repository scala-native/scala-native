package benchmarks

sealed abstract class BenchmarkResult(val name: String, val success: Boolean)

case class BenchmarkCompleted(override val name: String,
                              timesNs: Seq[Long],
                              override val success: Boolean)
    extends BenchmarkResult(name, success) {

  private def format(n: Double, decimals: Int = 3): String = {
    val s = n.toString
    s.substring(0, s.indexOf('.') + decimals + 1)
  }

  private def percentile[T](n: Int, sortedData: Seq[T]): T = {
    val index = Math.ceil(n * sortedData.length / 100.0).toInt - 1
    sortedData(index)
  }

  private def average(data: Seq[Double]): Double = {
    val count = data.length.toDouble
    data.foldLeft(0.0) { _ + _ / count }
  }

  override def toString: String = {
    val timesMs    = timesNs map (_ / 1e6)
    val sortedMs   = timesMs.sorted
    val minMs      = timesMs.min
    val maxMs      = timesMs.max
    val avgMs      = average(timesMs)
    val medianMs   = percentile(50, sortedMs)
    val p95Ms      = percentile(95, sortedMs)
    val p05Ms      = percentile(5, sortedMs)
    val iterations = timesNs.length
    val stddev =
      Math.sqrt(timesMs.map(t => Math.pow(t - avgMs, 2) / iterations).sum)
    val avgBetweenP05AndP95 =
      average(timesMs filter (t => t >= p05Ms && t <= p95Ms))

    (if (success) "  [ok] " else "  [fail] ") + name +
      s": $iterations iterations, min ${format(minMs)}ms, max ${format(maxMs)}ms," +
      s" avg ${format(avgMs)}ms, median ${format(medianMs)}, p05 ${format(p05Ms)}ms," +
      s" p95 ${format(p95Ms)}ms, avg in [p05, p95] ${format(avgBetweenP05AndP95)}ms"
  }
}

case class BenchmarkFailed(override val name: String, cause: Throwable)
    extends BenchmarkResult(name, false) {
  override def toString: String =
    s"""  [fail] $name threw an exception:
       |$cause""".stripMargin
}

case class BenchmarkDisabled(override val name: String)
    extends BenchmarkResult(name, true) {
  override def toString: String =
    s""" [???] $name is disabled."""
}

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
