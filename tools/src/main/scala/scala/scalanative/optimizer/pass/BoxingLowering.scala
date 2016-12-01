package scala.scalanative
package optimizer
package pass

import nir._
import analysis.ClassHierarchy.Top
import tools.Config
import util.unsupported

/**
  * Created by lukaskellenberger on 01.12.16.
  */
class BoxingLowering(implicit val fresh: Fresh) extends Pass {
  override def preInst = {
    case Inst.Let(name, box @ Op.Box(code, from)) =>
      val (module, id) = BoxingLowering.BoxTo(code)

      val ty = Type.Function(Seq(Arg(Type.Module(module)), Arg(from.ty)), box.resty)

      Seq(Inst.Let(name, Op.Call(
        ty,
        Val.Global(Global.Member(module, id), Type.Ptr),
        Seq(
          Val.Undef(Type.Module(module)),
          from
        )
      )))

    case Inst.Let(name, unbox @ Op.Unbox(code, from)) =>
      val (module, id) = BoxingLowering.UnboxTo(code)

      val ty = Type.Function(Seq(Arg(Type.Module(module)), Arg(from.ty)), unbox.resty)

      Seq(Inst.Let(name, Op.Call(
        ty,
        Val.Global(Global.Member(module, id), Type.Ptr),
        Seq(
          Val.Undef(Type.Module(module)),
          from
        )
      )))
  }
}

object BoxingLowering extends PassCompanion {
  override def apply(config: Config, top: Top) = new BoxingLowering()(top.fresh)


  private val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  private val RuntimeBoxes = Global.Top("scala.scalanative.runtime.Boxes$")

  val BoxTo: Map[Char, (Global, String)] = Seq(
    ('B', BoxesRunTime, "boxToBoolean_bool_class.java.lang.Boolean"),
    ('C', BoxesRunTime, "boxToCharacter_i16_class.java.lang.Character"),
    ('z', RuntimeBoxes, "boxToUByte_i8_class.java.lang.Object"),
    ('Z', BoxesRunTime, "boxToByte_i8_class.java.lang.Byte"),
    ('s', RuntimeBoxes, "boxToUShort_i16_class.java.lang.Object"),
    ('S', BoxesRunTime, "boxToShort_i16_class.java.lang.Short"),
    ('i', RuntimeBoxes, "boxToUInt_i32_class.java.lang.Object"),
    ('I', BoxesRunTime, "boxToInteger_i32_class.java.lang.Integer"),
    ('l', RuntimeBoxes, "boxToULong_i64_class.java.lang.Object"),
    ('L', BoxesRunTime, "boxToLong_i64_class.java.lang.Long"),
    ('F', BoxesRunTime, "boxToFloat_f32_class.java.lang.Float"),
    ('D', BoxesRunTime, "boxToDouble_f64_class.java.lang.Double")
  ).map {
    case (code, module, id) =>
      code -> (module, id)
  }.toMap

  val UnboxTo: Map[Char, (Global, String)] = Seq(
    ('B', BoxesRunTime, "unboxToBoolean_class.java.lang.Object_bool"),
    ('C', BoxesRunTime, "unboxToChar_class.java.lang.Object_i16"),
    ('z', RuntimeBoxes, "unboxToUByte_class.java.lang.Object_i8"),
    ('Z', BoxesRunTime, "unboxToByte_class.java.lang.Object_i8"),
    ('s', RuntimeBoxes, "unboxToUShort_class.java.lang.Object_i16"),
    ('S', BoxesRunTime, "unboxToShort_class.java.lang.Object_i16"),
    ('i', RuntimeBoxes, "unboxToUInt_class.java.lang.Object_i32"),
    ('I', BoxesRunTime, "unboxToInt_class.java.lang.Object_i32"),
    ('l', RuntimeBoxes, "unboxToULong_class.java.lang.Object_i64"),
    ('L', BoxesRunTime, "unboxToLong_class.java.lang.Object_i64"),
    ('F', BoxesRunTime, "unboxToFloat_class.java.lang.Object_f32"),
    ('D', BoxesRunTime, "unboxToDouble_class.java.lang.Object_f64")
  ).map {
    case (code, module, id) =>
      code -> (module, id)
  }.toMap
}
