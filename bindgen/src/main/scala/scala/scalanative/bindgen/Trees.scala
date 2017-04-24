package scala.scalanative
package bindgen

import native._

object Trees {

  /**
   * A simplified AST mapped from libclang and targeted towards generating
   * Scala code.
   */
  sealed trait Tree

  case class Function(name: String,
                      returnType: String,
                      args: List[Function.Param])
      extends Tree
  object Function {
    case class Param(name: String, tpe: String)
  }

  case class Enum(name: String, values: List[Enum.Value]) extends Tree
  object Enum {
    case class Value(name: String, value: CLongLong)
  }

  case class Typedef(name: String, underlying: String) extends Tree
}
