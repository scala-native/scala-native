package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class AttrParserTest extends FunSuite {
  Seq[Attr](
    Attr.MayInline,
    Attr.InlineHint,
    Attr.NoInline,
    Attr.AlwaysInline,
    Attr.Dyn,
    Attr.Stub,
    Attr.Extern,
    Attr.Link(""),
    Attr.Link("test"),
    Attr.Link("foo bar"),
    Attr.Abstract,
    Attr.UnOpt,
    Attr.DidOpt,
    Attr.BailOpt(""),
    Attr.BailOpt("reason"),
    Attr.BailOpt("long reason")
  ).foreach { attr =>
    test(s"parse attr `${attr.show}`") {
      val Parsed.Success(result, _) = parser.Attr.parser.parse(attr.show)
      assert(result == attr)
    }
  }
}
