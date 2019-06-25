package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class CompParserTest extends FunSuite {
  import Comp._

  Seq[Comp](Ieq,
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
            Fle).foreach { comp =>
    test(s"parse comp `${comp.show}`") {
      val Parsed.Success(result, _) = parser.Comp.parser.parse(comp.show)
      assert(result == comp)
    }
  }
}
