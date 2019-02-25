package scala.scalanative
package nir

final case class Local(id: Long) extends AnyVal {
  final def show: String = nir.Show(this)
}
