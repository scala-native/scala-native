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
    case Val.Byte(_)              => Type.Byte
    case Val.Short(_)             => Type.Short
    case Val.Int(_)               => Type.Int
    case Val.Long(_)              => Type.Long
    case Val.Float(_)             => Type.Float
    case Val.Double(_)            => Type.Double
    case Val.StructValue(vals)    => Type.StructValue(vals.map(_.ty))
    case Val.ArrayValue(ty, vals) => Type.ArrayValue(ty, vals.length)
    case Val.Chars(s)             => Type.ArrayValue(Type.Byte, countBytes(s))
    case Val.Local(_, ty)         => ty
    case Val.Global(_, ty)        => ty

    case Val.Unit       => Type.Unit
    case Val.Const(_)   => Type.Ptr
    case Val.String(_)  => Rt.String
    case Val.Virtual(_) => Type.Virtual
  }

  private def countBytes(s: String): Int = {
    import Character.isDigit

    def malformed() =
      throw new IllegalArgumentException("malformed C string: " + s)

    // Subtracts from the length of the bytes for each escape sequence
    // uses String, not Seq[Byte], but should be okay since we handle ASCII only
    @tailrec def uncountEscapes(from: Int, accum: Int): Int =
      s.indexOf('\\', from) match {
        case -1 => accum
        case idx if idx == s.length - 1 =>
          malformed()
        case idx =>
          def isOct(c: Char): Boolean = isDigit(c) && c != '8' && c != '9'
          def isHex(c: Char): Boolean =
            isDigit(c) ||
              c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f' ||
              c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F'
          s(idx + 1) match {
            case d if isOct(d) =>
              // octal ("\O", "\OO", "\OOO")
              val digitNum = s.drop(idx + 1).take(3).takeWhile(isOct).length
              uncountEscapes(idx + 1 + digitNum, accum - digitNum)
            case 'x' =>
              // hexademical ("\xH", "\xHH")
              val digitNum = s.drop(idx + 2).takeWhile(isHex).length
              // mimic clang, which reports compilation error against too many hex digits
              if (digitNum >= 3)
                malformed()
              uncountEscapes(idx + 2 + digitNum, accum - 1 - digitNum)
            // TODO: support unicode?
            // case 'u' =>
            // case 'U' =>
            case _ =>
              uncountEscapes(idx + 2, accum - 1)
          }
      }
    uncountEscapes(0, s.getBytes.length + 1)
  }

  final def show: String = nir.Show(this)

  final def isVirtual: Boolean =
    this.isInstanceOf[Val.Virtual]

  final def isCanonical: Boolean = this match {
    case Val.True | Val.False =>
      true
    case _: Val.Byte | _: Val.Short | _: Val.Int | _: Val.Long =>
      true
    case _: Val.Float | _: Val.Double =>
      true
    case _: Val.Global | Val.Null | _: Val.Virtual =>
      true
    case _ =>
      false
  }

  final def isDefault: Boolean = this match {
    case Val.False      => true
    case Val.Zero(_)    => true
    case Val.Byte(0)    => true
    case Val.Short(0)   => true
    case Val.Int(0)     => true
    case Val.Long(0L)   => true
    case Val.Float(0F)  => true
    case Val.Double(0F) => true
    case Val.Null       => true
    case _              => false
  }

  final def canonicalize: Val = this match {
    case Val.Zero(Type.Bool) =>
      Val.False
    case Val.Zero(Type.Char) =>
      Val.Short(0.toShort)
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
  final case class Chars(value: java.lang.String)                 extends Val
  final case class Local(name: nir.Local, valty: nir.Type)        extends Val
  final case class Global(name: nir.Global, valty: nir.Type)      extends Val

  // high-level
  final case object Unit                           extends Val
  final case class Const(value: Val)               extends Val
  final case class String(value: java.lang.String) extends Val
  final case class Virtual(key: scala.Long)        extends Val
}
