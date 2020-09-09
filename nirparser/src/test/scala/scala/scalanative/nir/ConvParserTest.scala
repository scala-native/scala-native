package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite

class ConvParserTest extends AnyFunSuite {
  import Conv._

  Seq[Conv](Trunc,
            Zext,
            Sext,
            Fptrunc,
            Fpext,
            Fptoui,
            Fptosi,
            Uitofp,
            Sitofp,
            Ptrtoint,
            Inttoptr,
            Bitcast).foreach { conv =>
    test(s"parse conv `${conv.show}`") {
      val Parsed.Success(result, _) = parser.Conv.parser.parse(conv.show)
      assert(result == conv)
    }
  }
}
