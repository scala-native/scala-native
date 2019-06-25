package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class LocalParserTest extends FunSuite {
  test("parse local") {
    val local                     = Local(1)
    val Parsed.Success(result, _) = parser.Local.parser.parse(local.show)
    assert(result == local)
  }
}
