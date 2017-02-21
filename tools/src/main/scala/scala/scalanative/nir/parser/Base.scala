package scala.scalanative
package nir
package parser

import fastparse.WhitespaceApi
import fastparse.all._

trait Base[T] {
  def parser: P[T]

  final def apply(nir: String) =
    parser.parse(nir)
}

object Base {

  val IgnoreWhitespace = WhitespaceApi.Wrapper {
    import fastparse.all._
    NoTrace(CharIn(Seq(' ', '\n')).rep)
  }

  import scalaparse.syntax.Basic.{DecNum, HexNum, Lower, Upper, OpChar}
  import scalaparse.syntax.Identifiers.{
    PlainId => _,
    VarId => _,
    VarId0 => _,
    _
  }
  private def VarId0(dollar: Boolean) = P(Lower ~ IdRest(dollar))
  private val VarId                   = VarId0(true)

  val mangledId: P[String] = {
    val mangledIdPart: P[String] =
      P(
        (Upper ~ IdRest(true)) | VarId | (Operator ~ (!OpChar | &(
          "/*" | "//")))).!
    P(mangledIdPart.! ~ ("." ~ mangledId).!.?) map {
      case (a, b) => a + b.getOrElse("")
    }
  }

  val qualifiedId: P[String] = {
    def PlainId: P[String] =
      P(
        ((Upper ~ IdRest(true)) | VarId | (Operator ~ (!OpChar | &(
          "/*" | "//")))).! ~ PlainId.!.?) map {
        case (a, b) => a + b.getOrElse("")
      }
    val idBase: P[String] =
      P(BacktickId | PlainId).!
    def qualifiedIdPart: P[String] =
      P(idBase.! ~ ("." ~ qualifiedIdPart).!.?) map {
        case (a, b) => a + b.getOrElse("")
      }

    P(qualifiedIdPart ~ ("_" ~ qualifiedId).!.?) map {
      case (a, b) => a + b.getOrElse("")
    }
  }

  object Literals extends scalaparse.syntax.Literals {
    override def Block: P0   = Fail
    override def Pattern: P0 = Fail
  }

  val int: P[Int] = Literals.Literals.NoInterp.Literal.! map (_.toInt)
  def neg(p: P[String]): P[String] = "-".!.? ~ p map {
    case (a, b) => a.getOrElse("") + b
  }
  val Byte: P[Byte]       = neg(DecNum.!) map (_.toByte)
  val Short: P[Short]     = neg((HexNum | DecNum).!) map (_.toInt.toShort)
  val Int: P[Int]         = neg((HexNum | DecNum).!) map (_.toInt)
  val Long: P[Long]       = neg((HexNum | DecNum).!) map (_.toLong)
  val Infinity: P[String] = P("Infinityf".!) map (_.init)
  val Float: P[Float]     = neg(Infinity | Literals.Literals.Float.!) map (_.toFloat)
  val Double: P[Double]   = neg(Infinity | Literals.Literals.Float.!) map (_.toDouble)
  val stringLit: P[String] = P(Literals.Literals.NoInterp.String.! map {
    _.init.tail
  })
}
