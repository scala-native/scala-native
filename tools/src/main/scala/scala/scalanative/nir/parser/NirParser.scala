package scala.scalanative
package nir
package parser

import fastparse.all._

object NirParser extends Base[Seq[nir.Defn]] {

  import Base.IgnoreWhitespace._

  override val parser: P[Seq[nir.Defn]] =
    Defn.parser.rep ~ End

}
