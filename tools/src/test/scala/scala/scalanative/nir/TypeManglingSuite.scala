package scala.scalanative
package nir

import org.scalatest._

class TypeManglingSuite extends FunSuite {
  Seq(
    Type.Vararg,
    Type.Ptr,
    Type.Byte,
    Type.Short,
    Type.Int,
    Type.Long,
    Type.Float,
    Type.Double,
    Type.ArrayValue(Type.Byte, 256),
    Type.StructValue(Seq(Type.Byte)),
    Type.StructValue(Seq(Type.Byte, Type.Int)),
    Type.StructValue(Seq(Type.Byte, Type.Int, Type.Float)),
    Type.Function(Seq.empty, Type.Int),
    Type.Function(Seq(Type.Int), Type.Int),
    Type.Function(Seq(Type.Float, Type.Int), Type.Int),
    Type.Null,
    Type.Nothing,
    Type.Unit,
    Type.Array(Rt.Object, nullable = false),
    Type.Array(Rt.Object, nullable = true),
    Type.Ref(Rt.Object.name, exact = true, nullable = true),
    Type.Ref(Rt.Object.name, exact = true, nullable = false),
    Type.Ref(Rt.Object.name, exact = false, nullable = true),
    Type.Ref(Rt.Object.name, exact = false, nullable = false)
  ).foreach { ty =>
    test(s"mangle/unmangle type `${ty.toString}`") {
      val mangled = ty.mangle
      assert(mangled.nonEmpty, "empty mangle")
      val unmangled = Unmangle.unmangleType(mangled)
      assert(unmangled == ty, "different unmangle")
      val remangled = unmangled.mangle
      assert(mangled == remangled, "different remangle")
    }
  }
}
