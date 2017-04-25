package scala.scalanative
package optimizer
package pass

import nir._
import analysis.ClassHierarchy.Top
import tools.Config

/** Translates Box/Unbox ops into static method calls. */
class BoxingLowering(implicit val fresh: Fresh) extends Pass {

  override def onInst(inst: Inst): Inst = inst match {
    case Inst.Let(name, box @ Op.Box(ty, from)) =>
      val (module, id) = BoxingLowering.BoxTo(ty)

      val boxTy =
        Type.Function(Seq(Type.Module(module), Type.unbox(ty)), ty)

      Inst.Let(name,
               Op.Call(boxTy,
                       Val.Global(Global.Member(module, id), Type.Ptr),
                       Seq(
                         Val.Undef(Type.Module(module)),
                         from
                       ),
                       Next.None))

    case Inst.Let(name, unbox @ Op.Unbox(ty, from)) =>
      val (module, id) = BoxingLowering.UnboxTo(ty)

      val unboxTy =
        Type.Function(Seq(Type.Module(module), ty), Type.unbox(ty))

      Inst.Let(name,
               Op.Call(unboxTy,
                       Val.Global(Global.Member(module, id), Type.Ptr),
                       Seq(
                         Val.Undef(Type.Module(module)),
                         from
                       ),
                       Next.None))

    case inst =>
      inst
  }
}

object BoxingLowering extends PassCompanion {
  override def apply(config: Config, top: Top) =
    new BoxingLowering()(top.fresh)

  override def depends: Seq[Global] =
    Seq(BoxesRunTime, RuntimeBoxes) ++
      BoxTo.values.map { case (owner, id)   => Global.Member(owner, id) } ++
      UnboxTo.values.map { case (owner, id) => Global.Member(owner, id) }

  private val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  private val RuntimeBoxes = Global.Top("scala.scalanative.runtime.Boxes$")

  val BoxTo: Map[Type, (Global, String)] = Seq(
    ("java.lang.Boolean", BoxesRunTime, "boxToBoolean_bool_java.lang.Boolean"),
    ("java.lang.Character",
     BoxesRunTime,
     "boxToCharacter_char_java.lang.Character"),
    ("scala.scalanative.native.UByte",
     RuntimeBoxes,
     "boxToUByte_i8_java.lang.Object"),
    ("java.lang.Byte", BoxesRunTime, "boxToByte_i8_java.lang.Byte"),
    ("scala.scalanative.native.UShort",
     RuntimeBoxes,
     "boxToUShort_i16_java.lang.Object"),
    ("java.lang.Short", BoxesRunTime, "boxToShort_i16_java.lang.Short"),
    ("scala.scalanative.native.UInt",
     RuntimeBoxes,
     "boxToUInt_i32_java.lang.Object"),
    ("java.lang.Integer", BoxesRunTime, "boxToInteger_i32_java.lang.Integer"),
    ("scala.scalanative.native.ULong",
     RuntimeBoxes,
     "boxToULong_i64_java.lang.Object"),
    ("java.lang.Long", BoxesRunTime, "boxToLong_i64_java.lang.Long"),
    ("java.lang.Float", BoxesRunTime, "boxToFloat_f32_java.lang.Float"),
    ("java.lang.Double", BoxesRunTime, "boxToDouble_f64_java.lang.Double")
  ).map {
    case (name, module, id) =>
      Type.Class(Global.Top(name)) -> (module, id)
  }.toMap

  val UnboxTo: Map[Type, (Global, String)] = Seq(
    ("java.lang.Boolean",
     BoxesRunTime,
     "unboxToBoolean_java.lang.Object_bool"),
    ("java.lang.Character", BoxesRunTime, "unboxToChar_java.lang.Object_char"),
    ("scala.scalanative.native.UByte",
     RuntimeBoxes,
     "unboxToUByte_java.lang.Object_i8"),
    ("java.lang.Byte", BoxesRunTime, "unboxToByte_java.lang.Object_i8"),
    ("scala.scalanative.native.UShort",
     RuntimeBoxes,
     "unboxToUShort_java.lang.Object_i16"),
    ("java.lang.Short", BoxesRunTime, "unboxToShort_java.lang.Object_i16"),
    ("scala.scalanative.native.UInt",
     RuntimeBoxes,
     "unboxToUInt_java.lang.Object_i32"),
    ("java.lang.Integer", BoxesRunTime, "unboxToInt_java.lang.Object_i32"),
    ("scala.scalanative.native.ULong",
     RuntimeBoxes,
     "unboxToULong_java.lang.Object_i64"),
    ("java.lang.Long", BoxesRunTime, "unboxToLong_java.lang.Object_i64"),
    ("java.lang.Float", BoxesRunTime, "unboxToFloat_java.lang.Object_f32"),
    ("java.lang.Double", BoxesRunTime, "unboxToDouble_java.lang.Object_f64")
  ).map {
    case (name, module, id) =>
      Type.Class(Global.Top(name)) -> (module, id)
  }.toMap
}
