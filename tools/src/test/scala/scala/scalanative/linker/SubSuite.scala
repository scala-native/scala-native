package scala.scalanative
package linker

import scalanative.nir._

import org.scalatest._

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

  val entry = Global.Member(
    Global.Top("Main$"),
    Sig.Method("main", Seq(Type.Array(Rt.String), Type.Unit)))

  implicit val linked = link(Seq(entry), Seq(source))(x => x)

  val primitiveTypes = Seq(
    Type.Bool,
    Type.Ptr,
    Type.Char,
    Type.Byte,
    Type.Short,
    Type.Int,
    Type.Long,
    Type.Float,
    Type.Double
  )

  val aggregateTypes = Seq(
    Type.StructValue(Seq(Type.Bool, Type.Int)),
    Type.ArrayValue(Type.Byte, 32)
  )

  val valueTypes =
    primitiveTypes ++ aggregateTypes

  val A  = Type.Ref(Global.Top("A"))
  val B  = Type.Ref(Global.Top("B"))
  val C  = Type.Ref(Global.Top("C"))
  val T1 = Type.Ref(Global.Top("T1"))
  val T2 = Type.Ref(Global.Top("T2"))
  val T3 = Type.Ref(Global.Top("T3"))

  val referenceTypes = Seq(
    Type.Null,
    Type.Unit,
    Type.Array(Type.Int),
    A,
    B,
    C,
    T1,
    T2,
    T3
  )

  val types =
    valueTypes ++ referenceTypes

  def testIs(l: Type, r: Type) =
    test(s"${l.show} is ${r.show}") {
      assert(Sub.is(l, r))
    }

  def testIsNot(l: Type, r: Type) =
    test(s"${l.show} is not ${r.show}") {
      assert(!Sub.is(l, r))
    }

  valueTypes.foreach { v1 =>
    valueTypes.foreach { v2 =>
      if (v1 == v2) {
        testIs(v1, v2)
      } else {
        testIsNot(v1, v2)
      }
    }
  }

  valueTypes.foreach { vty =>
    referenceTypes.filter(_ != Type.Null).foreach { rty =>
      testIsNot(vty, rty)
      testIsNot(rty, vty)
    }
  }

  referenceTypes.foreach { rty =>
    testIs(Type.Null, rty)
  }

  types.foreach { ty =>
    testIs(Type.Nothing, ty)
  }

  referenceTypes.foreach { rty =>
    testIs(rty, Type.Ref(Global.Top("java.lang.Object")))
  }

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
