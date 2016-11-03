package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class CompParserTest extends FlatSpec with Matchers {

  "The NIR parser" should "parse comp operations" in {
    import Comp._
    val comps: Seq[Comp] = Seq(Ieq,
                               Ine,
                               Ugt,
                               Uge,
                               Ult,
                               Ule,
                               Sgt,
                               Sge,
                               Slt,
                               Sle,
                               Feq,
                               Fne,
                               Fgt,
                               Fge,
                               Flt,
                               Fle)

    comps foreach { comp =>
      val Parsed.Success(result, _) =
        parser.Comp.parser.parse(sh"$comp".toString)
      result should be(comp)
    }
  }

}