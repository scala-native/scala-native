package scala.scalanative
package nir
package parser

import fastparse.all._

object Defn extends Base[nir.Defn] {

  import Base.IgnoreWhitespace._

  val Var =
    P(Attrs.parser ~ "var" ~ Global.parser ~ ":" ~ Type.parser ~ "=" ~ Val.parser map {
      case (attrs, name, ty, v) =>
        nir.Defn.Var(attrs, name, ty, v)
    })
  val Const =
    P(Attrs.parser ~ "const" ~ Global.parser ~ ":" ~ Type.parser ~ "=" ~ Val.parser map {
      case (attrs, name, ty, v) =>
        nir.Defn.Const(attrs, name, ty, v)
    })
  val Declare =
    P(Attrs.parser ~ "decl" ~ Global.parser ~ ":" ~ Type.parser map {
      case (attrs, name, ty) => nir.Defn.Declare(attrs, name, ty)
    })
  val Define =
    P(Attrs.parser ~ "def" ~ Global.parser ~ ":" ~ Type.parser ~ "{" ~ Inst.parser.rep ~ "}" map {
      case (attrs, name, ty, insts) =>
        nir.Defn.Define(attrs, name, ty, insts)
    })
  val Trait =
    P(
      Attrs.parser ~ "trait" ~ Global.parser ~ (":" ~ Global.parser.rep(
        sep = ",")).? map {
        case (attrs, name, ifaces) =>
          nir.Defn.Trait(attrs, name, ifaces getOrElse Seq())
      })
  val Class =
    P(
      Attrs.parser ~ "class" ~ Global.parser ~ (":" ~ Global.parser.rep(
        sep = ",")).? map {
        case (attrs, name, None) =>
          nir.Defn.Class(attrs, name, None, Seq())
        case (attrs, name, Some(inherits)) =>
          nir.Defn.Class(attrs, name, inherits.headOption, inherits.tail)
      })
  val Module =
    P(
      Attrs.parser ~ "module" ~ Global.parser ~ (":" ~ Global.parser.rep(
        sep = ",")).? map {
        case (attrs, name, None) =>
          nir.Defn.Module(attrs, name, None, Seq())
        case (attrs, name, Some(inherits)) =>
          nir.Defn.Module(attrs, name, inherits.headOption, inherits.tail)
      })
  override val parser: P[nir.Defn] =
    Var | Const | Define | Declare | Trait | Class | Module
}
