package scala.scalanative
package nir
package parser

import fastparse._

object Attr extends Base[nir.Attr] {

  import Base.stringLit
  import MultiLineWhitespace._
  //import IgnoreWhitespace._

  def MayInline[_: P]    = P("mayinline".!.map(_ => nir.Attr.MayInline))
  def InlineHint[_: P]   = P("inlinehint".!.map(_ => nir.Attr.InlineHint))
  def NoInline[_: P]     = P("noinline".!.map(_ => nir.Attr.NoInline))
  def AlwaysInline[_: P] = P("alwaysinline".!.map(_ => nir.Attr.AlwaysInline))
  def MaySpecialize[_: P] =
    P("mayspecialize".!.map(_ => nir.Attr.MaySpecialize))
  def NoSpecialize[_: P] = P("nospecialize".!.map(_ => nir.Attr.NoSpecialize))
  def UnOpt[_: P]        = P("unopt".!.map(_ => nir.Attr.UnOpt))
  def DidOpt[_: P]       = P("didopt".!.map(_ => nir.Attr.DidOpt))
  def BailOpt[_: P]      = P("bailopt(" ~ stringLit ~ ")" map (nir.Attr.BailOpt(_)))
  def Dyn[_: P]          = P("dyn".!.map(_ => nir.Attr.Dyn))
  def Stub[_: P]         = P("stub".!.map(_ => nir.Attr.Stub))
  def Extern[_: P]       = P("extern".!.map(_ => nir.Attr.Extern))
  def Link[_: P]         = P("link(" ~ stringLit ~ ")" map (nir.Attr.Link(_)))
  def Abstract[_: P]     = P("abstract".!.map(_ => nir.Attr.Abstract))

  override def parser[_: P]: P[nir.Attr] =
    MayInline | InlineHint | NoInline | AlwaysInline | MaySpecialize | NoSpecialize | UnOpt | DidOpt | BailOpt | Dyn | Stub | Extern | Link | Abstract

}
