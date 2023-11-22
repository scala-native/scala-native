package scala.scalanative
package nir

import java.lang.Float.floatToRawIntBits
import java.lang.Double.doubleToRawLongBits

/** A NIR value. */
sealed abstract class Val {

  /** The type of the value. */
  final def ty: Type = this match {
    case Val.Null =>
      Type.Null
    case Val.Zero(ty) =>
      ty
    case Val.True | Val.False =>
      Type.Bool
    case Val.Size(_) =>
      Type.Size
    case Val.Char(_) =>
      Type.Char
    case Val.Byte(_) =>
      Type.Byte
    case Val.Short(_) =>
      Type.Short
    case Val.Int(_) =>
      Type.Int
    case Val.Long(_) =>
      Type.Long
    case Val.Float(_) =>
      Type.Float
    case Val.Double(_) =>
      Type.Double
    case Val.StructValue(vals) =>
      Type.StructValue(vals.map(_.ty))
    case Val.ArrayValue(ty, vals) =>
      Type.ArrayValue(ty, vals.length)
    case v: Val.ByteString =>
      Type.ArrayValue(Type.Byte, v.byteCount)
    case Val.Local(_, ty) =>
      ty
    case Val.Global(_, ty) =>
      ty
    case Val.Unit =>
      Type.Unit
    case Val.Const(_) =>
      Type.Ptr
    case Val.String(_) =>
      Type.Ref(Rt.String.name, exact = true, nullable = false)
    case Val.Virtual(_) =>
      Type.Virtual
    case Val.ClassOf(n) =>
      Rt.Class
  }

  /** A textual representation of `this`. */
  final def show: String = nir.Show(this)

  /** `true` iff `this` represents a virtual value. */
  final def isVirtual: Boolean =
    this.isInstanceOf[Val.Virtual]

  /** `true` iff `this` is in canonical form. */
  final def isCanonical: Boolean = this match {
    case Val.True | Val.False =>
      true
    case Val.Char(_) =>
      true
    case Val.Size(_) =>
      true
    case Val.Byte(_) | Val.Short(_) | Val.Int(_) | Val.Long(_) =>
      true
    case Val.Float(_) | Val.Double(_) =>
      true
    case Val.Global(_, _) | Val.Null =>
      true
    case _ =>
      false
  }

  /** `true` iff `this` is the "zero" value of its type. */
  final def isZero: Boolean = this match {
    case Val.Zero(_) =>
      true
    case Val.False =>
      true
    case Val.Char('\u0000') =>
      true
    case Val.Size(0) =>
      true
    case Val.Byte(0) | Val.Short(0) | Val.Int(0) | Val.Long(0L) =>
      true
    case Val.Float(0f) | Val.Double(0d) =>
      true
    case Val.Null =>
      true
    case _ =>
      false
  }

  /** `true` iff `this` is the value "1" in its type. */
  final def isOne: Boolean = this match {
    case Val.True =>
      true
    case Val.Char(c) if c.toInt == 1 =>
      true
    case Val.Size(1) =>
      true
    case Val.Byte(1) | Val.Short(1) | Val.Int(1) | Val.Long(1L) =>
      true
    case Val.Float(1f) | Val.Double(1d) =>
      true
    case _ =>
      false
  }

  /** `true` iff `this` is a numerical value equal to "-1". */
  final def isMinusOne: Boolean = this match {
    case Val.Size(-1) =>
      true
    case Val.Byte(-1) | Val.Short(-1) | Val.Int(-1) | Val.Long(-1L) =>
      true
    case Val.Float(-1f) | Val.Double(-1d) =>
      true
    case _ =>
      false
  }

  /** `true` iff `this` is the minimum value of a signed numerical type. */
  final def isSignedMinValue(is32BitPlatform: Boolean): Boolean = this match {
    case Val.Size(v) =>
      if (is32BitPlatform) v == Int.MinValue else v == Long.MinValue
    case Val.Byte(v) =>
      v == Byte.MinValue
    case Val.Short(v) =>
      v == Short.MinValue
    case Val.Int(v) =>
      v == Int.MinValue
    case Val.Long(v) =>
      v == Long.MinValue
    case _ =>
      false
  }

  /** `true` iff `this` is the maximum value of a signed numerical type. */
  final def isSignedMaxValue(is32BitPlatform: Boolean): Boolean = this match {
    case Val.Size(v) =>
      if (is32BitPlatform) v == Int.MaxValue else v == Long.MaxValue
    case Val.Byte(v) =>
      v == Byte.MaxValue
    case Val.Short(v) =>
      v == Short.MaxValue
    case Val.Int(v) =>
      v == Int.MaxValue
    case Val.Long(v) =>
      v == Long.MaxValue
    case _ =>
      false
  }

