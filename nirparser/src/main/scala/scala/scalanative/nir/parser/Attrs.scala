package scala.scalanative
package nir
package parser

import fastparse._

object Attrs extends Base[nir.Attrs] {

  import MultiLineWhitespace._
  //import Base.IgnoreWhitespace._

  override def parser[_: P]: P[nir.Attrs] =
    P(Attr.parser.rep.map(nir.Attrs.fromSeq(_)))
}
