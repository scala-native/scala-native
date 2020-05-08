package scala.scalanative
package nir
package parser

//import fastparse.WhitespaceApi
import fastparse._
import NoWhitespace._

trait Base[T] {
  def parser[_: P]: P[T]

  final def apply(nir: String) =
    parser.parse(nir)
}

object Base {

  // val IgnoreWhitespace = WhitespaceApi.Wrapper {
  //   import fastparse._
  //   NoTrace(CharIn(Seq(' ', '\n')).rep)
  // }

  import scalaparse.syntax.Basic.{DecNum, HexNum, Lower, Upper, OpChar}
  import scalaparse.syntax.Identifiers.{
    PlainId => _,
    VarId => _,
    VarId0 => _,
    _
  }
  private def VarId0[_: P](dollar: Boolean) = P(Lower ~ IdRest(dollar))
  private def VarId[_: P]                   = VarId0(true)

  def mangledId[_: P]: P[String] = {
    val mangledIdPart: P[String] =
      P(
        (Upper ~ IdRest(true)) | VarId | (Operator ~ (!OpChar | &(
          "/*" | "//")))).!
    P(mangledIdPart.! ~ ("." ~ mangledId).!.?).map {
      case (a, b) => a + b.getOrElse("")
    }
  }

  def qualifiedId[_: P]: P[String] = {
    def PlainId: P[String] =
      P(
        ((Upper ~ IdRest(true)) | VarId | (Operator ~ (!OpChar | &(
          "/*" | "//")))).! ~ PlainId.!.?).map {
        case (a, b) => a + b.getOrElse("")
      }
    val idBase: P[String] =
      P(BacktickId | PlainId).!
    def qualifiedIdPart: P[String] =
      P(idBase.! ~ ("." ~ qualifiedIdPart).!.?).map {
        case (a, b) => a + b.getOrElse("")
      }

    P(qualifiedIdPart ~ ("_" ~ qualifiedId).!.?).map {
      case (a, b) => a + b.getOrElse("")
    }
  }

  object Literals extends scalaparse.syntax.Literals {
    override def Block[_: P]: P0   = Fail
    override def Pattern[_: P]: P0 = Fail
  }

  def int[_: P]: P[Int] = Literals.Literals.NoInterp.Literal.!.map(_.toInt)
  def neg[_: P](p: P[String]): P[String] = "-".!.? ~ p map {
    case (a, b) => a.getOrElse("") + b
  }
  def Byte[_: P]: P[Byte]       = neg(DecNum.!).map(_.toByte)
  def Short[_: P]: P[Short]     = neg((HexNum | DecNum).!).map(_.toInt.toShort)
  def Int[_: P]: P[Int]         = neg((HexNum | DecNum).!).map(_.toInt)
  def Long[_: P]: P[Long]       = neg((HexNum | DecNum).!).map(_.toLong)
  def Infinity[_: P]: P[String] = P("Infinityf".!).map(_.init)
  def Float[_: P]: P[Float] =
    neg(Infinity | Literals.Literals.Float.!).map(_.toFloat)
  def Double[_: P]: P[Double] =
    neg(Infinity | Literals.Literals.Float.!).map(_.toDouble)
  def stringLit[_: P]: P[String] =
    P(Literals.Literals.NoInterp.String.!.map { x => unquote(x.init.tail) })

  private def unquote(s: String): String = s.replace("\\\"", "\"")
}
