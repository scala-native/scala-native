package scala.scalanative
package nir

import java.lang.Float.floatToRawIntBits
import java.lang.Double.doubleToRawLongBits
import scala.annotation.tailrec

sealed abstract class Val {
  final def ty: Type = this match {
    case Val.Null                 => Type.Null
    case Val.Zero(ty)             => ty
    case Val.True | Val.False     => Type.Bool
    case Val.Char(_)              => Type.Char
    case Val.Byte(_)              => Type.Byte
    case Val.Short(_)             => Type.Short
    case Val.Int(_)               => Type.Int
    case Val.Long(_)              => Type.Long
    case Val.Float(_)             => Type.Float
    case Val.Double(_)            => Type.Double
    case Val.StructValue(vals)    => Type.StructValue(vals.map(_.ty))
    case Val.ArrayValue(ty, vals) => Type.ArrayValue(ty, vals.length)
    case v: Val.Chars             => Type.ArrayValue(Type.Byte, v.byteCount)
    case Val.Local(_, ty)         => ty
    case Val.Global(_, ty)        => ty

    case Val.Unit     => Type.Unit
    case Val.Const(_) => Type.Ptr
    case Val.String(_) =>
      Type.Ref(Rt.String.name, exact = true, nullable = false)
    case Val.Virtual(_) => Type.Virtual
  }

  final def show: String = nir.Show(this)

  final def isVirtual: Boolean =
    this.isInstanceOf[Val.Virtual]

  final def isCanonical: Boolean = this match {
    case Val.True | Val.False =>
      true
    case _: Val.Char =>
      true
    case _: Val.Byte | _: Val.Short | _: Val.Int | _: Val.Long =>
      true
    case _: Val.Float | _: Val.Double =>
      true
    case _: Val.Global | Val.Null =>
      true
    case _ =>
      false
  }

  final def isZero: Boolean = this match {
    case Val.Zero(_)    => true
    case Val.False      => true
    case Val.Char('\0') => true
    case Val.Byte(0)    => true
    case Val.Short(0)   => true
    case Val.Int(0)     => true
    case Val.Long(0L)   => true
    case Val.Float(0F)  => true
    case Val.Double(0D) => true
    case Val.Null       => true
    case _              => false
  }

  final def isOne: Boolean = this match {
    case Val.True                    => true
    case Val.Char(c) if c.toInt == 1 => true
    case Val.Byte(1)                 => true
    case Val.Short(1)                => true
    case Val.Int(1)                  => true
    case Val.Long(1L)                => true
    case Val.Float(1F)               => true
    case Val.Double(1D)              => true
    case _                           => false
  }

  final def isMinusOne: Boolean = this match {
    case Val.Byte(-1)    => true
    case Val.Short(-1)   => true
    case Val.Int(-1)     => true
    case Val.Long(-1L)   => true
    case Val.Float(-1F)  => true
    case Val.Double(-1D) => true
    case _               => false
  }

  final def isSignedMinValue: Boolean = this match {
    case Val.Byte(v)  => v == Byte.MinValue
    case Val.Short(v) => v == Short.MinValue
    case Val.Int(v)   => v == Int.MinValue
    case Val.Long(v)  => v == Long.MinValue
    case _            => false
  }

  final def isSignedMaxValue: Boolean = this match {
    case Val.Byte(v)  => v == Byte.MaxValue
    case Val.Short(v) => v == Short.MaxValue
    case Val.Int(v)   => v == Int.MaxValue
    case Val.Long(v)  => v == Long.MaxValue
    case _            => false
  }

  final def isUnsignedMinValue: Boolean =
    isZero

  final def isUnsignedMaxValue: Boolean =
    isMinusOne || (this match {
      case Val.Char(c) => c == Char.MaxValue
      case _           => false
    })

  final def canonicalize: Val = this match {
    case Val.Zero(Type.Bool) =>
      Val.False
    case Val.Zero(Type.Char) =>
      Val.Char('\0')
    case Val.Zero(Type.Byte) =>
      Val.Byte(0.toByte)
    case Val.Zero(Type.Short) =>
      Val.Short(0.toShort)
    case Val.Zero(Type.Int) =>
      Val.Int(0)
    case Val.Zero(Type.Long) =>
      Val.Long(0L)
    case Val.Zero(Type.Float) =>
      Val.Float(0F)
    case Val.Zero(Type.Double) =>
      Val.Double(0D)
    case Val.Zero(Type.Ptr) | Val.Zero(_: Type.RefKind) =>
      Val.Null
    case _ =>
      this
  }
}
object Val {
  // low-level
  final case object True  extends Val
  final case object False extends Val
  object Bool extends (Boolean => Val) {
    def apply(value: Boolean): Val =
      if (value) True else False
    def unapply(value: Val): Option[Boolean] = value match {
      case True  => Some(true)
      case False => Some(false)
      case _     => scala.None
    }
  }
  final case object Null                     extends Val
  final case class Zero(of: nir.Type)        extends Val
  final case class Char(value: scala.Char)   extends Val
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
  final case class StructValue(values: Seq[Val])                  extends Val
  final case class ArrayValue(elemty: nir.Type, values: Seq[Val]) extends Val
  final case class Chars(value: java.lang.String) extends Val {
    lazy val byteCount: scala.Int = {
      import Character.isDigit

      def isOct(c: scala.Char): Boolean =
        isDigit(c) && c != '8' && c != '9'
      def isHex(c: scala.Char): Boolean =
        isDigit(c) ||
          c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f' ||
          c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F'
      def malformed() =
        throw new IllegalArgumentException("malformed C string: " + value)

      // Subtracts from the length of the bytes for each escape sequence
      // uses String, not Seq[Byte], but should be okay since we handle ASCII only
      val chars = value.toArray
      var accum = chars.length + 1
      var idx   = 0

      while (idx < chars.length) {
        if (chars(idx) == '\\') {
          if (idx == chars.length - 1) {
            malformed()
          } else {
            chars(idx + 1) match {
              case d if isOct(d) =>
                // octal ("\O", "\OO", "\OOO")
                val digitNum =
                  chars.drop(idx + 1).take(3).takeWhile(isOct).length
                idx += digitNum
                accum -= digitNum
              case 'x' =>
                // hexademical ("\xH", "\xHH")
                val digitNum = chars.drop(idx + 2).takeWhile(isHex).length
                // mimic clang, which reports compilation error against too many hex digits
                if (digitNum >= 3) {
                  malformed()
                }
                idx += digitNum + 1
                accum -= digitNum + 1
              // TODO: support unicode?
              // case 'u' =>
              // case 'U' =>
              case _ =>
                idx += 1
                accum -= 1
            }
          }
        }
        idx += 1
      }

      accum
    }
  }
  final case class Local(name: nir.Local, valty: nir.Type)   extends Val
  final case class Global(name: nir.Global, valty: nir.Type) extends Val

  // high-level
  final case object Unit                           extends Val
  final case class Const(value: Val)               extends Val
  final case class String(value: java.lang.String) extends Val
  final case class Virtual(key: scala.Long)        extends Val
}
