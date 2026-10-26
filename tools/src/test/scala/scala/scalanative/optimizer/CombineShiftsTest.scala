package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec
import scala.scalanative.build.Mode

/** Interflow's rules for shifts keep the type of the value they fold. */
class CombineShiftsTest extends OptimizerSpec {

  private val sources = Map(
    "Test.scala" ->
      """|object Test {
         |  @noinline def longShiftsOut(x: Long): Long = (x >>> 40) >>> 30
         |  @noinline def intShiftsOut(x: Int): Int = (x >>> 20) >>> 20
         |  def main(args: Array[String]): Unit = {
         |    val n = args.length
         |    println(longShiftsOut(n.toLong))
         |    println(intShiftsOut(n))
         |  }
         |}
         |""".stripMargin
  )

  @Test def unsignedShiftsOutOfRangeFoldToAZeroOfTheirType(): Unit =
    optimize("Test", sources, _.withMode(Mode.releaseFull)) {
      case (_, result) =>
        val long = optimizedMethod(result.defns, "Test$", "longShiftsOut")
        assertEquals(long.show, Seq(nir.Val.Long(0L)), returned(long))
        val int = optimizedMethod(result.defns, "Test$", "intShiftsOut")
        assertEquals(int.show, Seq(nir.Val.Int(0)), returned(int))
    }
}
