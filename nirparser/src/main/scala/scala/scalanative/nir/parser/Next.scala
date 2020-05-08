package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._

object Next extends Base[nir.Next] {

  //import Base.IgnoreWhitespace._

  def Label[_: P] =
    P(Local.parser ~ ("(" ~ Val.parser.rep(sep = ",") ~ ")").? map {
      case (name, args) => nir.Next.Label(name, args getOrElse Seq())
    })
  def Unwind[_: P] =
    P("unwind" ~ Val.Local ~ "to" ~ Label map {
      case (name, next) =>
        nir.Next.Unwind(name, next)
    })
  def Case[_: P] =
    P("case" ~ Val.parser ~ "=>" ~ Label map {
      case (value, next) => nir.Next.Case(value, next)
    })
  override def parser[_: P]: P[nir.Next] = Label | Unwind | Case
}
