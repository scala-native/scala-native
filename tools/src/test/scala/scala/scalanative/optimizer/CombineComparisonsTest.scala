package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec
import scala.scalanative.build.Mode

/** Interflow's rules for comparisons see their operands in order. */
class CombineComparisonsTest extends OptimizerSpec {

  private val sources = Map(
    "Test.scala" ->
      """|object Test {
         |  @noinline def maxGt(x: Int): Boolean = Int.MaxValue > x
         |  @noinline def gtMax(x: Int): Boolean = x > Int.MaxValue
         |  @noinline def maxLe(x: Int): Boolean = Int.MaxValue <= x
         |  @noinline def leMax(x: Int): Boolean = x <= Int.MaxValue
         |  @noinline def gtOrEq(x: Int, y: Int): Boolean = (x > y) | (x == y)
         |  def main(args: Array[String]): Unit = {
         |    val n = args.length
         |    println(Seq(maxGt(n), gtMax(n), maxLe(n), leMax(n)))
         |    println(gtOrEq(n, 1))
         |  }
         |}
         |""".stripMargin
  )

  @Test def comparisonsWithMinAndMaxValues(): Unit =
    optimize("Test", sources, _.withMode(Mode.releaseFull)) {
      case (_, result) =>
        def check(name: String, folded: Option[Boolean]): Unit = {
          val defn = optimizedMethod(result.defns, "Test$", name)
          val constant = folded.map(nir.Val.Bool(_)).toSeq
          if (folded.isDefined)
            assertEquals(defn.show, constant, returned(defn))
          else
            assertFalse(
              defn.show,
              returned(defn)
                .exists(v => v == nir.Val.True || v == nir.Val.False)
            )
        }
        check("maxGt", None)
        check("gtMax", Some(false))
        check("maxLe", None)
        check("leMax", Some(true))
    }

  @Test def combinedComparisonsKeepTheirOperands(): Unit =
    optimize("Test", sources, _.withMode(Mode.releaseFull)) {
      case (_, result) =>
        val defn = optimizedMethod(result.defns, "Test$", "gtOrEq")
        val nir.Inst.Label(_, Seq(_, x, y)) = defn.insts.head: @unchecked
        val sge = defn.insts.collect {
          case nir.Inst.Let(_, nir.Op.Comp(nir.Comp.Sge, _, l, r), _) =>
            (l, r)
        }
        assertEquals(defn.show, Seq((x, y)), sge)
    }
}
