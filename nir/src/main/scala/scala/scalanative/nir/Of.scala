package scala.scalanative
package nir

object Of {
  def unapply(value: Val): Some[Type] =
    Some(value.ty)
}
