package scala.scalanative
package nir
package parser

import fastparse.all._

object Attr extends Base[nir.Attr] {

  import Base._
  import IgnoreWhitespace._

  val MayInline    = P("mayinline".! map (_ => nir.Attr.MayInline))
  val InlineHint   = P("inlinehint".! map (_ => nir.Attr.InlineHint))
  val NoInline     = P("noinline".! map (_ => nir.Attr.NoInline))
  val AlwaysInline = P("alwaysinline".! map (_ => nir.Attr.AlwaysInline))
  val Pure         = P("pure".! map (_ => nir.Attr.Pure))
  val Extern       = P("extern".! map (_ => nir.Attr.Extern))
  val Override =
    P("override(" ~ Global.parser ~ ")" map (nir.Attr.Override(_)))
  val Link      = P("link(" ~ qualifiedId ~ ")" map (nir.Attr.Link(_)))
  val PinAlways = P("pin(" ~ Global.parser ~ ")" map (nir.Attr.PinAlways(_)))
  val PinIf =
    P("pin-if(" ~ Global.parser ~ "," ~ Global.parser ~ ")" map {
      case (name, cond) => nir.Attr.PinIf(name, cond)
    })

  override val parser: P[nir.Attr] =
    MayInline | InlineHint | NoInline | AlwaysInline | Pure | Extern | Override | Link | PinAlways | PinIf

}
