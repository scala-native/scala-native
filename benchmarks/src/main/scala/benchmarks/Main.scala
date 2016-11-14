package benchmarks

import java.lang.System.exit

object Main {

  val nsPerBenchmark = 3e9.toLong

  def main(args: Array[String]): Unit = {

    val benchmarks = Discover.discovered

    val success =
      benchmarks forall { bench =>
        // Run once to estimate how long this benchmark takes
        val timeEstimate = bench.estimateTime()
        val iterations   = (nsPerBenchmark / timeEstimate).toInt

        val result = bench.loop(iterations)
        println(result)

        // Sleep between test to avoid error (???)
        Thread.sleep(50)

        result.success
      }

    if (success) exit(0) else exit(1)
  }
}
