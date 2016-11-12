package benchmarks

sealed abstract class BenchmarkResult(val name: String, val success: Boolean)

case class BenchmarkCompleted(override val name: String,
                              minNs: Long,
                              maxNs: Long,
                              avgNs: Long,
                              iterations: Int,
                              override val success: Boolean)
    extends BenchmarkResult(name, success) {
  override def toString: String = {
    def format(n: Double, decimals: Int = 3): String = {
      val s = n.toString
      s.substring(0, s.indexOf('.') + decimals + 1)
    }

    val minMs = format(minNs / 1e6)
    val maxMs = format(maxNs / 1e6)
    val avgMs = format(avgNs / 1e6)
    (if (success) "  [ok] " else "  [fail] ") + name +
      s": $iterations iterations, min ${minMs}ms, max ${maxMs}ms," +
      s" avg ${avgMs}ms"
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

  private class BenchmarkDisabledException extends Exception
  final def disableBenchmark(): Nothing = throw new BenchmarkDisabledException

  final def estimateTime(): Long = {
    val start = System.nanoTime()
    val _     = try { run() } catch { case _: Throwable => () }
    System.nanoTime() - start
  }

  final def loop(iterations: Int): BenchmarkResult =
    try {
      var min: Long        = Long.MaxValue
      var max: Long        = Long.MinValue
      var success: Boolean = true

      val t0 = System.nanoTime()
      for (_ <- 1 to iterations) {
        val start  = System.nanoTime()
        val result = run()
        val end    = System.nanoTime()

        success = success && check(result)
        val elapsed = end - start
        if (elapsed > max) max = elapsed
        if (elapsed < min) min = elapsed
      }
      val totalTime = System.nanoTime() - t0
      val average   = totalTime / iterations

      BenchmarkCompleted(this.getClass.getName,
                         min,
                         max,
                         average,
                         iterations,
                         success)
    } catch {
      case _: BenchmarkDisabledException =>
        BenchmarkDisabled(this.getClass.getName)
      case t: Throwable =>
        BenchmarkFailed(this.getClass.getName, t)
    }
}
