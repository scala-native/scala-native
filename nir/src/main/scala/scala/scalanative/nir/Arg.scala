package scala.scalanative
package nir

final case class Arg(ty: Type, passConvention: Option[PassConv] = None) {
  final def show: String = nir.Show(this)
}
