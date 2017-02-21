package scala.scalanative
package nir

import java.lang.Float.floatToRawIntBits
import java.lang.Double.doubleToRawLongBits

sealed abstract class Val {
  final def ty: Type = this match {
    case Val.None               => Type.None
    case Val.Zero(ty)           => ty
    case Val.Undef(ty)          => ty
    case Val.True | Val.False   => Type.Bool
    case Val.Byte(_)            => Type.Byte
    case Val.Short(_)           => Type.Short
    case Val.Int(_)             => Type.Int
    case Val.Long(_)            => Type.Long
    case Val.Float(_)           => Type.Float
    case Val.Double(_)          => Type.Double
    case Val.Struct(name, vals) => Type.Struct(name, vals.map(_.ty))
    case Val.Array(ty, vals)    => Type.Array(ty, vals.length)
    case Val.Chars(s)           => Type.Array(Type.Byte, s.getBytes.length + 1)
    case Val.Local(_, ty)       => ty
    case Val.Global(_, ty)      => ty

    case Val.Unit      => Type.Unit
    case Val.Const(_)  => Type.Ptr
    case Val.String(_) => Rt.String
  }

  final def show: String = nir.Show(this)
}
object Val {
  // low-level
  final case object None                     extends Val
  final case object True                     extends Val
  final case object False                    extends Val
  final case class Zero(of: nir.Type)        extends Val
  final case class Undef(of: nir.Type)       extends Val
  final case class Byte(value: scala.Byte)   extends Val
  final case class Short(value: scala.Short) extends Val
  final case class Int(value: scala.Int)     extends Val
  final case class Long(value: scala.Long)   extends Val
  final case class Float(value: scala.Float) extends Val {
    override def equals(that: Any): Boolean = that match {
      case Float(thatValue) =>
        val theseBits = floatToRawIntBits(value)
        val thoseBits = floatToRawIntBits(thatValue)
        theseBits == thoseBits
      case _ => false
    }
  }
  final case class Double(value: scala.Double) extends Val {
    override def equals(that: Any): Boolean = that match {
      case Double(thatValue) =>
        val theseBits = doubleToRawLongBits(value)
        val thoseBits = doubleToRawLongBits(thatValue)
        theseBits == thoseBits
      case _ => false
    }
  }
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
