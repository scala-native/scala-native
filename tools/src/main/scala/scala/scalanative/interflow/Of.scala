package scala.scalanative
package interflow

import scalanative.nir._

object Of {
  def unapply(value: Val): Some[Type] =
    Some(value.ty)
}

object Of2 {
  def unapply(value: Val): Some[(Type, Type)] =
    Some((value.ty, value.ty))
}
