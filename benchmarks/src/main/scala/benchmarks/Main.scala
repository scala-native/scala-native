package benchmarks

import java.lang.System.exit

object Main {
  def main(args: Array[String]): Unit = {
    val benchmarks = Discover.discovered.sortBy(_.getClass.getSimpleName)

    val opts = Opts(args)
    val results = benchmarks.map { bench =>
      val iterations = if (!opts.test) bench.iterations() else 1
      bench.loop(iterations)
      bench.loop(iterations)
    }
    val success = results.forall(_.success)

    println(opts.format.show(results))

    if (success) exit(0) else exit(1)
  }
}
