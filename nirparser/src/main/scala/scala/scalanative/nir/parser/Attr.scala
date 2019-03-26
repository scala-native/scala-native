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
  val Dyn          = P("dyn".! map (_ => nir.Attr.Dyn))
  val Stub         = P("stub".! map (_ => nir.Attr.Stub))
  val Extern       = P("extern".! map (_ => nir.Attr.Extern))
  val Link         = P("link(" ~ stringLit ~ ")" map (nir.Attr.Link(_)))
  val Abstract     = P("abstract".! map (_ => nir.Attr.Abstract))
  val UnOpt        = P("unopt".! map (_ => nir.Attr.UnOpt))
  val DidOpt       = P("didopt".! map (_ => nir.Attr.DidOpt))
  val BailOpt      = P("bailopt(" ~ stringLit ~ ")" map (nir.Attr.BailOpt(_)))

  override val parser: P[nir.Attr] =
    MayInline | InlineHint | NoInline | AlwaysInline | Dyn | Stub | Extern | Link | Abstract | UnOpt | DidOpt | BailOpt

}
