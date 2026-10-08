package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec

import _root_.scala.scalanative.nir.Attr

class InliningTest extends OptimizerSpec {

  @Test def negativeZeroSurvivesObjectAndArrayMaterialization(): Unit = {
    optimize(
      entry = "Test",
      setupConfig = _.withMode(scalanative.build.Mode.releaseFast),
      sources = Map(
        "Test.scala" ->
          """|import scala.noinline
             |final class FloatBox(val value: Float)
             |final class DoubleBox(val value: Double)
             |object Test {
             |  @noinline def consume(value: AnyRef): Unit = println(value)
             |  def main(args: Array[String]): Unit = {
             |    consume(new FloatBox(-0.0f))
             |    consume(new DoubleBox(-0.0d))
             |    consume(java.lang.Float.valueOf(-0.0f))
             |    consume(java.lang.Double.valueOf(-0.0d))
             |    val floats = new Array[Float](1)
             |    floats(0) = -0.0f
             |    consume(floats)
             |    val doubles = new Array[Double](1)
             |    doubles(0) = -0.0d
             |    consume(doubles)
             |  }
             |}
             |""".stripMargin
      )
    ) {
      case (_, result) =>
        val entry = findEntry(result.defns).get
        val stores = entry.insts.collect {
          case nir.Inst.Let(_, nir.Op.Fieldstore(_, _, _, value), _) => value
        }
        assertEquals(
          "custom and boxed Float must retain negative zero",
          2,
          stores.count {
            case nir.Val.Float(v) =>
              java.lang.Float.floatToRawIntBits(v) == Int.MinValue
            case _ => false
          }
        )
        assertEquals(
          "custom and boxed Double must retain negative zero",
          2,
          stores.count {
            case nir.Val.Double(v) =>
              java.lang.Double.doubleToRawLongBits(v) == Long.MinValue
            case _ => false
          }
        )
        val arrays = entry.insts.collect {
          case nir.Inst.Let(
                _,
                nir.Op.Arrayalloc(_, nir.Val.ArrayValue(_, values), _),
                _
              ) =>
            values
        }.flatten
        assertTrue(
          "Float array must retain negative zero\n" + entry.show,
          arrays.exists {
            case nir.Val.Float(v) =>
              java.lang.Float.floatToRawIntBits(v) == Int.MinValue
            case _ => false
          }
        )
        assertTrue(
          "Double array must retain negative zero\n" + entry.show,
          arrays.exists {
            case nir.Val.Double(v) =>
              java.lang.Double.doubleToRawLongBits(v) == Long.MinValue
            case _ => false
          }
        )
    }
  }

  @Test def issue4152(): Unit = {
    optimize(
      setupConfig = _.withMode(scalanative.build.Mode.releaseFast),
      entry = "Test",
      sources = Map(
        "Test.scala" ->
          """|
             |import scala.scalanative.annotation.alwaysinline
             |
             |object Test {
             |  abstract class NatTag {
             |    def toInt: Int
             |  }
             |
             |  class Base(value: Int) extends NatTag {
             |    def toInt: Int = value
             |  }
             |
             |  class Digit2(a: NatTag, b: NatTag) extends NatTag {
             |    @alwaysinline def toInt: Int = a.toInt + b.toInt
             |  }
             |
             |  def main(args: Array[String]): Unit = {
             |    println(new Digit2(new Base(1), new Base(2)).toInt)
             |  }
             |}
             |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          assertEquals(nir.Attr.DidOpt, defn.attrs.opt)
          // Do nothing, it did not lead to StackOverflowError
        }
    }
  }

  @Test def issue4152_digitEval(): Unit = {
    optimize(
      setupConfig = _.withMode(scalanative.build.Mode.releaseFast),
      entry = "Test",
      sources = Map(
        "Test.scala" ->
          """|
             |import scala.scalanative.unsafe._
             |
             |object Test {
             |  def main(args: Array[String]): Unit = {
             |    println(Tag.materializeNatDigit2Tag[Nat._1, Nat._2].toInt)
             |    println(implicitly[Tag[CArray[Int, Nat.Digit5[Nat._1, Nat._2, Nat._5, Nat._8, Nat._2]]]].size)
             |    val cArr = stackalloc[CArray[Long, Nat.Digit2[Nat._3, Nat._2]]]()
             |    println(cArr.length)
             | }
             |}
             |
             |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val jlInteger = nir.Type.Ref(nir.Global.Top("java.lang.Integer"))
          assertContainsAll(
            "Not all expression were inlined and constant folded",
            expected = Seq(
              nir.Val.Int(12), // Digi2[Nat._1, Nat._2].toInt
              nir.Val.Int(50328), // Tag[CArray[Int, 12582]].size
              nir.Val.Int(32) // (cArr: CArray[Long, 32]).length
            ),
            actual = defn.insts.collect {
              case nir.Inst.Let(
                    _,
                    nir.Op.Box(`jlInteger`, literalValue: nir.Val.Int),
                    _
                  ) =>
                literalValue
            }
          )
        }
    }
  }
}
