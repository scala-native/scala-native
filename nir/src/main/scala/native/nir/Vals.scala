package native
package nir

sealed abstract class Val {
  final def ty: Type = this match {
    case Val.None             => Type.None
    case Val.Zero(ty)         => ty
    case Val.True | Val.False => Type.Bool
    case Val.I8(_)            => Type.I8
    case Val.I16(_)           => Type.I16
    case Val.I32(_)           => Type.I32
    case Val.I64(_)           => Type.I64
    case Val.F32(_)           => Type.F32
    case Val.F64(_)           => Type.F64
    case Val.Struct(name, _)  => Type.Struct(name)
    case Val.Array(ty, vals)  => Type.Array(ty, vals.length)
    case Val.Chars(s)         => Type.Array(Type.I8, s.length)
    case Val.Local(_, ty)     => ty
    case Val.Global(_, ty)    => ty

    case Val.Bitcast(to, _) => to

    case Val.String(_)         => Intr.string
    case Val.ClassValue(n, _) => Type.ClassValue(n)
  }
}
object Val {
  // low-level
  final case object None                                      extends Val
  final case object True                                      extends Val
  final case object False                                     extends Val
  final case class Zero(zeroty: nir.Type)                     extends Val
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

  // constant exprs
  final case class Bitcast(to: Type, value: Val) extends Val

  // high-level
  final case class String(value: java.lang.String)                extends Val
  final case class ClassValue(name: nir.Global, values: Seq[Val]) extends Val
}
