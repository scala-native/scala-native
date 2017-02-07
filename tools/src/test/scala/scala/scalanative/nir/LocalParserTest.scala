package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class LocalParserTest extends FlatSpec with Matchers {

  "The NIR parser" should "parse `Local`" in {
    val local                     = Local("test", 1)
    val Parsed.Success(result, _) = parser.Local.parser.parse(local.show)
    result should be(local)
  }
}
