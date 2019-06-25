package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class BinParserTest extends FunSuite {
  import Bin._

  Seq[Bin](Iadd,
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
           Xor).foreach { bin =>
    test(s"parse bin `${bin.show}`") {
      val Parsed.Success(result, _) = parser.Bin.parser.parse(bin.show)
      assert(result == bin)
    }
  }
}
