package scala.scalanative
package nir

import java.lang.Float.floatToRawIntBits
import java.lang.Double.doubleToRawLongBits

sealed abstract class Val {
  final def ty: Type = this match {
    case Val.None               => Type.None
    case Val.Null               => Type.Ptr
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
    case Val.Chars(s)           => Type.Array(Type.Byte, countBytes(s))
    case Val.Local(_, ty)       => ty
    case Val.Global(_, ty)      => ty

    case Val.Unit      => Type.Unit
    case Val.Const(_)  => Type.Ptr
    case Val.String(_) => Rt.String
  }

  private def countBytes(s: String): Int = {
    import Character.isDigit

    // TODO: should be a compilation error, how to report?
    def malformed() = ???

    // Subtracts from the length of the bytes for each escape sequence
    // uses String, not Seq[Byte], but should be okay since we handle ASCII only
    def uncountEscapes(from: Int, accum: Int): Int =
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
}
object Val {
  // low-level
  final case object None                     extends Val
  final case object True                     extends Val
  final case object False                    extends Val
  final case object Null                     extends Val
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
  def Bool(bool: Boolean) = if (bool) True else False

  // high-level
  final case object Unit                           extends Val
  final case class Const(value: Val)               extends Val
  final case class String(value: java.lang.String) extends Val
}
