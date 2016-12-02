package benchmarks

import java.lang.System.exit

object Main {
  def main(args: Array[String]): Unit = {
    val benchmarks = Discover.discovered

    val success =
      benchmarks.forall { bench =>
        val result = bench.loop(bench.iterations)
        println(result)
        result.success
      }

    if (success) exit(0) else exit(1)
  }
}
