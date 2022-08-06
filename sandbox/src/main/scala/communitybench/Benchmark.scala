package communitybench

import scala.{Any, Array, Unit, Int, Long}
import java.lang.{String, System}
import scala.Predef.assert
import scala.Predef.augmentString

abstract class Benchmark {
  def run(input: String): Any

  def main(args: Array[String]): Unit = {
    assert(
      args.length == 4,
      "4 arguments expected: number of batches, batch size, input and expected output")
    val batches   = args(0).toInt
    val batchSize = args(1).toInt
    val input     = args(2)
    val output    = args(3)

    dump(loop(batches, batchSize, input, output))
  }

  def dump(times: Array[Long]): Unit = {
    var i = 0
    while (i < times.length) {
      System.out.println(times(i))
      i += 1
    }
  }

  def loop(batches: Int,
           batchSize: Int,
           input: String,
           output: String): Array[Long] = {
    assert(batches >= 1)
    assert(batchSize >= 1)

    var i       = 0
    val times   = new Array[Long](batches)
    val results = new Array[Any](batchSize)

    while (i < batches) {
      val start = System.nanoTime()

      var j = 0
      while (j < batchSize) {
        results(j) = run(input)
        j += 1
      }

      val end = System.nanoTime()

      j = 0
      while (j < batchSize) {
        val result = results(j)
        if (result.toString != output) {
          throw new java.lang.Exception(
            "validation failed: expected `" + output + "` got `" + result + "`")
        }
        results(j) = null
        j += 1
      }

      times(i) = end - start
      i += 1
    }

    times
  }
}
