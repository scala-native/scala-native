package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._

object Attrs extends Base[nir.Attrs] {

  //import Base.IgnoreWhitespace._

  override def parser[_: P]: P[nir.Attrs] =
    P(Attr.parser.rep.map(nir.Attrs.fromSeq(_)))
}
