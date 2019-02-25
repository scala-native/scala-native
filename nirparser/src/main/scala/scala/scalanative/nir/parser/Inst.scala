package scala.scalanative
package nir
package parser

import fastparse.all._

object Inst extends Base[nir.Inst] {
  import Base.IgnoreWhitespace._

  private val unwind: P[Next] =
    P(Next.parser.?).map(_.getOrElse(nir.Next.None))

  val Label =
    P(Local.parser ~ ("(" ~ Val.Local.rep(sep = ",") ~ ")").? ~ ":" map {
      case (name, params) => nir.Inst.Label(name, params getOrElse Seq())
    })
  val Let =
    P(Local.parser ~ "=" ~ Op.parser ~ unwind map {
      case (name, op, unwind) => nir.Inst.Let(name, op, unwind)
    })
  val Ret =
    P("ret" ~ Val.parser map (nir.Inst.Ret(_)))
  val Jump = P("jump" ~ Next.parser map (nir.Inst.Jump(_)))
  val If =
    P("if" ~ Val.parser ~ "then" ~ Next.parser ~ "else" ~ Next.parser map {
      case (cond, thenp, elsep) => nir.Inst.If(cond, thenp, elsep)
    })
  val Switch =
    P("switch" ~ Val.parser ~ "{" ~ Next.parser.rep ~ "default" ~ "=>" ~ Next.parser ~ "}" map {
      case (scrut, cases, default) => nir.Inst.Switch(scrut, default, cases)
    })
  val Throw = P("throw" ~ Val.parser ~ unwind).map {
    case (value, unwind) => nir.Inst.Throw(value, unwind)
  }
  val Unreachable =
    P("unreachable" ~ unwind map (nir.Inst.Unreachable(_)))

  override val parser: P[nir.Inst] =
    Label | Let | Ret | Jump | If | Switch | Throw | Unreachable
}
