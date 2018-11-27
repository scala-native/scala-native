package scala.scalanative
package nir
package parser

import fastparse.all._

object Next extends Base[nir.Next] {

  import Base.IgnoreWhitespace._

  val Label =
    P(Local.parser ~ ("(" ~ Val.parser.rep(sep = ",") ~ ")").? map {
      case (name, args) => nir.Next.Label(name, args getOrElse Seq())
    })
  val Unwind = P("unwind" ~ Val.Local ~ "to" ~ Label map {
    case (name, next) =>
      nir.Next.Unwind(name, next)
  })
  val Case =
    P("case" ~ Val.parser ~ "=>" ~ Label map {
      case (value, next) => nir.Next.Case(value, next)
    })
  override val parser: P[nir.Next] = Label | Unwind | Case
}
