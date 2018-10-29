package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class GlobalParserTest extends FunSuite {

  Seq[Global](
    Global.Top("java.lang.String"),
    Global.Top("java.lang.String") member Sig.Method("foobar", Seq(Type.Unit))
  ).foreach { global =>
    test(s"parse global `${global.show}`") {
      val Parsed.Success(result, _) = parser.Global.parser.parse(global.show)
      assert(result == global)
    }
  }
}
