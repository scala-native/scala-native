package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class LocalParserTest extends FlatSpec with Matchers {

  "The NIR parser" should "parse `Local`" in {
    val local = Local("test", 1)
    val Parsed.Success(result, _) =
      parser.Local.parser.parse(sh"$local".toString)
    result should be(local)
  }

}