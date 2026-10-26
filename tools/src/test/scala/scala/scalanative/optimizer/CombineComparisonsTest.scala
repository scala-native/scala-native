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

  /** The optimized copy of the method `name` of `owner`. */
  private def method(
      defns: Seq[nir.Defn],
      owner: String,
      name: String
  ): nir.Defn.Define = {
    // The mangled name of a method starts with its length
    val mangled = s"D${name.length}$name"
    val found = defns.collect {
      case d: nir.Defn.Define
          if d.name.top.id == owner && d.name.show.contains(mangled) =>
        d
    }
    assertTrue(s"$owner.$name not found", found.nonEmpty)
    found.maxBy(_.attrs.opt == nir.Attr.DidOpt)
  }

  private def returned(defn: nir.Defn.Define): Seq[nir.Val] =
    defn.insts.collect { case nir.Inst.Ret(v) => v }

  @Test def comparisonsWithMinAndMaxValues(): Unit =
    optimize("Test", sources, _.withMode(Mode.releaseFull)) {
      case (_, result) =>
        def check(name: String, folded: Option[Boolean]): Unit = {
          val defn = method(result.defns, "Test$", name)
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
        val defn = method(result.defns, "Test$", "gtOrEq")
        val nir.Inst.Label(_, Seq(_, x, y)) = defn.insts.head: @unchecked
        val sge = defn.insts.collect {
          case nir.Inst.Let(_, nir.Op.Comp(nir.Comp.Sge, _, l, r), _) =>
            (l, r)
        }
        assertEquals(defn.show, Seq((x, y)), sge)
    }
}
