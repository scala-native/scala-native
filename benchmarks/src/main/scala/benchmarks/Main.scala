package benchmarks

import java.lang.System.exit

object Main {
  def main(args: Array[String]): Unit = {
    val benchmarks = Discover.discovered

    val format = args.lift(0).map(Format(_)).getOrElse(TextFormat)
    val results = benchmarks.map { bench =>
      bench.loop(bench.iterations)
    }
    val success = results.forall(_.success)

    println(format.show(results))

    if (success) exit(0) else exit(1)
  }
}
