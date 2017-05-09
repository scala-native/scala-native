package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class AttrParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")

  "The NIR parser" should "parse attributes" in {
    import Attr._
    val attrs: Seq[Attr] = Seq(
      MayInline,
      InlineHint,
      NoInline,
      AlwaysInline,
      Dyn,
      Align(1024),
      Pure,
      Extern,
      Override(global),
      Link("test"),
      PinAlways(global),
      PinIf(global, global),
      PinWeak(global)
    )

    attrs foreach { attr =>
      val Parsed.Success(result, _) = parser.Attr.parser.parse(attr.show)
      result should be(attr)
    }
  }
}