  /** `true` iff `this` is the minimum value of an unsigned numerical type. */
  final def isUnsignedMinValue: Boolean =
    isZero

  /** `true` iff `this` is the maximum value of an unsigned numerical type. */
  final def isUnsignedMaxValue: Boolean = this match {
    case _ if isMinusOne =>
      true
    case Val.Char(c) =>
      c == Char.MaxValue
    case _ => false
  }

  /** The canonical form of `this` */
  final def canonicalize: Val = this match {
    case Val.Zero(Type.Bool) =>
      Val.False
    case Val.Zero(Type.Size) =>
      Val.Size(0)
    case Val.Zero(Type.Char) =>
      Val.Char('\u0000')
    case Val.Zero(Type.Byte) =>
      Val.Byte(0.toByte)
    case Val.Zero(Type.Short) =>
      Val.Short(0.toShort)
    case Val.Zero(Type.Int) =>
      Val.Int(0)
    case Val.Zero(Type.Long) =>
      Val.Long(0L)
    case Val.Zero(Type.Float) =>
      Val.Float(0f)
    case Val.Zero(Type.Double) =>
      Val.Double(0d)
    case Val.Zero(Type.Ptr) | Val.Zero(_: Type.RefKind) =>
      Val.Null
    case _ =>
      this
  }
}

object Val {

  /** The constant Boolean 'true'. */
  case object True extends Val

  /** The constant Boolean 'false'. */
  case object False extends Val

  /** A Boolean constant. */
  object Bool extends (Boolean => Val) {
    def apply(value: Boolean): Val =
      if (value) True else False
    def unapply(value: Val): Option[Boolean] = value match {
      case True  => Some(true)
      case False => Some(false)
      case _     => scala.None
    }
  }

  /** The constant 'null' value. */
  case object Null extends Val

  /** The "zero" value of the given NIR type. */
  final case class Zero(of: nir.Type) extends Val

  /** A numerical value suitable to represent the size of a container. */
  final case class Size(value: scala.Long) extends Val

  /** 16-bit unsigned Unicode character */
  final case class Char(value: scala.Char) extends Val

  /** A 8-bit signed two’s complement integer. */
  final case class Byte(value: scala.Byte) extends Val

  /** A 16-bit signed two’s complement integer. */
  final case class Short(value: scala.Short) extends Val

  /** A 32-bit signed two’s complement integer. */
  final case class Int(value: scala.Int) extends Val

  /** A 64-bit signed two’s complement integer. */
  final case class Long(value: scala.Long) extends Val

  /** A 32-bit IEEE 754 single-precision float. */
  final case class Float(value: scala.Float) extends Val {
    override def equals(that: Any): Boolean = that match {
      case Float(thatValue) =>
        val theseBits = floatToRawIntBits(value)
        val thoseBits = floatToRawIntBits(thatValue)
        theseBits == thoseBits
      case _ => false
    }
  }

  /** A 64-bit IEEE 754 double-precision float. */
  final case class Double(value: scala.Double) extends Val {
    override def equals(that: Any): Boolean = that match {
      case Double(thatValue) =>
        val theseBits = doubleToRawLongBits(value)
        val thoseBits = doubleToRawLongBits(thatValue)
        theseBits == thoseBits
      case _ => false
    }
  }

  /** A heterogeneous collection of data members. */
  final case class StructValue(values: Seq[Val]) extends Val

  /** A homogeneous collection of data members. */
  final case class ArrayValue(elemty: nir.Type, values: Seq[Val]) extends Val

  /** A collection of bytes. */
  final case class ByteString(bytes: Array[scala.Byte]) extends Val {
    def byteCount: scala.Int = bytes.length + 1
  }

  /** A local SSA variable. */
  final case class Local(id: nir.Local, valty: nir.Type) extends Val

  /** A symbol. */
  final case class Global(name: nir.Global, valty: nir.Type) extends Val

  /** The unit value. */
  case object Unit extends Val

  /** A constant. */
  final case class Const(value: Val) extends Val

  /** A character string. */
  final case class String(value: java.lang.String) extends Val

  /** A virtual value. */
  final case class Virtual(key: scala.Long) extends Val

  /** A class symbol. */
  final case class ClassOf(name: nir.Global) extends Val

}
