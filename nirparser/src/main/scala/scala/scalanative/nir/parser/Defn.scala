package scala.scalanative
package nir
package parser

import fastparse._

object Defn extends Base[nir.Defn] {

  import MultiLineWhitespace._
  //import Base.IgnoreWhitespace._

  def Var[_: P] =
    P(Attrs.parser ~ "var" ~ Global.parser ~ ":" ~ Type.parser ~ "=" ~ Val.parser map {
      case (attrs, name, ty, v) =>
        nir.Defn.Var(attrs, name, ty, v)
    })
  def Const[_: P] =
    P(Attrs.parser ~ "const" ~ Global.parser ~ ":" ~ Type.parser ~ "=" ~ Val.parser map {
      case (attrs, name, ty, v) =>
        nir.Defn.Const(attrs, name, ty, v)
    })
  def Declare[_: P] =
    P(Attrs.parser ~ "decl" ~ Global.parser ~ ":" ~ Type.parser map {
      case (attrs, name, ty) => nir.Defn.Declare(attrs, name, ty)
    })
  def Define[_: P] =
    P(Attrs.parser ~ "def" ~ Global.parser ~ ":" ~ Type.parser ~ "{" ~ Inst.parser.rep ~ "}" map {
      case (attrs, name, ty, insts) =>
        nir.Defn.Define(attrs, name, ty, insts)
    })
  def Trait[_: P] =
    P(
      Attrs.parser ~ "trait" ~ Global.parser ~ (":" ~ Global.parser.rep(
        sep = ",")).? map {
        case (attrs, name, ifaces) =>
          nir.Defn.Trait(attrs, name, ifaces getOrElse Seq())
      })
  def Class[_: P] =
    P(
      Attrs.parser ~ "class" ~ Global.parser ~ (":" ~ Global.parser.rep(
        sep = ",")).? map {
        case (attrs, name, None) =>
          nir.Defn.Class(attrs, name, None, Seq())
        case (attrs, name, Some(inherits)) =>
          nir.Defn.Class(attrs, name, inherits.headOption, inherits.tail)
      })
  def Module[_: P] =
    P(
      Attrs.parser ~ "module" ~ Global.parser ~ (":" ~ Global.parser.rep(
        sep = ",")).? map {
        case (attrs, name, None) =>
          nir.Defn.Module(attrs, name, None, Seq())
        case (attrs, name, Some(inherits)) =>
          nir.Defn.Module(attrs, name, inherits.headOption, inherits.tail)
      })
  override def parser[_: P]: P[nir.Defn] =
    Var | Const | Define | Declare | Trait | Class | Module
}
