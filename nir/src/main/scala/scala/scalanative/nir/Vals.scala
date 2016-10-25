package scala.scalanative
package nir

sealed abstract class Val {
  final def ty: Type = this match {
    case Val.None               => Type.None
    case Val.Zero(ty)           => ty
    case Val.Undef(ty)          => ty
    case Val.True | Val.False   => Type.Bool
    case Val.I8(_)              => Type.I8
    case Val.I16(_)             => Type.I16
    case Val.I32(_)             => Type.I32
    case Val.I64(_)             => Type.I64
    case Val.F32(_)             => Type.F32
    case Val.F64(_)             => Type.F64
    case Val.Struct(name, vals) => Type.Struct(name, vals.map(_.ty))
    case Val.Array(ty, vals)    => Type.Array(ty, vals.length)
    case Val.Chars(s)           => Type.Array(Type.I8, s.getBytes.length + 1)
    case Val.Local(_, ty)       => ty
    case Val.Global(_, ty)      => ty

    case Val.Unit      => Type.Unit
    case Val.Const(_)  => Type.Ptr
    case Val.String(_) => Rt.String
  }
}
object Val {
  // low-level
  final case object None                                      extends Val
  final case object True                                      extends Val
  final case object False                                     extends Val
  final case class Zero(of: nir.Type)                         extends Val
  final case class Undef(of: nir.Type)                        extends Val
  final case class I8(value: Byte)                            extends Val
  final case class I16(value: Short)                          extends Val
  final case class I32(value: Int)                            extends Val
  final case class I64(value: Long)                           extends Val
  final case class F32(value: Float)                          extends Val
  final case class F64(value: Double)                         extends Val
  final case class Struct(name: nir.Global, values: Seq[Val]) extends Val
  final case class Array(elemty: nir.Type, values: Seq[Val])  extends Val
  final case class Chars(value: java.lang.String)             extends Val
  final case class Local(name: nir.Local, valty: nir.Type)    extends Val
  final case class Global(name: nir.Global, valty: nir.Type)  extends Val
  val Null                = Zero(Type.Ptr)
  def Bool(bool: Boolean) = if (bool) True else False

  // high-level
  final case object Unit                           extends Val
  final case class Const(value: Val)               extends Val
  final case class String(value: java.lang.String) extends Val
}
