package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert.*

class SubSuite extends ReachabilitySuite {

  val source = """
    class A extends T1
    class B extends A with T3
    class C extends T2
    trait T1
    trait T2 extends T1
    trait T3

    object Main {
      def main(args: Array[String]): Unit =
        println(Seq(new A, new B, new C))
    }
  """

  val MainClass = "Main"
  val entry: nir.Global.Member =
    nir.Global.Top(MainClass).member(nir.Rt.ScalaMainSig)

  implicit val analysis: ReachabilityAnalysis.Result =
    link(Seq(entry), Seq(source), MainClass) {
      case result: ReachabilityAnalysis.Result => result
      case _ => fail("Failed to link"); util.unreachable
    }

  val primitiveTypes = Seq(
    nir.Type.Bool,
    nir.Type.Ptr,
    nir.Type.Char,
    nir.Type.Byte,
    nir.Type.Short,
    nir.Type.Int,
    nir.Type.Long,
    nir.Type.Float,
    nir.Type.Double
  )

  val aggregateTypes = Seq(
    nir.Type.StructValue(Seq(nir.Type.Bool, nir.Type.Int)),
    nir.Type.ArrayValue(nir.Type.Byte, 32)
  )

  val valueTypes =
    primitiveTypes ++ aggregateTypes

  val A = nir.Type.Ref(nir.Global.Top("A"))
  val B = nir.Type.Ref(nir.Global.Top("B"))
  val C = nir.Type.Ref(nir.Global.Top("C"))
  val T1 = nir.Type.Ref(nir.Global.Top("T1"))
  val T2 = nir.Type.Ref(nir.Global.Top("T2"))
  val T3 = nir.Type.Ref(nir.Global.Top("T3"))

  val referenceTypes = Seq(
    nir.Type.Null,
    nir.Type.Unit,
    nir.Type.Array(nir.Type.Int),
    A,
    B,
    C,
    T1,
    T2,
    T3
  )

  val types =
    valueTypes ++ referenceTypes

  def testIs(l: nir.Type, r: nir.Type) =
    assertTrue(s"${l.show} is ${r.show}", Sub.is(l, r))

  def testIsNot(l: nir.Type, r: nir.Type) =
    assertTrue(s"${l.show} is not ${r.show}", !Sub.is(l, r))

  @Test def valueTypeWithvalueTypes(): Unit = {
    valueTypes.foreach { v1 =>
      valueTypes.foreach { v2 =>
        if (v1 == v2) {
          testIs(v1, v2)
        } else {
          testIsNot(v1, v2)
        }
      }
    }
  }

  @Test def valueTypeWitRefTypes(): Unit = {
    valueTypes.foreach { vty =>
      referenceTypes.filter(_ != nir.Type.Null).foreach { rty =>
        testIsNot(vty, rty)
        testIsNot(rty, vty)
      }
    }
  }

  @Test def nullTypes(): Unit =
    referenceTypes.foreach { rty => testIs(nir.Type.Null, rty) }

  @Test def nothingType(): Unit =
    types.foreach { ty => testIs(nir.Type.Nothing, ty) }

  @Test def referenceObjectTypes(): Unit =
    referenceTypes.foreach { rty =>
      testIs(rty, nir.Type.Ref(nir.Global.Top("java.lang.Object")))
    }

  @Test def inheritence(): Unit = {
    testIs(A, A)
    testIsNot(A, B)
    testIsNot(A, C)
    testIs(A, T1)
    testIsNot(A, T2)
    testIsNot(A, T3)

    testIs(B, A)
    testIs(B, B)
    testIsNot(B, C)
    testIs(B, T1)
    testIsNot(B, T2)
    testIs(B, T3)

    testIsNot(C, A)
    testIsNot(C, B)
    testIs(C, C)
    testIs(C, T1)
    testIs(C, T2)
    testIsNot(C, T3)

    testIsNot(T1, A)
    testIsNot(T1, B)
    testIsNot(T1, C)
    testIs(T1, T1)
    testIsNot(T1, T2)
    testIsNot(T1, T3)

    testIsNot(T2, A)
    testIsNot(T2, B)
    testIsNot(T2, C)
    testIs(T2, T1)
    testIs(T2, T2)
    testIsNot(T2, T3)

    testIsNot(T3, A)
    testIsNot(T3, B)
    testIsNot(T3, C)
    testIsNot(T3, T1)
    testIsNot(T3, T2)
    testIs(T3, T3)
  }

}
