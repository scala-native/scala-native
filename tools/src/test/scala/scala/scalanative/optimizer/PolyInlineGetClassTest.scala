package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec

class PolyInlineGetClassTest extends OptimizerSpec {

  @Test def alwaysInlineTargetsAreInlinedIntoTheirBranches(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" ->
          """|import scala.scalanative.annotation.{alwaysinline, nooptimize}
             |sealed trait Operation { def apply(value: Int): Int }
             |final class Add extends Operation {
             |  @alwaysinline def apply(value: Int): Int = value + 1
             |}
             |final class Subtract extends Operation {
             |  @alwaysinline def apply(value: Int): Int = value - 1
             |}
             |object Test {
             |  @nooptimize def choose(value: Int): Operation =
             |    if (value == 0) new Add else new Subtract
             |  def main(args: Array[String]): Unit = {
             |    val operation = choose(args.length)
             |    println(operation(args.length))
             |  }
             |}
             |""".stripMargin
      ),
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFast)
    ) {
      case (_, result) =>
        val entry = findEntry(result.defns).get
        assertTrue(
          "the fixture must retain polymorphic dispatch",
          entry.insts.exists {
            case nir.Inst.Let(_, nir.Op.Method(_, sig), _) =>
              sig == nir.Rt.GetClassSig
            case _ => false
          }
        )
        assertFalse(
          "alwaysinline targets must not remain out-of-line:\n" + entry.show + "\n" + result.defns
            .collect {
              case d: nir.Defn.Define
                  if Set("Add", "Subtract").contains(d.name.top.id) =>
                d.show
            }
            .mkString("\n"),
          entry.insts.exists {
            case nir.Inst.Let(
                  _,
                  nir.Op
                    .Call(_, nir.Val.Global(method: nir.Global.Member, _), _),
                  _
                ) =>
              Set("Add", "Subtract").contains(
                method.top.id
              ) && (method.sig.unmangled match {
                case nir.Sig.Method("apply", _, _) => true
                case _                             => false
              })
            case _ => false
          }
        )
    }
  }

  @Test def inlinedBranchAllocationsAndWritesRemainIndependent(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" ->
          """|import scala.scalanative.annotation.{alwaysinline, nooptimize}
             |final class Box(var value: Int)
             |sealed trait Operation { def apply(box: Box): Box }
             |final class First extends Operation {
             |  @alwaysinline def apply(box: Box): Box = { box.value = 7; new Box(17) }
             |}
             |final class Second extends Operation {
             |  @alwaysinline def apply(box: Box): Box = { box.value = 11; new Box(23) }
             |}
             |object Test {
             |  @nooptimize def choose(value: Int): Operation =
             |    if (value == 0) new First else new Second
             |  def main(args: Array[String]): Unit = {
             |    val box = new Box(0)
             |    val result = choose(args.length)(box)
             |    println(box.value + result.value)
             |  }
             |}
             |""".stripMargin
      ),
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFast)
    ) {
      case (_, result) =>
        val entry = findEntry(result.defns).get
        val writes = entry.insts.collect {
          case nir.Inst.Let(
                _,
                nir.Op.Fieldstore(_, _, field, nir.Val.Int(value)),
                _
              ) if field.top.id == "Box" =>
            value
        }.toSet
        assertTrue(
          "both branches must retain their writes and returned objects: " + writes + "\n" + entry.show,
          Set(7, 11, 17, 23).subsetOf(writes)
        )
        assertFalse(
          "both branch targets must be inlined",
          entry.insts.exists {
            case nir.Inst.Let(
                  _,
                  nir.Op
                    .Call(_, nir.Val.Global(method: nir.Global.Member, _), _),
                  _
                ) =>
              Set("First", "Second").contains(
                method.top.id
              ) && (method.sig.unmangled match {
                case nir.Sig.Method("apply", _, _) => true
                case _                             => false
              })
            case _ => false
          }
        )
        assertTrue(
          "the joined result must still be read",
          entry.insts.exists {
            case nir.Inst.Let(_, nir.Op.Fieldload(_, _, field), _) =>
              field.top.id == "Box"
            case _ => false
          }
        )
    }
  }

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
