package scala.scalanative
package nir

final case class Local(scope: String, id: Int) {
  final def show: String = nir.Show(this)
}
