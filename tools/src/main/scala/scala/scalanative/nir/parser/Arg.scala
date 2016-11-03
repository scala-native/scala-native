package scala.scalanative
package nir
package parser

import fastparse.all._

object Arg extends Base[nir.Arg] {

  override val parser: P[nir.Arg] =
    P(PassConv.parser.? ~ Type.parser map {
      case (pc, ty) => nir.Arg(ty, pc)
    })

}
