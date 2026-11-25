package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec

// Folding of array type tests must take the element type into account
class ArrayTypeTestFoldingTest extends OptimizerSpec {

  private def sinkArgs(entry: nir.Defn.Define): Seq[nir.Val] =
    entry.insts.collect {
      case nir.Inst.Let(
            _,
            nir.Op.Call(_, nir.Val.Global(m: nir.Global.Member, _), args),
            _
          ) if m.sig.show.contains("sink") =>
        args.last
    }

  private def releaseFast(c: build.NativeConfig) =
    c.withMode(build.Mode.releaseFast)

  // False tests may fold to false or stay, never to true or a null check
  private def assertNotFoldedToTrue(entry: nir.Defn.Define): Unit = {
    val arrayTests = entry.insts.collect {
      case nir.Inst.Let(n, nir.Op.Is(_: nir.Type.Array, _), _) => n
    }.toSet
    val args = sinkArgs(entry)
    assertTrue(entry.show, args.nonEmpty)
    args.zipWithIndex.foreach {
      case (arg, idx) =>
        val ok = arg match {
          case nir.Val.False       => true
          case nir.Val.Local(n, _) => arrayTests.contains(n)
          case _                   => false
        }
        assertTrue(s"type test #$idx folded to $arg:\n${entry.show}", ok)
    }
  }

  @Test def isInstanceOfOnKnownArrayAllocations(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" ->
        """|import scala.scalanative.annotation.nooptimize
           |class Elem
           |class SubElem extends Elem
           |object Test {
           |  @noinline @nooptimize def sink(b: Boolean): Unit = println(b)
           |  def main(args: Array[String]): Unit = {
           |    val objects: AnyRef = new Array[AnyRef](1)
           |    sink(objects.isInstanceOf[Array[String]])
           |    val sized: AnyRef = new Array[AnyRef](args.length)
           |    sink(sized.isInstanceOf[Array[String]])
           |    val elems: AnyRef = new Array[Elem](1)
           |    sink(elems.isInstanceOf[Array[SubElem]])
           |    val matrix: AnyRef = new Array[Array[Int]](1)
           |    sink(matrix.isInstanceOf[Array[Array[Long]]])
           |    sink(matrix.isInstanceOf[Array[Array[AnyRef]]])
           |  }
           |}
           |""".stripMargin
    ),
    setupConfig = releaseFast
  ) {
    case (_, result) =>
      val entry = findEntry(result.defns).get
      assertEquals(entry.show, 5, sinkArgs(entry).size)
      assertNotFoldedToTrue(entry)
  }

  @Test def isInstanceOfOnStaticallyTypedArrays(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" ->
        """|import scala.scalanative.annotation.nooptimize
           |object Test {
           |  @noinline @nooptimize def sink(b: Boolean): Unit = println(b)
           |  @noinline @nooptimize def objects(): Array[AnyRef] = new Array[AnyRef](1)
           |  def main(args: Array[String]): Unit = {
           |    val arr: AnyRef = objects()
           |    sink(arr.isInstanceOf[Array[String]])
           |  }
           |}
           |""".stripMargin
    ),
    setupConfig = releaseFast
  ) {
    case (_, result) =>
      val entry = findEntry(result.defns).get
      assertEquals(entry.show, 1, sinkArgs(entry).size)
      assertNotFoldedToTrue(entry)
  }

  @Test def castOfKnownArrayIsKept(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" ->
        """|import scala.scalanative.annotation.nooptimize
           |object Test {
           |  @noinline @nooptimize def sink(b: Boolean): Unit = println(b)
           |  def main(args: Array[String]): Unit = {
           |    val objects: AnyRef = new Array[AnyRef](1)
           |    val strings = objects.asInstanceOf[Array[String]]
           |    sink(strings.length == 1)
           |  }
           |}
           |""".stripMargin
    ),
    setupConfig = releaseFast
  ) {
    case (_, result) =>
      val entry = findEntry(result.defns).get
      // The cast always fails: either it stays, or the code after it is gone
      val keepsCast = entry.insts.exists {
        case nir.Inst.Let(_, nir.Op.As(nir.Type.Array(elem, _), _), _) =>
          elem == nir.Rt.String
        case _ => false
      }
      assertTrue(entry.show, keepsCast || sinkArgs(entry).isEmpty)
  }
}
