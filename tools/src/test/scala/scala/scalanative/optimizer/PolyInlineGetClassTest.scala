package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec

class PolyInlineGetClassTest extends OptimizerSpec {

  /** Verifies that the poly-inline type-switch emits at most one
   *  `Op.Method(receiver, GetClassSig)` per receiver SSA value. Back-to-back
   *  virtual calls on the same receiver must share a single `getClass` load.
   */
  @Test def singleGetClassPerReceiver(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" ->
          """|import scala.scalanative.annotation.nooptimize
             |
             |sealed trait Shape {
             |  def area: Int
             |  def perim: Int
             |}
             |final class Square(s: Int) extends Shape {
             |  def area = s * s
             |  def perim = 4 * s
             |}
             |final class Triangle(s: Int) extends Shape {
             |  def area = s * s / 2
             |  def perim = 3 * s
             |}
             |
             |object Test {
             |  // @nooptimize prevents interflow from discovering the exact
             |  // class of the result, so the call sites on `shape` below
             |  // remain polymorphic and trigger polyInline.
             |  @nooptimize def pick(i: Int): Shape =
             |    if ((i & 1) == 0) new Square(i) else new Triangle(-i)
             |
             |  def main(args: Array[String]): Unit = {
             |    val shape = pick(args.length)
             |    println(shape.area)
             |    println(shape.perim)
             |  }
             |}
             |""".stripMargin
      ),
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFast)
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val getClassByReceiver = defn.insts
            .collect {
              case nir.Inst.Let(_, nir.Op.Method(obj, sig), _)
                  if sig == nir.Rt.GetClassSig =>
                obj
            }
            .groupBy(identity)
            .map { case (obj, occurrences) => obj -> occurrences.size }

          // The test is only meaningful if polyInline actually fired on at
          // least one receiver; otherwise the assertion below is vacuous and
          // the regression we care about can't be caught.
          assertTrue(
            "Expected at least one Op.Method(_, GetClassSig) in the " +
              "optimized entry - polyInline did not fire; adjust the fixture " +
              "so that a polymorphic call survives interflow.",
            getClassByReceiver.nonEmpty
          )

          getClassByReceiver.foreach {
            case (obj, count) =>
              assertEquals(
                s"Op.Method(_, GetClassSig) must appear at most once per " +
                  s"receiver, got $count occurrences for receiver $obj",
                1,
                count
              )
          }
        }
    }
  }

  @Test def distinctReceiversGetDistinctGetClass(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" ->
          """|import scala.scalanative.annotation.nooptimize
             |
             |sealed trait Shape {
             |  def area: Int
             |}
             |final class Square(s: Int) extends Shape {
             |  def area = s * s
             |}
             |final class Triangle(s: Int) extends Shape {
             |  def area = s * s / 2
             |}
             |
             |object Test {
             |  @nooptimize def pickA(i: Int): Shape =
             |    if ((i & 1) == 0) new Square(i) else new Triangle(-i)
             |
             |  @nooptimize def pickB(i: Int): Shape =
             |    if ((i & 2) == 0) new Triangle(i) else new Square(-i)
             |
             |  def main(args: Array[String]): Unit = {
             |    val a = pickA(args.length)
             |    val b = pickB(args.length)
             |    println(a.area)
             |    println(b.area)
             |  }
             |}
             |""".stripMargin
      ),
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFast)
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val receivers = defn.insts.collect {
            case nir.Inst.Let(_, nir.Op.Method(obj, sig), _)
                if sig == nir.Rt.GetClassSig =>
              obj
          }

          // Sanity: polyInline actually fired for both receivers.
          assertTrue(
            "Expected at least two Op.Method(_, GetClassSig) occurrences " +
              s"(one per independent receiver); got: $receivers",
            receivers.size >= 2
          )

          // Different receivers must NOT be collapsed into a single
          // getClass - the cache is keyed per SSA receiver value, not
          // per call site.
          val distinctReceivers = receivers.distinct
          assertTrue(
            "Distinct receivers must each have their own " +
              s"Op.Method(_, GetClassSig); got receivers: $receivers",
            distinctReceivers.size >= 2
          )
        }
    }
  }
}
