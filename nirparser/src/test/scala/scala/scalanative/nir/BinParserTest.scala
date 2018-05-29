package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class BinParserTest extends FlatSpec with Matchers {

  "The NIR parser" should "parse bin operations" in {
    import Bin._
    val bins: Seq[Bin] = Seq(Iadd,
                             Fadd,
                             Isub,
                             Fsub,
                             Imul,
                             Fmul,
                             Sdiv,
                             Udiv,
                             Fdiv,
                             Srem,
                             Urem,
                             Frem,
                             Shl,
                             Lshr,
                             Ashr,
                             And,
                             Or,
                             Xor)

    bins foreach { bin =>
      val Parsed.Success(result, _) = parser.Bin.parser.parse(bin.show)
      result should be(bin)
    }
  }
}
