package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class ArgParserTest extends FlatSpec with Matchers {

  "The NIR parser" should "parse arguments" in {
    val arg = Arg(Type.None, None)
    val Parsed.Success(result, _) =
      parser.Arg.parser.parse(sh"$arg".toString)
    result should be(arg)
  }

}