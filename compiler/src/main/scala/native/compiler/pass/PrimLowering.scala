package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Op.Prim*
 *  - Type.{Character, Boolean, Byte, Short, Integer, Long, Float, Double}Class
 */
trait PrimLowering extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  private val box = Map[Type, Val](
    Type.CharacterClass -> Val.Global(Global.Atom("sn_box_char"),   Type.Ptr(Type.Function(Seq(Type.I16),  i8_*))),
    Type.BooleanClass   -> Val.Global(Global.Atom("sn_box_bool"),   Type.Ptr(Type.Function(Seq(Type.Bool), i8_*))),
    Type.ByteClass      -> Val.Global(Global.Atom("sn_box_byte"),   Type.Ptr(Type.Function(Seq(Type.I8),   i8_*))),
    Type.ShortClass     -> Val.Global(Global.Atom("sn_box_short"),  Type.Ptr(Type.Function(Seq(Type.I16),  i8_*))),
    Type.IntegerClass   -> Val.Global(Global.Atom("sn_box_int"),    Type.Ptr(Type.Function(Seq(Type.I32),  i8_*))),
    Type.LongClass      -> Val.Global(Global.Atom("sn_box_long"),   Type.Ptr(Type.Function(Seq(Type.I64),  i8_*))),
    Type.FloatClass     -> Val.Global(Global.Atom("sn_box_float"),  Type.Ptr(Type.Function(Seq(Type.F32),  i8_*))),
    Type.DoubleClass    -> Val.Global(Global.Atom("sn_box_double"), Type.Ptr(Type.Function(Seq(Type.F64),  i8_*)))
  )

  private val unbox = Map[Type, Val](
    Type.CharacterClass -> Val.Global(Global.Atom("sn_unbox_char"),   Type.Ptr(Type.Function(Seq(i8_*), Type.I16 ))),
    Type.BooleanClass   -> Val.Global(Global.Atom("sn_unbox_bool"),   Type.Ptr(Type.Function(Seq(i8_*), Type.Bool))),
    Type.ByteClass      -> Val.Global(Global.Atom("sn_unbox_byte"),   Type.Ptr(Type.Function(Seq(i8_*), Type.I8  ))),
    Type.ShortClass     -> Val.Global(Global.Atom("sn_unbox_short"),  Type.Ptr(Type.Function(Seq(i8_*), Type.I16 ))),
    Type.IntegerClass   -> Val.Global(Global.Atom("sn_unbox_int"),    Type.Ptr(Type.Function(Seq(i8_*), Type.I32 ))),
    Type.LongClass      -> Val.Global(Global.Atom("sn_unbox_long"),   Type.Ptr(Type.Function(Seq(i8_*), Type.I64 ))),
    Type.FloatClass     -> Val.Global(Global.Atom("sn_unbox_float"),  Type.Ptr(Type.Function(Seq(i8_*), Type.F32 ))),
    Type.DoubleClass    -> Val.Global(Global.Atom("sn_unbox_double"), Type.Ptr(Type.Function(Seq(i8_*), Type.F64 )))
  )

  override def onOp(op: Op) = super.onOp(op match {
    case Op.PrimBox(ty, value) =>
      val fun = box(ty)
      val Type.Ptr(sig) = fun.ty
      Op.Call(sig, fun, Seq(value))
    case Op.PrimUnbox(ty, value) =>
      val fun = unbox(ty)
      val Type.Ptr(sig) = fun.ty
      Op.Call(sig, fun, Seq(value))
    case _ =>
      op
  })

  override def onType(ty: Type) = super.onType(ty match {
    case Type.CharacterClass
       | Type.BooleanClass
       | Type.ByteClass
       | Type.ShortClass
       | Type.IntegerClass
       | Type.LongClass
       | Type.FloatClass
       | Type.DoubleClass => i8_*
    case _                => ty
  })
}
