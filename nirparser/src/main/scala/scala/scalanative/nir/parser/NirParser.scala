package scala.scalanative
package nir
package parser

import fastparse._

object NirParser extends Base[Seq[nir.Defn]] {

  import MultiLineWhitespace._
  //import Base.IgnoreWhitespace._

  override def parser[_: P]: P[Seq[nir.Defn]] =
    Defn.parser.rep ~ End

}
