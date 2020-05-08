package scala.scalanative
package nir
package parser

import fastparse._

object Inst extends Base[nir.Inst] {

  import MultiLineWhitespace._
  //import Base.IgnoreWhitespace._

  private def unwind[_: P]: P[Next] =
    P(Next.parser.?).map(_.getOrElse(nir.Next.None))

  def Label[_: P] =
    P(Local.parser ~ ("(" ~ Val.Local.rep(sep = ",") ~ ")").? ~ ":" map {
      case (name, params) => nir.Inst.Label(name, params getOrElse Seq())
    })
  def Let[_: P] =
    P(Local.parser ~ "=" ~ Op.parser ~ unwind map {
      case (name, op, unwind) => nir.Inst.Let(name, op, unwind)
    })
  def Ret[_: P]  = P("ret" ~ Val.parser.map(nir.Inst.Ret(_)))
  def Jump[_: P] = P("jump" ~ Next.parser.map(nir.Inst.Jump(_)))
  def If[_: P] =
    P("if" ~ Val.parser ~ "then" ~ Next.parser ~ "else" ~ Next.parser map {
      case (cond, thenp, elsep) => nir.Inst.If(cond, thenp, elsep)
    })
  def Switch[_: P] =
    P("switch" ~ Val.parser ~ "{" ~ Next.parser.rep ~ "default" ~ "=>" ~ Next.parser ~ "}" map {
      case (scrut, cases, default) => nir.Inst.Switch(scrut, default, cases)
    })
  def Throw[_: P] = P("throw" ~ Val.parser ~ unwind).map {
    case (value, unwind) => nir.Inst.Throw(value, unwind)
  }
  def Unreachable[_: P] =
    P("unreachable" ~ unwind.map(nir.Inst.Unreachable(_)))

  override def parser[_: P]: P[nir.Inst] =
    Label | Let | Ret | Jump | If | Switch | Throw | Unreachable
}
