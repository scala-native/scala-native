package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class PassConvParserTest extends FlatSpec with Matchers {

  val noTpe = Type.None

  "The NIR parser" should "parse `PassConv.Byval`" in {
    val byval: PassConv = PassConv.Byval(noTpe)
    val Parsed.Success(result, _) =
      parser.PassConv.Byval.parse(sh"$byval".toString)
    result should be(byval)
  }

  it should "parse `PassConv.Sret`" in {
    val sret: PassConv = PassConv.Sret(noTpe)
    val Parsed.Success(result, _) =
      parser.PassConv.Sret.parse(sh"$sret".toString)
    result should be(sret)
  }

}
